package com.example.avatar.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class FaceAnalyzeClient {

    private final RestTemplate restTemplate;

    @Value("${ms.face.url}")
    private String faceMsUrl;

    public FaceAnalyzeClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void triggerFaceAnalyze(String requestId) {
        restTemplate.postForEntity(
                faceMsUrl,
                new FaceRequestDTO(requestId),
                Void.class
        );
    }

    static class FaceRequestDTO {
        public String requestId;
        public FaceRequestDTO(String requestId) {
            this.requestId = requestId;
        }
    }
}
