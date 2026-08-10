package com.makeyourprofile.api.controller;

import com.makeyourprofile.api.dto.request.LoginRequest;
import com.makeyourprofile.api.dto.request.RegisterRequest;
import com.makeyourprofile.api.dto.response.ApiResponseDto;
import com.makeyourprofile.api.dto.response.JwtResponse;
import com.makeyourprofile.api.entity.User;
import com.makeyourprofile.api.repository.UserRepository;
import com.makeyourprofile.api.security.JwtUtils;
import com.makeyourprofile.api.security.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<ApiResponseDto<JwtResponse>> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
        String name = user != null ? user.getName() : "";

        return ResponseEntity.ok(new ApiResponseDto<>(true, "User logged in successfully", 
                new JwtResponse(jwt, userDetails.getId(), userDetails.getUsername(), name)));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponseDto<String>> registerUser(@Valid @RequestBody RegisterRequest signUpRequest) {
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new ApiResponseDto<>(false, "Error: Email is already in use!", null));
        }

        // Create new user's account
        User user = User.builder()
                .name(signUpRequest.getName())
                .email(signUpRequest.getEmail())
                .password(encoder.encode(signUpRequest.getPassword()))
                .build();

        userRepository.save(user);

        return ResponseEntity.ok(new ApiResponseDto<>(true, "User registered successfully!", null));
    }
}
