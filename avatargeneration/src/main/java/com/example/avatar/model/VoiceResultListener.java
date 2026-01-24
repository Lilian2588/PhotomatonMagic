package com.example.avatar.model;

import com.example.avatar.DTO.VoiceAnalyzeResultDTO;
import com.example.avatar.service.AvatarOrchestrator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class VoiceResultListener {

    private static final Logger logger = LoggerFactory.getLogger(VoiceResultListener.class);

    private final AvatarOrchestrator orchestrator;
    private final ObjectMapper objectMapper; // L'outil de conversion JSON

    // Injection de dépendances via le constructeur
    public VoiceResultListener(AvatarOrchestrator orchestrator, ObjectMapper objectMapper) {
        this.orchestrator = orchestrator;
        this.objectMapper = objectMapper;
    }

    // On reçoit une String (le JSON brut)
    @RabbitListener(queues = "audio_response_queue")
    public void handleVoiceResult(String jsonMessage) {
        try {
            logger.info("async JSON Brut reçu : {}", jsonMessage);

            // On fait la conversion avec les champs du DTO
            VoiceAnalyzeResultDTO voiceData = objectMapper.readValue(jsonMessage, VoiceAnalyzeResultDTO.class);

            // On passe le relais à l'orchestrateur
            orchestrator.continueWorkflowAfterVoiceAnalyze(voiceData);

        } catch (Exception e) {
            logger.error("Erreur critique : Impossible de lire le JSON reçu par le traitement python", e);
        }
    }
}