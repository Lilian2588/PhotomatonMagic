package com.example.monolithic.client;

import com.example.monolithic.DTO.AvatarRequestDTO;
import com.example.monolithic.DTO.MediaCacheRequestDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class CacheClient {

    private static final Logger log = LoggerFactory.getLogger(CacheClient.class);

    private final RestTemplate restTemplate;

    // MODIFICATION : Injection de l'URL
    @Value("${ms.cache.url:http://localhost:8083/cache/media/upload}")
    private String cacheMsUrl;

    public CacheClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void uploadFromAvatarRequest(String requestId, AvatarRequestDTO request) {

        MediaCacheRequestDTO cacheRequest = new MediaCacheRequestDTO(
                requestId,
                "avatar.png",
                request.getImageBase64(),
                request.getAudioBase64()
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<MediaCacheRequestDTO> entity = new HttpEntity<>(cacheRequest, headers);

        log.info("[CacheClient] Upload vers : {} (requestId={})", cacheMsUrl, requestId);

        // On utilise la variable 'cacheMsUrl'
        restTemplate.postForObject(cacheMsUrl, entity, String.class);
    }
}