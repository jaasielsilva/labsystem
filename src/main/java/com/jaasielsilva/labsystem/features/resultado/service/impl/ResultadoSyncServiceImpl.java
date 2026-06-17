package com.jaasielsilva.labsystem.features.resultado.service.impl;

import com.jaasielsilva.labsystem.features.empresa.entity.Empresa;
import com.jaasielsilva.labsystem.features.empresa.repository.EmpresaRepository;
import com.jaasielsilva.labsystem.features.pedido.entity.Pedido;
import com.jaasielsilva.labsystem.features.pedido.entity.PedidoItem;
import com.jaasielsilva.labsystem.features.resultado.entity.Resultado;
import com.jaasielsilva.labsystem.features.resultado.entity.ResultadoStatus;
import com.jaasielsilva.labsystem.features.resultado.repository.ResultadoRepository;
import com.jaasielsilva.labsystem.features.resultado.service.ResultadoSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResultadoSyncServiceImpl implements ResultadoSyncService {

    private final ResultadoRepository resultadoRepository;
    private final EmpresaRepository empresaRepository;

    @Override
    @Transactional
    public void provisionForPedido(Pedido pedido, Long empresaId) {
        Empresa empresa = empresaRepository.getReferenceById(empresaId);

        for (PedidoItem item : pedido.getItens()) {
            if (item.getId() == null) {
                continue;
            }

            boolean exists = resultadoRepository
                    .findByPedidoItemIdAndEmpresaId(item.getId(), empresaId)
                    .isPresent();

            if (!exists) {
                log.info("Provisionando resultado pendente para pedidoItemId={}", item.getId());
                resultadoRepository.save(Resultado.builder()
                        .empresa(empresa)
                        .pedidoItem(item)
                        .status(ResultadoStatus.PENDENTE)
                        .build());
            }
        }
    }

    @Override
    @Transactional
    public void cancelByPedido(Pedido pedido, Long empresaId) {
        resultadoRepository.findAllByPedidoItemPedidoIdAndEmpresaId(pedido.getId(), empresaId)
                .stream()
                .filter(resultado -> resultado.getStatus() != ResultadoStatus.CANCELADO
                        && resultado.getStatus() != ResultadoStatus.DISPONIVEL)
                .forEach(resultado -> resultado.setStatus(ResultadoStatus.CANCELADO));
    }
}
