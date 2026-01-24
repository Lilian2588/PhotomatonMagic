package com.example.prompt.controller;

import com.example.prompt.DTO.PromptRequest;
import com.example.prompt.DTO.PromptResponse;
import com.example.prompt.service.PromptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/prompt")
public class PromptController {

    @Autowired
    private PromptService promptService;

    @Autowired
    private JmsTemplate jmsTemplate;

    @Value("${activemq.queue.name}")
    private String notificationQueue;

    private static final Logger logger = LoggerFactory.getLogger(PromptController.class);

    @PostMapping("/format")
    public PromptResponse formatPrompt(@RequestBody PromptRequest request) {
        logger.info("Processing prompt for RequestID: {}", request.getRequestId());

        // Appelle le service pour obtenir les 3 prompts spécialisés
        PromptResponse response = promptService.generateFullPromptConfig(
                request.getUserText(),
                request.getEmotion(),
                request.getFaceAnalysis()
        );

        // Envoi notification SSE
        sendNotification(request.getRequestId(), "PROMPT_CREATED", "Prompts générés avec succès");

        return response;
    }

    private void sendNotification(String requestId, String status, String message) {
        try {
            String json = String.format(
                    "{\"requestId\":\"%s\", \"imageUrl\":null, \"status\":\"%s\", \"message\":\"%s\"}",
                    requestId, status, message
            );
            jmsTemplate.convertAndSend(notificationQueue, json);
        } catch (Exception e) {
            logger.error("Erreur notification JMS: {}", e.getMessage());
        }
    }
}