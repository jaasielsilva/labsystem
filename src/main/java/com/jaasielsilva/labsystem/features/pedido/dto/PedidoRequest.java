package com.jaasielsilva.labsystem.features.pedido.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PedidoRequest(
    @NotNull(message = "Selecione o cliente")
    Long clienteId,

    @Size(max = 500, message = "A observação não pode exceder 500 caracteres")
    String observacao,

    @NotEmpty(message = "Adicione pelo menos um exame ao pedido")
    @Valid
    List<PedidoItemRequest> itens
) {}
