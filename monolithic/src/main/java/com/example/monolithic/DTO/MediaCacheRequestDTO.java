package com.example.monolithic.DTO;

public class MediaCacheRequestDTO {

    private String requestId;
    private String imageName;
    private String imageB64;
    private String audioB64;

    public MediaCacheRequestDTO() {}

    public MediaCacheRequestDTO(String requestId, String imageName, String imageB64, String audioB64) {
        this.requestId = requestId;
        this.imageName = imageName;
        this.imageB64 = imageB64;
        this.audioB64 = audioB64;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getImageB64() {
        return imageB64;
    }

    public void setImageB64(String imageB64) {
        this.imageB64 = imageB64;
    }

    public String getAudioB64() {
        return audioB64;
    }

    public void setAudioB64(String audioB64) {
        this.audioB64 = audioB64;
    }
}
