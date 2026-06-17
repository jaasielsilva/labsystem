package com.jaasielsilva.labsystem.features.resultado.service.impl;

import com.jaasielsilva.labsystem.common.TenantContext;
import com.jaasielsilva.labsystem.exception.BusinessException;
import com.jaasielsilva.labsystem.exception.ResourceNotFoundException;
import com.jaasielsilva.labsystem.features.pedido.entity.Pedido;
import com.jaasielsilva.labsystem.features.pedido.entity.PedidoStatus;
import com.jaasielsilva.labsystem.features.pedido.repository.PedidoRepository;
import com.jaasielsilva.labsystem.features.resultado.dto.ResultadoResponse;
import com.jaasielsilva.labsystem.features.resultado.dto.ResultadoSummaryResponse;
import com.jaasielsilva.labsystem.features.resultado.dto.ResultadoUpdateRequest;
import com.jaasielsilva.labsystem.features.resultado.entity.Resultado;
import com.jaasielsilva.labsystem.features.resultado.entity.ResultadoStatus;
import com.jaasielsilva.labsystem.features.resultado.mapper.ResultadoMapper;
import com.jaasielsilva.labsystem.features.resultado.repository.ResultadoRepository;
import com.jaasielsilva.labsystem.features.resultado.service.ResultadoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResultadoServiceImpl implements ResultadoService {

    private final ResultadoRepository repository;
    private final PedidoRepository pedidoRepository;
    private final ResultadoMapper mapper;
    private final TenantContext tenantContext;

    @Override
    @Transactional(readOnly = true)
    public Page<ResultadoSummaryResponse> findAll(Pageable pageable, String search) {
        Long empresaId = tenantContext.requireTenantEmpresaId();

        Page<Resultado> page;
        if (search == null || search.isBlank()) {
            log.info("Buscando resultados paginados para empresaId={}", empresaId);
            page = repository.findAllByEmpresaId(empresaId, pageable);
        } else {
            String term = search.trim();
            String digits = term.replaceAll("\\D", "");
            log.info("Buscando resultados com filtro para empresaId={}", empresaId);
            page = repository.searchByTermAndEmpresaId(empresaId, term, digits, pageable);
        }

        return page.map(mapper::toSummaryResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ResultadoResponse findById(Long id) {
        Long empresaId = tenantContext.requireTenantEmpresaId();
        return mapper.toResponse(findResultado(id, empresaId));
    }

    @Override
    @Transactional
    public ResultadoResponse update(Long id, ResultadoUpdateRequest request) {
        Long empresaId = tenantContext.requireTenantEmpresaId();
        log.info("Atualizando rascunho do resultado id={} empresaId={}", id, empresaId);

        Resultado resultado = findResultado(id, empresaId);
        ensureDraftEditable(resultado);

        applyDraft(resultado, request);
        Resultado updated = repository.save(resultado);
        return mapper.toResponse(findResultado(updated.getId(), empresaId));
    }

    @Override
    @Transactional
    public ResultadoResponse iniciarAnalise(Long id) {
        Long empresaId = tenantContext.requireTenantEmpresaId();
        log.info("Iniciando análise do resultado id={} empresaId={}", id, empresaId);

        Resultado resultado = findResultado(id, empresaId);
        Pedido pedido = resultado.getPedidoItem().getPedido();

        if (pedido.getStatus() == PedidoStatus.CANCELADO) {
            throw new BusinessException("Não é possível analisar resultado de pedido cancelado.");
        }
        if (pedido.getStatus() == PedidoStatus.CONCLUIDO) {
            throw new BusinessException("O pedido já está concluído.");
        }
        if (resultado.getStatus() == ResultadoStatus.DISPONIVEL) {
            throw new BusinessException("Resultado já liberado.");
        }
        if (resultado.getStatus() == ResultadoStatus.CANCELADO) {
            throw new BusinessException("Resultado cancelado não pode entrar em análise.");
        }

        resultado.setStatus(ResultadoStatus.EM_ANALISE);
        repository.save(resultado);

        if (pedido.getStatus() == PedidoStatus.ABERTO) {
            pedido.setStatus(PedidoStatus.EM_ANDAMENTO);
            pedidoRepository.save(pedido);
        }

        return mapper.toResponse(findResultado(id, empresaId));
    }

    @Override
    @Transactional
    public ResultadoResponse liberar(Long id, ResultadoUpdateRequest request) {
        Long empresaId = tenantContext.requireTenantEmpresaId();
        log.info("Liberando resultado id={} empresaId={}", id, empresaId);

        Resultado resultado = findResultado(id, empresaId);
        Pedido pedido = resultado.getPedidoItem().getPedido();

        if (pedido.getStatus() == PedidoStatus.CANCELADO) {
            throw new BusinessException("Não é possível liberar resultado de pedido cancelado.");
        }
        if (resultado.getStatus() == ResultadoStatus.CANCELADO) {
            throw new BusinessException("Resultado cancelado não pode ser liberado.");
        }
        if (resultado.getStatus() == ResultadoStatus.DISPONIVEL) {
            throw new BusinessException("Resultado já está disponível.");
        }

        applyDraft(resultado, request);
        if (resultado.getLaudo() == null || resultado.getLaudo().isBlank()) {
            throw new BusinessException("Informe o laudo antes de liberar o resultado.");
        }

        resultado.setStatus(ResultadoStatus.DISPONIVEL);
        resultado.setDataLiberacao(LocalDateTime.now());
        repository.save(resultado);

        syncPedidoStatus(pedido, empresaId);

        return mapper.toResponse(findResultado(id, empresaId));
    }

    @Override
    @Transactional
    public ResultadoResponse cancelar(Long id) {
        Long empresaId = tenantContext.requireTenantEmpresaId();
        log.info("Cancelando resultado id={} empresaId={}", id, empresaId);

        Resultado resultado = findResultado(id, empresaId);
        if (resultado.getStatus() == ResultadoStatus.DISPONIVEL) {
            throw new BusinessException("Resultado liberado não pode ser cancelado.");
        }

        resultado.setStatus(ResultadoStatus.CANCELADO);
        repository.save(resultado);

        return mapper.toResponse(findResultado(id, empresaId));
    }

    private Resultado findResultado(Long id, Long empresaId) {
        return repository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Resultado não encontrado com ID: " + id));
    }

    private void ensureDraftEditable(Resultado resultado) {
        if (resultado.getStatus() == ResultadoStatus.DISPONIVEL) {
            throw new BusinessException("Resultado liberado não pode ser alterado.");
        }
        if (resultado.getStatus() == ResultadoStatus.CANCELADO) {
            throw new BusinessException("Resultado cancelado não pode ser alterado.");
        }

        PedidoStatus pedidoStatus = resultado.getPedidoItem().getPedido().getStatus();
        if (pedidoStatus == PedidoStatus.CANCELADO) {
            throw new BusinessException("Pedido cancelado não permite alteração de resultado.");
        }
    }

    private void applyDraft(Resultado resultado, ResultadoUpdateRequest request) {
        if (request.observacaoInterna() != null) {
            String observacao = request.observacaoInterna().trim();
            resultado.setObservacaoInterna(observacao.isBlank() ? null : observacao);
        }
        if (request.laudo() != null) {
            String laudo = request.laudo().trim();
            resultado.setLaudo(laudo.isBlank() ? null : laudo);
        }
    }

    private void syncPedidoStatus(Pedido pedido, Long empresaId) {
        long total = repository.countByEmpresaIdAndPedidoItemPedidoId(empresaId, pedido.getId());
        long finalizados = repository.countByEmpresaIdAndPedidoItemPedidoIdAndStatusIn(
                empresaId,
                pedido.getId(),
                List.of(ResultadoStatus.DISPONIVEL, ResultadoStatus.CANCELADO)
        );

        if (total > 0 && finalizados == total && pedido.getStatus() != PedidoStatus.CONCLUIDO) {
            pedido.setStatus(PedidoStatus.CONCLUIDO);
            pedidoRepository.save(pedido);
            log.info("Pedido id={} concluído automaticamente após liberação de resultados", pedido.getId());
        }
    }
}
