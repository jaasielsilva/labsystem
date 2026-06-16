package com.jaasielsilva.labsystem.features.platform.security;

import com.jaasielsilva.labsystem.common.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("platformAccess")
@RequiredArgsConstructor
public class PlatformAccessEvaluator {

    private final TenantContext tenantContext;

    public boolean allow() {
        return tenantContext.isSuperAdmin() && !tenantContext.isImpersonating();
    }
}
