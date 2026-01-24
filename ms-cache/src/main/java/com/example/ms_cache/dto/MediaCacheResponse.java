package com.example.ms_cache.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MediaCacheResponse {
    private String requestId;
    private String imageB64;
    private String audioB64;
    private String imageName;
    private String createdAt;
}
