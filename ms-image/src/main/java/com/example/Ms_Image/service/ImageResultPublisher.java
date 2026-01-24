package com.example.Ms_Image.service;

import com.example.Ms_Image.DTO.AvatarGenerationResultDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Service
public class ImageResultPublisher {
    private static final Logger logger = LoggerFactory.getLogger(ImageResultPublisher.class);
    private final JmsTemplate jmsTemplate;

    public ImageResultPublisher(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    public void publishToMonolith(String requestId, String imageBase64, String status, String message) {
        // Création du DTO (sans Lombok, via constructeur complet)
        AvatarGenerationResultDTO result = new AvatarGenerationResultDTO(requestId, imageBase64, status, message);

        // Envoi vers la queue "AvatarResultQueue"
        jmsTemplate.convertAndSend("AvatarResultQueue", result);

        logger.info("[JMS] Message envoyé au Monolithe pour requestId : {} | Statut : {}", requestId, status);
    }
}