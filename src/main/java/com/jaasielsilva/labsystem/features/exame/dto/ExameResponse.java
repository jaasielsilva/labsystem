package com.jaasielsilva.labsystem.features.exame.dto;

import com.jaasielsilva.labsystem.features.exame.entity.Exame;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ExameResponse(
    Long id,
    String codigo,
    String nome,
    String descricao,
    String categoria,
    Exame.TipoAmostra tipoAmostra,
    Integer prazoDias,
    BigDecimal valor,
    boolean ativo,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
