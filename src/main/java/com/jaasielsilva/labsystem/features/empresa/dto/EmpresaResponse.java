package com.jaasielsilva.labsystem.features.empresa.dto;

import java.time.LocalDateTime;

public record EmpresaResponse(
    Long id,
    String nome,
    String cnpj,
    String email,
    String telefone,
    String endereco,
    String cidade,
    String uf,
    boolean ativo,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
