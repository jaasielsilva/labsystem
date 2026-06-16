package com.jaasielsilva.labsystem.features.auth.service.impl;

import com.jaasielsilva.labsystem.common.AccessScope;
import com.jaasielsilva.labsystem.common.ImpersonationContext;
import com.jaasielsilva.labsystem.common.ImpersonationState;
import com.jaasielsilva.labsystem.common.JwtTokenContext;
import com.jaasielsilva.labsystem.config.JwtService;
import com.jaasielsilva.labsystem.exception.BusinessException;
import com.jaasielsilva.labsystem.features.auth.dto.LoginRequest;
import com.jaasielsilva.labsystem.features.auth.dto.LoginResponse;
import com.jaasielsilva.labsystem.features.auth.dto.RefreshTokenRequest;
import com.jaasielsilva.labsystem.features.auth.dto.UsuarioResponse;
import com.jaasielsilva.labsystem.features.auth.entity.Usuario;
import com.jaasielsilva.labsystem.features.auth.repository.UsuarioRepository;
import com.jaasielsilva.labsystem.features.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        log.info("Tentativa de login para email={}", request.email());

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.senha())
            );
        } catch (AuthenticationException ex) {
            log.warn("Falha de autenticação para email={}", request.email());
            throw new BusinessException("E-mail ou senha inválidos.");
        }

        Usuario usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException("E-mail ou senha inválidos."));

        if (!usuario.isAtivo()) {
            log.warn("Login bloqueado para usuário inativo id={}", usuario.getId());
            throw new BusinessException("Usuário inativo. Contate o administrador.");
        }

        return buildLoginResponse(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponse refresh(RefreshTokenRequest request) {
        String refreshToken = request.refreshToken();

        if (!jwtService.isRefreshToken(refreshToken)) {
            throw new BusinessException("Refresh token inválido.");
        }

        JwtTokenContext tokenContext = jwtService.readTokenContext(refreshToken);
        Usuario usuario = usuarioRepository.findByEmail(tokenContext.email())
                .orElseThrow(() -> new BusinessException("Refresh token inválido."));

        if (!usuario.isAtivo() || !jwtService.isTokenValid(refreshToken, tokenContext.email())) {
            throw new BusinessException("Refresh token inválido ou expirado.");
        }

        log.info("Refresh token renovado para usuarioId={}", usuario.getId());

        if (tokenContext.scope() == AccessScope.TENANT_IMPERSONATION) {
            return buildImpersonationLoginResponse(
                    usuario,
                    tokenContext.actingEmpresaId(),
                    tokenContext.actingEmpresaNome()
            );
        }

        return buildLoginResponse(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponse me(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado."));

        ImpersonationState impersonation = ImpersonationContext.get();
        if (impersonation != null) {
            return toImpersonationResponse(usuario, impersonation);
        }

        return toResponse(usuario);
    }

    private LoginResponse buildLoginResponse(Usuario usuario) {
        Long empresaId = usuario.getEmpresa().getId();
        String accessToken = jwtService.generateAccessToken(usuario.getEmail(), usuario.getPerfil(), empresaId);
        String refreshToken = jwtService.generateRefreshToken(usuario.getEmail(), usuario.getPerfil(), empresaId);
        return new LoginResponse(accessToken, refreshToken, toResponse(usuario));
    }

    private LoginResponse buildImpersonationLoginResponse(
            Usuario usuario,
            Long actingEmpresaId,
            String actingEmpresaNome) {
        Long platformEmpresaId = usuario.getEmpresa().getId();
        String accessToken = jwtService.generateImpersonationAccessToken(
                usuario.getEmail(),
                usuario.getPerfil(),
                platformEmpresaId,
                actingEmpresaId,
                actingEmpresaNome
        );
        String refreshToken = jwtService.generateImpersonationRefreshToken(
                usuario.getEmail(),
                usuario.getPerfil(),
                platformEmpresaId,
                actingEmpresaId,
                actingEmpresaNome
        );
        return new LoginResponse(
                accessToken,
                refreshToken,
                toImpersonationResponse(usuario, ImpersonationState.of(actingEmpresaId, actingEmpresaNome))
        );
    }

    private UsuarioResponse toResponse(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getPerfil(),
                usuario.getEmpresa().getId(),
                usuario.getEmpresa().getNome(),
                AccessScope.fromPerfil(usuario.getPerfil()),
                null,
                null
        );
    }

    private UsuarioResponse toImpersonationResponse(Usuario usuario, ImpersonationState impersonation) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getPerfil(),
                usuario.getEmpresa().getId(),
                usuario.getEmpresa().getNome(),
                AccessScope.TENANT_IMPERSONATION,
                impersonation.empresaId(),
                impersonation.empresaNome()
        );
    }
}
