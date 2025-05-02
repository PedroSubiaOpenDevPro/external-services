package com.opendev.external_services.iFlowlead;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "distribuidor")
@Data
public class IFlowleadConstants {

    private String credencialesIflowlead;

    public static String CREDENCIALES_IFLOWLEAD;

    public static String INTEGRACION_IFLOWLEAD_SNOVIO = "iFlowLead_Snovio";

    @PostConstruct
    private void init(){
        CREDENCIALES_IFLOWLEAD = credencialesIflowlead;
    }
}
