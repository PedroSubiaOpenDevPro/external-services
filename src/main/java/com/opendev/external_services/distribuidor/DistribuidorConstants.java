package com.opendev.external_services.distribuidor;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "distribuidor")
@Data
public class DistribuidorConstants {

    private String baseUrl;
    private String pathLogin;
    private String pathRest;
    private String pathRestrr;

    public static String DISTRIBUIDOR_BASE_URL;
    public static String DISTRIBUIDOR_LOGIN;
    public static String DISTRIBUIDOR_INTEGRACION_REST;
    public static String DISTRIBUIDOR_INTEGRACION_REST_RR;

    @PostConstruct
    private void init() {
        DISTRIBUIDOR_BASE_URL = baseUrl;
        DISTRIBUIDOR_LOGIN = pathLogin;
        DISTRIBUIDOR_INTEGRACION_REST = pathRest;
        DISTRIBUIDOR_INTEGRACION_REST_RR = pathRestrr;
    }
}
