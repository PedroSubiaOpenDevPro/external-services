package com.opendev.external_services.woo.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.opendev.external_services.woo.service.WooService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/woo")
public class WooController {

    @Autowired
    private WooService wooService;

    @PostMapping("/seguridad/{integracion}")
    public Mono<ResponseEntity<String>> procesarSeguridad(@PathVariable String integracion,
                                                          @RequestHeader("Authorization") String credencial,
                                                          @RequestBody JsonNode body) {
        return wooService.executeIntegration(integracion, credencial, body);
    }

}
