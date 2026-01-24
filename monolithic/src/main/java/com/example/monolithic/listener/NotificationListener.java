package com.example.monolithic.listener;

import com.example.monolithic.model.Notification;
import com.example.monolithic.service.SseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotificationListener {

    private final SseService sseService;
    private final ObjectMapper objectMapper;

    public NotificationListener(SseService sseService, ObjectMapper objectMapper) {
        this.sseService = sseService;
        this.objectMapper = objectMapper;
    }

    @JmsListener(destination = "${activemq.queue.name}")
    public void receiveNotification(Message message) {
        try {
            if (message instanceof TextMessage) {
                String jsonMessage = ((TextMessage) message).getText();
                log.info("Message JSON brut reçu via JMS: {}", jsonMessage);
                // Conversion manuelle du JSON en objet Notification
                Notification notification = objectMapper.readValue(jsonMessage, Notification.class);
                
                sseService.broadcastNotification(notification);
            } else {
                log.warn("Message reçu n'est pas un TextMessage: {}", message);
            }
        } catch (Exception e) {
            log.error("Erreur lors de la désérialisation ou du traitement de la notification", e);
        }
    }
}
