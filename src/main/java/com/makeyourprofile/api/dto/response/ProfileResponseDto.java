package com.makeyourprofile.api.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ProfileResponseDto {
    private String id;
    private String userId;
    private String title;
    private String profileData;
    private String themeConfig;
    private Instant createdAt;
    private Instant updatedAt;
}
