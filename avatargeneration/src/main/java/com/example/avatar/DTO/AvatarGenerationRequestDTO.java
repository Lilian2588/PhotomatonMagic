package com.example.avatar.DTO;

import java.io.Serializable;

public class AvatarGenerationRequestDTO implements Serializable {

    private String requestId;
    private boolean userPermission;

    // Constructeur vide (Jackson / JMS)
    public AvatarGenerationRequestDTO() {}

    public AvatarGenerationRequestDTO(String requestId) {
        this.requestId = requestId;
    }
    
    public boolean isUserPermission() {
        return userPermission;
    }

    public void setUserPermission(boolean userPermission) {
        this.userPermission = userPermission;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    @Override
    public String toString() {
        return "AvatarGenerationRequestDTO{" +
                "requestId='" + requestId + '\'' +
                '}';
    }
}
