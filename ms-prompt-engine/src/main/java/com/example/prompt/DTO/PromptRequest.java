package com.example.prompt.DTO;

import java.util.Map;

public class PromptRequest {

    private String requestId;
    private String userText;
    private String emotion;
    private Map<String, Object> faceAnalysis;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getUserText() {
        return userText;
    }

    public void setUserText(String userText) {
        this.userText = userText;
    }

    public Map<String, Object> getFaceAnalysis() {
        return faceAnalysis;
    }

    public void setFaceAnalysis(Map<String, Object> faceAnalysis) {
        this.faceAnalysis = faceAnalysis;
    }

    public String getEmotion() {
        return emotion;
    }
    public void setEmotion() {
        this.emotion = emotion;
    }
}
