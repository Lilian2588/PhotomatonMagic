package com.example.avatar.DTO;

public class PromptResponse {
    private String facePrompt;
    private String bodyPrompt;
    private String backgroundPrompt;
    private boolean preserveFace;
    private boolean preservePose;
    private String modelName; // Ajouté

    public PromptResponse() {}

    public PromptResponse(String facePrompt, String bodyPrompt, String backgroundPrompt,
                          boolean preserveFace, boolean preservePose, String modelName) {
        this.facePrompt = facePrompt;
        this.bodyPrompt = bodyPrompt;
        this.backgroundPrompt = backgroundPrompt;
        this.preserveFace = preserveFace;
        this.preservePose = preservePose;
        this.modelName = modelName;
    }

    // Getters et Setters
    public String getFacePrompt() { return facePrompt; }
    public void setFacePrompt(String facePrompt) { this.facePrompt = facePrompt; }

    public String getBodyPrompt() { return bodyPrompt; }
    public void setBodyPrompt(String bodyPrompt) { this.bodyPrompt = bodyPrompt; }

    public String getBackgroundPrompt() { return backgroundPrompt; }
    public void setBackgroundPrompt(String backgroundPrompt) { this.backgroundPrompt = backgroundPrompt; }

    public boolean isPreserveFace() { return preserveFace; }
    public void setPreserveFace(boolean preserveFace) { this.preserveFace = preserveFace; }

    public boolean isPreservePose() { return preservePose; }
    public void setPreservePose(boolean preservePose) { this.preservePose = preservePose; }

    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }

    @Override
    public String toString() {
        return "PromptResponse{" +
                "facePrompt='" + facePrompt + '\'' +
                ", bodyPrompt='" + bodyPrompt + '\'' +
                ", backgroundPrompt='" + backgroundPrompt + '\'' +
                ", preserveFace=" + preserveFace +
                ", preservePose=" + preservePose +
                ", modelName='" + modelName + '\'' +
                '}';
    }
}