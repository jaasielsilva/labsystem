package com.jaasielsilva.labsystem.features.platform.service;

import com.jaasielsilva.labsystem.features.auth.dto.LoginResponse;

public interface PlatformImpersonationService {

    LoginResponse start(Long laboratorioId, String superAdminEmail);

    LoginResponse exit(String superAdminEmail);
}
