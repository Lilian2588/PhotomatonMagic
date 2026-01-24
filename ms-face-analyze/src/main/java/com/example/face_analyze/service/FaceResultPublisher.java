package com.example.face_analyze.service;

import com.example.face_analyze.DTO.FaceAnalyzeResultDTO;
import com.example.face_analyze.config.ActiveMQConfig;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Service
public class FaceResultPublisher {
    private final JmsTemplate jmsTemplate;

    public FaceResultPublisher(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    public void publish(FaceAnalyzeResultDTO result) {
        System.out.println("[Face-Analyze] Envoi du résultat vers le broker : " + result.getRequestId());
        jmsTemplate.convertAndSend("FaceResultQueue", result);
    }
}