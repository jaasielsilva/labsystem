package com.jaasielsilva.labsystem.features.tenant.security;

import com.jaasielsilva.labsystem.common.ImpersonationContext;
import com.jaasielsilva.labsystem.common.TenantContext;
import com.jaasielsilva.labsystem.features.auth.entity.Perfil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TenantAccessEvaluatorTest {

    @Mock
    private TenantContext tenantContext;

    @InjectMocks
    private TenantAccessEvaluator evaluator;

    @AfterEach
    void tearDown() {
        ImpersonationContext.clear();
    }

    @Test
    void read_devePermitirSuperAdminEmImpersonacao() {
        when(tenantContext.isImpersonating()).thenReturn(true);
        when(tenantContext.isSuperAdmin()).thenReturn(true);

        assertThat(evaluator.read()).isTrue();
    }

    @Test
    void read_deveNegarSuperAdminSemImpersonacao() {
        when(tenantContext.isImpersonating()).thenReturn(false);
        when(tenantContext.currentPerfil()).thenReturn(Perfil.SUPER_ADMIN);

        assertThat(evaluator.read()).isFalse();
    }

    @Test
    void read_devePermitirOperadorDoTenant() {
        when(tenantContext.isImpersonating()).thenReturn(false);
        when(tenantContext.currentPerfil()).thenReturn(Perfil.OPERADOR);

        assertThat(evaluator.read()).isTrue();
    }

    @Test
    void admin_devePermitirSuperAdminEmImpersonacao() {
        when(tenantContext.isImpersonating()).thenReturn(true);
        when(tenantContext.isSuperAdmin()).thenReturn(true);

        assertThat(evaluator.admin()).isTrue();
    }

    @Test
    void admin_devePermitirAdminDoTenant() {
        when(tenantContext.isImpersonating()).thenReturn(false);
        when(tenantContext.currentPerfil()).thenReturn(Perfil.ADMIN);

        assertThat(evaluator.admin()).isTrue();
    }
}
