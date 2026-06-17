package com.jaasielsilva.labsystem.features.audit.service.impl;

import com.jaasielsilva.labsystem.common.TenantContext;
import com.jaasielsilva.labsystem.exception.ResourceNotFoundException;
import com.jaasielsilva.labsystem.features.audit.dto.AuditResponse;
import com.jaasielsilva.labsystem.features.audit.entity.AuditLog;
import com.jaasielsilva.labsystem.features.audit.mapper.AuditMapper;
import com.jaasielsilva.labsystem.features.audit.repository.AuditRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditServiceImplTest {

    private static final Long EMPRESA_ID = 1L;

    @Mock
    private AuditRepository repository;

    @Mock
    private AuditMapper mapper;

    @Mock
    private TenantContext tenantContext;

    @InjectMocks
    private AuditServiceImpl service;

    private AuditLog auditLog;
    private AuditResponse auditResponse;

    @BeforeEach
    void setUp() {
        auditLog = AuditLog.builder()
                .id(10L)
                .usuarioId(2L)
                .usuarioEmail("admin@labsystem.local")
                .perfil("ADMIN")
                .empresaId(EMPRESA_ID)
                .action("CREATE")
                .entidade("CLIENTE")
                .entidadeId(5L)
                .detalhes("Cliente cadastrado")
                .scope("TENANT")
                .createdAt(LocalDateTime.now())
                .build();

        auditResponse = new AuditResponse(
                10L,
                "admin@labsystem.local",
                "ADMIN",
                "CREATE",
                "CLIENTE",
                5L,
                "Cliente cadastrado",
                "TENANT",
                auditLog.getCreatedAt()
        );
    }

    @Test
    void findAll_deveRetornarPagina() {
        PageRequest pageable = PageRequest.of(0, 10);
        Page<AuditLog> page = new PageImpl<>(List.of(auditLog));

        when(tenantContext.requireTenantEmpresaId()).thenReturn(EMPRESA_ID);
        when(repository.findAllByEmpresaId(EMPRESA_ID, pageable)).thenReturn(page);
        when(mapper.toResponse(auditLog)).thenReturn(auditResponse);

        Page<AuditResponse> result = service.findAll(pageable, null);

        assertEquals(1, result.getTotalElements());
        assertEquals("CREATE", result.getContent().get(0).action());
    }

    @Test
    void findById_deveRetornarLog() {
        when(tenantContext.requireTenantEmpresaId()).thenReturn(EMPRESA_ID);
        when(repository.findByIdAndEmpresaId(10L, EMPRESA_ID)).thenReturn(Optional.of(auditLog));
        when(mapper.toResponse(auditLog)).thenReturn(auditResponse);

        AuditResponse result = service.findById(10L);

        assertEquals(10L, result.id());
        assertEquals("CLIENTE", result.entidade());
    }

    @Test
    void findById_deveLancarQuandoNaoEncontrado() {
        when(tenantContext.requireTenantEmpresaId()).thenReturn(EMPRESA_ID);
        when(repository.findByIdAndEmpresaId(99L, EMPRESA_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findById(99L));
    }

    @Test
    void findAll_comBusca_deveUsarRepositorioDePesquisa() {
        PageRequest pageable = PageRequest.of(0, 10);
        Page<AuditLog> page = new PageImpl<>(List.of(auditLog));

        when(tenantContext.requireTenantEmpresaId()).thenReturn(EMPRESA_ID);
        when(repository.searchByTermAndEmpresaId(eq(EMPRESA_ID), eq("cliente"), eq(pageable))).thenReturn(page);
        when(mapper.toResponse(auditLog)).thenReturn(auditResponse);

        service.findAll(pageable, "cliente");

        verify(repository).searchByTermAndEmpresaId(EMPRESA_ID, "cliente", pageable);
    }
}
