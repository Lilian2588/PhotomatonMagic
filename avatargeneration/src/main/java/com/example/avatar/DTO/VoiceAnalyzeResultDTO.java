package com.example.avatar.DTO;

import java.io.Serializable;

public class VoiceAnalyzeResultDTO implements Serializable {

    private String requestId;
    private String emotion;
    private String transcription;

    public VoiceAnalyzeResultDTO() {
    }

    // Constructeur complet
    public VoiceAnalyzeResultDTO(String requestId, String emotion, String transcription) {
        this.requestId = requestId;
        this.emotion = emotion;
        this.transcription = transcription;
    }

    // --- Getters et Setters ---

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getEmotion() {
        return emotion;
    }

    public void setEmotion(String emotion) {
        this.emotion = emotion;
    }

    public String getTranscription() {
        return transcription;
    }

    public void setTranscription(String transcription) {
        this.transcription = transcription;
    }
}