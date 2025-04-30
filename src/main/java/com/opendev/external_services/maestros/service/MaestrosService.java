package com.opendev.external_services.maestros.service;

import com.opendev.external_services.helpers.WebClientHelper;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.util.Map;

import static com.opendev.external_services.maestros.MaestrosConstants.*;

@Service
public class MaestrosService {

    private WebClientHelper webClientHelper;

    public MaestrosService(WebClient.Builder webClientBuilder){

        this.webClientHelper = new WebClientHelper(webClientBuilder
                // para pruebas en local host descomentar la linea ".clientConnector"
                // .clientConnector(new ReactorClientHttpConnector(getInsecureHttpClient()))
                .defaultHeaders(httpHeaders -> httpHeaders.addAll(getDefaultHeaders()))
                , MAESTROS_BASE_URL);
    }

    public Mono<ResponseEntity<String>> consultarMaestros(String nombreMaestros, MultiValueMap<String, String> queryParams) {
        return webClientHelper.executeGetRequestWithQueryParams("/" + nombreMaestros, queryParams, Map.of());
    }

    private HttpHeaders getDefaultHeaders(){
        return new HttpHeaders() {{
            add("Content-Type", "application/json");
            add("Authorization", CREDENCIALES_MAESTROS);
        }};
    }

    // IMPORTANTE usar solo en localhost
    private HttpClient getInsecureHttpClient(){
        return HttpClient.create().secure(t -> t.sslContext(
                SslContextBuilder.forClient().
                        trustManager(InsecureTrustManagerFactory.INSTANCE)));
    }
}
