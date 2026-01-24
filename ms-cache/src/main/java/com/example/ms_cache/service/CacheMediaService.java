package com.example.ms_cache.service;

import com.example.ms_cache.dto.MediaCacheRequest;
import com.example.ms_cache.dto.MediaCacheResponse;
import com.example.ms_cache.exception.BadRequestException;
import com.example.ms_cache.exception.MediaNotFoundException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Base64;
import java.util.Map;

@Service
public class CacheMediaService {

    private final RedisTemplate<String, byte[]> byteRedisTemplate;
    private final StringRedisTemplate stringRedisTemplate;

    private static final String KEY_PREFIX = "cache:media:";
    private static final String IMAGE_SUFFIX = ":image";
    private static final String AUDIO_SUFFIX = ":audio";
    private static final String META_SUFFIX = ":meta";

    public CacheMediaService(RedisTemplate<String, byte[]> byteRedisTemplate,
                             StringRedisTemplate stringRedisTemplate) {
        this.byteRedisTemplate = byteRedisTemplate;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public void saveMedia(MediaCacheRequest request) {
        // Validation
        if (request.getRequestId() == null || request.getRequestId().trim().isEmpty()) {
            throw new BadRequestException("requestId ne peut pas être vide");
        }

        // Décoder Base64
        byte[] imageBytes;
        byte[] audioBytes;
        
        try {
            imageBytes = Base64.getDecoder().decode(request.getImageB64());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Impossible de décoder imageB64: " + e.getMessage());
        }

        try {
            audioBytes = Base64.getDecoder().decode(request.getAudioB64());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Impossible de décoder audioB64: " + e.getMessage());
        }

        String requestId = request.getRequestId();
        String imageKey = KEY_PREFIX + requestId + IMAGE_SUFFIX;
        String audioKey = KEY_PREFIX + requestId + AUDIO_SUFFIX;
        String metaKey = KEY_PREFIX + requestId + META_SUFFIX;

        // Stocker les binaires
        byteRedisTemplate.opsForValue().set(imageKey, imageBytes);
        byteRedisTemplate.opsForValue().set(audioKey, audioBytes);

        // Stocker les métadonnées
        stringRedisTemplate.opsForHash().put(metaKey, "imageName", request.getImageName());
        stringRedisTemplate.opsForHash().put(metaKey, "createdAt", Instant.now().toString());
    }

    public void updateImageName(String requestId, String newImageName) {
        if (requestId == null || requestId.trim().isEmpty()) {
            throw new BadRequestException("requestId ne peut pas être vide");
        }

        String metaKey = KEY_PREFIX + requestId + META_SUFFIX;

        // Vérifier si le hash existe
        if (Boolean.FALSE.equals(stringRedisTemplate.hasKey(metaKey))) {
            throw new MediaNotFoundException("Aucune métadonnée trouvée pour requestId: " + requestId);
        }

        // Mettre à jour imageName
        stringRedisTemplate.opsForHash().put(metaKey, "imageName", newImageName);
    }

    public MediaCacheResponse getMedia(String requestId, boolean deleteAfterRead) {
        if (requestId == null || requestId.trim().isEmpty()) {
            throw new BadRequestException("requestId ne peut pas être vide");
        }

        String imageKey = KEY_PREFIX + requestId + IMAGE_SUFFIX;
        String audioKey = KEY_PREFIX + requestId + AUDIO_SUFFIX;
        String metaKey = KEY_PREFIX + requestId + META_SUFFIX;

        // Lire les binaires
        byte[] imageBytes = byteRedisTemplate.opsForValue().get(imageKey);
        byte[] audioBytes = byteRedisTemplate.opsForValue().get(audioKey);

        if (imageBytes == null || audioBytes == null) {
            throw new MediaNotFoundException("Media non trouvé pour requestId: " + requestId);
        }

        // Lire les métadonnées
        Map<Object, Object> metaMap = stringRedisTemplate.opsForHash().entries(metaKey);
        String imageName = (String) metaMap.get("imageName");
        String createdAt = (String) metaMap.get("createdAt");

        // Encoder en Base64
        String imageB64 = Base64.getEncoder().encodeToString(imageBytes);
        String audioB64 = Base64.getEncoder().encodeToString(audioBytes);

        // Supprimer si demandé
        if (deleteAfterRead) {
            byteRedisTemplate.delete(imageKey);
            byteRedisTemplate.delete(audioKey);
            stringRedisTemplate.delete(metaKey);
        }

        return new MediaCacheResponse(requestId, imageB64, audioB64, imageName, createdAt);
    }
}
