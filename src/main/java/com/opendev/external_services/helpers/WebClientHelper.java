package com.opendev.external_services.helpers;

import com.fasterxml.jackson.databind.JsonNode;
import com.opendev.external_services.exceptions.ExternalApiException;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

public class WebClientHelper {

    private final WebClient webClient;

    public WebClientHelper(WebClient.Builder webClientBuilder, String baseUrl) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
    }

    public Mono<ResponseEntity<String>> executeGetRequest(String path, Map<String, String> headers) {
        return webClient.get()
                .uri(path)
                .headers(httpHeaders -> httpHeaders.setAll(headers))
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleErrorResponse)
                .toEntity(String.class);
    }

    public Mono<ResponseEntity<JsonNode>> executeJsonGetRequest(String path, Map<String, String> headers) {
        return webClient.get()
                .uri(path)
                .headers(httpHeaders -> httpHeaders.setAll(headers))
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleErrorResponse)
                .toEntity(JsonNode.class);
    }


    public Mono<ResponseEntity<String>> executeGetRequestWithQueryParams(String path, MultiValueMap<String, String> queryParams, Map<String, String> headers) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path(path).queryParams(queryParams).build())
                .headers(httpHeaders -> httpHeaders.setAll(headers))
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleErrorResponse)
                .toEntity(String.class);
    }

    public Mono<ResponseEntity<String>> executePostRequest(String path, Map<String, String> headers, JsonNode body) {
        return webClient.post()
                .uri(path)
                .headers(httpHeaders -> httpHeaders.setAll(headers))
                .bodyValue(body)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleErrorResponse)
                .toEntity(String.class);
    }

    public Mono<ResponseEntity<String>> executePostRequestWithoutBody(String path, Map<String, String> headers) {
        return webClient.post()
                .uri(path)
                .headers(httpHeaders -> httpHeaders.setAll(headers))
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleErrorResponse)
                .toEntity(String.class);
    }

    private Mono<ExternalApiException> handleErrorResponse(ClientResponse response) {
        return response.bodyToMono(String.class)
                .flatMap(errorBody -> Mono.error(new ExternalApiException(
                        response.statusCode(),
                        response.headers().asHttpHeaders(),
                        errorBody)));
    }
}