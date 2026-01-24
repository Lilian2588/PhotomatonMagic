package com.example.avatar.DTO;

import java.util.Map;

public class TransformRequest {
    private String requestId;
    private Map<String, Object> analysisResult;
    private String facePrompt;
    private String bodyPrompt;
    private String backgroundPrompt;
    private String modelName; // Nouveau champ
    private Config config;

    public TransformRequest() {}

    public TransformRequest(String requestId, Map<String, Object> analysisResult, String facePrompt, String bodyPrompt, String backgroundPrompt, boolean preserveFace, boolean preservePose, String modelName) {
        this.requestId = requestId;
        this.analysisResult = analysisResult;
        this.facePrompt = facePrompt;
        this.bodyPrompt = bodyPrompt;
        this.backgroundPrompt = backgroundPrompt;
        this.modelName = modelName; // Initialisation du modèle
        this.config = new Config(preserveFace, preservePose);
    }

    // Getters et Setters
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public Map<String, Object> getAnalysisResult() { return analysisResult; }
    public void setAnalysisResult(Map<String, Object> analysisResult) { this.analysisResult = analysisResult; }

    public String getFacePrompt() { return facePrompt; }
    public void setFacePrompt(String facePrompt) { this.facePrompt = facePrompt; }

    public String getBodyPrompt() { return bodyPrompt; }
    public void setBodyPrompt(String bodyPrompt) { this.bodyPrompt = bodyPrompt; }

    public String getBackgroundPrompt() { return backgroundPrompt; }
    public void setBackgroundPrompt(String backgroundPrompt) { this.backgroundPrompt = backgroundPrompt; }

    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }

    public Config getConfig() { return config; }
    public void setConfig(Config config) { this.config = config; }

    public static class Config {
        private boolean preserveFace;
        private boolean preservePose;
        public Config() {}
        public Config(boolean preserveFace, boolean preservePose) {
            this.preserveFace = preserveFace;
            this.preservePose = preservePose;
        }
        public boolean isPreserveFace() { return preserveFace; }
        public void setPreserveFace(boolean preserveFace) { this.preserveFace = preserveFace; }
        public boolean isPreservePose() { return preservePose; }
        public void setPreservePose(boolean preservePose) { this.preservePose = preservePose; }
    }
}