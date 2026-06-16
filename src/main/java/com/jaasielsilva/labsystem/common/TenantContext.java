package com.jaasielsilva.labsystem.common;

import com.jaasielsilva.labsystem.exception.BusinessException;
import com.jaasielsilva.labsystem.features.auth.entity.Perfil;
import com.jaasielsilva.labsystem.features.auth.security.UsuarioUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class TenantContext {

    public AccessScope effectiveScope() {
        if (ImpersonationContext.isActive()) {
            return AccessScope.TENANT_IMPERSONATION;
        }
        return AccessScope.fromPerfil(currentPerfilInternal());
    }

    public boolean isSuperAdmin() {
        return currentPerfilInternal() == Perfil.SUPER_ADMIN;
    }

    public boolean isImpersonating() {
        return ImpersonationContext.isActive();
    }

    public Perfil currentPerfil() {
        return currentPerfilInternal();
    }

    public Long requireTenantEmpresaId() {
        if (isImpersonating()) {
            return ImpersonationContext.requireEmpresaId();
        }
        if (isSuperAdmin()) {
            throw new BusinessException("Operação disponível apenas no contexto de um laboratório.");
        }
        return requireEmpresaId();
    }

    public Long requireEmpresaId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException("Usuário não autenticado.");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UsuarioUserDetails userDetails) {
            var empresa = userDetails.getUsuario().getEmpresa();
            if (empresa == null || empresa.getId() == null) {
                throw new BusinessException("Usuário sem empresa vinculada.");
            }
            return empresa.getId();
        }

        throw new BusinessException("Contexto de empresa não disponível.");
    }

    private Perfil currentPerfilInternal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException("Usuário não autenticado.");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UsuarioUserDetails userDetails) {
            return userDetails.getUsuario().getPerfil();
        }

        throw new BusinessException("Contexto de usuário não disponível.");
    }
}
