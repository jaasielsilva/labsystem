package com.jaasielsilva.labsystem.features.tenant.security;

import com.jaasielsilva.labsystem.common.TenantContext;
import com.jaasielsilva.labsystem.features.auth.entity.Perfil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component("tenantAccess")
@RequiredArgsConstructor
public class TenantAccessEvaluator {

    private final TenantContext tenantContext;

    public boolean read() {
        if (tenantContext.isImpersonating()) {
            return tenantContext.isSuperAdmin();
        }
        return hasPerfil(Perfil.ADMIN, Perfil.OPERADOR, Perfil.VISUALIZADOR);
    }

    public boolean write() {
        if (tenantContext.isImpersonating()) {
            return tenantContext.isSuperAdmin();
        }
        return hasPerfil(Perfil.ADMIN, Perfil.OPERADOR);
    }

    public boolean admin() {
        if (tenantContext.isImpersonating()) {
            return tenantContext.isSuperAdmin();
        }
        return hasPerfil(Perfil.ADMIN);
    }

    private boolean hasPerfil(Perfil... perfis) {
        Perfil current = tenantContext.currentPerfil();
        if (current == Perfil.SUPER_ADMIN) {
            return false;
        }
        return Arrays.asList(perfis).contains(current);
    }
}
