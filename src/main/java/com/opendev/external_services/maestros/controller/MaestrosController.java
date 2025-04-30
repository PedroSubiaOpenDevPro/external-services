package com.opendev.external_services.maestros.controller;

import com.opendev.external_services.maestros.service.MaestrosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/maestros")
public class MaestrosController {

    @Autowired
    private MaestrosService maestrosService;

    @GetMapping("/{nombreMaestros}")
    public Mono<ResponseEntity<String>> consultarMaestros(@PathVariable String nombreMaestros,
                                                          @RequestParam MultiValueMap<String, String> queryParams){
        return maestrosService.consultarMaestros(nombreMaestros, queryParams);
    }

}
