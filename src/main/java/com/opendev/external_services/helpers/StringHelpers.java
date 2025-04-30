package com.opendev.external_services.helpers;

public class StringHelpers {

    public static String borrarSaltosLineaYTabulaciones (String cadena ){
        return cadena.replaceAll("[\\n\\t]", "").replaceAll("\\s+", " ");
    }
}
