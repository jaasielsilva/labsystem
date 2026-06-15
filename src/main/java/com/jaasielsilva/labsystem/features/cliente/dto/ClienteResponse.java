package com.jaasielsilva.labsystem.features.cliente.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ClienteResponse(
    Long id,
    String nome,
    String cpf,
    String email,
    String telefone,
    LocalDate dataNascimento,
    boolean ativo,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
