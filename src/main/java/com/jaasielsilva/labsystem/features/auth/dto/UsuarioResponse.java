package com.jaasielsilva.labsystem.features.auth.dto;

import com.jaasielsilva.labsystem.common.AccessScope;
import com.jaasielsilva.labsystem.features.auth.entity.Perfil;

public record UsuarioResponse(
    Long id,
    String nome,
    String email,
    Perfil perfil,
    Long empresaId,
    String empresaNome,
    AccessScope scope
) {}
