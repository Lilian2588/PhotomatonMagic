package com.example.Ms_Image.service;

import com.example.Ms_Image.DTO.TransformRequest;
import com.example.Ms_Image.config.ActiveMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Service
public class ImageTransformAsyncService {
    private static final Logger logger = LoggerFactory.getLogger(ImageTransformAsyncService.class);

    private final JmsTemplate jmsTemplate;

    public ImageTransformAsyncService(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    public void enqueueRequest(TransformRequest request) {
        logger.info("[Async] Mise en file interne pour le requestId: {}", request.getRequestId());

        // Envoie l'objet dans la queue définie dans ActiveMQConfig
        jmsTemplate.convertAndSend(ActiveMQConfig.IMAGE_TRANSFORM_QUEUE, request);
    }
}