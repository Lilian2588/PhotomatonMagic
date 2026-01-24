package com.example.monolithic.DTO;

import java.io.Serializable;

public class AvatarGenerationResultDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String requestId;
    private String base64Image; // La donnée brute
    private String status;
    private String message;

    public AvatarGenerationResultDTO() {}

    public AvatarGenerationResultDTO(String requestId, String base64Image, String status, String message) {
        this.requestId = requestId;
        this.base64Image = base64Image;
        this.status = status;
        this.message = message;
    }

    // Getters et Setters
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public String getBase64Image() { return base64Image; }
    public void setBase64Image(String base64Image) { this.base64Image = base64Image; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getImageUrl() {
        if (this.base64Image != null && !this.base64Image.startsWith("data:image")) {
            return "data:image/jpeg;base64," + this.base64Image;
        }
        return this.base64Image;
    }
}