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

    public static String DISTRIBUIDOR_CREDENTIALS_WOO;

    @PostConstruct
    private void init() {
        DISTRIBUIDOR_CREDENTIALS_WOO = credencialesWoo;
    }
}
