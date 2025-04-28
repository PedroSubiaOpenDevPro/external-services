package com.opendev.external_services.helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FuntionsHandlerJsonHelper {

    public static String extractValue(String jsonAsString, String fieldName) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(jsonAsString);
        return jsonNode.get(fieldName).asText();
    }

}
