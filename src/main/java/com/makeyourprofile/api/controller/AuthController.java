package com.makeyourprofile.api.controller;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.makeyourprofile.api.dto.request.GoogleLoginRequest;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Value("${google.client.id:YOUR_GOOGLE_CLIENT_ID}")
    private String googleClientId;

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
    @PostMapping("/google")
    public ResponseEntity<ApiResponseDto<JwtResponse>> authenticateGoogleUser(@Valid @RequestBody GoogleLoginRequest googleRequest) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(googleRequest.getIdToken());
            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();
                String email = payload.getEmail();
                String name = (String) payload.get("name");

                // Check if user exists, if not, create them
                User user = userRepository.findByEmail(email).orElse(null);
                if (user == null) {
                    user = User.builder()
                            .name(name)
                            .email(email)
                            // Generate a random password for Google users as they won't use it
                            .password(encoder.encode(UUID.randomUUID().toString()))
                            .build();
                    userRepository.save(user);
                }

                // Authenticate user in Spring Security to generate JWT
                UserDetailsImpl userDetails = UserDetailsImpl.build(user);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);

                String jwt = jwtUtils.generateJwtToken(authentication);

                return ResponseEntity.ok(new ApiResponseDto<>(true, "Google login successful",
                        new JwtResponse(jwt, userDetails.getId(), userDetails.getUsername(), user.getName())));

            } else {
                return ResponseEntity.badRequest().body(new ApiResponseDto<>(false, "Invalid Google ID token", null));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(new ApiResponseDto<>(false, "Google authentication failed", null));
        }
    }
}
