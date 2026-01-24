package com.example.monolithic.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "transformed_images")
public class TransformedImage {

    @Id
    private String requestId;

    private String filePath; // Chemin vers le fichier sur le disque
    private String contentType;
    private LocalDateTime createdAt;

    public TransformedImage() {
        this.createdAt = LocalDateTime.now();
    }

    public TransformedImage(String requestId, String filePath, String contentType) {
        this();
        this.requestId = requestId;
        this.filePath = filePath;
        this.contentType = contentType;
    }

    // Getters et Setters
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}