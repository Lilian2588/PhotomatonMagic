package com.example.monolithic.DTO;

public class AvatarRequestDTO {

    private String audioBase64;
    private String imageBase64;
    private boolean userPermission;

    public String getAudioBase64() {
        return audioBase64;
    }

    public void setAudioBase64(String audioBase64) {
        this.audioBase64 = audioBase64;
    }

    public String getImageBase64() {
        return imageBase64;
    }

    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }

    public boolean isUserPermission() {
        return userPermission;
    }

    public void setUserPermission(boolean userPermission) {
        this.userPermission = userPermission;
    }

}
