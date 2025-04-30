package com.opendev.external_services.roche.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.opendev.external_services.distribuidor.services.DistribuidorService;
import com.opendev.external_services.helpers.Validators;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;

import static com.opendev.external_services.helpers.JsonHelpers.buildJsonFromMap;
import static com.opendev.external_services.helpers.JsonHelpers.extractValue;
import static com.opendev.external_services.roche.RocheConstants.*;

@Service
public class RocheService {

    @Autowired
    DistribuidorService distribuidorService;

    public Mono<ResponseEntity<String>> proccessGet(String id, String token, String integracion){
        Validators.validateExpiresDateJWT(token);
        JsonNode body = integracion.equals(REMITO_GET)
                ? buildJsonFromMap(Map.of("ordenExterna", id))
                : buildJsonFromMap(Map.of("idTransaccion", id));
        return distribuidorService.getLogin(CREDENCIALES_ROCHE).flatMap(loginDistribuidorResponse -> {
            String tokenDistribuidor = extractValue(loginDistribuidorResponse.getBody(), "token");
            return distribuidorService.callIntegracionRestRR(integracion, tokenDistribuidor, body);
        });
    }

    public Mono<ResponseEntity<String>> proccessPost(JsonNode body, String token, String integracion){
        Validators.validateExpiresDateJWT(token);
        return distribuidorService.getLogin(CREDENCIALES_ROCHE).flatMap(loginDistribuidorResponse -> {
            String tokenDistribuidor = extractValue(loginDistribuidorResponse.getBody(), "token");
            return distribuidorService.callIntegracionRestRR(integracion, tokenDistribuidor, body);
        });
    }

}
