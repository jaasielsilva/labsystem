package com.jaasielsilva.labsystem.features.resultado.service;

import com.jaasielsilva.labsystem.features.pedido.entity.Pedido;

public interface ResultadoSyncService {

    void provisionForPedido(Pedido pedido, Long empresaId);

    void cancelByPedido(Pedido pedido, Long empresaId);
}
