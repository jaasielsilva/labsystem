package com.jaasielsilva.labsystem.features.empresa.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record EmpresaRequest(
    @NotBlank(message = "A razão social é obrigatória")
    @Size(max = 150, message = "A razão social não pode exceder 150 caracteres")
    String nome,

    @NotBlank(message = "O CNPJ é obrigatório")
    @Pattern(regexp = "\\d{14}", message = "O CNPJ deve conter exatamente 14 dígitos numéricos")
    String cnpj,

    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "O e-mail deve ser válido")
    @Size(max = 100, message = "O e-mail não pode exceder 100 caracteres")
    String email,

    @Size(max = 20, message = "O telefone não pode exceder 20 caracteres")
    String telefone,

    @Size(max = 200, message = "O endereço não pode exceder 200 caracteres")
    String endereco,

    @Size(max = 100, message = "A cidade não pode exceder 100 caracteres")
    String cidade,

    @Size(max = 2, message = "A UF deve ter no máximo 2 caracteres")
    String uf,

    @NotNull(message = "O status é obrigatório")
    Boolean ativo
) {}
