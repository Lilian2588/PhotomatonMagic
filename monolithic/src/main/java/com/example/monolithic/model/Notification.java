package com.example.monolithic.model;

import java.io.Serializable;

public class Notification implements Serializable {

    private String requestId;
    private String imageUrl;
    private String status;
    private String message; // On le passe en String simple pour éviter l'erreur de type

    public Notification() {
    }

    public Notification(String requestId, String imageUrl, String status, String message) {
        this.requestId = requestId;
        this.imageUrl = imageUrl;
        this.status = status;
        this.message = message;
    }

    // --- GETTERS ET SETTERS ---

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}