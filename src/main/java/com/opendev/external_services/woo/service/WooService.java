package com.opendev.external_services.woo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.opendev.external_services.andreani.service.AndreaniService;
import com.opendev.external_services.distribuidor.services.DistribuidorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;

import static com.opendev.external_services.helpers.JsonHelpers.buildJsonFromMap;
import static com.opendev.external_services.helpers.JsonHelpers.extractValue;
import static com.opendev.external_services.woo.WooConstants.*;

@Service
public class WooService {

    @Autowired
    AndreaniService andreaniService;

    @Autowired
    DistribuidorService distribuidorService;

    private static final Map<String, String> listIntegrationsNames = Map.of(
            "sucursales", "Woo_Gla_SP49_BUS4457",
            "cotizaciones", "Woo_Cotizaciones_BUS4592",
            "orden", "Woo_Gla_orden_SP56_BUS5548"
    );

    public Mono<ResponseEntity<String>> executeIntegration(String integracion, String credencial, JsonNode body) {
        String pathIntegration = listIntegrationsNames.get(integracion);
        if (pathIntegration == null) {
            String msgError = buildJsonFromMap(Map.of("status", "500", "message", "error interno del servidor")).asText();
            return Mono.just(ResponseEntity.internalServerError().body(msgError));
        }
        return andreaniService.getLogin(credencial)
                .flatMap(loginAndreaniResponse ->
                        distribuidorService.getLogin(DISTRIBUIDOR_CREDENTIALS_WOO)
                                .flatMap(loginDistribuidorResponse -> {
                                    String tokenDistribuidor = extractValue(loginDistribuidorResponse.getBody(), "token");
                                    return distribuidorService.callIntegracionRestRR(pathIntegration, tokenDistribuidor, body);
                                })
                );
    }

}
