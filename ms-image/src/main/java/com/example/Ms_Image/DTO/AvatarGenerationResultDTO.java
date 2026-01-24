package com.example.Ms_Image.DTO;

import java.io.Serializable;

public class AvatarGenerationResultDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String requestId;
    private String base64Image; // On remplace imageUrl par la donnée brute en Base64
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
}