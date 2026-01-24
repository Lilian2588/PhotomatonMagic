package com.example.Ms_Image.DTO;

public class TransformRequest {
    private String requestId;
    private String facePrompt;
    private String bodyPrompt;
    private String backgroundPrompt;
    private String modelName; // Ajouté
    private FaceAnalysisResult analysisResult; // Conservé selon ton souhait
    private Config config;

    // Getters / Setters
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public String getFacePrompt() { return facePrompt; }
    public void setFacePrompt(String facePrompt) { this.facePrompt = facePrompt; }

    public String getBodyPrompt() { return bodyPrompt; }
    public void setBodyPrompt(String bodyPrompt) { this.bodyPrompt = bodyPrompt; }

    public String getBackgroundPrompt() { return backgroundPrompt; }
    public void setBackgroundPrompt(String backgroundPrompt) { this.backgroundPrompt = backgroundPrompt; }

    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }

    public FaceAnalysisResult getAnalysisResult() { return analysisResult; }
    public void setAnalysisResult(FaceAnalysisResult analysisResult) { this.analysisResult = analysisResult; }

    public Config getConfig() { return config; }
    public void setConfig(Config config) { this.config = config; }

    public static class Config {
        private boolean preserveFace;
        private boolean preservePose;
        public boolean isPreserveFace() { return preserveFace; }
        public void setPreserveFace(boolean preserveFace) { this.preserveFace = preserveFace; }
        public boolean isPreservePose() { return preservePose; }
        public void setPreservePose(boolean preservePose) { this.preservePose = preservePose; }
    }
}