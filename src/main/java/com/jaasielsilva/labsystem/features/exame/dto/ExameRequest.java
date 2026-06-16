package com.jaasielsilva.labsystem.features.exame.dto;

import com.jaasielsilva.labsystem.features.exame.entity.Exame;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record ExameRequest(
    @NotBlank(message = "O código é obrigatório")
    @Size(max = 50, message = "O código não pode exceder 50 caracteres")
    String codigo,

    @NotBlank(message = "O nome do exame é obrigatório")
    @Size(max = 200, message = "O nome não pode exceder 200 caracteres")
    String nome,

    @Size(max = 500, message = "A descrição não pode exceder 500 caracteres")
    String descricao,

    @Size(max = 100, message = "A categoria não pode exceder 100 caracteres")
    String categoria,

    @NotNull(message = "O tipo de amostra é obrigatório")
    Exame.TipoAmostra tipoAmostra,

    @NotNull(message = "O prazo é obrigatório")
    @Min(value = 1, message = "O prazo deve ser de no mínimo 1 dia")
    @Max(value = 365, message = "O prazo não pode exceder 365 dias")
    Integer prazoDias,

    @DecimalMin(value = "0.00", message = "O valor não pode ser negativo")
    BigDecimal valor,

    @NotNull(message = "O status é obrigatório")
    Boolean ativo
) {}
