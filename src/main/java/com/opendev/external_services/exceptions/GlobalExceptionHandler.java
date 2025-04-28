package com.opendev.external_services.exceptions;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ExternalApiException.class)
    public ResponseEntity<String> handleExternalApiException(ExternalApiException ex) {
        return ResponseEntity
                .status(ex.getStatusCode())
                .headers( httpHeaders -> httpHeaders.addAll(ex.getHeaders()))
                .body(ex.getResponseBody());
    }

}
