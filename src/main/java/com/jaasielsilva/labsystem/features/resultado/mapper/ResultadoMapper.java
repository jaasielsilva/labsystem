package com.jaasielsilva.labsystem.features.resultado.mapper;

import com.jaasielsilva.labsystem.features.pedido.entity.Pedido;
import com.jaasielsilva.labsystem.features.pedido.entity.PedidoItem;
import com.jaasielsilva.labsystem.features.resultado.dto.ResultadoResponse;
import com.jaasielsilva.labsystem.features.resultado.dto.ResultadoSummaryResponse;
import com.jaasielsilva.labsystem.features.resultado.entity.Resultado;
import org.springframework.stereotype.Component;

@Component
public class ResultadoMapper {

    public ResultadoResponse toResponse(Resultado resultado) {
        PedidoItem item = resultado.getPedidoItem();
        Pedido pedido = item.getPedido();

        return new ResultadoResponse(
                resultado.getId(),
                pedido.getId(),
                item.getId(),
                pedido.getCliente().getId(),
                pedido.getCliente().getNome(),
                pedido.getStatus(),
                item.getExame().getId(),
                item.getExame().getCodigo(),
                item.getExame().getNome(),
                resultado.getStatus(),
                resultado.getLaudo(),
                resultado.getObservacaoInterna(),
                resultado.getDataLiberacao(),
                pedido.getDataPedido(),
                resultado.getCreatedAt(),
                resultado.getUpdatedAt()
        );
    }

    public ResultadoSummaryResponse toSummaryResponse(Resultado resultado) {
        PedidoItem item = resultado.getPedidoItem();
        Pedido pedido = item.getPedido();

        return new ResultadoSummaryResponse(
                resultado.getId(),
                pedido.getId(),
                pedido.getCliente().getNome(),
                item.getExame().getCodigo(),
                item.getExame().getNome(),
                resultado.getStatus(),
                pedido.getStatus(),
                pedido.getDataPedido(),
                resultado.getDataLiberacao()
        );
    }
}
