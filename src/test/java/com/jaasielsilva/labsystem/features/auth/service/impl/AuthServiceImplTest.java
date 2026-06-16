package com.jaasielsilva.labsystem.features.auth.service.impl;

import com.jaasielsilva.labsystem.config.JwtService;
import com.jaasielsilva.labsystem.exception.BusinessException;
import com.jaasielsilva.labsystem.features.auth.dto.LoginRequest;
import com.jaasielsilva.labsystem.features.auth.dto.LoginResponse;
import com.jaasielsilva.labsystem.features.auth.dto.RefreshTokenRequest;
import com.jaasielsilva.labsystem.features.auth.entity.Perfil;
import com.jaasielsilva.labsystem.features.auth.entity.Usuario;
import com.jaasielsilva.labsystem.features.auth.repository.UsuarioRepository;
import com.jaasielsilva.labsystem.features.empresa.entity.Empresa;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    private static final Long EMPRESA_ID = 1L;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;

    private Usuario usuario;
    private LoginRequest loginRequest;
    private Empresa empresa;

    @BeforeEach
    void setUp() {
        empresa = Empresa.builder()
                .id(EMPRESA_ID)
                .nome("Laboratório Demo")
                .cnpj("00000000000000")
                .email("contato@labsystem.local")
                .ativo(true)
                .build();

        usuario = Usuario.builder()
                .id(1L)
                .nome("Admin")
                .email("admin@labsystem.local")
                .senhaHash("hash")
                .perfil(Perfil.ADMIN)
                .ativo(true)
                .empresa(empresa)
                .build();

        loginRequest = new LoginRequest("admin@labsystem.local", "admin123");
    }

    @Test
    void login_WhenCredentialsValid_ShouldReturnTokens() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(usuarioRepository.findByEmail(loginRequest.email())).thenReturn(Optional.of(usuario));
        when(jwtService.generateAccessToken(usuario.getEmail(), usuario.getPerfil(), EMPRESA_ID)).thenReturn("access");
        when(jwtService.generateRefreshToken(usuario.getEmail(), usuario.getPerfil(), EMPRESA_ID)).thenReturn("refresh");

        LoginResponse response = authService.login(loginRequest);

        assertEquals("access", response.accessToken());
        assertEquals("refresh", response.refreshToken());
        assertEquals("admin@labsystem.local", response.usuario().email());
        assertEquals(EMPRESA_ID, response.usuario().empresaId());
        assertEquals("Laboratório Demo", response.usuario().empresaNome());
    }

    @Test
    void login_WhenCredentialsInvalid_ShouldThrowBusinessException() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("invalid"));

        BusinessException ex = assertThrows(BusinessException.class, () -> authService.login(loginRequest));
        assertEquals("E-mail ou senha inválidos.", ex.getMessage());
    }

    @Test
    void login_WhenUserInactive_ShouldThrowBusinessException() {
        usuario.setAtivo(false);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(usuarioRepository.findByEmail(loginRequest.email())).thenReturn(Optional.of(usuario));

        BusinessException ex = assertThrows(BusinessException.class, () -> authService.login(loginRequest));
        assertEquals("Usuário inativo. Contate o administrador.", ex.getMessage());
    }

    @Test
    void refresh_WhenTokenValid_ShouldReturnNewTokens() {
        RefreshTokenRequest request = new RefreshTokenRequest("refresh-token");
        when(jwtService.isRefreshToken("refresh-token")).thenReturn(true);
        when(jwtService.readTokenContext("refresh-token")).thenReturn(
                new com.jaasielsilva.labsystem.common.JwtTokenContext(
                        usuario.getEmail(),
                        usuario.getPerfil(),
                        EMPRESA_ID,
                        com.jaasielsilva.labsystem.common.AccessScope.TENANT,
                        null,
                        null,
                        "refresh"
                )
        );
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        when(jwtService.isTokenValid("refresh-token", usuario.getEmail())).thenReturn(true);
        when(jwtService.generateAccessToken(usuario.getEmail(), usuario.getPerfil(), EMPRESA_ID)).thenReturn("new-access");
        when(jwtService.generateRefreshToken(usuario.getEmail(), usuario.getPerfil(), EMPRESA_ID)).thenReturn("new-refresh");

        LoginResponse response = authService.refresh(request);

        assertEquals("new-access", response.accessToken());
        assertEquals("new-refresh", response.refreshToken());
    }

    @Test
    void refresh_WhenTokenInvalid_ShouldThrowBusinessException() {
        RefreshTokenRequest request = new RefreshTokenRequest("invalid");
        when(jwtService.isRefreshToken("invalid")).thenReturn(false);

        assertThrows(BusinessException.class, () -> authService.refresh(request));
    }
}
