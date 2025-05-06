package com.opendev.external_services.ceva.services;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.opendev.external_services.andreani.service.AndreaniService;
import com.opendev.external_services.distribuidor.services.DistribuidorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import reactor.core.publisher.Mono;

import static com.opendev.external_services.ceva.CevaConstants.CREDENCIALES_CEVA;
import static com.opendev.external_services.helpers.JsonHelpers.extractValue;

@Service
public class CevaService {

    @Autowired
    AndreaniService andreaniService;
    @Autowired
    DistribuidorService distribuidorService;

    public Mono<ResponseEntity<String>> executeGet(String idTransaccion, String token, JsonNode body) {
        return andreaniService.getLogin(token)
                .flatMap(loginAndreaniResponse -> {
                    HttpStatusCode status = loginAndreaniResponse.getStatusCode();

                    if (status == HttpStatus.UNAUTHORIZED || status == HttpStatus.FORBIDDEN) {
                        return Mono.just(ResponseEntity.status(status)
                                .body(loginAndreaniResponse.getBody()));
                    }
                    try {
                        ((ObjectNode) body).put("idTransaccion", Integer.parseInt(idTransaccion));
                    } catch (NumberFormatException e) {
                        return Mono.just(ResponseEntity.badRequest().body("idTransaccion inválido: debe ser un número"));
                    }
                    return distribuidorService.getLogin(CREDENCIALES_CEVA)
                            .flatMap(loginDistribuidorResponse -> {
                                String tokenDistribuidor = extractValue(loginDistribuidorResponse.getBody(), "token");
                                return distribuidorService.callIntegracionRestRR("ceva_contratos_pedidos_get", tokenDistribuidor, body);
                            });
                });
    }

    public Mono<ResponseEntity<String>> executePost(String token, JsonNode body) {
        return andreaniService.getLogin(token)
                .flatMap(loginAndreaniResponse -> {
                    HttpStatusCode status = loginAndreaniResponse.getStatusCode();

                    if (status == HttpStatus.UNAUTHORIZED || status == HttpStatus.FORBIDDEN) {
                        return Mono.just(ResponseEntity.status(status)
                                .body(loginAndreaniResponse.getBody()));
                    }

                    return distribuidorService.getLogin(CREDENCIALES_CEVA)
                            .flatMap(loginDistribuidorResponse -> {
                                String tokenDistribuidor = extractValue(loginDistribuidorResponse.getBody(), "token");
                                return distribuidorService.callIntegracionRestRR("ceva_contratos_pedidos_post", tokenDistribuidor, body);
                            });
                });
    }
}
