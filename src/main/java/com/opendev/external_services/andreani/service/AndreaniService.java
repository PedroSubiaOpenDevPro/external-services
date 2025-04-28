package com.opendev.external_services.andreani.service;

import com.opendev.external_services.helpers.WebClientHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

import static com.opendev.external_services.andreani.AndreaniConstants.ANDREANI_PATH_LOGIN;
import static com.opendev.external_services.andreani.AndreaniConstants.ANDREANI_URL_BASE;

@Service
@Slf4j
public class AndreaniService {

    private final WebClientHelper webClientHelper;

    public AndreaniService(WebClient.Builder webClientBuilder) {
        this.webClientHelper = new WebClientHelper(webClientBuilder, ANDREANI_URL_BASE);
    }

    public Mono<ResponseEntity<String>> getLogin(String basicAuthorization) {
        return webClientHelper.executeGetRequest(ANDREANI_PATH_LOGIN, Map.of("Authorization", basicAuthorization));
    }

}
