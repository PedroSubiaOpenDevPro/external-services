package com.opendev.external_services.iFlowlead.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.opendev.external_services.iFlowlead.service.IFlowleadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/iFlow")
public class IFlowleadController {

    @Autowired
    private IFlowleadService iFlowleadService;

    @PostMapping("/generateReport")
    public Mono<ResponseEntity<String>> generarReporte(@RequestBody JsonNode body){
        return iFlowleadService.executeIntegration(body);
    }

}
