package com.example.Ms_Image.service;

import com.example.Ms_Image.client.CacheClient;
import com.example.Ms_Image.client.FooocusApiClient;
import com.example.Ms_Image.DTO.TransformRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
public class ImageTransformService {
    private static final Logger log = LoggerFactory.getLogger(ImageTransformService.class);
    private final MaskGeneratorService maskGenerator;
    private final FooocusApiClient apiClient;
    private final CacheClient cacheClient;

    public ImageTransformService(CacheClient cacheClient, MaskGeneratorService mg, FooocusApiClient ac) {
        this.cacheClient = cacheClient;
        this.maskGenerator = mg;
        this.apiClient = ac;
    }

    public String transform(TransformRequest req) throws Exception {
        log.info("Récupération de l'image pour le requestId: {}", req.getRequestId());

        // 1. Récupération de l'image originale depuis le cache
        String originalImg = cacheClient.getImageBase64(req.getRequestId());
        if (originalImg == null) throw new RuntimeException("Image introuvable dans le cache");

        // 2. Génération des masques (In-Memory)
        MaskGeneratorService.MaskSet masks = maskGenerator.generateMasks(originalImg, req.getAnalysisResult());

        String currentImg = originalImg;

        // 3. Étape Corps (Inpaint)
        log.info("Étape 1: Modification du corps...");
        use.Map<String, Object> bodyPayload = apiClient.buildBodyPayload(currentImg, masks.getBodyMask(), req.getBodyPrompt(), req.getConfig().isPreservePose(), req.getModelName());
        log.info("Payload Body: {}", sanitizePayloadForLog(bodyPayload));
        currentImg = apiClient.processStep("image-inpaint-outpaint", bodyPayload);
        log.info("Taille de l'image 1 : {}", currentImg.length());
        currentImg = apiClient.processStep("image-inpaint-outpaint", apiClient.buildFacePayload(currentImg, masks.getHeadMask(), req.getFacePrompt(), req.getConfig().isPreserveFace(),req.getModelName()));

        // 4. Étape Visage (FaceSwap)
        log.info("Étape 2: Ajustement du visage...");
        Map<String, Object> facePayload = apiClient.buildFacePayload(currentImg, masks.getHeadMask(), req.getFacePrompt(), req.getConfig().isPreserveFace(), req.getModelName());
        log.info("Payload Face: {}", sanitizePayloadForLog(facePayload));
        currentImg = apiClient.processStep("image-inpaint-outpaint", facePayload);

        log.info("Taille de l'image 2 : {}", currentImg.length());



        // 5. Étape Background
        log.info("Étape 3: Nouveau décor...");
        Map<String, Object> bgPayload = apiClient.buildBackgroundPayload(currentImg, masks.getBackgroundMask(), req.getBackgroundPrompt(), req.getModelName());
        log.info("Payload Background: {}", sanitizePayloadForLog(bgPayload));
        currentImg = apiClient.processStep("image-inpaint-outpaint", bgPayload);

        // 6. Upscale Final HD
        log.info("Étape 4: Upscale 2x final...");
        return upscaleImage(currentImg,req.getModelName());
    }

    private String upscaleImage(String base64Image,String modelName) throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("input_image", base64Image);
        payload.put("uov_method", "Upscale (2x)");
        payload.put("upscale_value", 2);
        payload.put("base_model_name", modelName);
        payload.put("performance_selection", "Quality");
        payload.put("require_base64", true);
        payload.put("async_process", true);
        return apiClient.processStep("image-upscale-vary", payload);
    }

    private Map<String, Object> sanitizePayloadForLog(Map<String, Object> payload) {
        Map<String, Object> sanitized = new HashMap<>(payload);
        if (sanitized.containsKey("input_image")) sanitized.put("input_image", "<BASE64_IMAGE_TRUNCATED>");
        if (sanitized.containsKey("input_mask")) sanitized.put("input_mask", "<BASE64_MASK_TRUNCATED>");
        
        if (sanitized.containsKey("image_prompts")) {
            // C'est une liste de maps
            java.util.List<Map<String, Object>> prompts = (java.util.List<Map<String, Object>>) sanitized.get("image_prompts");
            java.util.List<Map<String, Object>> sanitizedPrompts = new java.util.ArrayList<>();
            
            for (Map<String, Object> p : prompts) {
                Map<String, Object> newP = new HashMap<>(p);
                if (newP.containsKey("cn_img")) newP.put("cn_img", "<BASE64_CN_IMG_TRUNCATED>");
                sanitizedPrompts.add(newP);
            }
            sanitized.put("image_prompts", sanitizedPrompts);
        }
        return sanitized;
    }
}