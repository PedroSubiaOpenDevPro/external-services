package com.opendev.external_services.helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Map;
import java.util.List;

public class JsonHelpers {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    public static String extractValue(String jsonAsString, String fieldName) {
        try {
            JsonNode jsonNode = objectMapper.readTree(jsonAsString);
            return jsonNode.get(fieldName).asText();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static ObjectNode buildJsonFromMap(Map<String, Object> input) {
        ObjectNode jsonNode = objectMapper.createObjectNode();
        for (Map.Entry<String, Object> entry : input.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof String) {
                jsonNode.put(key, (String) value);
            } else if (value instanceof Map) {
                jsonNode.set(key, buildJsonFromMap((Map<String, Object>) value));
            } else if (value instanceof List) {
                ArrayNode arrayNode = objectMapper.createArrayNode();
                for (Object item : (List<?>) value) {
                    if (item instanceof Map) {
                        arrayNode.add(buildJsonFromMap((Map<String, Object>) item));
                    } else if (item instanceof String) {
                        arrayNode.add((String) item);
                    } else {
                        // Para otros tipos: números, booleanos, etc.
                        JsonNode node = objectMapper.valueToTree(item);
                        arrayNode.add(node);
                    }
                }
                jsonNode.set(key, arrayNode);
            } else {
                // Para otros tipos primitivos o null
                JsonNode node = objectMapper.valueToTree(value);
                jsonNode.set(key, node);
            }
        }
        return jsonNode;
    }

}
