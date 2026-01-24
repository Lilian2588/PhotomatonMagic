package com.example.avatar.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// Cette classe sert de "zone d'attente" pour un RequestId donné
public class WorkflowContext {
    private Map<String, Object> faceAnalysis;
    private String transcription;
    private String emotion;

    // Flags pour savoir ce qu'on a reçu
    private boolean faceReceived = false;
    private boolean voiceReceived = false;

    public synchronized void setFaceData(Map<String, Object> faceAnalysis) {
        this.faceAnalysis = faceAnalysis;
        this.faceReceived = true;
    }

    public synchronized void setVoiceData(String transcription, String emotion) {
        this.transcription = transcription;
        this.emotion = emotion;
        this.voiceReceived = true;
    }

    // On est prêt seulement si on a LES DEUX
    public synchronized boolean isReady() {
        return faceReceived && voiceReceived;
    }

    // Getters...
    public Map<String, Object> getFaceAnalysis() { return faceAnalysis; }
    public String getTranscription() { return transcription; }
    public String getEmotion() { return emotion; }
}