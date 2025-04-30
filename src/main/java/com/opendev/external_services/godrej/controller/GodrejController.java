package com.opendev.external_services.godrej.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.opendev.external_services.godrej.service.GodrejService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/godrej")
public class GodrejController {

    @Autowired
    private GodrejService godrejService;

    @PostMapping("/reporte")
    public Mono<ResponseEntity<String>> generateReporte(@RequestBody JsonNode body){
        return godrejService.executeIntegration(body);
    }

}
