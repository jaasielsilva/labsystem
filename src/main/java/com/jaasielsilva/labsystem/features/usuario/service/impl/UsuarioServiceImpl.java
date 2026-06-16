package com.jaasielsilva.labsystem.features.usuario.service.impl;

import com.jaasielsilva.labsystem.common.TenantContext;
import com.jaasielsilva.labsystem.exception.BusinessException;
import com.jaasielsilva.labsystem.exception.ResourceNotFoundException;
import com.jaasielsilva.labsystem.features.auth.entity.Usuario;
import com.jaasielsilva.labsystem.features.auth.repository.UsuarioRepository;
import com.jaasielsilva.labsystem.features.empresa.entity.Empresa;
import com.jaasielsilva.labsystem.features.empresa.repository.EmpresaRepository;
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
    private final TenantContext tenantContext;
    private final EmpresaRepository empresaRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<UsuarioResponse> findAll(Pageable pageable, String search) {
        Long empresaId = tenantContext.requireTenantEmpresaId();

        if (search == null || search.isBlank()) {
            log.info("Buscando usuários paginados para empresaId={}", empresaId);
            return repository.findAllByEmpresaId(empresaId, pageable).map(mapper::toResponse);
        }

        String term = search.trim();
        log.info("Buscando usuários com filtro para empresaId={}", empresaId);
        return repository.searchByNomeAndEmpresaId(empresaId, term, pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponse findById(Long id) {
        Usuario usuario = findUsuarioForCurrentUser(id);
        return mapper.toResponse(usuario);
    }

    @Override
    @Transactional
    public UsuarioResponse create(UsuarioRequest request) {
        Long empresaId = tenantContext.requireTenantEmpresaId();
        log.info("Criando novo usuário para empresaId={}", empresaId);

        if (repository.existsByEmail(request.getEmail())) {
            throw new BusinessException("E-mail já cadastrado.");
        }

        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new BusinessException("Empresa não encontrada."));

        Usuario usuario = mapper.toEntity(request);
        usuario.setEmpresa(empresa);

        if (request.getSenha() == null || request.getSenha().isBlank()) {
            throw new BusinessException("A senha é obrigatória.");
        }

        usuario.setSenhaHash(passwordEncoder.encode(request.getSenha()));
        return mapper.toResponse(repository.save(usuario));
    }

    @Override
    @Transactional
    public UsuarioResponse update(Long id, UsuarioRequest request) {
        Usuario usuario = findUsuarioForCurrentUser(id);
        log.info("Atualizando usuário id={}", id);

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
        Usuario usuario = findUsuarioForCurrentUser(id);
        log.info("Deletando usuário id={}", id);
        repository.delete(usuario);
    }

    private Usuario findUsuarioForCurrentUser(Long id) {
        Long empresaId = tenantContext.requireTenantEmpresaId();
        return repository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com ID: " + id));
    }
}
