package com.opendev.external_services.ceva;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "distribuidor")
@Data
public class CevaConstants {

    private String credencialesCeva;

    public static String CREDENCIALES_CEVA;

    @PostConstruct
    private void init() {
        CREDENCIALES_CEVA = credencialesCeva;
    }
}
