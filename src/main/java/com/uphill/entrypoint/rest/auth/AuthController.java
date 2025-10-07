package com.uphill.entrypoint.rest.auth;

import com.uphill.core.application.service.auth.AuthService;
import com.uphill.core.domain.Admin;
import com.uphill.entrypoint.rest.common.response.ApiResponse;
import com.uphill.entrypoint.rest.auth.dto.LoginRequest;
import com.uphill.entrypoint.rest.auth.dto.LoginResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody final LoginRequest request) {
        return authService.authenticate(request.getEmail(), request.getPassword())
                .map(admin -> {
                    final String token = authService.generateToken(admin);
                    return createLoginResponse(token, admin);
                })
                .map(loginResponse -> ResponseEntity.ok(ApiResponse.success(loginResponse)))
                .orElse(ResponseEntity.badRequest()
                        .body(ApiResponse.failure("INVALID_CREDENTIALS", "Invalid email or password")));
    }
    
    private LoginResponse createLoginResponse(final String token, final Admin admin) {
        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(3600L)
                .user(LoginResponse.UserInfo.builder()
                        .id(admin.getId())
                        .name(admin.getName())
                        .email(admin.getEmail())
                        .role("ADMIN")
                        .build())
                .build();
    }
    
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout() {
        // In a real application, you might want to blacklist the token
        return ResponseEntity.ok(ApiResponse.success("Logout successful"));
    }
}
