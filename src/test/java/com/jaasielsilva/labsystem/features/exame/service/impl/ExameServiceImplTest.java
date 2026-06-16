package com.jaasielsilva.labsystem.features.exame.service.impl;

import com.jaasielsilva.labsystem.common.TenantContext;
import com.jaasielsilva.labsystem.exception.BusinessException;
import com.jaasielsilva.labsystem.exception.ResourceNotFoundException;
import com.jaasielsilva.labsystem.features.empresa.entity.Empresa;
import com.jaasielsilva.labsystem.features.empresa.repository.EmpresaRepository;
import com.jaasielsilva.labsystem.features.exame.dto.ExameRequest;
import com.jaasielsilva.labsystem.features.exame.dto.ExameResponse;
import com.jaasielsilva.labsystem.features.exame.entity.Exame;
import com.jaasielsilva.labsystem.features.exame.mapper.ExameMapper;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExameServiceImplTest {

    private static final Long EMPRESA_ID = 1L;

    @Mock
    private ExameRepository repository;

    @Mock
    private ExameMapper mapper;

    @Mock
    private TenantContext tenantContext;

    @Mock
    private EmpresaRepository empresaRepository;

    @InjectMocks
    private ExameServiceImpl service;

    private Exame exame;
    private ExameRequest request;
    private ExameResponse response;
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

        exame = Exame.builder()
                .id(1L)
                .codigo("HEMO001")
                .nome("Hemograma Completo")
                .categoria("Hematologia")
                .tipoAmostra(Exame.TipoAmostra.SANGUE)
                .prazoDias(1)
                .valor(new BigDecimal("45.00"))
                .ativo(true)
                .empresa(empresa)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        request = new ExameRequest(
                "HEMO001",
                "Hemograma Completo",
                "Descrição",
                "Hematologia",
                Exame.TipoAmostra.SANGUE,
                1,
                new BigDecimal("45.00"),
                true
        );

        response = new ExameResponse(
                1L,
                "HEMO001",
                "Hemograma Completo",
                "Descrição",
                "Hematologia",
                Exame.TipoAmostra.SANGUE,
                1,
                new BigDecimal("45.00"),
                true,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    @Test
    void findAll_ShouldReturnPagedExames() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Exame> page = new PageImpl<>(Collections.singletonList(exame));

        when(tenantContext.requireTenantEmpresaId()).thenReturn(EMPRESA_ID);
        when(repository.findAllByEmpresaId(EMPRESA_ID, pageable)).thenReturn(page);
        when(mapper.toResponse(any(Exame.class))).thenReturn(response);

        Page<ExameResponse> result = service.findAll(pageable, null);

        assertEquals(1, result.getTotalElements());
        verify(repository).findAllByEmpresaId(EMPRESA_ID, pageable);
    }

    @Test
    void findAll_WithSearch_ShouldUseRepositorySearch() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Exame> page = new PageImpl<>(Collections.singletonList(exame));

        when(tenantContext.requireTenantEmpresaId()).thenReturn(EMPRESA_ID);
        when(repository.searchByTermAndEmpresaId(EMPRESA_ID, "Hemo", pageable)).thenReturn(page);
        when(mapper.toResponse(any(Exame.class))).thenReturn(response);

        Page<ExameResponse> result = service.findAll(pageable, "Hemo");

        assertEquals(1, result.getTotalElements());
        verify(repository).searchByTermAndEmpresaId(EMPRESA_ID, "Hemo", pageable);
    }

    @Test
    void findById_WhenExists_ShouldReturnExame() {
        when(tenantContext.requireTenantEmpresaId()).thenReturn(EMPRESA_ID);
        when(repository.findByIdAndEmpresaId(1L, EMPRESA_ID)).thenReturn(Optional.of(exame));
        when(mapper.toResponse(exame)).thenReturn(response);

        ExameResponse result = service.findById(1L);

        assertEquals("HEMO001", result.codigo());
    }

    @Test
    void findById_WhenNotExists_ShouldThrowResourceNotFoundException() {
        when(tenantContext.requireTenantEmpresaId()).thenReturn(EMPRESA_ID);
        when(repository.findByIdAndEmpresaId(1L, EMPRESA_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findById(1L));
    }

    @Test
    void create_WhenCodigoIsUnique_ShouldCreateExame() {
        when(tenantContext.requireTenantEmpresaId()).thenReturn(EMPRESA_ID);
        when(repository.existsByCodigoAndEmpresaId(request.codigo(), EMPRESA_ID)).thenReturn(false);
        when(empresaRepository.getReferenceById(EMPRESA_ID)).thenReturn(empresa);
        when(mapper.toEntity(request)).thenReturn(exame);
        when(repository.save(exame)).thenReturn(exame);
        when(mapper.toResponse(exame)).thenReturn(response);

        ExameResponse result = service.create(request);

        assertNotNull(result);
        verify(repository).save(exame);
    }

    @Test
    void create_WhenCodigoExists_ShouldThrowBusinessException() {
        when(tenantContext.requireTenantEmpresaId()).thenReturn(EMPRESA_ID);
        when(repository.existsByCodigoAndEmpresaId(request.codigo(), EMPRESA_ID)).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.create(request));
        assertEquals("Código já cadastrado no sistema.", ex.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void delete_WhenExists_ShouldDeleteExame() {
        when(tenantContext.requireTenantEmpresaId()).thenReturn(EMPRESA_ID);
        when(repository.findByIdAndEmpresaId(1L, EMPRESA_ID)).thenReturn(Optional.of(exame));

        assertDoesNotThrow(() -> service.delete(1L));
        verify(repository).delete(exame);
    }
}
