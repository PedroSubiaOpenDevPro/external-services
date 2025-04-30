package com.opendev.external_services.godrej;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "distribuidor")
@Data
public class GodrejConstants {

    private String credencialesAndreani;

    public static String CREDENCIALES_ANDREANI;

    public static String PULL_NOVEDADES_DISTRIBUCION = "godrej_pull_novedades_distribucion";

    @PostConstruct
    private void init(){
        CREDENCIALES_ANDREANI = credencialesAndreani;
    }

}
