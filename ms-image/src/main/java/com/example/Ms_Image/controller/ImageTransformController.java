package com.example.Ms_Image.controller;

import com.example.Ms_Image.DTO.TransformRequest;
import com.example.Ms_Image.service.ImageTransformAsyncService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/transform")
public class ImageTransformController {
    private final ImageTransformAsyncService asyncService;

    public ImageTransformController(ImageTransformAsyncService asyncService) {
        this.asyncService = asyncService;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> process(@RequestBody TransformRequest request) {
        // On met en file d'attente
        asyncService.enqueueRequest(request);

        // On répond tout de suite au client
        return ResponseEntity.accepted().body(Map.of(
                "requestId", request.getRequestId(),
                "status", "PENDING",
                "message", "Traitement en cours dans la file d'attente."
        ));
    }
}