package com.opendev.external_services.roche;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "distribuidor")
@Data
public class RocheConstants {

    private String credencialesRoche;

    public static String CREDENCIALES_ROCHE;

    public static String ALTA_PRODUCTO_GET = "roche_alta_producto_get";
    public static String ALTA_PRODUCTO_POST = "roche_alta_producto_post";

    public static String ALTA_ABASTECIMIENTO_GET = "roche_alta_abastecimiento_get";
    public static String ALTA_ABASTECIMIENTO_POST = "roche_alta_abastecimiento_post";

    public static String ALTA_LOTES_GET = "roche_alta_lotes_get";
    public static String ALTA_LOTES_POST = "roche_alta_lotes_post";

    public static String CAMBIO_ESTADO_LOTE_GET = "roche_cambio_estado_lotes_get";
    public static String CAMBIO_ESTADO_LOTE_POST = "roche_cambio_estado_lotes_post";

    public static String ALTA_PEDIDOS_GET = "roche_alta_pedidos_get";
    public static String ALTA_PEDIDOS_POST = "roche_alta_pedidos_post";

    public static String FACTURACION_GET = "roche_facturacion_get";
    public static String FACTURACION_POST = "roche_facturacion_post";

    public static String ABASTECIMIENTO_GET = "roche_solicitud_kitting_get";
    public static String ABASTECIMIENTO_POST = "roche_solicitud_kitting_post";

    public static String REMITO_GET = "roche_get_remito";
    public static String REMITO_POST = "roche_get_remito";

    @PostConstruct
    private void init(){
        CREDENCIALES_ROCHE = credencialesRoche;
    }
}
