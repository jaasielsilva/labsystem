package com.jaasielsilva.labsystem.common;

import com.jaasielsilva.labsystem.features.auth.entity.Perfil;

public record JwtTokenContext(
        String email,
        Perfil perfil,
        Long empresaId,
        AccessScope scope,
        Long actingEmpresaId,
        String actingEmpresaNome,
        String type
) {
}
