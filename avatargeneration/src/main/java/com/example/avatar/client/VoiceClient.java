package com.example.avatar.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class VoiceClient {

    private static final Logger logger = LoggerFactory.getLogger(VoiceClient.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    // Nom de la queue RabbitMQ (pourrait aussi être externalisé)
    private static final String QUEUE_NAME_REQUEST = "audio_request_queue";

    public void sendAudioRequest(String requestId) {
        // On envoie dans la queue de requête
        logger.info("Envoi asynchrone de l'analyse de la voix pour requestId={} vers RabbitMQ", requestId);
        rabbitTemplate.convertAndSend(QUEUE_NAME_REQUEST, requestId);
    }
}
