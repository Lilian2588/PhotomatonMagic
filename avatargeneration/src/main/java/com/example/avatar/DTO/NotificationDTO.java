package com.example.avatar.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

public class NotificationDTO implements Serializable {

    @JsonProperty("id")
    private String id;

    @JsonProperty("message")
    private NotificationMessage message;

    @JsonProperty("service")
    private String service;

    public NotificationDTO() {}

    public NotificationDTO(String id, String status, String service) {
        this.id = id;
        this.message = new NotificationMessage(status);
        this.service = service;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public NotificationMessage getMessage() { return message; }
    public void setMessage(NotificationMessage message) { this.message = message; }
    public String getService() { return service; }
    public void setService(String service) { this.service = service; }

    public static class NotificationMessage implements Serializable {
        @JsonProperty("status")
        private String status;

        public NotificationMessage() {}
        public NotificationMessage(String status) { this.status = status; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}
