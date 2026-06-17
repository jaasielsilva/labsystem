package com.jaasielsilva.labsystem.features.pedido.dto;

import com.jaasielsilva.labsystem.features.pedido.entity.PedidoStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PedidoSummaryResponse(
    Long id,
    Long clienteId,
    String clienteNome,
    PedidoStatus status,
    LocalDateTime dataPedido,
    int quantidadeItens,
    BigDecimal valorTotal
) {}
