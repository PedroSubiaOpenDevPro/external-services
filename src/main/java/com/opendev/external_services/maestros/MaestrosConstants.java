package com.opendev.external_services.maestros;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "maestros")
@Data
public class MaestrosConstants {

    private String baseUrl;
    private String credencialesMaestros;

    public static String MAESTROS_BASE_URL;
    public static String CREDENCIALES_MAESTROS;

    @PostConstruct
    private void init() {
        MAESTROS_BASE_URL = baseUrl;
        CREDENCIALES_MAESTROS = credencialesMaestros;
    }

}
