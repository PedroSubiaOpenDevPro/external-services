package com.opendev.external_services.aper;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "distribuidor")
@Data
public class AperConstants {

    private String credencialesAper;
    private String api_key;
    private String tarifas;

    public static String CREDENCIALES_APER;
    public static String API_KEY;
    public static String TARIFAS_APER;
    @PostConstruct
    private void init() {
        CREDENCIALES_APER = credencialesAper;
        API_KEY = api_key;
        TARIFAS_APER = tarifas;
    }
}
