package com.example.avatar.controller;

import com.example.avatar.DTO.AvatarGenerationRequestDTO;
import com.example.avatar.service.AvatarGenerationAsyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/avatar")
public class AvatarGenerationController {

    private static final Logger logger =
            LoggerFactory.getLogger(AvatarGenerationController.class);

    private final AvatarGenerationAsyncService asyncService;

    public AvatarGenerationController(AvatarGenerationAsyncService asyncService) {
        this.asyncService = asyncService;
    }

    @PostMapping("/process")
    public String generateAvatar(@RequestBody AvatarGenerationRequestDTO request) {

        if (request.getRequestId() == null || request.getRequestId().isEmpty()) {
            throw new IllegalArgumentException("requestId obligatoire");
        }

        logger.info("[Controller] Requête reçue | requestId={}", request.getRequestId());

        asyncService.enqueueRequest(request.getRequestId());

        return request.getRequestId();
    }
}
