package com.example.monolithic.controller;

import com.example.monolithic.model.TransformedImage;
import com.example.monolithic.repository.TransformedImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/images")
public class ImageDisplayController {

    @Autowired
    private TransformedImageRepository repository;

    /**
     * Endpoint pour récupérer la liste de toutes les images enregistrées (métadonnées)
     */
    @GetMapping("/all")
    public ResponseEntity<List<TransformedImage>> getAllImages() {
        List<TransformedImage> images = repository.findAll();
        return ResponseEntity.ok(images);
    }

    /**
     * Endpoint pour afficher/visualiser une image spécifique par son ID
     */
    @GetMapping("/{requestId}/view")
    public ResponseEntity<Resource> viewImage(@PathVariable String requestId) {
        var imageOpt = repository.findById(requestId);

        if (imageOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        try {
            Path path = Paths.get(imageOpt.get().getFilePath());
            Resource resource = new UrlResource(path.toUri());

            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_PNG)
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}