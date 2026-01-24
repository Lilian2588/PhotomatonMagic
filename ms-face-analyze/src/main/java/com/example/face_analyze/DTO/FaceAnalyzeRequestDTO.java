package com.example.face_analyze.DTO;

import java.io.Serializable;

public class FaceAnalyzeRequestDTO implements Serializable {

    private String requestId;

    // Constructeur vide (Jackson / JMS)
    public FaceAnalyzeRequestDTO() {}

    public FaceAnalyzeRequestDTO(String requestId) {
        this.requestId = requestId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    @Override
    public String toString() {
        return "FaceRequestDTO{" +
                "requestId='" + requestId + '\'' +
                '}';
    }
}
