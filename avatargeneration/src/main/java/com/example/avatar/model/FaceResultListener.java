package com.example.avatar.model;

import com.example.avatar.DTO.FaceAnalyzeResultDTO;
import com.example.avatar.service.AvatarOrchestrator;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class FaceResultListener {

    private final AvatarOrchestrator orchestrator;

    public FaceResultListener(AvatarOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    @JmsListener(destination = "FaceResultQueue")
    public void onReceiveFaceResult(FaceAnalyzeResultDTO result) {
        System.out.println("[Avatar MS] Message reçu ! Analyse OK pour " + result.getRequestId());
        orchestrator.continueWorkflowAfterFaceAnalyze(result);
    }
}