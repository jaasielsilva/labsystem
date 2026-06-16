package com.jaasielsilva.labsystem.features.usuario.mapper;

import com.jaasielsilva.labsystem.features.auth.entity.Perfil;
import com.jaasielsilva.labsystem.features.auth.entity.Usuario;
import com.jaasielsilva.labsystem.features.usuario.dto.UsuarioRequest;
import com.jaasielsilva.labsystem.features.usuario.dto.UsuarioResponse;
import org.springframework.stereotype.Component;

@Component
public class UsuarioMapper {

    public UsuarioResponse toResponse(Usuario usuario) {
        UsuarioResponse response = new UsuarioResponse();
        response.setId(usuario.getId());
        response.setNome(usuario.getNome());
        response.setEmail(usuario.getEmail());
        response.setAtivo(usuario.isAtivo());
        response.setPerfil(usuario.getPerfil() != null ? usuario.getPerfil().name() : null);

        if (usuario.getEmpresa() != null) {
            response.setEmpresaId(usuario.getEmpresa().getId());
            response.setEmpresaNome(usuario.getEmpresa().getNome());
        }

        return response;
    }

    public Usuario toEntity(UsuarioRequest request) {
        return Usuario.builder()
                .nome(request.getNome())
                .email(request.getEmail())
                .perfil(request.getPerfil() != null ? Perfil.valueOf(request.getPerfil()) : null)
                .ativo(request.isAtivo())
                .build();
    }

    public void updateEntity(UsuarioRequest request, Usuario usuario) {
        usuario.setNome(request.getNome());
        usuario.setEmail(request.getEmail());
        usuario.setAtivo(request.isAtivo());
        if (request.getPerfil() != null) {
            usuario.setPerfil(Perfil.valueOf(request.getPerfil()));
        }
    }
}
