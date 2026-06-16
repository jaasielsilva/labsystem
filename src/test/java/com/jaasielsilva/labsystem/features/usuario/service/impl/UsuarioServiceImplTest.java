package com.jaasielsilva.labsystem.features.usuario.service.impl;

import com.jaasielsilva.labsystem.common.TenantContext;
import com.jaasielsilva.labsystem.exception.BusinessException;
import com.jaasielsilva.labsystem.exception.ResourceNotFoundException;
import com.jaasielsilva.labsystem.features.auth.entity.Perfil;
import com.jaasielsilva.labsystem.features.auth.entity.Usuario;
import com.jaasielsilva.labsystem.features.auth.repository.UsuarioRepository;
import com.jaasielsilva.labsystem.features.empresa.entity.Empresa;
import com.jaasielsilva.labsystem.features.empresa.repository.EmpresaRepository;
import com.jaasielsilva.labsystem.features.usuario.dto.UsuarioRequest;
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

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceImplTest {

    private static final Long EMPRESA_ID = 1L;

    @Mock
    private UsuarioRepository repository;

    @Mock
    private UsuarioMapper mapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TenantContext tenantContext;

    @Mock
    private EmpresaRepository empresaRepository;

    @InjectMocks
    private UsuarioServiceImpl service;

    private Usuario usuario;
    private UsuarioRequest usuarioRequest;
    private UsuarioResponse usuarioResponse;
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
                .nome("Maria Silva")
                .email("maria@test.com")
                .senhaHash("hash")
                .perfil(Perfil.OPERADOR)
                .empresa(empresa)
                .ativo(true)
                .build();

        usuarioRequest = new UsuarioRequest();
        usuarioRequest.setNome("Maria Silva");
        usuarioRequest.setEmail("maria@test.com");
        usuarioRequest.setSenha("senha123");
        usuarioRequest.setPerfil("OPERADOR");
        usuarioRequest.setAtivo(true);

        usuarioResponse = new UsuarioResponse();
        usuarioResponse.setId(1L);
        usuarioResponse.setNome("Maria Silva");
        usuarioResponse.setEmail("maria@test.com");
        usuarioResponse.setPerfil("OPERADOR");
        usuarioResponse.setEmpresaId(EMPRESA_ID);
        usuarioResponse.setEmpresaNome("Laboratório Demo");
        usuarioResponse.setAtivo(true);
    }

    @Test
    void findAll_ShouldReturnPagedUsuariosForEmpresa() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Usuario> page = new PageImpl<>(Collections.singletonList(usuario));

        when(tenantContext.requireTenantEmpresaId()).thenReturn(EMPRESA_ID);
        when(repository.findAllByEmpresaId(EMPRESA_ID, pageable)).thenReturn(page);
        when(mapper.toResponse(any(Usuario.class))).thenReturn(usuarioResponse);

        Page<UsuarioResponse> result = service.findAll(pageable, null);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(repository, times(1)).findAllByEmpresaId(EMPRESA_ID, pageable);
        verify(repository, never()).findAll(any(Pageable.class));
    }

    @Test
    void findAll_WithSearch_ShouldFilterByEmpresa() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Usuario> page = new PageImpl<>(Collections.singletonList(usuario));

        when(tenantContext.requireTenantEmpresaId()).thenReturn(EMPRESA_ID);
        when(repository.searchByNomeAndEmpresaId(EMPRESA_ID, "Maria", pageable)).thenReturn(page);
        when(mapper.toResponse(any(Usuario.class))).thenReturn(usuarioResponse);

        Page<UsuarioResponse> result = service.findAll(pageable, "Maria");

        assertEquals(1, result.getTotalElements());
        verify(repository, times(1)).searchByNomeAndEmpresaId(EMPRESA_ID, "Maria", pageable);
        verify(repository, never()).searchByNome(any(), any());
    }

    @Test
    void findById_WhenExistsInEmpresa_ShouldReturnUsuario() {
        when(tenantContext.requireTenantEmpresaId()).thenReturn(EMPRESA_ID);
        when(repository.findByIdAndEmpresaId(1L, EMPRESA_ID)).thenReturn(Optional.of(usuario));
        when(mapper.toResponse(usuario)).thenReturn(usuarioResponse);

        UsuarioResponse result = service.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(repository, times(1)).findByIdAndEmpresaId(1L, EMPRESA_ID);
        verify(repository, never()).findById(any());
    }

    @Test
    void findById_WhenNotInEmpresa_ShouldThrowResourceNotFoundException() {
        when(tenantContext.requireTenantEmpresaId()).thenReturn(EMPRESA_ID);
        when(repository.findByIdAndEmpresaId(1L, EMPRESA_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findById(1L));
        verify(repository, never()).findById(any());
    }

    @Test
    void create_WhenEmailIsUnique_ShouldCreateUsuarioForEmpresaFromToken() {
        when(tenantContext.requireTenantEmpresaId()).thenReturn(EMPRESA_ID);
        when(repository.existsByEmail(usuarioRequest.getEmail())).thenReturn(false);
        when(empresaRepository.findById(EMPRESA_ID)).thenReturn(Optional.of(empresa));
        when(mapper.toEntity(usuarioRequest)).thenReturn(usuario);
        when(passwordEncoder.encode("senha123")).thenReturn("encoded");
        when(repository.save(usuario)).thenReturn(usuario);
        when(mapper.toResponse(usuario)).thenReturn(usuarioResponse);

        UsuarioResponse result = service.create(usuarioRequest);

        assertNotNull(result);
        verify(repository, times(1)).save(usuario);
        verify(empresaRepository, times(1)).findById(EMPRESA_ID);
    }

    @Test
    void create_WhenEmailExists_ShouldThrowBusinessException() {
        when(tenantContext.requireTenantEmpresaId()).thenReturn(EMPRESA_ID);
        when(repository.existsByEmail(usuarioRequest.getEmail())).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class, () -> service.create(usuarioRequest));
        assertEquals("E-mail já cadastrado.", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void delete_WhenExistsInEmpresa_ShouldDelete() {
        when(tenantContext.requireTenantEmpresaId()).thenReturn(EMPRESA_ID);
        when(repository.findByIdAndEmpresaId(1L, EMPRESA_ID)).thenReturn(Optional.of(usuario));
        doNothing().when(repository).delete(usuario);

        assertDoesNotThrow(() -> service.delete(1L));
        verify(repository, times(1)).delete(usuario);
    }
}
