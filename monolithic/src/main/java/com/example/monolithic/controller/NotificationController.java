package com.example.monolithic.controller;

import com.example.monolithic.service.SseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller REST pour gérer les connexions SSE
 */
@Slf4j
@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    private final SseService sseService;

    public NotificationController(SseService sseService) {
        this.sseService = sseService;
    }

    /**
     * Endpoint SSE pour que les clients front-end se connectent
     * GET /api/notifications/stream
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> streamNotifications() { // <--- Changement de type de retour
        SseEmitter emitter = sseService.createEmitter();

        // 👇 LA CORRECTION MAGIQUE 👇
        return ResponseEntity.ok()
                .header("X-Accel-Buffering", "no") // Vital pour Docker/Nginx !
                .header("Cache-Control", "no-cache") // Vital pour le navigateur
                .header("Connection", "keep-alive")
                .body(emitter);
    }

    /**
     * Endpoint pour vérifier le statut du service
     * GET /api/notifications/status
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("service", "monolithic-notification");
        status.put("status", "UP");
        status.put("connectedClients", sseService.getConnectedClientsCount());

        return ResponseEntity.ok(status);
    }
}
