package com.example.monolithic.controller;

import com.example.monolithic.client.AvatarClient;
import com.example.monolithic.client.CacheClient;
import com.example.monolithic.DTO.AvatarRequestDTO;
import com.example.monolithic.model.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@CrossOrigin
@RestController
@RequestMapping("/avatar")
public class AvatarRestController {

    private static final Logger log = LoggerFactory.getLogger(AvatarRestController.class);

    private final AvatarClient avatarClient;
    private final CacheClient cacheClient;

    @Autowired
    private JmsTemplate jmsTemplate;

    @Value("${activemq.queue.name}")
    private String notificationQueue;

    public AvatarRestController(AvatarClient avatarClient, CacheClient cacheClient) {
        this.avatarClient = avatarClient;
        this.cacheClient = cacheClient;
    }

    @PostMapping(value = "/process", consumes = "application/json")
    public ResponseEntity<String> processAvatar(@RequestBody AvatarRequestDTO request) {

        log.info("[Monolithe] 1. Requête reçue. Permission : {}", request.isUserPermission());

        // 1. Validation
        if (request.getAudioBase64() == null || request.getImageBase64() == null) {
            log.warn("[Monolithe] Données manquantes");
            return ResponseEntity.badRequest().body("Erreur : Audio ou image manquant");
        }

        // 2. Génération ID
        String requestId = UUID.randomUUID().toString();
        log.info("[Monolithe] 2. ID généré : {}", requestId);

        // 3. Appel Cache (Avec gestion d'erreur)
        try {
            log.info("[Monolithe] 3. Envoi au Cache...");
            cacheClient.uploadFromAvatarRequest(requestId, request);
            log.info("[Monolithe] ✅ Cache OK");
        } catch (Exception e) {
            log.error("Erreur connexion MS Cache", e);
            return ResponseEntity.status(503).body("Erreur: Service de stockage indisponible.");
        }

        // Notification: Image en cache
        sendNotification(requestId, "IMAGE_CACHED", "Photo sauvegardée, analyse en cours...");

        // 4. Appel Avatar (Avec gestion d'erreur)
        String responseMS;
        try {
            log.info("[Monolithe] 4. Envoi au MS Avatar...");
            responseMS = avatarClient.processAvatar(requestId, request.isUserPermission());
            log.info("[Monolithe] ✅ Avatar MS OK");
        } catch (Exception e) {
            log.error("Erreur connexion MS Avatar", e);
            return ResponseEntity.status(503).body("Erreur: Service de génération indisponible.");
        }

        // 5. Succès
        return ResponseEntity.ok(
                "Traitement démarré avec succès | requestId=" + requestId +
                        " | Réponse MS: " + responseMS
        );
    }

    private void sendNotification(String requestId, String status, String message) {
        try {
            Notification notification = new Notification(requestId, null, status, message);
            jmsTemplate.convertAndSend(notificationQueue, notification);
        } catch (Exception e) {
            log.error("Erreur envoi notification JMS", e);
        }
    }
}