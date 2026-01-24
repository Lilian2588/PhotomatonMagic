package com.example.ms_cache.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CacheUploadResponseDTO {
    private String imageUrl;
    private String audioUrl;
    private String status;
}
