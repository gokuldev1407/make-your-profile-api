package com.makeyourprofile.api.controller;

import com.makeyourprofile.api.dto.request.ProfileRequestDto;
import com.makeyourprofile.api.dto.response.ApiResponseDto;
import com.makeyourprofile.api.dto.response.ProfileResponseDto;
import com.makeyourprofile.api.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @PostMapping
    public ResponseEntity<ApiResponseDto<ProfileResponseDto>> createProfile(@Valid @RequestBody ProfileRequestDto request) {
        String userId = getAuthenticatedUserId();
        request.setUserId(userId);
        ProfileResponseDto response = profileService.createProfile(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDto<>(true, "Profile created successfully", response));
    }

    @GetMapping("/my-profiles")
    public ResponseEntity<ApiResponseDto<List<ProfileResponseDto>>> getMyProfiles() {
        String userId = getAuthenticatedUserId();
        List<ProfileResponseDto> profiles = profileService.getProfilesByUserId(userId);
        return ResponseEntity.ok(new ApiResponseDto<>(true, "Profiles retrieved", profiles));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<ProfileResponseDto>> getProfile(@PathVariable String id) {
        ProfileResponseDto profile = profileService.getProfileById(id);
        // Note: For full security, we should check if profile.getUserId().equals(getAuthenticatedUserId())
        if (!profile.getUserId().equals(getAuthenticatedUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(new ApiResponseDto<>(true, "Profile retrieved", profile));
    }

    private String getAuthenticatedUserId() {
        Object principal = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof com.makeyourprofile.api.security.UserDetailsImpl) {
            return ((com.makeyourprofile.api.security.UserDetailsImpl) principal).getId();
        }
        return "";
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDto<ProfileResponseDto>> updateProfile(
            @PathVariable String id, @RequestBody ProfileRequestDto request) {
        ProfileResponseDto existing = profileService.getProfileById(id);
        if (!existing.getUserId().equals(getAuthenticatedUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        request.setUserId(getAuthenticatedUserId());
        ProfileResponseDto updated = profileService.updateProfile(id, request);
        return ResponseEntity.ok(new ApiResponseDto<>(true, "Profile updated", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDto<Void>> deleteProfile(@PathVariable String id) {
        ProfileResponseDto existing = profileService.getProfileById(id);
        if (!existing.getUserId().equals(getAuthenticatedUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        profileService.deleteProfile(id);
        return ResponseEntity.ok(new ApiResponseDto<>(true, "Profile deleted", null));
    }
}
