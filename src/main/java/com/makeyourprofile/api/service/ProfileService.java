package com.makeyourprofile.api.service;

import com.makeyourprofile.api.dto.request.ProfileRequestDto;
import com.makeyourprofile.api.dto.response.ProfileResponseDto;
import com.makeyourprofile.api.model.ProfileEntity;
import com.makeyourprofile.api.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;

    public ProfileResponseDto createProfile(ProfileRequestDto requestDto) {
        ProfileEntity entity = ProfileEntity.builder()
                .userId(requestDto.getUserId())
                .title(requestDto.getTitle() != null ? requestDto.getTitle() : "Untitled Profile")
                .profileData(requestDto.getProfileData())
                .themeConfig(requestDto.getThemeConfig() != null ? requestDto.getThemeConfig() : "{\"theme\": \"light\", \"accentColor\": \"#3b82f6\"}")
                .build();

        ProfileEntity savedEntity = profileRepository.save(entity);
        return mapToDto(savedEntity);
    }

    public List<ProfileResponseDto> getProfilesByUserId(String userId) {
        return profileRepository.findByUserId(userId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public ProfileResponseDto getProfileById(String id) {
        ProfileEntity entity = profileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Profile not found with id: " + id));
        return mapToDto(entity);
    }

    public ProfileResponseDto updateProfile(String id, ProfileRequestDto requestDto) {
        ProfileEntity entity = profileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Profile not found with id: " + id));

        if (requestDto.getTitle() != null) entity.setTitle(requestDto.getTitle());
        if (requestDto.getProfileData() != null) entity.setProfileData(requestDto.getProfileData());
        if (requestDto.getThemeConfig() != null) entity.setThemeConfig(requestDto.getThemeConfig());

        ProfileEntity updatedEntity = profileRepository.save(entity);
        return mapToDto(updatedEntity);
    }

    public void deleteProfile(String id) {
        profileRepository.deleteById(id);
    }

    private ProfileResponseDto mapToDto(ProfileEntity entity) {
        return ProfileResponseDto.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .title(entity.getTitle())
                .profileData(entity.getProfileData())
                .themeConfig(entity.getThemeConfig())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
