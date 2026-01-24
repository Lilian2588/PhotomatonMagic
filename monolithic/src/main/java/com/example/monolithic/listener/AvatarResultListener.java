package com.example.monolithic.listener;

import com.example.monolithic.DTO.AvatarGenerationResultDTO;
import com.example.monolithic.model.Notification;
import com.example.monolithic.model.TransformedImage;
import com.example.monolithic.repository.TransformedImageRepository;
import com.example.monolithic.service.SseService;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

@Component
public class AvatarResultListener {
    private static final Logger log = LoggerFactory.getLogger(AvatarResultListener.class);

    @Autowired
    private TransformedImageRepository repository;

    @Autowired
    private SseService sseService;

    // Répertoire de stockage : user.home/myapp/images/
    private final String UPLOAD_DIR = System.getProperty("user.home") + File.separator + "myapp" + File.separator + "images" + File.separator;

    @JmsListener(destination = "AvatarResultQueue")
    public void onAvatarResult(AvatarGenerationResultDTO result) {
        log.info("[Monolithe] Résultat reçu pour requestId = {}", result.getRequestId());
        log.info("[Monolithe] Status = {}", result.getStatus());
        log.info("Path pour user.home {}",System.getProperty("user.home"));

        if ("SUCCESS".equals(result.getStatus()) && result.getBase64Image() != null) {
            try {
                // --- PARTIE 1 : DÉCODAGE ---
                String base64Data = result.getBase64Image();
                if (base64Data.contains(",")) {
                    base64Data = base64Data.split(",")[1];
                }
                base64Data = base64Data.replaceAll("\\s", "");
                byte[] imageBytes = Base64.getMimeDecoder().decode(base64Data);

                // --- PARTIE 2 : STOCKAGE PHYSIQUE (DISQUE) ---
                File directory = new File(UPLOAD_DIR);
                if (!directory.exists()) {
                    directory.mkdirs(); // Crée les dossiers s'ils n'existent pas
                }

                String fileName = result.getRequestId() + ".png";
                Path path = Paths.get(UPLOAD_DIR + fileName);
                Files.write(path, imageBytes);
                log.info("[Monolithe] Fichier écrit sur le disque : {}", path);

                // --- PARTIE 3 : PERSISTANCE (H2) ---
                TransformedImage img = new TransformedImage();
                img.setRequestId(result.getRequestId());
                img.setFilePath(path.toString()); 
                img.setContentType("image/png");

                repository.save(img);
                log.info("[Monolithe] ✅ Référence sauvegardée en BDD pour le requestId : {}", result.getRequestId());

                // --- PARTIE 4 : NOTIFICATION SSE ---
                Notification notif = new Notification();
                notif.setRequestId(result.getRequestId());

                notif.setImageUrl(result.getBase64Image());
                notif.setStatus("COMPLETED");
                notif.setMessage("Avatar généré avec succès !");

                sseService.broadcastNotification(notif);
                log.info("[Monolithe] Notification envoyée au Frontend via SSE.");

            } catch (Exception e) {
                log.error("[Monolithe] ❌ Erreur technique pour requestId : {}. Détail : {}", result.getRequestId(), e.getMessage());

                // Optionnel : Notifier le front de l'erreur
                Notification errorNotif = new Notification();
                errorNotif.setRequestId(result.getRequestId());
                errorNotif.setStatus("FAILED");
                errorNotif.setMessage("Erreur lors de l'enregistrement de l'image.");
                sseService.broadcastNotification(errorNotif);
            }
        }
    }
}