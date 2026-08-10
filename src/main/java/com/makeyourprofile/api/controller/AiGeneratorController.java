package com.makeyourprofile.api.controller;

import com.makeyourprofile.api.dto.request.AiGenerateRequestDto;
import com.makeyourprofile.api.dto.response.AiGenerateResponseDto;
import com.makeyourprofile.api.dto.response.ApiResponseDto;
import com.makeyourprofile.api.service.GroqAiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiGeneratorController {

    private final GroqAiService groqAiService;

    @PostMapping("/polish")
    public ResponseEntity<ApiResponseDto<AiGenerateResponseDto>> polishText(@Valid @RequestBody AiGenerateRequestDto request) {
        String polished = groqAiService.polishResumeText(request.getOriginalText());
        return ResponseEntity.ok(new ApiResponseDto<>(true, "Text polished", new AiGenerateResponseDto(polished)));
    }

    @PostMapping("/summary")
    public ResponseEntity<ApiResponseDto<AiGenerateResponseDto>> generateSummary(@Valid @RequestBody AiGenerateRequestDto request) {
        String title = request.getContext() != null ? request.getContext() : "Professional";
        String summary = groqAiService.generateProfessionalSummary(title, request.getOriginalText());
        return ResponseEntity.ok(new ApiResponseDto<>(true, "Summary generated", new AiGenerateResponseDto(summary)));
    }

    @PostMapping("/update-profile")
    public ResponseEntity<ApiResponseDto<AiGenerateResponseDto>> updateProfileJson(@Valid @RequestBody AiGenerateRequestDto request) {
        // request.getOriginalText() will hold the current JSON
        // request.getContext() will hold the user's prompt
        String updatedJson = groqAiService.updatePortfolioJson(request.getOriginalText(), request.getContext());
        return ResponseEntity.ok(new ApiResponseDto<>(true, "Profile updated via AI", new AiGenerateResponseDto(updatedJson)));
    }
}
