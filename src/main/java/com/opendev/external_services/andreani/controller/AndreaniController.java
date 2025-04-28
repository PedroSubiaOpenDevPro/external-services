package com.opendev.external_services.andreani.controller;

import com.opendev.external_services.andreani.service.AndreaniService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/andreani")
@Slf4j
public class AndreaniController {

    @Autowired
    private AndreaniService andreaniService;

    @GetMapping("/login")
    public Mono<ResponseEntity<String>> login(@RequestHeader("Authorization") String basicAuthorization){
        return this.andreaniService.getLogin(basicAuthorization);
    }

}
