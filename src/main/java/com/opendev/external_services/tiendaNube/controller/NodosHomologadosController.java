package com.opendev.external_services.tiendaNube.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.opendev.external_services.tiendaNube.service.NodosHomologadosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/nodos_homologados/tienda_nube")
public class NodosHomologadosController {

    @Autowired
    private NodosHomologadosService nodosHomologadosService;

    @PostMapping("/post_master")
    public Mono<ResponseEntity<String>> postMaster(@RequestBody JsonNode body){
        return nodosHomologadosService.executePostMaster(body);
    }

}
