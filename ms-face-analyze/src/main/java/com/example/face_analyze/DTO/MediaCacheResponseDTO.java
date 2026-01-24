package com.example.face_analyze.DTO;

import java.time.OffsetDateTime;

public class MediaCacheResponseDTO {
    private String requestId;
    private String imageB64;
    private String audioB64;
    private String imageName;
    private OffsetDateTime createdAt;

    public MediaCacheResponseDTO() {}

    // Getters et setters
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public String getImageB64() { return imageB64; }
    public void setImageB64(String imageB64) { this.imageB64 = imageB64; }

    public String getAudioB64() { return audioB64; }
    public void setAudioB64(String audioB64) { this.audioB64 = audioB64; }

    public String getImageName() { return imageName; }
    public void setImageName(String imageName) { this.imageName = imageName; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
