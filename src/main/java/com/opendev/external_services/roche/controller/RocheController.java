package com.opendev.external_services.roche.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.opendev.external_services.distribuidor.services.DistribuidorService;
import com.opendev.external_services.roche.service.RocheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import static com.opendev.external_services.roche.RocheConstants.*;

@RestController
@RequestMapping("/services")
public class RocheController {

    @Autowired
    private RocheService rocheService;

    @Autowired
    private DistribuidorService distribuidorService;

    @GetMapping("/login")
    public Mono<ResponseEntity<String>> getLogin(@RequestHeader("Authorization") String basicAuthorization) {
        return this.distribuidorService.getLogin(basicAuthorization);
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<String>> postLogin(@RequestHeader("Authorization") String basicAuthorization) {
        return this.distribuidorService.getLogin(basicAuthorization);
    }

    @GetMapping("/productos/{id}")
    public Mono<ResponseEntity<String>> getProductos(@PathVariable String id,
                                                     @RequestHeader("x-authorization-token") String tokenFlexi) {
        return rocheService.proccessGet(id, tokenFlexi, ALTA_PRODUCTO_GET);
    }

    @PostMapping("/productos")
    public Mono<ResponseEntity<String>> postProductos(@RequestBody JsonNode body,
                                                      @RequestHeader("x-authorization-token") String tokenFlexi) {
        return rocheService.proccessPost(body, tokenFlexi, ALTA_PRODUCTO_POST);
    }

    @GetMapping("/abastecimiento/{id}")
    public Mono<ResponseEntity<String>> getAbastecimiento(@PathVariable String id,
                                                          @RequestHeader("x-authorization-token") String tokenFlexi) {
        return rocheService.proccessGet(id, tokenFlexi, ALTA_ABASTECIMIENTO_GET);
    }

    @PostMapping("/abastecimiento")
    public Mono<ResponseEntity<String>> postAbastecimiento(@RequestBody JsonNode body,
                                                           @RequestHeader("x-authorization-token") String tokenFlexi) {
        return rocheService.proccessPost(body, tokenFlexi, ALTA_ABASTECIMIENTO_POST);
    }

    @GetMapping("/lotes/{id}")
    public Mono<ResponseEntity<String>> getLotes(@PathVariable String id,
                                                 @RequestHeader("x-authorization-token") String tokenFlexi) {
        return rocheService.proccessGet(id, tokenFlexi, ALTA_LOTES_GET);
    }

    @PostMapping("/lotes")
    public Mono<ResponseEntity<String>> postLotes(@RequestBody JsonNode body,
                                                  @RequestHeader("x-authorization-token") String tokenFlexi) {
        return rocheService.proccessPost(body, tokenFlexi, ALTA_LOTES_POST);
    }

    @GetMapping("/cambioestadolote/{id}")
    public Mono<ResponseEntity<String>> getCambioEstadoLote(@PathVariable String id,
                                                            @RequestHeader("x-authorization-token") String tokenFlexi) {
        return rocheService.proccessGet(id, tokenFlexi, CAMBIO_ESTADO_LOTE_GET);
    }

    @PostMapping("/cambioestadolote")
    public Mono<ResponseEntity<String>> postCambioEstadoLote(@RequestBody JsonNode body,
                                                             @RequestHeader("x-authorization-token") String tokenFlexi) {
        return rocheService.proccessPost(body, tokenFlexi, CAMBIO_ESTADO_LOTE_POST);
    }

    @GetMapping("/pedidos/{id}")
    public Mono<ResponseEntity<String>> getPedidos(@PathVariable String id,
                                                   @RequestHeader("x-authorization-token") String tokenFlexi) {
        return rocheService.proccessGet(id, tokenFlexi, ALTA_PEDIDOS_GET);
    }

    @PostMapping("/pedidos")
    public Mono<ResponseEntity<String>> postPedidos(@RequestBody JsonNode body,
                                                    @RequestHeader("x-authorization-token") String tokenFlexi) {
        return rocheService.proccessPost(body, tokenFlexi, ALTA_PEDIDOS_POST);
    }

    @GetMapping("/facturacion/{id}")
    public Mono<ResponseEntity<String>> getFacturacion(@PathVariable String id,
                                                       @RequestHeader("x-authorization-token") String tokenFlexi) {
        return rocheService.proccessGet(id, tokenFlexi, FACTURACION_GET);
    }

    @PostMapping("/facturacion")
    public Mono<ResponseEntity<String>> postFacturacion(@RequestBody JsonNode body,
                                                        @RequestHeader("x-authorization-token") String tokenFlexi) {
        return rocheService.proccessPost(body, tokenFlexi, FACTURACION_POST);
    }

    @GetMapping("/acondicionamiento/{id}")
    public Mono<ResponseEntity<String>> getAcondicionamiento(@PathVariable String id,
                                                             @RequestHeader("x-authorization-token") String tokenFlexi) {
        return rocheService.proccessGet(id, tokenFlexi, FACTURACION_GET);
    }

    @PostMapping("/acondicionamiento")
    public Mono<ResponseEntity<String>> postAcondicionamiento(@RequestBody JsonNode body,
                                                              @RequestHeader("x-authorization-token") String tokenFlexi) {
        return rocheService.proccessPost(body, tokenFlexi, FACTURACION_POST);
    }

    @GetMapping("/restrr/{id}")
    public Mono<ResponseEntity<String>> getRemito(@PathVariable String id,
                                                  @RequestHeader("x-authorization-token") String tokenFlexi) {
        return rocheService.proccessGet(id, tokenFlexi, REMITO_GET);
    }

//    @PostMapping("/restrr")
//    public Mono<ResponseEntity<String>> postRemito(@RequestBody JsonNode body,
//                                                   @RequestHeader("x-authorization-token") String tokenFlexi) {
//        return rocheService.proccessPost(body, tokenFlexi, REMITO_POST);
//    }

}
