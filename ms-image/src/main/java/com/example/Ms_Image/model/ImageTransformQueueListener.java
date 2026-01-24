package com.example.Ms_Image.model;

import com.example.Ms_Image.DTO.TransformRequest;
import com.example.Ms_Image.config.ActiveMQConfig;
import com.example.Ms_Image.service.ImageTransformService;
import com.example.Ms_Image.service.ImageResultPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class ImageTransformQueueListener {
    private static final Logger log = LoggerFactory.getLogger(ImageTransformQueueListener.class);

    private final ImageTransformService syncService;
    private final ImageResultPublisher resultPublisher;

    public ImageTransformQueueListener(ImageTransformService syncService, ImageResultPublisher resultPublisher) {
        this.syncService = syncService;
        this.resultPublisher = resultPublisher;
    }

    @JmsListener(destination = ActiveMQConfig.IMAGE_TRANSFORM_QUEUE)
    public void onMessage(TransformRequest request) {
        log.info("🚀 Traitement lourd démarré pour: {}", request.getRequestId());
        try {
            // 1. Exécution des 4 étapes Fooocus (Cache -> Masques -> Inpaint -> FaceSwap -> BG -> Upscale)
            String finalResult = syncService.transform(request);

            // 2. Succès : Notification du Monolithe sur la queue "AvatarResultQueue"
            resultPublisher.publishToMonolith(
                    request.getRequestId(),
                    finalResult,
                    "SUCCESS",
                    "Génération 4-étapes terminée"
            );

            log.info("✅ Succès et notification envoyée pour: {}", request.getRequestId());

        } catch (Exception e) {
            log.error("❌ Échec pour {}: {}", request.getRequestId(), e.getMessage());

            // 3. Échec : Notification du Monolithe pour qu'il ne reste pas bloqué
            resultPublisher.publishToMonolith(
                    request.getRequestId(),
                    null,
                    "FAILURE",
                    "Erreur interne: " + e.getMessage()
            );
        }
    }
}