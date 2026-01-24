package com.example.monolithic.service;

import com.example.monolithic.model.Notification;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Service
public class SseService {

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private final ObjectMapper objectMapper;

    public SseService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public SseEmitter createEmitter() {
        SseEmitter emitter = new SseEmitter(300000L);
        emitters.add(emitter);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(e -> emitters.remove(emitter));

        log.info(" [SSE] Nouveau client connecté. Total: {}", emitters.size());

        return emitter;
    }

    public void broadcastNotification(Notification notification) {
        log.info("Broadcasting notification: {}", notification);
        List<SseEmitter> deadEmitters = new CopyOnWriteArrayList<>();

        emitters.forEach(emitter -> {
            try {
                String jsonNotification = objectMapper.writeValueAsString(notification);

                emitter.send(SseEmitter.event()
                        .data(jsonNotification));

            } catch (IOException e) {
                deadEmitters.add(emitter);
            }
        });

        if (emitters.isEmpty()) {
            log.warn("⚠️ [SSE] Aucun client connecté ! Le frontend a dû se déconnecter.");
        } else {
            log.info("✅ [SSE] Envoyé à {} client(s).", emitters.size());
        }

        deadEmitters.forEach(emitters::remove);
    }

    // --- LE HEARTBEAT (NOUVEAU) ---
    // S'exécute toutes les 15000 ms (15 secondes)
    @Scheduled(fixedRate = 8000)
    public void sendHeartbeat() {
        if (emitters.isEmpty()) return;

        List<SseEmitter> deadEmitters = new CopyOnWriteArrayList<>();

        emitters.forEach(emitter -> {
            try {
                // On envoie un événement nommé "ping" (le front l'ignorera ou le loguera)
                emitter.send(SseEmitter.event().name("ping").data("keep-alive"));
            } catch (IOException e) {
                deadEmitters.add(emitter);
            }
        });

        if (!deadEmitters.isEmpty()) {
            emitters.removeAll(deadEmitters);
            log.info(" [SSE-Heartbeat] Nettoyage de {} clients déconnectés.", deadEmitters.size());
        }
    }

    public int getConnectedClientsCount() {
        return emitters.size();
    }
}
