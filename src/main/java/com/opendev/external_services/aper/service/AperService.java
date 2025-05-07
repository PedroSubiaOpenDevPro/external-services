package com.opendev.external_services.aper.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.opendev.external_services.distribuidor.services.DistribuidorService;
import com.opendev.external_services.helpers.WebClientHelper;
import com.opendev.external_services.maestros.service.MaestrosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.opendev.external_services.aper.AperConstants.*;
import static com.opendev.external_services.helpers.JsonHelpers.extractValue;

import java.util.*;


@Service
public class AperService {

    @Autowired
    WebClientHelper webClientHelper;
    @Autowired
    DistribuidorService distribuidorService;
    @Autowired
    MaestrosService maestrosService;

    public Mono<List<ObjectNode>> createResponseToAper(JsonNode body) {
        ArrayNode contratosParaCotizar = body.has("contratosParaCotizar") && body.get("contratosParaCotizar").isArray() ? (ArrayNode) body.get("contratosParaCotizar") : new ObjectMapper().createArrayNode();
        List<Mono<ObjectNode>> listContract = new ArrayList<>();

        for (JsonNode element : contratosParaCotizar) {
            listContract.add(fetchTarifa(element));
        }

        return Flux.merge(listContract).filter(Objects::nonNull).collectList();
    }

    private Mono<ObjectNode> fetchTarifa(JsonNode element) {
        String bulto = element.path("bultos[0][kilos]").asText("");
        String valorDeclarado = element.path("bultos[0][valorDeclarado]").asText("");

        String url = TARIFAS_APER + "?cpDestino=" + element.path("cpDestino").asText() + "&contrato=" + element.path("contrato").asText() + "&cliente=" + element.path("cliente").asText() + "&bultos[0][kilos]=" + bulto + "&bultos[0][valorDeclarado]=" + valorDeclarado;

        Map<String, String> headers = Map.of("Content-Type", "application/json");

        return webClientHelper.executeJsonGetRequest(url, headers)
                .flatMap(resp -> {
                    JsonNode jsonResponse = resp.getBody();
                    if (jsonResponse.hasNonNull("pesoAforado")) {
                        ObjectNode result = JsonNodeFactory.instance.objectNode();
                        result.put("idcontract", element.path("contrato").asInt(0));
                        result.put("tipoEntrega", element.path("tipoEntrega").asText(""));

                        ArrayNode envios = JsonNodeFactory.instance.arrayNode();
                        envios.add(jsonResponse);
                        result.set("envios", envios);

                        return Mono.just(result);
                    }
                    return Mono.empty();
                })
                .onErrorResume(ex -> {
                    return Mono.empty();
                });
    }

    public Mono<ResponseEntity<String>> sendWebhook(String apiKey, JsonNode body) {
        boolean keyValidated = apiKey.equals(API_KEY);

        if (!keyValidated) {
            return Mono.just(ResponseEntity.status(401).body("{\"message\": \"unauthorized\"}"));
        }
        return distribuidorService.getLogin(CREDENCIALES_APER).flatMap(loginDistribuidorResponse -> {
            String tokenDistribuidor = extractValue(loginDistribuidorResponse.getBody(), "token");
            return distribuidorService.callIntegracionRest("flowChart_61c2ce81", tokenDistribuidor, body);
        });
    }

    public Mono<ResponseEntity<String>> getCotizaciones(String apiKey, JsonNode body) {
        boolean keyValidated = apiKey.equals(API_KEY);

        if (!keyValidated) {
            return Mono.just(ResponseEntity.status(401).body("{\"message\": \"unauthorized\"}"));
        }

        return getMaestroContratosAper().flatMap(maestroContratos -> {
            try {
                JsonNode products = body.get("products");
                JsonNode contratos = body.get("contratos");
                String cliente = body.get("cliente").asText();
                String codigoPostalDestino = body.get("codigoPostalDestino").asText();
                validateFieldsNumbers(products);
                return distribuidorService.getLogin(CREDENCIALES_APER).flatMap(loginDistribuidorResponse -> {
                    String tokenDistribuidor = extractValue(loginDistribuidorResponse.getBody(), "token");
                    return distribuidorService.callIntegracionRest("flowChart_61c2ce81", tokenDistribuidor, body);
                });

            } catch (Exception e) {
                return Mono.just(ResponseEntity.status(400).body("{\"message\": \"Error en la validación: " + e.getMessage() + "\"}"));
            }
        });
    }

    private Mono<JsonNode> getMaestroContratosAper() {
        long startTime = System.currentTimeMillis();

        return maestrosService.getMaestrosWithoutParams("contratos_aper_ambiente_testing")
                .map(ResponseEntity::getBody)
                .map(json -> {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        return mapper.readTree(json);
                    } catch (Exception e) {
                        throw new RuntimeException("Error al parsear JSON del maestro de contratos", e);
                    }
                });
    }

    private void validateFieldsNumbers(JsonNode products) {
        List<String> camposConNaN = new ArrayList<>();

        int index = 0;
        for (JsonNode product : products) {
            parsearNumeros(index, product.get("quantity"), "quantity", camposConNaN);
            parsearNumeros(index, product.get("unit_price_tax_incl"), "unit_price_tax_incl", camposConNaN);
            parsearNumeros(index, product.get("weight"), "weight", camposConNaN);
            parsearNumeros(index, product.get("height"), "height", camposConNaN);
            parsearNumeros(index, product.get("depth"), "depth", camposConNaN);
            parsearNumeros(index, product.get("width"), "width", camposConNaN);
            index++;
        }

        if (!camposConNaN.isEmpty()) {
            throw new IllegalArgumentException("Error: Campos inválidos: " + String.join(", ", camposConNaN) + ", se esperaban números.");
        }
    }

    private void parsearNumeros(int index, JsonNode valueNode, String fieldName, List<String> errores) {
        if (valueNode == null || valueNode.isNull() || !valueNode.isTextual()) {
            errores.add("product[" + index + "]." + fieldName);
            return;
        }

        String valueStr = valueNode.asText().trim();

        try {
            Double.parseDouble(valueStr);
            if (!valueStr.matches("^-?\\d+(\\.\\d+)?$")) {
                errores.add("product[" + index + "]." + fieldName);
            }
        } catch (NumberFormatException e) {
            errores.add("product[" + index + "]." + fieldName);
        }
    }

    private double calcularPesoTotal(JsonNode products) {
        double pesoTotal = 0.0;

        for (JsonNode product : products) {
            String pesoStr = product.get("weight").asText().trim();
            String cantidadStr = product.get("quantity").asText().trim();

            try {
                double peso = Double.parseDouble(pesoStr);
                int cantidad = Integer.parseInt(cantidadStr);
                pesoTotal += peso * cantidad;
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Error al calcular peso total: valores no numéricos detectados.");
            }
        }

        return pesoTotal;
    }

}
