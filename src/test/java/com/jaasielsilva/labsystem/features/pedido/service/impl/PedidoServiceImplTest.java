package com.jaasielsilva.labsystem.features.pedido.service.impl;

import com.jaasielsilva.labsystem.common.TenantContext;
import com.jaasielsilva.labsystem.exception.BusinessException;
import com.jaasielsilva.labsystem.exception.ResourceNotFoundException;
import com.jaasielsilva.labsystem.features.cliente.entity.Cliente;
import com.jaasielsilva.labsystem.features.cliente.repository.ClienteRepository;
import com.jaasielsilva.labsystem.features.empresa.entity.Empresa;
import com.jaasielsilva.labsystem.features.exame.entity.Exame;
import com.jaasielsilva.labsystem.features.exame.repository.ExameRepository;
import com.jaasielsilva.labsystem.features.pedido.dto.PedidoCancelarRequest;
import com.jaasielsilva.labsystem.features.pedido.dto.PedidoItemRequest;
import com.jaasielsilva.labsystem.features.pedido.dto.PedidoRequest;
import com.jaasielsilva.labsystem.features.pedido.dto.PedidoResponse;
import com.jaasielsilva.labsystem.features.pedido.entity.Pedido;
import com.jaasielsilva.labsystem.features.pedido.entity.PedidoItem;
import com.jaasielsilva.labsystem.features.pedido.entity.PedidoStatus;
import com.jaasielsilva.labsystem.features.pedido.mapper.PedidoMapper;
import com.jaasielsilva.labsystem.features.pedido.repository.PedidoRepository;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedidoServiceImplTest {

    private static final Long EMPRESA_ID = 1L;

    @Mock
    private PedidoRepository repository;

    @Mock
    private PedidoMapper mapper;

    @Mock
    private TenantContext tenantContext;

    @Mock
    private com.jaasielsilva.labsystem.features.empresa.repository.EmpresaRepository empresaRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ExameRepository exameRepository;

    @Mock
    private com.jaasielsilva.labsystem.features.resultado.service.ResultadoSyncService resultadoSyncService;

    @InjectMocks
    private PedidoServiceImpl service;

    private Empresa empresa;
    private Cliente cliente;
    private Exame exame;
    private Pedido pedido;
    private PedidoRequest request;
    private PedidoResponse response;

    @BeforeEach
    void setUp() {
        empresa = Empresa.builder().id(EMPRESA_ID).nome("Laboratório Demo").ativo(true).build();
        cliente = Cliente.builder().id(10L).nome("Maria Silva").cpf("12345678901").ativo(true).empresa(empresa).build();
        exame = Exame.builder()
                .id(20L)
                .codigo("HEM001")
                .nome("Hemograma")
                .valor(new BigDecimal("45.00"))
                .ativo(true)
                .empresa(empresa)
                .build();

        PedidoItem item = PedidoItem.builder()
                .id(100L)
                .exame(exame)
                .valorUnitario(new BigDecimal("45.00"))
                .build();

        pedido = Pedido.builder()
                .id(1L)
                .empresa(empresa)
                .cliente(cliente)
                .status(PedidoStatus.ABERTO)
                .dataPedido(LocalDateTime.now())
                .itens(new java.util.ArrayList<>(List.of(item)))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        item.setPedido(pedido);

        request = new PedidoRequest(10L, "Coleta em jejum", List.of(new PedidoItemRequest(20L)));

        response = new PedidoResponse(
                1L, 10L, "Maria Silva", PedidoStatus.ABERTO, "Coleta em jejum", null,
                pedido.getDataPedido(), List.of(), new BigDecimal("45.00"), 1,
                pedido.getCreatedAt(), pedido.getUpdatedAt()
        );
    }

    @Test
    void create_WhenDataIsValid_ShouldCreatePedidoAberto() {
        when(tenantContext.requireTenantEmpresaId()).thenReturn(EMPRESA_ID);
        when(clienteRepository.findByIdAndEmpresaId(10L, EMPRESA_ID)).thenReturn(Optional.of(cliente));
        when(exameRepository.findByIdAndEmpresaId(20L, EMPRESA_ID)).thenReturn(Optional.of(exame));
        when(empresaRepository.getReferenceById(EMPRESA_ID)).thenReturn(empresa);
        when(repository.save(any(Pedido.class))).thenAnswer(invocation -> {
            Pedido saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });
        when(repository.findByIdAndEmpresaId(1L, EMPRESA_ID)).thenReturn(Optional.of(pedido));
        when(mapper.toResponse(pedido)).thenReturn(response);

        PedidoResponse result = service.create(request);

        assertNotNull(result);
        verify(repository).save(any(Pedido.class));
    }

    @Test
    void create_WhenExameDuplicado_ShouldThrowBusinessException() {
        when(tenantContext.requireTenantEmpresaId()).thenReturn(EMPRESA_ID);
        when(clienteRepository.findByIdAndEmpresaId(10L, EMPRESA_ID)).thenReturn(Optional.of(cliente));

        PedidoRequest duplicated = new PedidoRequest(
                10L,
                null,
                List.of(new PedidoItemRequest(20L), new PedidoItemRequest(20L))
        );

        BusinessException ex = assertThrows(BusinessException.class, () -> service.create(duplicated));
        assertEquals("Não é permitido repetir o mesmo exame no pedido.", ex.getMessage());
    }

    @Test
    void concluir_WhenPedidoAberto_ShouldSetConcluido() {
        when(tenantContext.requireTenantEmpresaId()).thenReturn(EMPRESA_ID);
        when(repository.findByIdAndEmpresaId(1L, EMPRESA_ID)).thenReturn(Optional.of(pedido));
        when(repository.save(pedido)).thenReturn(pedido);
        when(mapper.toResponse(pedido)).thenReturn(response);

        service.concluir(1L);

        assertEquals(PedidoStatus.CONCLUIDO, pedido.getStatus());
    }

    @Test
    void cancelar_WhenPedidoConcluido_ShouldThrowBusinessException() {
        pedido.setStatus(PedidoStatus.CONCLUIDO);
        when(tenantContext.requireTenantEmpresaId()).thenReturn(EMPRESA_ID);
        when(repository.findByIdAndEmpresaId(1L, EMPRESA_ID)).thenReturn(Optional.of(pedido));

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> service.cancelar(1L, new PedidoCancelarRequest("Erro no cadastro"))
        );
        assertEquals("Pedido concluído não pode ser cancelado.", ex.getMessage());
    }

    @Test
    void findById_WhenNotFound_ShouldThrowResourceNotFoundException() {
        when(tenantContext.requireTenantEmpresaId()).thenReturn(EMPRESA_ID);
        when(repository.findByIdAndEmpresaId(99L, EMPRESA_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findById(99L));
    }

    @Test
    void findAll_ShouldReturnPagedPedidos() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Pedido> page = new PageImpl<>(List.of(pedido));

        when(tenantContext.requireTenantEmpresaId()).thenReturn(EMPRESA_ID);
        when(repository.findAllByEmpresaId(EMPRESA_ID, pageable)).thenReturn(page);
        when(mapper.toSummaryResponse(pedido)).thenReturn(
                new com.jaasielsilva.labsystem.features.pedido.dto.PedidoSummaryResponse(
                        1L, 10L, "Maria Silva", PedidoStatus.ABERTO, pedido.getDataPedido(), 1, new BigDecimal("45.00")
                )
        );

        Page<?> result = service.findAll(pageable, null);

        assertEquals(1, result.getTotalElements());
    }
}
