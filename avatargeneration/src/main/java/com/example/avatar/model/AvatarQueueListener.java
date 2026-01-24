package com.example.avatar.model;

import com.example.avatar.DTO.AvatarGenerationRequestDTO;
import com.example.avatar.service.AvatarOrchestrator;
import com.example.avatar.config.ActiveMQConfig;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class AvatarQueueListener {

    private final AvatarOrchestrator orchestrator;

    public AvatarQueueListener(AvatarOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    @JmsListener(destination = ActiveMQConfig.AVATAR_REQUEST_QUEUE)
    public void onMessage(AvatarGenerationRequestDTO request) {
        // On passe par l'orchestrateur pour lancer TOUT (Face + Voice)
        orchestrator.startParallelOrchestration(request.getRequestId());
    }
}
