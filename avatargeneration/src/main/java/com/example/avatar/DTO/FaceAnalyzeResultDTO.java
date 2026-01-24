package com.example.avatar.DTO;

import java.io.Serializable;
import java.util.Map;

public class FaceAnalyzeResultDTO implements Serializable {

    private String requestId;
    private Map<String, Object> analysis;

    public FaceAnalyzeResultDTO() {}

    public FaceAnalyzeResultDTO(String requestId, Map<String, Object> analysis) {
        this.requestId = requestId;
        this.analysis = analysis;
    }

    public String getRequestId() {
        return requestId;
    }

    public Map<String, Object> getAnalysis() {
        return analysis;
    }
}
