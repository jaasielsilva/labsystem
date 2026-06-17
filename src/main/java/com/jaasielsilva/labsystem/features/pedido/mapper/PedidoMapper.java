package com.jaasielsilva.labsystem.features.pedido.mapper;

import com.jaasielsilva.labsystem.features.pedido.dto.PedidoItemResponse;
import com.jaasielsilva.labsystem.features.pedido.dto.PedidoResponse;
import com.jaasielsilva.labsystem.features.pedido.dto.PedidoSummaryResponse;
import com.jaasielsilva.labsystem.features.pedido.entity.Pedido;
import com.jaasielsilva.labsystem.features.pedido.entity.PedidoItem;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class PedidoMapper {

    public PedidoResponse toResponse(Pedido pedido) {
        List<PedidoItemResponse> itens = pedido.getItens().stream()
                .map(this::toItemResponse)
                .toList();

        return new PedidoResponse(
                pedido.getId(),
                pedido.getCliente().getId(),
                pedido.getCliente().getNome(),
                pedido.getStatus(),
                pedido.getObservacao(),
                pedido.getMotivoCancelamento(),
                pedido.getDataPedido(),
                itens,
                calculateTotal(pedido.getItens()),
                pedido.getItens().size(),
                pedido.getCreatedAt(),
                pedido.getUpdatedAt()
        );
    }

    public PedidoSummaryResponse toSummaryResponse(Pedido pedido) {
        return new PedidoSummaryResponse(
                pedido.getId(),
                pedido.getCliente().getId(),
                pedido.getCliente().getNome(),
                pedido.getStatus(),
                pedido.getDataPedido(),
                pedido.getItens().size(),
                calculateTotal(pedido.getItens())
        );
    }

    public PedidoItemResponse toItemResponse(PedidoItem item) {
        return new PedidoItemResponse(
                item.getId(),
                item.getExame().getId(),
                item.getExame().getCodigo(),
                item.getExame().getNome(),
                item.getValorUnitario()
        );
    }

    private BigDecimal calculateTotal(List<PedidoItem> itens) {
        return itens.stream()
                .map(PedidoItem::getValorUnitario)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
