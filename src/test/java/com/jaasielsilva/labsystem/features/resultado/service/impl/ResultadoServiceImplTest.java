package com.jaasielsilva.labsystem.features.resultado.service.impl;

import com.jaasielsilva.labsystem.common.TenantContext;
import com.jaasielsilva.labsystem.exception.BusinessException;
import com.jaasielsilva.labsystem.features.cliente.entity.Cliente;
import com.jaasielsilva.labsystem.features.empresa.entity.Empresa;
import com.jaasielsilva.labsystem.features.exame.entity.Exame;
import com.jaasielsilva.labsystem.features.pedido.entity.Pedido;
import com.jaasielsilva.labsystem.features.pedido.entity.PedidoItem;
import com.jaasielsilva.labsystem.features.pedido.entity.PedidoStatus;
import com.jaasielsilva.labsystem.features.pedido.repository.PedidoRepository;
import com.jaasielsilva.labsystem.features.resultado.dto.ResultadoUpdateRequest;
import com.jaasielsilva.labsystem.features.resultado.entity.Resultado;
import com.jaasielsilva.labsystem.features.resultado.entity.ResultadoStatus;
import com.jaasielsilva.labsystem.features.resultado.mapper.ResultadoMapper;
import com.jaasielsilva.labsystem.features.resultado.repository.ResultadoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResultadoServiceImplTest {

    private static final Long EMPRESA_ID = 1L;

    @Mock
    private ResultadoRepository repository;

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private ResultadoMapper mapper;

    @Mock
    private TenantContext tenantContext;

    @InjectMocks
    private ResultadoServiceImpl service;

    private Resultado resultado;
    private Pedido pedido;

    @BeforeEach
    void setUp() {
        Empresa empresa = Empresa.builder().id(EMPRESA_ID).nome("Lab").ativo(true).build();
        Cliente cliente = Cliente.builder().id(5L).nome("Maria").cpf("12345678901").ativo(true).empresa(empresa).build();
        Exame exame = Exame.builder().id(8L).codigo("GLI001").nome("Glicose").valor(BigDecimal.TEN).ativo(true).empresa(empresa).build();

        pedido = Pedido.builder()
                .id(2L)
                .empresa(empresa)
                .cliente(cliente)
                .status(PedidoStatus.ABERTO)
                .dataPedido(LocalDateTime.now())
                .build();

        PedidoItem item = PedidoItem.builder().id(20L).pedido(pedido).exame(exame).valorUnitario(BigDecimal.TEN).build();
        pedido.setItens(new java.util.ArrayList<>(List.of(item)));

        resultado = Resultado.builder()
                .id(1L)
                .empresa(empresa)
                .pedidoItem(item)
                .status(ResultadoStatus.PENDENTE)
                .build();
    }

    @Test
    void iniciarAnalise_WhenPendente_ShouldSetEmAnaliseAndPedidoEmAndamento() {
        when(tenantContext.requireTenantEmpresaId()).thenReturn(EMPRESA_ID);
        when(repository.findByIdAndEmpresaId(1L, EMPRESA_ID)).thenReturn(Optional.of(resultado));
        when(repository.save(resultado)).thenReturn(resultado);
        when(mapper.toResponse(any(Resultado.class))).thenReturn(null);

        service.iniciarAnalise(1L);

        assertEquals(ResultadoStatus.EM_ANALISE, resultado.getStatus());
        assertEquals(PedidoStatus.EM_ANDAMENTO, pedido.getStatus());
        verify(pedidoRepository).save(pedido);
    }

    @Test
    void liberar_WithoutLaudo_ShouldThrowBusinessException() {
        when(tenantContext.requireTenantEmpresaId()).thenReturn(EMPRESA_ID);
        when(repository.findByIdAndEmpresaId(1L, EMPRESA_ID)).thenReturn(Optional.of(resultado));

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> service.liberar(1L, new ResultadoUpdateRequest(null, "  "))
        );
        assertEquals("Informe o laudo antes de liberar o resultado.", ex.getMessage());
    }

    @Test
    void liberar_WhenValid_ShouldSetDisponivelAndConcludePedido() {
        resultado.setStatus(ResultadoStatus.EM_ANALISE);
        when(tenantContext.requireTenantEmpresaId()).thenReturn(EMPRESA_ID);
        when(repository.findByIdAndEmpresaId(1L, EMPRESA_ID)).thenReturn(Optional.of(resultado));
        when(repository.save(resultado)).thenReturn(resultado);
        when(repository.countByEmpresaIdAndPedidoItemPedidoId(EMPRESA_ID, 2L)).thenReturn(1L);
        when(repository.countByEmpresaIdAndPedidoItemPedidoIdAndStatusIn(eq(EMPRESA_ID), eq(2L), anyList())).thenReturn(1L);
        when(mapper.toResponse(any(Resultado.class))).thenReturn(null);

        service.liberar(1L, new ResultadoUpdateRequest(null, "Resultado dentro da normalidade."));

        assertEquals(ResultadoStatus.DISPONIVEL, resultado.getStatus());
        assertNotNull(resultado.getDataLiberacao());
        assertEquals(PedidoStatus.CONCLUIDO, pedido.getStatus());
        verify(pedidoRepository).save(pedido);
    }
}
