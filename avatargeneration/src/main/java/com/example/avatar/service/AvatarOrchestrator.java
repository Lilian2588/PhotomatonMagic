package com.example.avatar.service;

import com.example.avatar.DTO.*;
import com.example.avatar.client.FaceAnalyzeClient;
import com.example.avatar.client.ImageClient;
import com.example.avatar.client.PromptClient;
import com.example.avatar.client.VoiceClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import jakarta.jms.TextMessage;

import java.time.Duration;
import java.util.Map;
import java.util.HashMap;

@Service
public class AvatarOrchestrator {
    private static final Logger logger = LoggerFactory.getLogger(AvatarOrchestrator.class);

    // Clients
    private final PromptClient promptClient;
    private final VoiceClient voiceClient;
    private final ImageClient imageClient;
    private final FaceAnalyzeClient faceAnalyzeClient;

    // Gestion du cache Redis
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String REDIS_PREFIX_FACE = "sync:face:";
    private static final String REDIS_PREFIX_VOICE = "sync:voice:";

    @Autowired
    private JmsTemplate jmsTemplate;

    // Queue pour les notifications frontend (doit correspondre au Monolithe)
    private static final String NOTIFICATION_QUEUE = "Notification-queue";
    private final ObjectMapper objectMapper;

    public AvatarOrchestrator(VoiceClient voiceClient,
                              PromptClient promptClient,
                              ImageClient imageClient,
                              FaceAnalyzeClient faceAnalyzeClient,
                              RedisTemplate<String, Object> redisTemplate,
                              ObjectMapper objectMapper) {
        this.voiceClient = voiceClient;
        this.promptClient = promptClient;
        this.imageClient = imageClient;
        this.faceAnalyzeClient = faceAnalyzeClient;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * POINT D'ENTRÉE : Déclenche les deux analyses en parallèle.
     * Appelé par ton controller initial ou l'arrivée d'une nouvelle requête.
     */
    public void startParallelOrchestration(String requestId) {
        logger.info("[Orchestrator] 🚀cache Lancement du workflow parallèle pour : {}", requestId);

        sendNotification(requestId, "IMAGE_CACHED", "Mise en cache...");


        try {
            // 1. Lancement Analyse Faciale (Asynchrone)
            faceAnalyzeClient.triggerFaceAnalyze(requestId);

            // 2. Lancement Analyse Vocale (Asynchrone via RabbitMQ)
            voiceClient.sendAudioRequest(requestId);

            logger.info("[Orchestrator] Requêtes Face et Voice envoyées. En attente des réponses...");
        } catch (Exception e) {
            logger.error("[Orchestrator] Erreur au lancement du parallélisme", e);
        }
    }

    /**
     * CALLBACK 1 : Reçu quand MS-FACE a terminé (via ton Controller ou Listener Face)
     */
    public void continueWorkflowAfterFaceAnalyze(FaceAnalyzeResultDTO faceData) {
        String requestId = faceData.getRequestId();
        logger.info("[Orchestrator] 🧩 Partie FACE reçue pour {}", requestId);

        sendNotification(requestId, "FACE_ANALYZED", "Analyse du visage...");

        if (faceData.getAnalysis() == null) {
            logger.error("Analyse faciale nulle pour {}", requestId);
            return;
        }

        // Stockage dans Redis (expire après 30 min par sécurité)
        redisTemplate.opsForValue().set(REDIS_PREFIX_FACE + requestId, faceData.getAnalysis(), Duration.ofMinutes(30));

        // Vérification si l'autre partie est déjà là
        checkSynchronization(requestId);
    }

    /**
     * CALLBACK 2 : Reçu quand MS-VOICE a terminé (via VoiceResultListener)
     */
    public void continueWorkflowAfterVoiceAnalyze(VoiceAnalyzeResultDTO voiceData) {
        String requestId = voiceData.getRequestId();
        logger.info("[Orchestrator] 🧩 Partie VOICE reçue pour {}", requestId);

        sendNotification(requestId, "VOICE_ANALYZED", "Analyse de la voix...");

        // Stockage dans Redis
        redisTemplate.opsForValue().set(REDIS_PREFIX_VOICE + requestId, voiceData, Duration.ofMinutes(30));

        // Vérification si l'autre partie est déjà là
        checkSynchronization(requestId);
    }

    /**
     * LA SYNCHRONISATION (Rendez-vous)
     * Vérifie si les deux morceaux du puzzle sont présents dans Redis.
     */
    private void checkSynchronization(String requestId) {
        String faceKey = REDIS_PREFIX_FACE + requestId;
        String voiceKey = REDIS_PREFIX_VOICE + requestId;

        Map<String, Object> faceAnalysis = (Map<String, Object>) redisTemplate.opsForValue().get(faceKey);
        VoiceAnalyzeResultDTO voiceData = (VoiceAnalyzeResultDTO) redisTemplate.opsForValue().get(voiceKey);

        if (faceAnalysis != null && voiceData != null) {
            logger.info("[Orchestrator] 🎯 Synchro complète pour {}. Finalisation du workflow...", requestId);

            // Nettoyage immédiat du cache
            redisTemplate.delete(faceKey);
            redisTemplate.delete(voiceKey);

            // Lancement de l'étape finale
            finalizeWorkflow(requestId, faceAnalysis, voiceData);
        } else {
            logger.info("[Orchestrator] En attente de la partie manquante pour {}...", requestId);
        }
    }

    /**
     * ÉTAPE FINALE : Prompt -> Image
     */
    private void finalizeWorkflow(String requestId, Map<String, Object> faceAnalysis, VoiceAnalyzeResultDTO voiceData) {
        try {
            // 1. Génération des Prompts
            PromptResponse promptData = promptClient.formatPrompt(
                    voiceData.getTranscription(),
                    voiceData.getEmotion(),
                    faceAnalysis,
                    requestId
            );
            logger.info("[Orchestrator] 📝 Résultat Prompt Engine reçu : {}", promptData);

            sendNotification(requestId, "PROMPT_CREATED", "Construction du prompt...");



            // 2. Construction de la requête finale de transformation
            TransformRequest finalRequest = new TransformRequest();
            finalRequest.setRequestId(requestId);
            finalRequest.setAnalysisResult(faceAnalysis);
            finalRequest.setFacePrompt(promptData.getFacePrompt());
            finalRequest.setBodyPrompt(promptData.getBodyPrompt());
            finalRequest.setBackgroundPrompt(promptData.getBackgroundPrompt());
            finalRequest.setModelName(promptData.getModelName());

            TransformRequest.Config cfg = new TransformRequest.Config();
            cfg.setPreserveFace(promptData.isPreserveFace());
            cfg.setPreservePose(promptData.isPreservePose());
            finalRequest.setConfig(cfg);

            logger.info("[Orchestrator] 🔥 Envoi de la TransformRequest au MS-Image pour {}", requestId);
            logger.info("[Orchestrator] 📦 Détails de la requête MS-Image: Model={}, FacePrompt='{}', BodyPrompt='{}', BgPrompt='{}', PreserveFace={}, PreservePose={}",
                    finalRequest.getModelName(),
                    finalRequest.getFacePrompt(),
                    finalRequest.getBodyPrompt(),
                    finalRequest.getBackgroundPrompt(),
                    finalRequest.getConfig().isPreserveFace(),
                    finalRequest.getConfig().isPreservePose()
            );

            // Envoi notification "Génération Image Start"
            sendNotification(requestId, "IMAGE_GEN_START", "Génération de l'image en cours...");

            // 3. Appel au service de génération d'image
            imageClient.processTransform(finalRequest);

        } catch (Exception e) {
            logger.error("[Orchestrator] Erreur critique lors de la finalisation pour {}", requestId, e);
        }
    }

    private void sendNotification(String requestId, String status, String message) {
        try {
            // 1. On prépare la Map
            Map<String, Object> notifMap = new HashMap<>();
            notifMap.put("requestId", requestId);
            notifMap.put("imageUrl", null); // Toujours null au début
            notifMap.put("status", status);
            notifMap.put("message", message);

            // 2. On génère le JSON propre (String)
            String json = objectMapper.writeValueAsString(notifMap);

            // 3. CORRECTION ICI : On envoie un TextMessage BRUT pour éviter la double sérialisation
            jmsTemplate.send(NOTIFICATION_QUEUE, session -> {
                TextMessage textMessage = session.createTextMessage(json);
                return textMessage;
            });

            logger.info("Notification envoyée (Raw JMS): {}", json);
        } catch (Exception e) {
            logger.error("Erreur notification JMS: {}", e.getMessage());
        }
    }

    // Méthode de compatibilité pour l'ancien listener JMS (si encore utilisé)
    // Elle déclenche le mode parallèle
    public void orchestrate(String requestId, Map<String, Object> faceAnalysis) {
        // Si cette méthode est appelée, c'est que l'ancien flux Face -> Orchestrator est actif.
        // On considère que Face est fait. On lance Voice.
        logger.info("[Orchestrator] Legacy orchestrate call. Triggering Voice via RabbitMQ...");

        // On sauvegarde Face dans Redis
        String faceKey = REDIS_PREFIX_FACE + requestId;
        redisTemplate.opsForValue().set(faceKey, faceAnalysis, Duration.ofMinutes(30));

        // On lance Voice
        voiceClient.sendAudioRequest(requestId);
    }
}