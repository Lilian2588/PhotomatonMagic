package com.example.ms_cache.controller;

import com.example.ms_cache.exception.BadRequestException;
import com.example.ms_cache.exception.MediaNotFoundException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cache/file")
public class FileRetrievalController {

    private final RedisTemplate<String, byte[]> byteRedisTemplate;

    private static final String KEY_PREFIX = "cache:media:";
    private static final String IMAGE_SUFFIX = ":image";
    private static final String AUDIO_SUFFIX = ":audio";

    public FileRetrievalController(RedisTemplate<String, byte[]> byteRedisTemplate) {
        this.byteRedisTemplate = byteRedisTemplate;
    }

    /**
     * GET /cache/file/{requestId}/image
     * Retrieve image file by requestId
     */
    @GetMapping("/{requestId}/image")
    public ResponseEntity<byte[]> getImage(@PathVariable String requestId) {
        if (requestId == null || requestId.trim().isEmpty()) {
            throw new BadRequestException("requestId ne peut pas être vide");
        }

        String imageKey = KEY_PREFIX + requestId + IMAGE_SUFFIX;
        byte[] imageBytes = byteRedisTemplate.opsForValue().get(imageKey);

        if (imageBytes == null) {
            throw new MediaNotFoundException("Image non trouvée pour requestId: " + requestId);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        headers.setContentLength(imageBytes.length);

        return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
    }

    /**
     * GET /cache/file/{requestId}/audio
     * Retrieve audio file by requestId
     */
    @GetMapping("/{requestId}/audio")
    public ResponseEntity<byte[]> getAudio(@PathVariable String requestId) {
        if (requestId == null || requestId.trim().isEmpty()) {
            throw new BadRequestException("requestId ne peut pas être vide");
        }

        String audioKey = KEY_PREFIX + requestId + AUDIO_SUFFIX;
        byte[] audioBytes = byteRedisTemplate.opsForValue().get(audioKey);

        if (audioBytes == null) {
            throw new MediaNotFoundException("Audio non trouvé pour requestId: " + requestId);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf("audio/wav"));
        headers.setContentLength(audioBytes.length);

        return new ResponseEntity<>(audioBytes, headers, HttpStatus.OK);
    }
}
