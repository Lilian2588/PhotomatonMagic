package com.example.face_analyze.service;

import com.example.face_analyze.client.CacheClient;
import com.example.face_analyze.DTO.FaceAnalyzeResultDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.jms.core.JmsTemplate;

import java.util.Base64;
import java.util.Map;

@Service
public class FaceAnalyzeService {

    private static final Logger logger = LoggerFactory.getLogger(FaceAnalyzeService.class);

    private final CacheClient cacheClient;
    private final RestTemplate restTemplate;
    private final JmsTemplate jmsTemplate;

    private static final String FACE_API_URL = "https://cedriccpe-face-analyze.hf.space/analyze_photo";
    @Value("${hf.token}")
    private static final String HF_TOKEN = System.getenv("HF_TOKEN");


    public FaceAnalyzeService(CacheClient cacheClient, RestTemplate restTemplate, JmsTemplate jmsTemplate) {
        this.cacheClient = cacheClient;
        this.restTemplate = restTemplate;
        this.jmsTemplate = jmsTemplate;
    }

    public void analyzeFace(String requestId) {
        logger.info("[FaceService] Début analyse pour requestId={}", requestId);

        String imageB64 = cacheClient.getImageBase64(requestId);
        if (imageB64 == null) {
            logger.error("[FaceService] ÉCHEC : Pas d'image trouvée en cache pour requestId={}", requestId);
            return;
        }

        byte[] imageBytes = Base64.getDecoder().decode(imageB64);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("photo", new ByteArrayResource(imageBytes) {
            @Override
            public String getFilename() {
                return "avatar.png";
            }
        });

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setBearerAuth(HF_TOKEN);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        try {
            logger.info("[FaceService] Appel de l'API Hugging Face...");
            ResponseEntity<Map> response = restTemplate.postForEntity(FACE_API_URL, requestEntity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> apiResult = response.getBody();

                // LOG DES RESULTATS BRUTS
                logger.info("[FaceService] SUCCÈS API Hugging Face pour requestId={}", requestId);
                logger.info("[FaceService] Contenu du résultat : {}", apiResult);

                FaceAnalyzeResultDTO resultDTO = new FaceAnalyzeResultDTO(requestId, apiResult);

                // ENVOI AVEC LE HEADER OBLIGATOIRE
                jmsTemplate.convertAndSend("FaceResultQueue", resultDTO, message -> {
                    message.setStringProperty("_type", "FaceResult");
                    return message;
                });

                logger.info("[FaceService] Résultat envoyé au broker (FaceResultQueue) pour {}", requestId);
            } else {
                logger.warn("[FaceService] API a répondu avec un code erreur : {}", response.getStatusCode());
            }
        } catch (Exception e) {
            logger.error("[FaceService] EXCEPTION lors de l'analyse pour requestId={}", requestId, e);
        }
    }
}