package com.jaasielsilva.labsystem.features.platform.service.impl;

import com.jaasielsilva.labsystem.common.AccessScope;
import com.jaasielsilva.labsystem.config.JwtService;
import com.jaasielsilva.labsystem.exception.BusinessException;
import com.jaasielsilva.labsystem.features.auth.dto.LoginResponse;
import com.jaasielsilva.labsystem.features.auth.entity.Perfil;
import com.jaasielsilva.labsystem.features.auth.entity.Usuario;
import com.jaasielsilva.labsystem.features.auth.repository.UsuarioRepository;
import com.jaasielsilva.labsystem.features.empresa.entity.Empresa;
import com.jaasielsilva.labsystem.features.empresa.entity.TipoEmpresa;
import com.jaasielsilva.labsystem.features.empresa.repository.EmpresaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlatformImpersonationServiceImplTest {

    private static final Long PLATFORM_EMPRESA_ID = 1L;
    private static final Long LABORATORIO_ID = 2L;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private EmpresaRepository empresaRepository;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private PlatformImpersonationServiceImpl service;

    private Usuario superAdmin;
    private Empresa laboratorio;

    @BeforeEach
    void setUp() {
        Empresa plataforma = Empresa.builder()
                .id(PLATFORM_EMPRESA_ID)
                .nome("Labsystem Plataforma")
                .tipo(TipoEmpresa.PLATAFORMA)
                .ativo(true)
                .build();

        laboratorio = Empresa.builder()
                .id(LABORATORIO_ID)
                .nome("Laboratório Demo")
                .tipo(TipoEmpresa.LABORATORIO)
                .ativo(true)
                .build();

        superAdmin = Usuario.builder()
                .id(10L)
                .email("super@labsystem.local")
                .nome("Super Admin")
                .perfil(Perfil.SUPER_ADMIN)
                .ativo(true)
                .empresa(plataforma)
                .build();
    }

    @Test
    void start_WhenSuperAdminAndLaboratorioValid_ShouldReturnImpersonationTokens() {
        when(usuarioRepository.findByEmail(superAdmin.getEmail())).thenReturn(Optional.of(superAdmin));
        when(empresaRepository.findById(LABORATORIO_ID)).thenReturn(Optional.of(laboratorio));
        when(jwtService.generateImpersonationAccessToken(
                superAdmin.getEmail(),
                Perfil.SUPER_ADMIN,
                PLATFORM_EMPRESA_ID,
                LABORATORIO_ID,
                laboratorio.getNome()
        )).thenReturn("access");
        when(jwtService.generateImpersonationRefreshToken(
                superAdmin.getEmail(),
                Perfil.SUPER_ADMIN,
                PLATFORM_EMPRESA_ID,
                LABORATORIO_ID,
                laboratorio.getNome()
        )).thenReturn("refresh");

        LoginResponse response = service.start(LABORATORIO_ID, superAdmin.getEmail());

        assertEquals("access", response.accessToken());
        assertEquals(AccessScope.TENANT_IMPERSONATION, response.usuario().scope());
        assertEquals(LABORATORIO_ID, response.usuario().actingEmpresaId());
    }

    @Test
    void start_WhenNotSuperAdmin_ShouldThrowBusinessException() {
        Usuario admin = Usuario.builder()
                .email("admin@labsystem.local")
                .perfil(Perfil.ADMIN)
                .ativo(true)
                .empresa(laboratorio)
                .build();

        when(usuarioRepository.findByEmail(admin.getEmail())).thenReturn(Optional.of(admin));

        assertThrows(BusinessException.class, () -> service.start(LABORATORIO_ID, admin.getEmail()));
    }

    @Test
    void exit_WhenSuperAdmin_ShouldReturnPlatformTokens() {
        when(usuarioRepository.findByEmail(superAdmin.getEmail())).thenReturn(Optional.of(superAdmin));
        when(jwtService.generateAccessToken(superAdmin.getEmail(), Perfil.SUPER_ADMIN, PLATFORM_EMPRESA_ID))
                .thenReturn("access");
        when(jwtService.generateRefreshToken(superAdmin.getEmail(), Perfil.SUPER_ADMIN, PLATFORM_EMPRESA_ID))
                .thenReturn("refresh");

        LoginResponse response = service.exit(superAdmin.getEmail());

        assertEquals(AccessScope.PLATFORM, response.usuario().scope());
        assertNull(response.usuario().actingEmpresaId());
    }
}
