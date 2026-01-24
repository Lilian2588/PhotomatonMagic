package com.example.ms_cache.controller;

import com.example.ms_cache.dto.CacheUploadResponseDTO;
import com.example.ms_cache.dto.MediaCacheRequest;
import com.example.ms_cache.dto.MediaCacheResponse;
import com.example.ms_cache.dto.UpdateImageNameRequest;
import com.example.ms_cache.service.CacheMediaService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cache/media")
public class CacheMediaController {

    private final CacheMediaService cacheMediaService;

    public CacheMediaController(CacheMediaService cacheMediaService) {
        this.cacheMediaService = cacheMediaService;
    }

    /**
     * POST /cache/media
     * Stock une image et un audio en Base64 dans Redis
     */
    @PostMapping
    public ResponseEntity<String> saveMedia(@RequestBody MediaCacheRequest request) {
        cacheMediaService.saveMedia(request);
        return ResponseEntity.ok("Media sauvegardé avec succès pour requestId: " + request.getRequestId());
    }

    /**
     * POST /cache/media/{requestId}
     * Met à jour le nom de l'image
     */
    @PostMapping("/{requestId}")
    public ResponseEntity<String> updateImageName(
            @PathVariable String requestId,
            @RequestBody UpdateImageNameRequest request) {
        cacheMediaService.updateImageName(requestId, request.getNewImageName());
        return ResponseEntity.ok("imageName mis à jour avec succès pour requestId: " + requestId);
    }

    /**
     * GET /cache/media/{requestId}?deleteAfterRead={true|false}
     * Recupere les médias et peux les supprimer
     */
    @GetMapping("/{requestId}")
    public ResponseEntity<MediaCacheResponse> getMedia(
            @PathVariable String requestId,
            @RequestParam(required = false, defaultValue = "false") boolean deleteAfterRead) {
        MediaCacheResponse response = cacheMediaService.getMedia(requestId, deleteAfterRead);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /cache/upload
     * Upload endpoint that saves media and returns URLs for accessing them
     */
    @PostMapping("/upload")
    public ResponseEntity<CacheUploadResponseDTO> uploadMedia(@RequestBody MediaCacheRequest request) {
        cacheMediaService.saveMedia(request);

        // Generate URLs for accessing the uploaded files
        String baseUrl = "http://localhost:8083/cache/file";
        String imageUrl = baseUrl + "/" + request.getRequestId() + "/image";
        String audioUrl = baseUrl + "/" + request.getRequestId() + "/audio";

        CacheUploadResponseDTO response = new CacheUploadResponseDTO(imageUrl, audioUrl, "success");
        return ResponseEntity.ok(response);
    }
}
