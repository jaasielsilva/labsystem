package com.jaasielsilva.labsystem.features.platform.service;

import com.jaasielsilva.labsystem.features.usuario.dto.UsuarioRequest;
import com.jaasielsilva.labsystem.features.usuario.dto.UsuarioResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PlatformUsuarioService {

    Page<UsuarioResponse> findAll(Pageable pageable, String search);

    UsuarioResponse findById(Long id);

    UsuarioResponse create(UsuarioRequest request);

    UsuarioResponse update(Long id, UsuarioRequest request);

    void delete(Long id);
}
