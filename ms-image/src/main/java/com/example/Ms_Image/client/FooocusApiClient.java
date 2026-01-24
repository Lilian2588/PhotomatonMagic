package com.example.Ms_Image.client;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.*;

@Service
public class FooocusApiClient {
    private final RestTemplate restTemplate;

    @org.springframework.beans.factory.annotation.Value("${fooocus.api.url:http://tp.cpe.fr:8088}")
    private String baseUrl;

    private static final String TOKEN = "8705b669-c34c-4973-956e-0597e401637d";

    public FooocusApiClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String processStep(String endpoint, Map<String, Object> payload) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(TOKEN);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl + "/deep/img/v2/generation/" + endpoint, entity, Map.class);

        String jobId = (String) response.getBody().get("job_id");
        return waitForJob(jobId);
    }

    private String waitForJob(String jobId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(TOKEN);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        while (true) {
            ResponseEntity<Map> response = restTemplate.exchange(
                    baseUrl + "/deep/img/v1/generation/query-job?job_id=" + jobId,
                    HttpMethod.GET, entity, Map.class);
            Map<String, Object> result = response.getBody();
            String status = (String) result.get("job_status");
            if ("finished".equalsIgnoreCase(status) || "success".equalsIgnoreCase(status)) {
                List<Map<String, Object>> jobResult = (List) result.get("job_result");
                return (String) jobResult.get(0).get("base64");
            } else if ("error".equalsIgnoreCase(status)) {
                throw new RuntimeException("Job Fooocus en erreur");
            }
            try { Thread.sleep(2000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
    }

    private Map<String, Object> createBasePayload(String img, String mask, String prompt, String modelName) {
        Map<String, Object> p = new HashMap<>();
        p.put("prompt", prompt);
        p.put("negative_prompt", "low quality, bad anatomy, text, photorealistic");
        // Style adaptatif
        if (modelName.toLowerCase().contains("toon") || modelName.toLowerCase().contains("3d")) {
            p.put("style_selections", List.of("SAI Digital Art", "SAI 3D Model"));
        } else {
            p.put("style_selections", List.of("SAI Anime"));
        }
        p.put("performance_selection", "Speed");
        p.put("aspect_ratios_selection", "1152*896");
        p.put("base_model_name", modelName); // Utilisation du modèle dynamique
        p.put("input_image", img);
        p.put("input_mask", mask);
        p.put("inpaint_mode", "Inpaint (modify content)");
        p.put("require_base64", true);
        p.put("async_process", true);
        return p;
    }

    public Map<String, Object> buildBodyPayload(String img, String mask, String prompt, boolean preserve, String modelName) {
        Map<String, Object> p = createBasePayload(img, mask, prompt, modelName);
        boolean isCartoon = modelName.toLowerCase().contains("toon") || modelName.toLowerCase().contains("3d");

        Map<String, Object> cn = new HashMap<>();
        cn.put("cn_img", img);
        cn.put("cn_stop", 1.0);     // 100% de la génération sous contrôle
        cn.put("cn_weight", 1.0);   // Force maximale pour la pose

        // Testez CPDS si PyraCanny est trop permissif
        cn.put("cn_type", "CPDS");

        p.put("image_prompts", List.of(cn));

        Map<String, Object> adv = new HashMap<>();
        adv.put("inpaint_engine", "v2.6");

        if (isCartoon) {
            adv.put("inpaint_strength", 0.85); // Permet de changer les vêtements
            adv.put("guidance_scale", 16.0);   // Force le changement de style
        } else {
            adv.put("inpaint_strength", 1);  // Tes paramètres originaux Anime
            adv.put("guidance_scale", 9.0);
        }
        p.put("advanced_params", adv);

        return p;
    }

    public Map<String, Object> buildFacePayload(String img, String mask, String prompt, boolean preserve, String modelName) {
        boolean isCartoon = modelName.toLowerCase().contains("toon") || modelName.toLowerCase().contains("3d");

        Map<String, Object> p = createBasePayload(img, mask, prompt, modelName);

        Map<String, Object> cn = new HashMap<>();
        cn.put("cn_img", img);
        cn.put("cn_type", "FaceSwap");

        // On garde le contrôle jusqu'au bout pour l'identité
        cn.put("cn_stop", 1.0);
        // On met le poids au maximum si on veut préserver les traits
        cn.put("cn_weight", 0.8);

        p.put("image_prompts", List.of(cn));

        Map<String, Object> adv = new HashMap<>();
        adv.put("inpaint_engine", "v2.6");

        if (isCartoon) {
            // RÉGLAGE CLÉ : 0.35 max pour ne pas perdre les traits originaux
            adv.put("inpaint_strength", 0.35);
            // On monte la guidance pour forcer le respect du visage
            adv.put("guidance_scale", 15.0);
        } else {
            // Mode Anime classique
            adv.put("inpaint_strength", 0.45);
            adv.put("guidance_scale", 10.0);
        }

        adv.put("sharpness", 2.5);
        p.put("advanced_params", adv);

        return p;
    }

    public Map<String, Object> buildBackgroundPayload(String img, String mask, String prompt, String modelName) {
        Map<String, Object> p = createBasePayload(img, mask, prompt, modelName);
        Map<String, Object> cn = new HashMap<>();
        cn.put("cn_img", img);
        cn.put("cn_stop", 0.2);
        cn.put("cn_weight", 0.3);
        cn.put("cn_type", "ImagePrompt");
        p.put("image_prompts", List.of(cn));

        Map<String, Object> adv = new HashMap<>();
        adv.put("inpaint_engine", "v2.6");
        adv.put("inpaint_strength", 0.98);
        adv.put("guidance_scale", 9.0);
        p.put("advanced_params", adv);
        return p;
    }
}