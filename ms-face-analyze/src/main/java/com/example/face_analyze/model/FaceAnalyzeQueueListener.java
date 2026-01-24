package com.example.face_analyze.model;

import com.example.face_analyze.DTO.FaceAnalyzeRequestDTO;
import com.example.face_analyze.config.ActiveMQConfig;
import com.example.face_analyze.service.FaceAnalyzeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class FaceAnalyzeQueueListener {

    private static final Logger logger = LoggerFactory.getLogger(FaceAnalyzeQueueListener.class);

    private final FaceAnalyzeService faceService;

    public FaceAnalyzeQueueListener(FaceAnalyzeService faceService) {
        this.faceService = faceService;
    }

    @JmsListener(destination = ActiveMQConfig.FACE_REQUEST_QUEUE)
    public void listen(FaceAnalyzeRequestDTO request) {
        String requestId = request.getRequestId();
        logger.info("[FaceQueueListener] Message reçu | requestId={}", requestId);

        // On appelle le service.
        // Comme il est void, on ne peut pas récupérer de résultat ici.
        // C'est le service lui-même qui publiera le résultat dans FaceResultQueue.
        faceService.analyzeFace(requestId);

        logger.info("[FaceQueueListener] Demande d'analyse traitée pour requestId={}. Le résultat sera envoyé via JMS.", requestId);
    }
}