package com.opendev.external_services.aper.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.opendev.external_services.aper.service.AperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/aper")
public class AperController {

    @Autowired
    private AperService service;

    @PostMapping
    public Mono<List<ObjectNode>> getAperData(@RequestBody JsonNode body) {
        return service.createResponseToAper(body);
    }

    @PostMapping("/webhook")
    public Mono<ResponseEntity<String>> sendWebhook(@RequestHeader("api_key") String apiKey, @RequestBody JsonNode body) {
        return service.sendWebhook(apiKey, body);
    }

    @PostMapping("/webhook/cotizaciones")
    public Mono<ResponseEntity<String>> getCotizaciones(@RequestHeader("api_key") String apiKey, @RequestBody JsonNode body) {
        return service.getCotizaciones(apiKey, body);
    }


}
