package com.opendev.external_services.ceva.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.opendev.external_services.ceva.services.CevaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;


@RestController
@RequestMapping("/ceva")
public class CevaController {


    @Autowired
    private CevaService cevaService;

    @GetMapping("/{idTransaccion}")
    public Mono<ResponseEntity<String>> executeGetFlux(@PathVariable String idTransaccion, @RequestHeader("x-authorization-token") String token,
                                                    @RequestBody JsonNode body) {
        return cevaService.executeGet(idTransaccion, token, body);
    }

    @PostMapping
    public Mono<ResponseEntity<String>> executePostFlux(@RequestHeader("x-authorization-token") String token,
                                                    @RequestBody JsonNode body) {
        return cevaService.executePost(token, body);
    }


}
