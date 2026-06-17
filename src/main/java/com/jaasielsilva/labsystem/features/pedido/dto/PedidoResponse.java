package com.jaasielsilva.labsystem.features.pedido.dto;

import com.jaasielsilva.labsystem.features.pedido.entity.PedidoStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoResponse(
    Long id,
    Long clienteId,
    String clienteNome,
    PedidoStatus status,
    String observacao,
    String motivoCancelamento,
    LocalDateTime dataPedido,
    List<PedidoItemResponse> itens,
    BigDecimal valorTotal,
    int quantidadeItens,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
