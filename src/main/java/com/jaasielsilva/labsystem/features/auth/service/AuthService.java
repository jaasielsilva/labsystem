package com.jaasielsilva.labsystem.features.auth.service;

import com.jaasielsilva.labsystem.features.auth.dto.LoginRequest;
import com.jaasielsilva.labsystem.features.auth.dto.LoginResponse;
import com.jaasielsilva.labsystem.features.auth.dto.RefreshTokenRequest;
import com.jaasielsilva.labsystem.features.auth.dto.UsuarioResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    LoginResponse refresh(RefreshTokenRequest request);

    UsuarioResponse me(String email);
}
