package com.jaasielsilva.labsystem.features.resultado.dto;

import jakarta.validation.constraints.Size;

public record ResultadoUpdateRequest(
    @Size(max = 500, message = "A observação interna não pode exceder 500 caracteres")
    String observacaoInterna,

    String laudo
) {}
