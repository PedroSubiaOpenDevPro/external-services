package com.opendev.external_services.godrej.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.opendev.external_services.distribuidor.services.DistribuidorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import static com.opendev.external_services.helpers.JsonHelpers.extractValue;
import static com.opendev.external_services.godrej.GodrejConstants.*;

@Service
public class GodrejService {

    @Autowired
    DistribuidorService distribuidorService;

    public Mono<ResponseEntity<String>> executeIntegration(JsonNode body){
        return distribuidorService.getLogin(CREDENCIALES_ANDREANI).flatMap(loginDistribuidorResponse -> {
            String tokenDistribuidor = extractValue(loginDistribuidorResponse.getBody(), "token");
            return distribuidorService.callIntegracionRest(PULL_NOVEDADES_DISTRIBUCION, tokenDistribuidor, body);
        });
    }

}
