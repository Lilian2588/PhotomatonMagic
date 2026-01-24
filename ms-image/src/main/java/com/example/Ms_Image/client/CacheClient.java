package com.example.Ms_Image.client;

import com.example.Ms_Image.DTO.MediaCacheResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;

@Component
public class CacheClient {

    private static final Logger log = LoggerFactory.getLogger(CacheClient.class);

    private final RestTemplate restTemplate;

    @Value("${ms.cache.url}")
    private String cacheMsGetUrl;

    public CacheClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Récupère l'image en Base64 depuis le cache pour un requestId donné
     */
    public String getImageBase64(String requestId) {
        try {
            log.info("[CacheClient] Récupération média complet pour requestId={}", requestId);

            ResponseEntity<MediaCacheResponseDTO> response = restTemplate.getForEntity(
                    cacheMsGetUrl,
                    MediaCacheResponseDTO.class,
                    requestId
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                log.info("[CacheClient] Média récupéré avec succès pour requestId={}", requestId);
                return response.getBody().getImageB64(); // On ne retourne que l'image Base64
            } else {
                log.warn("[CacheClient] Statut inattendu {} pour requestId={}", response.getStatusCode(), requestId);
                return null;
            }

        } catch (Exception e) {
            log.error("[CacheClient] Erreur lors de la récupération de l'image pour requestId={}", requestId, e);
            return null;
        }
    }
}
