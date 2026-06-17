package com.jaasielsilva.labsystem.features.resultado.dto;

import com.jaasielsilva.labsystem.features.pedido.entity.PedidoStatus;
import com.jaasielsilva.labsystem.features.resultado.entity.ResultadoStatus;

import java.time.LocalDateTime;

public record ResultadoResponse(
    Long id,
    Long pedidoId,
    Long pedidoItemId,
    Long clienteId,
    String clienteNome,
    PedidoStatus pedidoStatus,
    Long exameId,
    String exameCodigo,
    String exameNome,
    ResultadoStatus status,
    String laudo,
    String observacaoInterna,
    LocalDateTime dataLiberacao,
    LocalDateTime dataPedido,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
