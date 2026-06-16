package com.jaasielsilva.labsystem.common;

import com.jaasielsilva.labsystem.features.auth.entity.Perfil;

public enum AccessScope {
    PLATFORM,
    TENANT;

    public static AccessScope fromPerfil(Perfil perfil) {
        return perfil == Perfil.SUPER_ADMIN ? PLATFORM : TENANT;
    }
}
