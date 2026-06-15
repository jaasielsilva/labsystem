package com.jaasielsilva.labsystem.features.auth.controller;

import com.jaasielsilva.labsystem.common.ApiResponse;
import com.jaasielsilva.labsystem.features.auth.dto.LoginRequest;
import com.jaasielsilva.labsystem.features.auth.dto.LoginResponse;
import com.jaasielsilva.labsystem.features.auth.dto.RefreshTokenRequest;
import com.jaasielsilva.labsystem.features.auth.dto.UsuarioResponse;
import com.jaasielsilva.labsystem.features.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok("Login realizado com sucesso", response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        LoginResponse response = authService.refresh(request);
        return ResponseEntity.ok(ApiResponse.ok("Token renovado com sucesso", response));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UsuarioResponse>> me(@AuthenticationPrincipal UserDetails userDetails) {
        UsuarioResponse response = authService.me(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
