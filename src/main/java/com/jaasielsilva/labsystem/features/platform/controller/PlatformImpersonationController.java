package com.jaasielsilva.labsystem.features.platform.controller;

import com.jaasielsilva.labsystem.common.ApiResponse;
import com.jaasielsilva.labsystem.features.auth.dto.LoginResponse;
import com.jaasielsilva.labsystem.features.platform.service.PlatformImpersonationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/platform/impersonate")
@RequiredArgsConstructor
public class PlatformImpersonationController {

    private final PlatformImpersonationService service;

    @PostMapping("/{laboratorioId}")
    @PreAuthorize("@platformAccess.allow()")
    public ResponseEntity<ApiResponse<LoginResponse>> start(
            @PathVariable Long laboratorioId,
            @AuthenticationPrincipal UserDetails userDetails) {
        LoginResponse response = service.start(laboratorioId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Modo suporte iniciado", response));
    }

    @PostMapping("/exit")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<LoginResponse>> exit(@AuthenticationPrincipal UserDetails userDetails) {
        LoginResponse response = service.exit(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Modo suporte encerrado", response));
    }
}
