package com.opendev.external_services.aper.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.opendev.external_services.distribuidor.services.DistribuidorService;
import com.opendev.external_services.exceptions.ExternalApiException;
import com.opendev.external_services.exceptions.GlobalExceptionHandler;
import com.opendev.external_services.helpers.WebClientHelper;
import com.opendev.external_services.maestros.service.MaestrosService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.opendev.external_services.aper.AperConstants.*;
import static com.opendev.external_services.helpers.JsonHelpers.extractValue;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Service
@Slf4j
public class AperService {


    private final WebClientHelper webClientHelper;
    @Autowired
    DistribuidorService distribuidorService;
    @Autowired
    MaestrosService maestrosService;

    public AperService(WebClient.Builder webClientBuilder) {
        this.webClientHelper = new WebClientHelper(webClientBuilder, TARIFAS_APER);
    }

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

        String url = "?cpDestino=" + element.path("cpDestino").asText() + "&contrato=" + element.path("contrato").asText() + "&cliente=" + element.path("cliente").asText() + "&bultos[0][kilos]=" + bulto + "&bultos[0][valorDeclarado]=" + valorDeclarado;

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
                List productsValidated = validateFieldsNumbers(body.get("products"));
                double volumenTotal = calcularVolumenTotal(productsValidated);
                double pesoAforadoTotal = volumenTotal * 350 / 1000000;
                double pesoTotal = calcularPesoTotal(productsValidated);
                double pesoMayor = Math.max(pesoTotal, pesoAforadoTotal);
                Map<String, List<JsonNode>> resultado = filtrarContratos(body.get("contratos"), pesoMayor, maestroContratos);

                List<JsonNode> contratosValidos = resultado.get("contratosValidos");
                List<JsonNode> contratosNoValidos = resultado.get("contratosNoValidos");
                List<JsonNode> contratosNoExistentesEnMaestros = resultado.get("contratosNoExistentesEnMaestros");
                Mono<List<JsonNode>> tarifasMono = consultaTarifasContratos(body.get("codigoPostalDestino").asText(), contratosValidos, body.get("cliente").asText(), productsValidated);

                return tarifasMono.flatMap(tarifasContratosValidos -> {
                    ObjectNode bodyFinal = JsonNodeFactory.instance.objectNode();
                    ArrayNode contratosValidosArray = JsonNodeFactory.instance.arrayNode();
                    tarifasContratosValidos.forEach(contrato -> contratosValidosArray.add(contrato));
                    bodyFinal.put("contratosValidos", contratosValidosArray);

                    List<JsonNode> contratosNoValidosList = armarNoValidos(contratosNoValidos, pesoTotal);
                    ArrayNode contratosNoValidosArray = JsonNodeFactory.instance.arrayNode();
                    contratosNoValidosList.forEach(contratosNoValidosArray::add);
                    bodyFinal.put("contratosNoValidos", contratosNoValidosArray);

                    List<JsonNode> contratosNoEncontradosList = armarNoEncontrados(contratosNoExistentesEnMaestros);
                    ArrayNode contratosNoEncontradosArray = JsonNodeFactory.instance.arrayNode();
                    contratosNoEncontradosList.forEach(contratosNoEncontradosArray::add);
                    bodyFinal.put("contratosNoEncontrados", contratosNoEncontradosArray);

                    ObjectNode bodyRequest = JsonNodeFactory.instance.objectNode();
                    bodyRequest.set("bodyRecibido", body);
                    bodyRequest.set("bodySalida", bodyFinal);

                    return distribuidorService.getLogin(CREDENCIALES_APER).flatMap(loginDistribuidorResponse -> {
                        String tokenDistribuidor = extractValue(loginDistribuidorResponse.getBody(), "token");
                        return distribuidorService.callIntegracionRest("aper_cotizarTarifas", tokenDistribuidor, bodyRequest);
                    });
                });

            } catch (Exception e) {
                return Mono.just(ResponseEntity.status(400).body("{\"message\": \"Error en la validación: " + e.getMessage() + "\"}"));
            }
        });
    }

    private List<JsonNode> armarNoValidos(List<JsonNode> contratosNoValidos, double pesoTotal) {
        return contratosNoValidos.stream().map(contrato -> {
            if (contrato instanceof ObjectNode objectNode) {
                String motivo = "idDeliveryType no Valido. El peso Total es " + pesoTotal +
                        " Kgs y el idDeliveryType es valido para contratos " +
                        (pesoTotal > 50 ? "menores" : "mayores") + " que 50 Kgs.";
                objectNode.put("motivo", motivo);
            }
            return contrato;
        }).collect(Collectors.toList());
    }

    private List<JsonNode> armarNoEncontrados(List<JsonNode> contratosNoExistentesEnMaestros) {
        return contratosNoExistentesEnMaestros.stream().map(contrato -> {
            if (contrato instanceof ObjectNode objectNode) {
                String motivo = "idDeliveryType no fue encontrado en Maestros. Por este motivo no se puede determinar si el contrato corresponde o no para un peso total mayor a 50 kgs.";
                objectNode.put("motivo", motivo);
            }
            return contrato;
        }).collect(Collectors.toList());
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

    private List<Map<String, Double>> validateFieldsNumbers(JsonNode products) {
        List<String> camposConNaN = new ArrayList<>();
        List<Map<String, Double>> productos = new ArrayList<>();
        String[] campos = {"quantity", "unit_price_tax_incl", "weight", "height", "depth", "width"};

        int index = 0;
        for (JsonNode product : products) {
            Map<String, Double> producto = new HashMap<>();
            for (String campo : campos) {
                producto.put(campo, parsearNumeros(index, product.get(campo), campo, camposConNaN));
            }
            productos.add(producto);
            index++;
        }

        if (!camposConNaN.isEmpty()) {
            throw new IllegalArgumentException("Error: Campos inválidos: " + String.join(", ", camposConNaN) + ", se esperaban números.");
        }
        return productos;
    }

    private double parsearNumeros(int index, JsonNode valueNode, String fieldName, List<String> errores) {
        if (valueNode == null || valueNode.isNull() || !valueNode.isTextual()) {
            errores.add("product[" + index + "]." + fieldName);
            return 0;
        }

        String valueStr = valueNode.asText().trim();

        try {
            double valor = Double.parseDouble(valueStr);
            if (!valueStr.matches("^-?\\d+(\\.\\d+)?$")) {
                errores.add("product[" + index + "]." + fieldName);
            }
            return valor;
        } catch (NumberFormatException e) {
            errores.add("product[" + index + "]." + fieldName);
            return 0;
        }
    }

    private double calcularVolumenTotal(List<Map<String, Double>> products) {
        return products.stream()
                .mapToDouble(p -> p.get("width") * p.get("height") * p.get("depth") * p.get("quantity"))
                .sum();
    }

    private double calcularPesoTotal(List<Map<String, Double>> products) {
        return products.stream()
                .mapToDouble(p -> p.get("weight") * p.get("quantity"))
                .sum();
    }

    private Map<String, List<JsonNode>> filtrarContratos(JsonNode contratos, double pesoTotal, JsonNode maestroContratosAper) {
        List<JsonNode> contratosNoExistentesEnMaestros = cargarContratosInexistentesEnMaestros(contratos, maestroContratosAper);
        List<String> idsNoExistenEnMaestros = new ArrayList<>();
        for (JsonNode contrato : contratosNoExistentesEnMaestros) {
            String idContrato = contrato.path("idDeliveryType").asText();
            idsNoExistenEnMaestros.add(idContrato);
        }
        List<JsonNode> contratosValidosParaElPesoTotal = cargarContratosValidos(maestroContratosAper, pesoTotal);
        Set<String> idsValidos = contratosValidosParaElPesoTotal.stream()
                .map(c -> c.path("idDeliveryType").asText())
                .collect(Collectors.toSet());

        List<JsonNode> contratosValidos = new ArrayList<>();
        List<JsonNode> contratosNoValidos = new ArrayList<>();

        for (JsonNode contrato : contratos) {
            String id = contrato.path("idDeliveryType").asText();
            if (!idsNoExistenEnMaestros.contains(id)) {
                if (idsValidos.contains(id)) {
                    contratosValidos.add(contrato);
                } else {
                    contratosNoValidos.add(contrato);
                }
            }
        }

        Map<String, List<JsonNode>> resultado = new HashMap<>();
        resultado.put("contratosValidos", contratosValidos);
        resultado.put("contratosNoValidos", contratosNoValidos);
        resultado.put("contratosNoExistentesEnMaestros", contratosNoExistentesEnMaestros);

        return resultado;
    }

    private List<JsonNode> cargarContratosInexistentesEnMaestros(JsonNode contratos, JsonNode maestroContratosAper) {
        Set<String> idsEnMaestros = new HashSet<>();
        for (JsonNode item : maestroContratosAper) {
            idsEnMaestros.add(item.path("idDeliveryType").asText());
        }

        List<JsonNode> contratosNoExistentes = new ArrayList<>();
        for (JsonNode contrato : contratos) {
            String idContrato = contrato.path("idDeliveryType").asText();
            if (!idsEnMaestros.contains(idContrato)) {
                contratosNoExistentes.add(contrato);
            }
        }
        return contratosNoExistentes;
    }

    private List<JsonNode> cargarContratosValidos(JsonNode maestroContratosAper, double pesoTotal) {
        List<JsonNode> contratosValidos = new ArrayList<>();
        String pesoCondicion = (pesoTotal > 50) ? "si" : "no";

        for (JsonNode contrato : maestroContratosAper) {
            String flag = contrato.path("validoParaPesoTotalMayor50Kg").asText("").toLowerCase();
            if (flag.equals(pesoCondicion)) {
                contratosValidos.add(contrato);
            }
        }

        return contratosValidos;
    }

    private Mono<List<JsonNode>> consultaTarifasContratos(String codigoPostalDestino, List<JsonNode> contratosValidos, String cliente, List<Map<String, Double>> productosParseados) {
        if (contratosValidos == null || contratosValidos.isEmpty()) {
            return Mono.just(Collections.emptyList());
        }

        List<Mono<JsonNode>> llamadas = contratosValidos.stream()
                .map(contrato -> {
                    Map<String, String> params = armarParams(codigoPostalDestino, contrato, cliente, productosParseados);
                    return getTarifaFromAndreani(params).map(json -> (JsonNode) json);
                })
                .collect(Collectors.toList());

        return Flux.merge(llamadas) // retorno llamadas en paralelo al mismo tiempo y las espero todas juntas
                .filter(Objects::nonNull)
                .collectList();
    }

    private Map<String, String> armarParams(String codigoPostalDestino, JsonNode contrato, String cliente, List<Map<String, Double>> productosParseados) {
        Map<String, String> params = new HashMap<>();
        params.put("cpDestino", codigoPostalDestino);
        params.put("contrato", contrato.path("idcontract").asText(""));
        params.put("cliente", cliente);
        params.put("sucursalOrigen", "");
        params.put("deliveryTypeName", contrato.path("deliveryTypeName").asText(""));
        return params;
    }

    public Mono<ObjectNode> getTarifaFromAndreani(Map<String, String> paramsConsultaApi) {
        long startTime = System.currentTimeMillis();
        Map<String, String> params = new HashMap<>();
        paramsConsultaApi.forEach((key, value) -> {
            if (value != null && !value.isEmpty() && !key.equals("deliveryTypeName")) {
                params.put(key, value);
            }
        });

        // Construir URL con query params
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(TARIFAS_APER);
        params.forEach(builder::queryParam);
        String finalUrl = builder.build().toUriString();

        String logUrl = java.net.URLDecoder.decode(finalUrl, StandardCharsets.UTF_8);
        System.out.println(LocalDateTime.now().toString().replace("T", " ") + " GET: " + logUrl);

        Map<String, String> headers = Map.of("Content-Type", "application/json",
                "Accept", "application/json");

        return webClientHelper.executeGetRequest(finalUrl, headers)
                .flatMap(jsonResponse -> {
                    long elapsed = System.currentTimeMillis() - startTime;
                    System.out.println("⏱️ Call Api Tarifa Andreani Service Time: " + elapsed + "ms");

                    ObjectNode result = JsonNodeFactory.instance.objectNode();
                    result.put("idcontract", paramsConsultaApi.get("contrato"));
                    result.put("tipoEntrega", paramsConsultaApi.get("deliveryTypeName"));
                    ArrayNode envios = JsonNodeFactory.instance.arrayNode();
                    envios.add(jsonResponse.getBody());
                    result.set("envios", envios);

                    return Mono.just(result);
                })
                .onErrorResume(error -> {
                    error.printStackTrace();
                    return Mono.empty();
                });

    }

}
