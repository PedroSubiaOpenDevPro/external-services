package com.opendev.external_services.woo;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "distribuidor")
@Data
public class WooConstants {

    private String credencialesWoo;

    public static String CREDENCIALES_WOO;

    @PostConstruct
    private void init() {
        CREDENCIALES_WOO = credencialesWoo;
    }
}
