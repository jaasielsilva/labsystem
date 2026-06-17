package com.jaasielsilva.labsystem.features.audit.service.impl;

import com.jaasielsilva.labsystem.common.AccessScope;
import com.jaasielsilva.labsystem.common.ImpersonationContext;
import com.jaasielsilva.labsystem.common.TenantContext;
import com.jaasielsilva.labsystem.exception.ResourceNotFoundException;
import com.jaasielsilva.labsystem.features.audit.dto.AuditResponse;
import com.jaasielsilva.labsystem.features.audit.entity.AuditLog;
import com.jaasielsilva.labsystem.features.audit.mapper.AuditMapper;
import com.jaasielsilva.labsystem.features.audit.repository.AuditRepository;
import com.jaasielsilva.labsystem.features.audit.service.AuditService;
import com.jaasielsilva.labsystem.features.auth.entity.Usuario;
import com.jaasielsilva.labsystem.features.auth.security.UsuarioUserDetails;
import com.jaasielsilva.labsystem.features.empresa.entity.Empresa;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final AuditRepository repository;
    private final AuditMapper mapper;
    private final TenantContext tenantContext;

    @Override
    @Transactional(readOnly = true)
    public Page<AuditResponse> findAll(Pageable pageable, String search) {
        Long empresaId = tenantContext.requireTenantEmpresaId();
        log.info("Buscando auditoria empresaId={}", empresaId);

        if (search == null || search.isBlank()) {
            return repository.findAllByEmpresaId(empresaId, pageable).map(mapper::toResponse);
        }

        String term = search.trim();
        return repository.searchByTermAndEmpresaId(empresaId, term, pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public AuditResponse findById(Long id) {
        Long empresaId = tenantContext.requireTenantEmpresaId();
        log.info("Buscando log auditoria id={} empresaId={}", id, empresaId);

        return mapper.toResponse(findAuditLog(id, empresaId));
    }

    @Override
    @Transactional
    public void log(String action, String entidade, Long entidadeId, String detalhes) {
        try {
            Usuario usuario = resolveCurrentUsuario();
            if (usuario == null) {
                log.warn("Auditoria ignorada: usuário não autenticado");
                return;
            }

            AccessScope scope = tenantContext.effectiveScope();
            Long empresaId = resolveEmpresaId(scope, usuario);
            if (empresaId == null) {
                log.warn("Auditoria ignorada: empresa não resolvida para action={}", action);
                return;
            }

            Empresa empresaVinculo = usuario.getEmpresa();
            Long plataformaEmpresaId = empresaVinculo != null ? empresaVinculo.getId() : null;
            Long actingEmpresaId = scope == AccessScope.TENANT_IMPERSONATION
                    ? ImpersonationContext.requireEmpresaId()
                    : null;

            AuditLog auditLog = AuditLog.builder()
                    .usuarioId(usuario.getId())
                    .usuarioEmail(usuario.getEmail())
                    .perfil(usuario.getPerfil().name())
                    .empresaId(empresaId)
                    .action(action)
                    .entidade(entidade)
                    .entidadeId(entidadeId)
                    .detalhes(truncate(detalhes, 2000))
                    .scope(scope.name())
                    .plataformaEmpresaId(plataformaEmpresaId)
                    .actingEmpresaId(actingEmpresaId)
                    .createdAt(LocalDateTime.now())
                    .build();

            repository.save(auditLog);
        } catch (Exception e) {
            log.error("Erro ao registrar auditoria: {}", e.getMessage(), e);
        }
    }

    private AuditLog findAuditLog(Long id, Long empresaId) {
        return repository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Log não encontrado com ID: " + id));
    }

    private Usuario resolveCurrentUsuario() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UsuarioUserDetails userDetails) {
            return userDetails.getUsuario();
        }
        return null;
    }

    private Long resolveEmpresaId(AccessScope scope, Usuario usuario) {
        if (scope == AccessScope.TENANT_IMPERSONATION) {
            return ImpersonationContext.requireEmpresaId();
        }

        Empresa empresa = usuario.getEmpresa();
        return empresa != null ? empresa.getId() : null;
    }

    private String truncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.length() <= maxLength) {
            return trimmed.isEmpty() ? null : trimmed;
        }
        return trimmed.substring(0, maxLength);
    }
}
