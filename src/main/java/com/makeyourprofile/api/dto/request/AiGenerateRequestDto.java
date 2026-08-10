package com.makeyourprofile.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AiGenerateRequestDto {
    @NotBlank(message = "Original text is required")
    private String originalText;
    
    private String context; // Optional context, e.g., role or skills
}
