package com.opendev.external_services.helpers;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.JsonNode;
import com.opendev.external_services.exceptions.ExternalApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import java.util.Date;
import java.util.Map;

import static com.opendev.external_services.helpers.JsonHelpers.buildJsonFromMap;
import static com.opendev.external_services.helpers.StringHelpers.borrarSaltosLineaYTabulaciones;

@Slf4j
public class Validators {

    public static void validateExpiresDateJWT(String jwtString) {
        HttpHeaders defaultsHeaders = new HttpHeaders();
        defaultsHeaders.add("Content-Type", "application/json");
        JsonNode msg = (buildJsonFromMap(Map.of("message", "token unauthorized")));
        try{
            DecodedJWT decodedJWT = JWT.decode(jwtString);
            Date expiresAt = decodedJWT.getExpiresAt();
            if (expiresAt.before(new Date()))
                throw new ExternalApiException(HttpStatus.UNAUTHORIZED, defaultsHeaders, borrarSaltosLineaYTabulaciones(msg.toPrettyString()));
            log.info("El token no ha expirado. Continuando con la peticion");
        } catch (JWTDecodeException e){
            msg = (buildJsonFromMap(Map.of("error", e.getMessage())));
            throw new ExternalApiException(HttpStatus.UNAUTHORIZED, defaultsHeaders, borrarSaltosLineaYTabulaciones(msg.toPrettyString()));
        }
    }
}
