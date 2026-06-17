package com.jaasielsilva.labsystem.features.pedido.dto;

import jakarta.validation.constraints.NotNull;

public record PedidoItemRequest(
    @NotNull(message = "Selecione o exame")
    Long exameId
) {}
