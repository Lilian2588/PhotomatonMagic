package com.example.avatar.DTO;

import java.io.Serializable;

public class AvatarGenerationResultDTO implements Serializable {

    private String requestId;
    private String imageUrl;
    private String status; // "SUCCESS", "FAILURE" etc.
    private String message; // Optionnel : erreurs ou infos

    public AvatarGenerationResultDTO() {}

    public AvatarGenerationResultDTO(String requestId, String imageUrl, String status, String message) {
        this.requestId = requestId;
        this.imageUrl = imageUrl;
        this.status = status;
        this.message = message;
    }

    // Getters et setters
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
