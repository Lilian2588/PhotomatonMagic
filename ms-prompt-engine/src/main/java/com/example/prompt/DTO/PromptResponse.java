package com.example.prompt.DTO;

public class PromptResponse {
    private String facePrompt;
    private String bodyPrompt;
    private String backgroundPrompt;
    private boolean preserveFace;
    private boolean preservePose;
    private String modelName; // Nouveau champ

    public PromptResponse(String facePrompt, String bodyPrompt, String backgroundPrompt,
                          boolean preserveFace, boolean preservePose, String modelName) {
        this.facePrompt = facePrompt;
        this.bodyPrompt = bodyPrompt;
        this.backgroundPrompt = backgroundPrompt;
        this.preserveFace = preserveFace;
        this.preservePose = preservePose;
        this.modelName = modelName;
    }

    // Getters
    public String getFacePrompt() { return facePrompt; }
    public String getBodyPrompt() { return bodyPrompt; }
    public String getBackgroundPrompt() { return backgroundPrompt; }
    public boolean isPreserveFace() { return preserveFace; }
    public boolean isPreservePose() { return preservePose; }
    public String getModelName() { return modelName; }
}