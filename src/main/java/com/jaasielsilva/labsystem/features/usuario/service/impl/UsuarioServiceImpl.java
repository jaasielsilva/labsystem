package com.jaasielsilva.labsystem.features.usuario.service.impl;

import com.jaasielsilva.labsystem.exception.BusinessException;
import com.jaasielsilva.labsystem.exception.ResourceNotFoundException;
import com.jaasielsilva.labsystem.features.auth.entity.Usuario;
import com.jaasielsilva.labsystem.features.auth.repository.UsuarioRepository;
import com.jaasielsilva.labsystem.features.usuario.dto.UsuarioRequest;
import com.jaasielsilva.labsystem.features.usuario.dto.UsuarioResponse;
import com.jaasielsilva.labsystem.features.usuario.mapper.UsuarioMapper;
import com.jaasielsilva.labsystem.features.usuario.service.UsuarioService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository repository;
    private final UsuarioMapper mapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public Page<UsuarioResponse> findAll(Pageable pageable, String search) {
        if (search == null || search.isBlank()) {
            log.info("Buscando usuários paginados");
            return repository.findAll(pageable).map(mapper::toResponse);
        }
        String term = search.trim();
        log.info("Buscando usuários com filtro: {}", term);
        return repository.searchByNome(term, pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponse findById(Long id) {
        log.info("Buscando usuário por id: {}", id);
        Usuario usuario = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com ID: " + id));
        return mapper.toResponse(usuario);
    }

    @Override
    @Transactional
    public UsuarioResponse create(UsuarioRequest request) {
        log.info("Criando novo usuário");
        if (repository.existsByEmail(request.getEmail())) {
            throw new BusinessException("E-mail já cadastrado.");
        }
        Usuario usuario = mapper.toEntity(request);
        if (request.getSenha() != null && !request.getSenha().isBlank()) {
            usuario.setSenhaHash(passwordEncoder.encode(request.getSenha()));
        }
        return mapper.toResponse(repository.save(usuario));
    }

    @Override
    @Transactional
    public UsuarioResponse update(Long id, UsuarioRequest request) {
        log.info("Atualizando usuário com id: {}", id);
        Usuario usuario = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com ID: " + id));
        if (repository.existsByEmailAndIdNot(request.getEmail(), id)) {
            throw new BusinessException("E-mail já está em uso por outro usuário.");
        }
        mapper.updateEntity(request, usuario);
        if (request.getSenha() != null && !request.getSenha().isBlank()) {
            usuario.setSenhaHash(passwordEncoder.encode(request.getSenha()));
        }
        return mapper.toResponse(repository.save(usuario));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.info("Deletando usuário com id: {}", id);
        Usuario usuario = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com ID: " + id));
        repository.delete(usuario);
    }
}
