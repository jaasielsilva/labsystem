package com.jaasielsilva.labsystem.features.usuario.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UsuarioRequest {

    @NotBlank
    private String nome;

    @Email
    @NotBlank
    private String email;

    private String senha;

    private boolean ativo;

    private String perfil;

    private Long empresaId;
}