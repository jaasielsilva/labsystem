package com.jaasielsilva.labsystem.features.pedido.dto;

import com.jaasielsilva.labsystem.features.pedido.entity.PedidoStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PedidoCancelarRequest(
    @NotBlank(message = "Informe o motivo do cancelamento")
    @Size(max = 300, message = "O motivo não pode exceder 300 caracteres")
    String motivo
) {}
