package com.example.monolithic.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
public class AvatarClient {

    private static final Logger log = LoggerFactory.getLogger(AvatarClient.class);

    private final RestTemplate restTemplate;

    // MODIFICATION : On injecte l'URL depuis la configuration (application.properties ou Docker)
    // La valeur par défaut (après les :) reste localhost pour tes tests sans Docker
    @Value("${ms.avatar.url:http://localhost:8081/avatar/process}")
    private String avatarMsUrl;

    public AvatarClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Appel MS Avatar avec requestId ET permission.
     */
    public String processAvatar(String requestId, boolean userPermission) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Construction du payload JSON
        Map<String, Object> body = new HashMap<>();
        body.put("requestId", requestId);
        body.put("userPermission", userPermission);

        HttpEntity<Map<String, Object>> requestEntity =
                new HttpEntity<>(body, headers);

        log.info("[AvatarClient] Appel vers : {} (requestId={})", avatarMsUrl, requestId);

        // Pas de try-catch ici : on laisse l'exception remonter si ça échoue
        // On utilise la variable 'avatarMsUrl' au lieu de la constante
        return restTemplate.postForObject(
                avatarMsUrl,
                requestEntity,
                String.class
        );
    }
}