package com.opendev.external_services.santander.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.opendev.external_services.santander.services.SantanderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import static com.opendev.external_services.santander.SantanderConstants.ALTA_PAQUETES;

@RestController
@RequestMapping("/santander")
public class SantanderController {

    @Autowired
    private SantanderService service;


    @PostMapping("/services/altaDePaquetes")
    public Mono<ResponseEntity<String>> postCallDistributor(@RequestHeader("x-forwarded-for") String ip,
                                                          @RequestBody JsonNode body) {
        return service.postCallDistributor(ALTA_PAQUETES, ip, body);
    }
}
