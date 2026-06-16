package com.jaasielsilva.labsystem.features.platform.service.impl;

import com.jaasielsilva.labsystem.exception.BusinessException;
import com.jaasielsilva.labsystem.exception.ResourceNotFoundException;
import com.jaasielsilva.labsystem.features.auth.entity.Perfil;
import com.jaasielsilva.labsystem.features.auth.entity.Usuario;
import com.jaasielsilva.labsystem.features.auth.repository.UsuarioRepository;
import com.jaasielsilva.labsystem.features.empresa.entity.Empresa;
import com.jaasielsilva.labsystem.features.empresa.entity.TipoEmpresa;
import com.jaasielsilva.labsystem.features.empresa.repository.EmpresaRepository;
import com.jaasielsilva.labsystem.features.platform.service.PlatformUsuarioService;
import com.jaasielsilva.labsystem.features.usuario.dto.UsuarioRequest;
import com.jaasielsilva.labsystem.features.usuario.dto.UsuarioResponse;
import com.jaasielsilva.labsystem.features.usuario.mapper.UsuarioMapper;
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
public class PlatformUsuarioServiceImpl implements PlatformUsuarioService {

    private final UsuarioRepository repository;
    private final UsuarioMapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final EmpresaRepository empresaRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<UsuarioResponse> findAll(Pageable pageable, String search) {
        if (search == null || search.isBlank()) {
            log.info("Buscando usuários paginados (escopo plataforma)");
            return repository.findAll(pageable).map(mapper::toResponse);
        }

        String term = search.trim();
        log.info("Buscando usuários com filtro (escopo plataforma)");
        return repository.searchByNome(term, pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponse findById(Long id) {
        Usuario usuario = findUsuario(id);
        return mapper.toResponse(usuario);
    }

    @Override
    @Transactional
    public UsuarioResponse create(UsuarioRequest request) {
        validateWritablePerfil(request.getPerfil());
        Empresa empresa = resolveLaboratorio(request.getEmpresaId());
        log.info("Criando usuário global para empresaId={}", empresa.getId());

        if (repository.existsByEmail(request.getEmail())) {
            throw new BusinessException("E-mail já cadastrado.");
        }

        if (request.getSenha() == null || request.getSenha().isBlank()) {
            throw new BusinessException("A senha é obrigatória.");
        }

        Usuario usuario = mapper.toEntity(request);
        usuario.setEmpresa(empresa);
        usuario.setSenhaHash(passwordEncoder.encode(request.getSenha()));
        return mapper.toResponse(repository.save(usuario));
    }

    @Override
    @Transactional
    public UsuarioResponse update(Long id, UsuarioRequest request) {
        Usuario usuario = findUsuario(id);
        validateWritablePerfil(request.getPerfil());
        log.info("Atualizando usuário global id={}", id);

        if (repository.existsByEmailAndIdNot(request.getEmail(), id)) {
            throw new BusinessException("E-mail já está em uso por outro usuário.");
        }

        if (usuario.getPerfil() == Perfil.SUPER_ADMIN) {
            throw new BusinessException("Contas de super administrador não podem ser alteradas por esta tela.");
        }

        mapper.updateEntity(request, usuario);

        if (request.getEmpresaId() != null) {
            usuario.setEmpresa(resolveLaboratorio(request.getEmpresaId()));
        }

        if (request.getSenha() != null && !request.getSenha().isBlank()) {
            usuario.setSenhaHash(passwordEncoder.encode(request.getSenha()));
        }

        return mapper.toResponse(repository.save(usuario));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Usuario usuario = findUsuario(id);
        log.info("Deletando usuário global id={}", id);

        if (usuario.getPerfil() == Perfil.SUPER_ADMIN) {
            if (repository.countByPerfil(Perfil.SUPER_ADMIN) <= 1) {
                throw new BusinessException("Não é possível remover o único super administrador da plataforma.");
            }
            throw new BusinessException("Contas de super administrador não podem ser removidas por esta tela.");
        }

        repository.delete(usuario);
    }

    private Usuario findUsuario(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com ID: " + id));
    }

    private Empresa resolveLaboratorio(Long empresaId) {
        if (empresaId == null) {
            throw new BusinessException("Selecione o laboratório do usuário.");
        }

        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new BusinessException("Laboratório não encontrado."));

        if (empresa.getTipo() != TipoEmpresa.LABORATORIO) {
            throw new BusinessException("Selecione um laboratório válido.");
        }

        return empresa;
    }

    private void validateWritablePerfil(String perfil) {
        if (perfil == null || perfil.isBlank()) {
            throw new BusinessException("Selecione um perfil.");
        }

        if (Perfil.SUPER_ADMIN.name().equals(perfil)) {
            throw new BusinessException("O perfil SUPER_ADMIN não pode ser atribuído por esta tela.");
        }
    }
}
