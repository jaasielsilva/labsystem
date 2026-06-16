package com.jaasielsilva.labsystem.features.platform.service.impl;

import com.jaasielsilva.labsystem.common.AccessScope;
import com.jaasielsilva.labsystem.config.JwtService;
import com.jaasielsilva.labsystem.exception.BusinessException;
import com.jaasielsilva.labsystem.exception.ResourceNotFoundException;
import com.jaasielsilva.labsystem.features.auth.dto.LoginResponse;
import com.jaasielsilva.labsystem.features.auth.dto.UsuarioResponse;
import com.jaasielsilva.labsystem.features.auth.entity.Perfil;
import com.jaasielsilva.labsystem.features.auth.entity.Usuario;
import com.jaasielsilva.labsystem.features.auth.repository.UsuarioRepository;
import com.jaasielsilva.labsystem.features.empresa.entity.Empresa;
import com.jaasielsilva.labsystem.features.empresa.entity.TipoEmpresa;
import com.jaasielsilva.labsystem.features.empresa.repository.EmpresaRepository;
import com.jaasielsilva.labsystem.features.platform.service.PlatformImpersonationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlatformImpersonationServiceImpl implements PlatformImpersonationService {

    private final UsuarioRepository usuarioRepository;
    private final EmpresaRepository empresaRepository;
    private final JwtService jwtService;

    @Override
    @Transactional(readOnly = true)
    public LoginResponse start(Long laboratorioId, String superAdminEmail) {
        Usuario usuario = requireSuperAdmin(superAdminEmail);
        Empresa laboratorio = requireLaboratorio(laboratorioId);

        log.info("SUPER_ADMIN id={} iniciando impersonação do laboratório id={}",
                usuario.getId(), laboratorio.getId());

        Long platformEmpresaId = usuario.getEmpresa().getId();
        String accessToken = jwtService.generateImpersonationAccessToken(
                usuario.getEmail(),
                usuario.getPerfil(),
                platformEmpresaId,
                laboratorio.getId(),
                laboratorio.getNome()
        );
        String refreshToken = jwtService.generateImpersonationRefreshToken(
                usuario.getEmail(),
                usuario.getPerfil(),
                platformEmpresaId,
                laboratorio.getId(),
                laboratorio.getNome()
        );

        return new LoginResponse(accessToken, refreshToken, toImpersonationResponse(usuario, laboratorio));
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponse exit(String superAdminEmail) {
        Usuario usuario = requireSuperAdmin(superAdminEmail);
        log.info("SUPER_ADMIN id={} encerrando impersonação", usuario.getId());

        Long platformEmpresaId = usuario.getEmpresa().getId();
        String accessToken = jwtService.generateAccessToken(
                usuario.getEmail(),
                usuario.getPerfil(),
                platformEmpresaId
        );
        String refreshToken = jwtService.generateRefreshToken(
                usuario.getEmail(),
                usuario.getPerfil(),
                platformEmpresaId
        );

        return new LoginResponse(accessToken, refreshToken, toPlatformResponse(usuario));
    }

    private Usuario requireSuperAdmin(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado."));

        if (usuario.getPerfil() != Perfil.SUPER_ADMIN) {
            throw new BusinessException("Apenas super administradores podem entrar em um laboratório.");
        }

        if (!usuario.isAtivo()) {
            throw new BusinessException("Usuário inativo.");
        }

        return usuario;
    }

    private Empresa requireLaboratorio(Long laboratorioId) {
        Empresa laboratorio = empresaRepository.findById(laboratorioId)
                .orElseThrow(() -> new ResourceNotFoundException("Laboratório não encontrado com ID: " + laboratorioId));

        if (laboratorio.getTipo() != TipoEmpresa.LABORATORIO) {
            throw new BusinessException("Selecione um laboratório válido.");
        }

        if (!laboratorio.isAtivo()) {
            throw new BusinessException("Não é possível entrar em um laboratório inativo.");
        }

        return laboratorio;
    }

    private UsuarioResponse toImpersonationResponse(Usuario usuario, Empresa laboratorio) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getPerfil(),
                usuario.getEmpresa().getId(),
                usuario.getEmpresa().getNome(),
                AccessScope.TENANT_IMPERSONATION,
                laboratorio.getId(),
                laboratorio.getNome()
        );
    }

    private UsuarioResponse toPlatformResponse(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getPerfil(),
                usuario.getEmpresa().getId(),
                usuario.getEmpresa().getNome(),
                AccessScope.PLATFORM,
                null,
                null
        );
    }
}
