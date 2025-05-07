package com.opendev.external_services.santander;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "distribuidor")
@Data
public class SantanderConstants {

    private String credencialesSantander;
    private String url_permited;

    public static String DISTRIBUIDOR_CREDENTIALS_SANTANDER;
    public static String URL_PERMITED;
    public static String ALTA_PAQUETES = "santander_altaDePaquetes";

    @PostConstruct
    private void init() {
        DISTRIBUIDOR_CREDENTIALS_SANTANDER = credencialesSantander;
        URL_PERMITED = url_permited;
    }
}
