package com.jaasielsilva.labsystem.features.audit.dto;

import java.time.LocalDateTime;

public record AuditResponse(
        Long id,
        String usuarioEmail,
        String perfil,
        String action,
        String entidade,
        Long entidadeId,
        String detalhes,
        String scope,
        LocalDateTime createdAt
) {}