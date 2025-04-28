package com.opendev.external_services.distribuidor.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.opendev.external_services.distribuidor.services.DistribuidorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/distribuidor")
public class DistribuidorController {

    @Autowired
    private DistribuidorService distribuidorService;

    @PostMapping("/login")
    public Mono<ResponseEntity<String>> login(@RequestHeader("Authorization") String basicAuthorization) {
        return this.distribuidorService.getLogin(basicAuthorization);
    }

    @PostMapping("/rest")
    public Mono<ResponseEntity<String>> callIntegrationRest(@RequestHeader("integration") String integration,
                                                            @RequestHeader("x-authorization-token") String token,
                                                            @RequestBody JsonNode body) {
        return this.distribuidorService.callIntegracionRest(integration, token, body);
    }

    @PostMapping("/restrr")
    public Mono<ResponseEntity<String>> callIntegrationRestRR(@RequestHeader("integration") String integration,
                                                              @RequestHeader("x-authorization-token") String token,
                                                              @RequestBody JsonNode body) {
        return this.distribuidorService.callIntegracionRestRR(integration, token, body);
    }

}