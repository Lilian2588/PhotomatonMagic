package com.example.avatar.client;

import com.example.avatar.DTO.TransformRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@Service
public class ImageClient {
    private static final Logger logger = LoggerFactory.getLogger(ImageClient.class);
    private final RestTemplate restTemplate;

    @Value("${ms.image.url}")
    private String imageMsUrl;

    public ImageClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Map<String, String> processTransform(TransformRequest request) {
        logger.info("[ImageClient] Envoi requête MS-Image pour ID: {}", request.getRequestId());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<TransformRequest> entity = new HttpEntity<>(request, headers);

        try {
            return restTemplate.postForObject(imageMsUrl, entity, Map.class);
        } catch (Exception e) {
            logger.error("[ImageClient] Erreur appel MS-Image: {}", e.getMessage());
            throw e;
        }
    }
}