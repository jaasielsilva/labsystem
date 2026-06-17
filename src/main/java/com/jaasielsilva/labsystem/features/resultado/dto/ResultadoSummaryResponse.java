package com.jaasielsilva.labsystem.features.resultado.dto;

import com.jaasielsilva.labsystem.features.pedido.entity.PedidoStatus;
import com.jaasielsilva.labsystem.features.resultado.entity.ResultadoStatus;

import java.time.LocalDateTime;

public record ResultadoSummaryResponse(
    Long id,
    Long pedidoId,
    String clienteNome,
    String exameCodigo,
    String exameNome,
    ResultadoStatus status,
    PedidoStatus pedidoStatus,
    LocalDateTime dataPedido,
    LocalDateTime dataLiberacao
) {}
