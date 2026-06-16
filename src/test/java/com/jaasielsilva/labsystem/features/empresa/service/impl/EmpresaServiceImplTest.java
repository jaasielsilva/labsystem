package com.jaasielsilva.labsystem.features.empresa.service.impl;

import com.jaasielsilva.labsystem.exception.BusinessException;
import com.jaasielsilva.labsystem.exception.ResourceNotFoundException;
import com.jaasielsilva.labsystem.features.auth.repository.UsuarioRepository;
import com.jaasielsilva.labsystem.features.cliente.repository.ClienteRepository;
import com.jaasielsilva.labsystem.features.empresa.dto.EmpresaRequest;
import com.jaasielsilva.labsystem.features.empresa.dto.EmpresaResponse;
import com.jaasielsilva.labsystem.features.empresa.entity.Empresa;
import com.jaasielsilva.labsystem.features.empresa.mapper.EmpresaMapper;
import com.jaasielsilva.labsystem.features.empresa.repository.EmpresaRepository;
import com.jaasielsilva.labsystem.features.exame.repository.ExameRepository;
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

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmpresaServiceImplTest {

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

    @InjectMocks
    private EmpresaServiceImpl service;

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
    void findAll_ShouldReturnPagedEmpresas() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Empresa> page = new PageImpl<>(Collections.singletonList(empresa));

        when(repository.findAll(pageable)).thenReturn(page);
        when(mapper.toResponse(any(Empresa.class))).thenReturn(response);

        Page<EmpresaResponse> result = service.findAll(pageable, null);

        assertEquals(1, result.getTotalElements());
        verify(repository).findAll(pageable);
    }

    @Test
    void findAll_WithSearch_ShouldUseRepositorySearch() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Empresa> page = new PageImpl<>(Collections.singletonList(empresa));

        when(repository.searchByTerm("Demo", "", pageable)).thenReturn(page);
        when(mapper.toResponse(any(Empresa.class))).thenReturn(response);

        Page<EmpresaResponse> result = service.findAll(pageable, "Demo");

        assertEquals(1, result.getTotalElements());
        verify(repository).searchByTerm("Demo", "", pageable);
    }

    @Test
    void findById_WhenExists_ShouldReturnEmpresa() {
        when(repository.findById(1L)).thenReturn(Optional.of(empresa));
        when(mapper.toResponse(empresa)).thenReturn(response);

        EmpresaResponse result = service.findById(1L);

        assertEquals(response.nome(), result.nome());
    }

    @Test
    void findById_WhenNotExists_ShouldThrowResourceNotFoundException() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findById(1L));
    }

    @Test
    void create_WhenCnpjIsUnique_ShouldCreateEmpresa() {
        when(repository.existsByCnpj(request.cnpj())).thenReturn(false);
        when(mapper.toEntity(request)).thenReturn(empresa);
        when(repository.save(empresa)).thenReturn(empresa);
        when(mapper.toResponse(empresa)).thenReturn(response);

        EmpresaResponse result = service.create(request);

        assertNotNull(result);
        verify(repository).save(empresa);
    }

    @Test
    void create_WhenCnpjExists_ShouldThrowBusinessException() {
        when(repository.existsByCnpj(request.cnpj())).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.create(request));
        assertEquals("CNPJ já cadastrado no sistema.", ex.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void update_WhenExistsAndCnpjIsUnique_ShouldUpdateEmpresa() {
        when(repository.findById(1L)).thenReturn(Optional.of(empresa));
        when(repository.existsByCnpjAndIdNot(request.cnpj(), 1L)).thenReturn(false);
        when(repository.save(empresa)).thenReturn(empresa);
        when(mapper.toResponse(empresa)).thenReturn(response);

        EmpresaResponse result = service.update(1L, request);

        assertNotNull(result);
        verify(mapper).updateEntity(request, empresa);
    }

    @Test
    void delete_WhenHasLinkedUsers_ShouldThrowBusinessException() {
        when(repository.findById(1L)).thenReturn(Optional.of(empresa));
        when(repository.count()).thenReturn(2L);
        when(usuarioRepository.countByEmpresa_Id(1L)).thenReturn(1L);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.delete(1L));
        assertEquals("Não é possível remover empresa com usuários vinculados.", ex.getMessage());
        verify(repository, never()).delete(any());
    }

    @Test
    void delete_WhenOnlyEmpresa_ShouldThrowBusinessException() {
        when(repository.findById(1L)).thenReturn(Optional.of(empresa));
        when(repository.count()).thenReturn(1L);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.delete(1L));
        assertEquals("Não é possível remover a única empresa do sistema.", ex.getMessage());
        verify(repository, never()).delete(any());
    }

    @Test
    void delete_WhenSafe_ShouldDeleteEmpresa() {
        when(repository.findById(1L)).thenReturn(Optional.of(empresa));
        when(repository.count()).thenReturn(2L);
        when(usuarioRepository.countByEmpresa_Id(1L)).thenReturn(0L);
        when(clienteRepository.countByEmpresa_Id(1L)).thenReturn(0L);
        when(exameRepository.countByEmpresa_Id(1L)).thenReturn(0L);

        assertDoesNotThrow(() -> service.delete(1L));
        verify(repository).delete(empresa);
    }
}
