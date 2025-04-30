package com.opendev.external_services.tiendaNube;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "distribuidor")
public class TiendaNubeConstants {

    private String credencialesTiendaNube;

    public static String CREDENCIALES_TIENDA_NUBE;
    public static String INTEGRACION_REQUEST_MASTER = "tiendaNube_nodosHomologados_requestMaster";

    @PostConstruct
    private void init(){
        CREDENCIALES_TIENDA_NUBE = credencialesTiendaNube;
    }

}
