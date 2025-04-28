package com.opendev.external_services.config;

import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.*;
import org.springframework.http.server.reactive.*;
import org.springframework.stereotype.Component;
import org.springframework.web.server.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class LoggingWebFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        if (request.getHeaders().getContentLength() <= 0) {
            log.info("App Incoming Request => {} {} Body: (no Body)", request.getMethod(), request.getPath());
            ServerHttpResponse mutatedResponse = createMutatedResponse(response, request);
            return chain.filter(exchange.mutate()
                    .request(request)
                    .response(mutatedResponse)
                    .build());
        }
        // 1. Obtener el cuerpo de la solicitud
        Flux<DataBuffer> body = request.getBody();
        return DataBufferUtils.join(body).flatMap(dataBuffer -> {
            // Guardar el contenido del buffer en un array de bytes
            byte[] bodyContent = new byte[dataBuffer.readableByteCount()];
            dataBuffer.read(bodyContent);
            DataBufferUtils.release(dataBuffer);
            String requestBody = (new String(bodyContent, StandardCharsets.UTF_8)).replaceAll("\\n", "").replaceAll("\\t", "");
            log.info("App Incoming Request => {} {} Body: {}", request.getMethod(), request.getPath(), requestBody);
            ServerHttpRequest mutatedRequest = createMutatedRequest(request, bodyContent);
            ServerHttpResponse mutatedResponse = createMutatedResponse(response, request);
            return chain.filter(exchange.mutate()
                    .request(mutatedRequest)
                    .response(mutatedResponse)
                    .build());
        });
    }

    private ServerHttpRequest createMutatedRequest(ServerHttpRequest request, byte[] bodyContent) {
        return new ServerHttpRequestDecorator(request) {
            @Override
            public Flux<DataBuffer> getBody() {
                // Devolver el cuerpo cacheado
                return Flux.defer(() -> {
                    DataBuffer buffer = new DefaultDataBufferFactory().wrap(bodyContent);
                    return Mono.just(buffer);
                });
            }
        };
    }

    private ServerHttpResponse createMutatedResponse(ServerHttpResponse response, ServerHttpRequest request) {
        DataBufferFactory bufferFactory = response.bufferFactory();
        return new ServerHttpResponseDecorator(response) {
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                return Flux.from(body)
                        .collectList()
                        .flatMap(dataBuffers -> {
                            DataBuffer joined = bufferFactory.join(dataBuffers);
                            byte[] content = new byte[joined.readableByteCount()];
                            joined.read(content);
                            DataBufferUtils.release(joined);
                            String responseBody = new String(content, StandardCharsets.UTF_8);
                            log.info("App Outgoing Response => {} {} Status: {} Body: {}",
                                    request.getMethod(), request.getPath(), getStatusCode(), responseBody);
                            return getDelegate().writeWith(Mono.just(bufferFactory.wrap(content)));
                        });
            }
        };
    }
}
