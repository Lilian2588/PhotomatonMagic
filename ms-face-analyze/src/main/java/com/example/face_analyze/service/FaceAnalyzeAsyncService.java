package com.example.face_analyze.service;

import com.example.face_analyze.config.ActiveMQConfig;
import com.example.face_analyze.DTO.FaceAnalyzeRequestDTO;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Service
public class FaceAnalyzeAsyncService {

    private final JmsTemplate jmsTemplate;

    public FaceAnalyzeAsyncService(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    public void enqueueRequest(String requestId) {

        FaceAnalyzeRequestDTO request =
                new FaceAnalyzeRequestDTO(requestId);

        System.out.println("[AsyncService] Envoi dans la queue | requestId=" + requestId);

        jmsTemplate.convertAndSend(
                ActiveMQConfig.FACE_REQUEST_QUEUE,
                request
        );
    }
}
