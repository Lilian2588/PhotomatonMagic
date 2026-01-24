package com.example.avatar.service;

import com.example.avatar.DTO.NotificationDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationPublisher {

    private static final Logger logger = LoggerFactory.getLogger(NotificationPublisher.class);
    private final JmsTemplate jmsTemplate;
    // Nom de la queue doit correspondre à celle écoutée par le Monolithe
    private static final String NOTIFICATION_QUEUE = "Notification-queue";

    public NotificationPublisher(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    public void sendNotification(String requestId, String status) {
        try {
            NotificationDTO notification = new NotificationDTO(requestId, status, "AVATAR_SERVICE");
            jmsTemplate.convertAndSend(NOTIFICATION_QUEUE, notification);
            logger.info("[NotificationPublisher] Notification envoyée : {} - {}", requestId, status);
        } catch (Exception e) {
            logger.error("[NotificationPublisher] Erreur lors de l'envoi de la notification", e);
        }
    }
}
