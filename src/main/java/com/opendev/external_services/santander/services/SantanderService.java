package com.opendev.external_services.santander.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.opendev.external_services.distribuidor.services.DistribuidorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Arrays;

import static com.opendev.external_services.helpers.JsonHelpers.extractValue;
import static com.opendev.external_services.santander.SantanderConstants.*;

@Service
public class SantanderService {

    @Autowired
    DistribuidorService distribuidorService;

    public Mono<ResponseEntity<String>> postCallDistributor(String integracion, String ip, JsonNode body) {
        String[] allowedIps = System.getenv("URL_PERMITED").split(",");
        boolean ipAllowed = Arrays.asList(allowedIps).contains(ip);

        if (!ipAllowed) {
            return Mono.just(ResponseEntity.status(403).body("{\"message\": \"unauthorized\"}"));
        }
        return distribuidorService.getLogin(DISTRIBUIDOR_CREDENTIALS_SANTANDER).flatMap(loginDistribuidorResponse -> {

            String tokenDistribuidor = extractValue(loginDistribuidorResponse.getBody(), "token");
            return distribuidorService.callIntegracionRestRR(integracion, tokenDistribuidor, body);

        });
    }

}

