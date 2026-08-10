package com.makeyourprofile.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProfileRequestDto {
    private String userId;

    private String title;

    @NotNull(message = "Profile data (JSON) is required")
    private String profileData;

    private String themeConfig;
}
