package com.jaasielsilva.labsystem.features.platform.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PrimeiroAdminRequest(
    @NotBlank(message = "O nome do administrador é obrigatório")
    @Size(max = 150, message = "O nome não pode exceder 150 caracteres")
    String nome,

    @NotBlank(message = "O e-mail do administrador é obrigatório")
    @Email(message = "O e-mail do administrador deve ser válido")
    @Size(max = 100, message = "O e-mail não pode exceder 100 caracteres")
    String email,

    @NotBlank(message = "A senha do administrador é obrigatória")
    @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres")
    String senha
) {}
