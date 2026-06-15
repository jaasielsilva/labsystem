package com.jaasielsilva.labsystem.features.auth.dto;

public record LoginResponse(
    String accessToken,
    String refreshToken,
    UsuarioResponse usuario
) {}
