package com.jaasielsilva.labsystem.features.platform.service.impl;

import com.jaasielsilva.labsystem.exception.BusinessException;
import com.jaasielsilva.labsystem.exception.ResourceNotFoundException;
import com.jaasielsilva.labsystem.features.auth.entity.Perfil;
import com.jaasielsilva.labsystem.features.auth.entity.Usuario;
import com.jaasielsilva.labsystem.features.auth.repository.UsuarioRepository;
import com.jaasielsilva.labsystem.features.cliente.repository.ClienteRepository;
import com.jaasielsilva.labsystem.features.empresa.dto.EmpresaRequest;
import com.jaasielsilva.labsystem.features.empresa.dto.EmpresaResponse;
import com.jaasielsilva.labsystem.features.empresa.entity.Empresa;
import com.jaasielsilva.labsystem.features.empresa.entity.TipoEmpresa;
import com.jaasielsilva.labsystem.features.empresa.mapper.EmpresaMapper;
import com.jaasielsilva.labsystem.features.empresa.repository.EmpresaRepository;
import com.jaasielsilva.labsystem.features.exame.repository.ExameRepository;
import com.jaasielsilva.labsystem.features.platform.dto.LaboratorioOnboardingRequest;
import com.jaasielsilva.labsystem.features.platform.dto.PrimeiroAdminRequest;
import com.jaasielsilva.labsystem.features.usuario.dto.UsuarioResponse;
import com.jaasielsilva.labsystem.features.usuario.mapper.UsuarioMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlatformEmpresaServiceImplTest {

    @Mock
    private EmpresaRepository repository;

    @Mock
    private EmpresaMapper mapper;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ExameRepository exameRepository;

    @Mock
    private UsuarioMapper usuarioMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PlatformEmpresaServiceImpl service;

    private Empresa empresa;
    private EmpresaRequest request;
    private EmpresaResponse response;

    @BeforeEach
    void setUp() {
        empresa = Empresa.builder()
                .id(1L)
                .nome("Laboratório Demo")
                .cnpj("00000000000000")
                .email("contato@labsystem.local")
                .tipo(TipoEmpresa.LABORATORIO)
                .ativo(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        request = new EmpresaRequest(
                "Nova Empresa LTDA",
                "12345678000199",
                "nova@empresa.com",
                "11999998888",
                "Rua A, 10",
                "São Paulo",
                "SP",
                true
        );

        response = new EmpresaResponse(
                1L,
                "Nova Empresa LTDA",
                "12345678000199",
                "nova@empresa.com",
                "11999998888",
                "Rua A, 10",
                "São Paulo",
                "SP",
                true,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    @Test
    void findAll_ShouldReturnPagedLaboratorios() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Empresa> page = new PageImpl<>(Collections.singletonList(empresa));

        when(repository.findAllByTipo(TipoEmpresa.LABORATORIO, pageable)).thenReturn(page);
        when(mapper.toResponse(any(Empresa.class))).thenReturn(response);

        Page<EmpresaResponse> result = service.findAll(pageable, null);

        assertEquals(1, result.getTotalElements());
        verify(repository).findAllByTipo(TipoEmpresa.LABORATORIO, pageable);
    }

    @Test
    void create_WhenCnpjIsUnique_ShouldCreateLaboratorio() {
        when(repository.existsByCnpj(request.cnpj())).thenReturn(false);
        when(mapper.toEntity(request)).thenReturn(empresa);
        when(repository.save(empresa)).thenReturn(empresa);
        when(mapper.toResponse(empresa)).thenReturn(response);

        EmpresaResponse result = service.create(request);

        assertNotNull(result);
        assertEquals(TipoEmpresa.LABORATORIO, empresa.getTipo());
        verify(repository).save(empresa);
    }

    @Test
    void onboard_WhenDataIsValid_ShouldCreateLaboratorioAndAdmin() {
        PrimeiroAdminRequest adminRequest = new PrimeiroAdminRequest(
                "Gestor Lab",
                "gestor@lab.com",
                "senha123"
        );
        LaboratorioOnboardingRequest onboardingRequest = new LaboratorioOnboardingRequest(request, adminRequest);

        Usuario admin = Usuario.builder()
                .id(5L)
                .nome(adminRequest.nome())
                .email(adminRequest.email())
                .perfil(Perfil.ADMIN)
                .ativo(true)
                .empresa(empresa)
                .build();

        UsuarioResponse adminResponse = new UsuarioResponse();
        adminResponse.setId(5L);
        adminResponse.setNome(adminRequest.nome());
        adminResponse.setEmail(adminRequest.email());
        adminResponse.setPerfil(Perfil.ADMIN.name());
        adminResponse.setEmpresaId(empresa.getId());

        when(usuarioRepository.existsByEmail(adminRequest.email())).thenReturn(false);
        when(repository.existsByCnpj(request.cnpj())).thenReturn(false);
        when(mapper.toEntity(request)).thenReturn(empresa);
        when(repository.save(empresa)).thenReturn(empresa);
        when(passwordEncoder.encode(adminRequest.senha())).thenReturn("hash");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(admin);
        when(mapper.toResponse(empresa)).thenReturn(response);
        when(usuarioMapper.toResponse(admin)).thenReturn(adminResponse);

        var result = service.onboard(onboardingRequest);

        assertNotNull(result);
        assertEquals(response, result.laboratorio());
        assertEquals(adminResponse, result.admin());
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    void onboard_WhenAdminEmailExists_ShouldThrowBusinessException() {
        PrimeiroAdminRequest adminRequest = new PrimeiroAdminRequest(
                "Gestor Lab",
                "gestor@lab.com",
                "senha123"
        );
        LaboratorioOnboardingRequest onboardingRequest = new LaboratorioOnboardingRequest(request, adminRequest);

        when(usuarioRepository.existsByEmail(adminRequest.email())).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.onboard(onboardingRequest));
        assertEquals("E-mail do administrador já cadastrado.", ex.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void delete_WhenOnlyLaboratorio_ShouldThrowBusinessException() {
        when(repository.findById(1L)).thenReturn(Optional.of(empresa));
        when(repository.countByTipo(TipoEmpresa.LABORATORIO)).thenReturn(1L);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.delete(1L));
        assertEquals("Não é possível remover o único laboratório do sistema.", ex.getMessage());
        verify(repository, never()).delete(any());
    }
}
