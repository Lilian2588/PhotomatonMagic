package com.example.avatar.service;

import com.example.avatar.config.ActiveMQConfig;
import com.example.avatar.DTO.AvatarGenerationRequestDTO;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Service
public class AvatarGenerationAsyncService {

    private final JmsTemplate jmsTemplate;

    public AvatarGenerationAsyncService(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    public void enqueueRequest(String requestId) {

        AvatarGenerationRequestDTO request =
                new AvatarGenerationRequestDTO(requestId);

        System.out.println("[AsyncService] Envoi dans la queue | requestId=" + requestId);

        jmsTemplate.convertAndSend(
                ActiveMQConfig.AVATAR_REQUEST_QUEUE,
                request
        );
    }
}
