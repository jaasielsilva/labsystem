package com.jaasielsilva.labsystem.features.usuario.dto;

import lombok.Data;

@Data
public class UsuarioResponse {

    private Long id;
    private String nome;
    private String email;
    private boolean ativo;
    private String perfil;
}