package com.example.prompt.service;

import com.example.prompt.DTO.PromptResponse;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.Normalizer;
import java.time.Duration;
import java.util.*;

@Service
public class PromptService {

    private static final Logger logger = LoggerFactory.getLogger(PromptService.class);

    @Value("${libretranslate.url:http://localhost:5500}")
    private String translationServiceUrl;

    // Configuration des modèles par style
    private static final Map<String, String> STYLE_TO_MODEL = Map.of(
            "anime", "novaAnimeXL_xlV10.safetensors",
            "manga", "novaAnimeXL_xlV10.safetensors",
            "cartoon", "arthemyToons_v40.safetensors",
            "toon", "arthemyToons_v40.safetensors",
            "pixar", "arthemyToons_v40.safetensors",
            "disney", "arthemyToons_v40.safetensors"
    );

    // Modèles par défaut (si aucun style n'est précisé)
    private static final List<String> DEFAULT_MODELS = List.of(
            "juggernaut_reborn.safetensors",
            "realvisxl_v40.safetensors"
    );

    private static final Set<String> FILLER_WORDS = Set.of(
            "euh", "hum", "voila", "donc", "en fait", "je veux", "je voudrais", "un peu"
    );

    private HttpClient httpClient;
    private final Random random = new Random();

    @PostConstruct
    public void init() {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public PromptResponse generateFullPromptConfig(String userText, String emotion, Map<String, Object> faceAnalysis) {
        logger.info("[PromptService] Analyse pour : '{}'", userText);

        if (userText == null || userText.isBlank()) {
            return new PromptResponse("detailed face", "outfit", "background", true, true, DEFAULT_MODELS.get(0));
        }

        String inputLower = cleanText(userText);

        // 1. Détection du Modèle
        String selectedModel = detectModel(inputLower);

        // 2. Nettoyage et Traduction pour les prompts
        String cleanedText = removeFillerWords(inputLower);
        String baseEnglish = translateToEnglish(cleanedText);
        String translatedEmotion = translateToEnglish(emotion);

        // 3. Flags de Préservation intelligents
        boolean preserveFace = determineFacePreservation(inputLower, selectedModel);
        boolean preservePose = determinePosePreservation(inputLower);

        // 4. Construction des prompts
        String face = buildFacePrompt(baseEnglish, faceAnalysis, selectedModel, translatedEmotion);
        String body = buildBodyPrompt(baseEnglish);
        String background = buildBackgroundPrompt(baseEnglish);

        logger.info("[PromptService] Modèle choisi : {} | FacePreserve: {} | PosePreserve: {}",
                selectedModel, preserveFace, preservePose);

        return new PromptResponse(face, body, background, preserveFace, preservePose, selectedModel);
    }

    private String detectModel(String text) {
        // Cherche un mot clé de style
        for (Map.Entry<String, String> entry : STYLE_TO_MODEL.entrySet()) {
            if (text.contains(entry.getKey())) {
                logger.debug("[PromptService] Style détecté : {}", entry.getKey());
                return entry.getValue();
            }
        }
        // Sinon, prend un modèle réaliste au hasard
        String randomDefault = DEFAULT_MODELS.get(random.nextInt(DEFAULT_MODELS.size()));
        logger.debug("[PromptService] Aucun style détecté, modèle aléatoire : {}", randomDefault);
        return randomDefault;
    }

    private boolean determineFacePreservation(String text, String model) {
        // Si c'est un monstre/robot OU si on utilise un modèle de dessin animé (qui déforme trop)
        List<String> radicalChanges = List.of("robot", "monster", "alien", "zombie", "skull", "skeleton", "cyborg");
        boolean hasRadicalChange = radicalChanges.stream().anyMatch(text::contains);
        boolean isToonModel = model.contains("arthemyToons");

        return !(hasRadicalChange || isToonModel);
    }

    private boolean determinePosePreservation(String text) {
        // Si l'utilisateur décrit une action physique intense, on laisse l'IA libre
        List<String> actions = List.of("flying", "running", "jumping", "dancing", "fighting", "swimming", "climbing");
        return actions.stream().noneMatch(text::contains);
    }

    private String buildFacePrompt(String base, Map<String, Object> faceAnalysis, String model, String translatedEmotion) {
        StringBuilder sb = new StringBuilder("Extremely detailed face portrait of ");
        sb.append(base);

        if (faceAnalysis != null && faceAnalysis.containsKey("attributes")) {
            Map<?, ?> attr = (Map<?, ?>) faceAnalysis.get("attributes");
            if (attr.containsKey("gender")) sb.append(", ").append(attr.get("gender"));
            if (attr.containsKey("age")) sb.append(", ").append(attr.get("age")).append(" years old");
        }
        if (translatedEmotion != "error"){
            sb.append(", ").append(translatedEmotion);
        }

        // Ajout de mots clés spécifiques au style
        if (model.contains("novaAnime")) sb.append(", anime style, high quality illustration");
        else if (model.contains("arthemyToons")) sb.append(", cartoon style, 3d render, pixar look");
        else sb.append(", highly detailed skin, pores, 8k uhd, cinematic lighting");

        return sb.toString();
    }

    private String buildBodyPrompt(String base) {
        return "Full body costume of " + base + ", intricate details, high quality fabric, cinematic lighting, masterpiece";
    }

    private String buildBackgroundPrompt(String base) {
        return "Cinematic background scenery, " + base + " environment, volumetric lighting, 8k, highly detailed";
    }

    // --- Fonctions utilitaires ---

    private String cleanText(String text) {
        if (text == null) return "";
        String normalized = Normalizer.normalize(text.toLowerCase(), Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}", "");
        return normalized.replaceAll("[^\\w\\s]", "").trim();
    }

    private String removeFillerWords(String text) {
        String result = text;
        for (String filler : FILLER_WORDS) {
            result = result.replaceAll("\\b" + filler + "\\b", "");
        }
        return result.replaceAll("\\s+", " ").trim();
    }

    private String translateToEnglish(String text) {
        try {
            JSONObject body = new JSONObject().put("q", text).put("source", "fr").put("target", "en").put("format", "text");
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(translationServiceUrl + "/translate"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return (response.statusCode() == 200) ? new JSONObject(response.body()).getString("translatedText") : text;
        } catch (Exception e) {
            return text;
        }
    }
}