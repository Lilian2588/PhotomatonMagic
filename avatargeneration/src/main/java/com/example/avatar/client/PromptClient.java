package com.example.avatar.client;

import com.example.avatar.DTO.PromptResponse; // Import du nouveau DTO
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class PromptClient {

    private static final Logger logger = LoggerFactory.getLogger(PromptClient.class);

    private final RestTemplate restTemplate;

    @Value("${ms.prompt.url}")
    private String promptMsUrl;

    public PromptClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Appelle le MS-Prompt pour obtenir une configuration de prompt détaillée.
     */
    public PromptResponse formatPrompt(String userText, String emotion,
                                       Map<String, Object>  faceAnalysis,
                                       String requestId) {

        logger.info("[PromptClient] Demande de formatage de prompt pour requestId: {}", requestId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Corps de la requête envoyé au MS-Prompt
        Map<String, Object> body = Map.of(
                "userText", userText,
                "emotion", emotion,
                "faceAnalysis", faceAnalysis,
                "requestId", requestId
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            // Changement important : on attend un PromptResponse au lieu d'un Map
            return restTemplate.postForObject(
                    promptMsUrl,
                    entity,
                    PromptResponse.class
            );
        } catch (Exception e) {
            logger.error("[PromptClient] Erreur lors de l'appel au MS-Prompt: {}", e.getMessage());
            // Retourne une config par défaut ou propage l'exception selon ta stratégie
            throw e;
        }
    }
}