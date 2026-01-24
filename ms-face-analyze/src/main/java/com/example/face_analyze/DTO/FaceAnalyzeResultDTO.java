package com.example.face_analyze.DTO;
import java.util.Map;
import java.io.Serializable;

public class FaceAnalyzeResultDTO implements Serializable {
    private String requestId;
    // CHANGE ICI : faceAttributes devient analysis pour matcher avec le MS Avatar
    private Map<String, Object> analysis;

    public FaceAnalyzeResultDTO() {}

    public FaceAnalyzeResultDTO(String requestId, Map<String, Object> analysis) {
        this.requestId = requestId;
        this.analysis = analysis;
    }

    public String getRequestId() { return requestId; }
    public Map<String, Object> getAnalysis() { return analysis; }

    public void setRequestId(String requestId) { this.requestId = requestId; }
    public void setAnalysis(Map<String, Object> analysis) { this.analysis = analysis; }
}