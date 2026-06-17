package com.jaasielsilva.labsystem.features.pedido.dto;

import java.math.BigDecimal;

public record PedidoItemResponse(
    Long id,
    Long exameId,
    String exameCodigo,
    String exameNome,
    BigDecimal valorUnitario
) {}
