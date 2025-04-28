package com.opendev.external_services.exceptions;

import lombok.Data;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;

@Data
public class ExternalApiException extends RuntimeException {

    private final HttpStatusCode statusCode;
    private final HttpHeaders headers;
    private final String responseBody;

    public ExternalApiException(HttpStatusCode statusCode,HttpHeaders headers, String responseBody) {
        super("External API error: " + statusCode.value());
        this.statusCode = statusCode;
        this.headers = headers;
        this.responseBody = responseBody;
    }

}