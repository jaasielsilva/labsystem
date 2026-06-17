package com.jaasielsilva.labsystem.features.pedido.service;

import com.jaasielsilva.labsystem.features.pedido.dto.PedidoCancelarRequest;
import com.jaasielsilva.labsystem.features.pedido.dto.PedidoRequest;
import com.jaasielsilva.labsystem.features.pedido.dto.PedidoResponse;
import com.jaasielsilva.labsystem.features.pedido.dto.PedidoSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PedidoService {

    Page<PedidoSummaryResponse> findAll(Pageable pageable, String search);

    PedidoResponse findById(Long id);

    PedidoResponse create(PedidoRequest request);

    PedidoResponse update(Long id, PedidoRequest request);

    PedidoResponse concluir(Long id);

    PedidoResponse cancelar(Long id, PedidoCancelarRequest request);

    void delete(Long id);
}
