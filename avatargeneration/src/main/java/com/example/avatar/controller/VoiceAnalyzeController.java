package com.example.avatar.controller;

import com.example.avatar.client.VoiceClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analyze")
public class VoiceAnalyzeController {

    private final VoiceClient voiceClient;

    public VoiceAnalyzeController(VoiceClient voiceClient) {
        this.voiceClient = voiceClient;
    }

    @PostMapping("/voice/{requestId}")
    public ResponseEntity<String> startVoiceAnalysis(@PathVariable String requestId) {
        // Méthode de VoiceClient qui envoie la demande la request_queue
        voiceClient.sendAudioRequest(requestId);
        return ResponseEntity.ok("Demande reçue ! Traitement en cours pour l'ID : " + requestId);
    }
}