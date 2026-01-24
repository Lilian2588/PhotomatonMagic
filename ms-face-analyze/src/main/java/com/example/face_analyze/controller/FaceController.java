package com.example.face_analyze.controller;

import com.example.face_analyze.DTO.FaceAnalyzeRequestDTO;
import com.example.face_analyze.service.FaceAnalyzeAsyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/face")
public class FaceController {

    private static final Logger logger =
            LoggerFactory.getLogger(FaceController.class);

    private final FaceAnalyzeAsyncService asyncService;

    public FaceController(FaceAnalyzeAsyncService asyncService) {
        this.asyncService = asyncService;
    }

    @PostMapping("/process")
    public String faceAnalyze(@RequestBody FaceAnalyzeRequestDTO request) {

        if (request.getRequestId() == null || request.getRequestId().isEmpty()) {
            throw new IllegalArgumentException("requestId obligatoire");
        }

        logger.info("[Controller] Requête reçue | requestId={}", request.getRequestId());

        asyncService.enqueueRequest(request.getRequestId());

        return request.getRequestId();
    }
}
