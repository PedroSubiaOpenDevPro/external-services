package com.opendev.external_services.andreani;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "andreani")
@Data
public class AndreaniConstants {

    private String baseUrl;
    private String pathLogin;

    public static String ANDREANI_URL_BASE;
    public static String ANDREANI_PATH_LOGIN;

    @PostConstruct
    private void init() {
        ANDREANI_URL_BASE = baseUrl;
        ANDREANI_PATH_LOGIN = pathLogin;
    }
}