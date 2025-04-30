package com.opendev.external_services.tiendaNube.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.opendev.external_services.distribuidor.services.DistribuidorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;

import static com.opendev.external_services.helpers.JsonHelpers.*;
import static com.opendev.external_services.tiendaNube.TiendaNubeConstants.CREDENCIALES_TIENDA_NUBE;
import static com.opendev.external_services.tiendaNube.TiendaNubeConstants.INTEGRACION_REQUEST_MASTER;

@Service
public class NodosHomologadosService {

    @Autowired
    DistribuidorService distribuidorService;

    public Mono<ResponseEntity<String>> executePostMaster(JsonNode body){
        return distribuidorService.getLogin(CREDENCIALES_TIENDA_NUBE).flatMap(respLoginDistri -> {
            String tokenDistribuidor = extractValue(respLoginDistri.getBody(), "token");
            return distribuidorService.callIntegracionRestRR(INTEGRACION_REQUEST_MASTER, tokenDistribuidor, body).flatMap(respIntegration -> {
                int statusCode = respIntegration.getStatusCode().value();
                Map<String, Object> responseBody = parseStringToMap(respIntegration.getBody());
                if (statusCode == 200 && responseBody.isEmpty()) {
                    JsonNode msgError = buildJsonFromMap(Map.of("status", 404, "message", "No se encontró un usuario con ese clientId"));
                    return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(msgError.toString()));
                } else if (statusCode == 200) {
                    JsonNode msg = buildJsonFromMap(Map.of("status", 200, "content", responseBody, "message", "Created"));
                    return Mono.just(ResponseEntity.status(HttpStatus.CREATED).body(msg.toString()));
                } else {
                    return Mono.just(respIntegration);
                }
            });
        });
    }
}
