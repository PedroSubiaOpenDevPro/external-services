package com.opendev.external_services.distribuidor.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.opendev.external_services.helpers.WebClientHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

import static com.opendev.external_services.distribuidor.DistribuidorConstants.*;

@Service
@Slf4j
public class DistribuidorService {

    private final WebClientHelper webClientHelper;

    public DistribuidorService(WebClient.Builder webClientBuilder) {
        this.webClientHelper = new WebClientHelper(webClientBuilder, DISTRIBUIDOR_BASE_URL);
    }

    public Mono<ResponseEntity<String>> getLogin(String basicAuthorization) {
        return webClientHelper.executePostRequestWithoutBody(DISTRIBUIDOR_LOGIN, Map.of("Authorization", basicAuthorization));
    }

    public Mono<ResponseEntity<String>> callIntegracionRest(String integration, String token, JsonNode bodyIntegration) {
        return webClientHelper.executePostRequest(DISTRIBUIDOR_INTEGRACION_REST + integration, Map.of("x-authorization-token", token), bodyIntegration);
    }

    public Mono<ResponseEntity<String>> callIntegracionRestRR(String integration, String token, JsonNode bodyIntegration) {
        return webClientHelper.executePostRequest(DISTRIBUIDOR_INTEGRACION_REST_RR + integration, Map.of("x-authorization-token", token), bodyIntegration);
    }
}
