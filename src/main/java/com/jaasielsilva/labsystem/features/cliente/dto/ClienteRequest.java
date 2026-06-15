package com.jaasielsilva.labsystem.features.cliente.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record ClienteRequest(
    @NotBlank(message = "O nome é obrigatório")
    @Size(max = 150, message = "O nome não pode exceder 150 caracteres")
    String nome,

    @NotBlank(message = "O CPF é obrigatório")
    @Pattern(regexp = "\\d{11}", message = "O CPF deve conter exatamente 11 dígitos numéricos")
    String cpf,

    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "O e-mail deve ser válido")
    @Size(max = 100, message = "O e-mail não pode exceder 100 caracteres")
    String email,

    @Size(max = 20, message = "O telefone não pode exceder 20 caracteres")
    String telefone,

    @NotNull(message = "A data de nascimento é obrigatória")
    LocalDate dataNascimento
) {}
