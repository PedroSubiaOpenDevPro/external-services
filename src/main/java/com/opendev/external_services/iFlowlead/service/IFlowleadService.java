package com.opendev.external_services.iFlowlead.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.opendev.external_services.distribuidor.services.DistribuidorService;
import com.opendev.external_services.helpers.WebClientHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import static com.opendev.external_services.helpers.JsonHelpers.extractValue;
import static com.opendev.external_services.iFlowlead.IFlowleadConstants.CREDENCIALES_IFLOWLEAD;
import static com.opendev.external_services.iFlowlead.IFlowleadConstants.INTEGRACION_IFLOWLEAD_SNOVIO;

@Service
public class IFlowleadService {

    @Autowired
    DistribuidorService distribuidorService;

    public Mono<ResponseEntity<String>> executeIntegration(JsonNode body){
        JsonNode newBody = mapBody(body);
        return distribuidorService.getLogin(CREDENCIALES_IFLOWLEAD).flatMap(responseDistribuidor -> {
            String tokenDistribuidor = extractValue(responseDistribuidor.getBody(), "token");
            return distribuidorService.callIntegracionRestRR(INTEGRACION_IFLOWLEAD_SNOVIO, tokenDistribuidor, newBody);
        });
    }

    private JsonNode mapBody(JsonNode body){
        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode prospect = body.get("data").get("prospect");
        JsonNode campaign = body.get("data").get("campaign");
        JsonNode recipient = prospect.get("recipient");

        ObjectNode result = objectMapper.createObjectNode();
        result.put("id", prospect.get("id").asText());
        result.put("firstName", prospect.get("first_name").asText());
        result.put("lastName", prospect.get("last_name").asText());
        result.put("email", recipient.get("email").asText());
        result.put("campaignId", campaign.get("id").asText());
        result.put("campaignName", campaign.get("name").asText());
        result.put("lastEvent", body.get("event").asText());

        return result; // Devuelve como JsonNode
    }
}
