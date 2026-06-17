package com.jaasielsilva.labsystem.features.pedido.service.impl;

import com.jaasielsilva.labsystem.common.TenantContext;
import com.jaasielsilva.labsystem.exception.BusinessException;
import com.jaasielsilva.labsystem.exception.ResourceNotFoundException;
import com.jaasielsilva.labsystem.features.cliente.entity.Cliente;
import com.jaasielsilva.labsystem.features.cliente.repository.ClienteRepository;
import com.jaasielsilva.labsystem.features.empresa.entity.Empresa;
import com.jaasielsilva.labsystem.features.empresa.repository.EmpresaRepository;
import com.jaasielsilva.labsystem.features.exame.entity.Exame;
import com.jaasielsilva.labsystem.features.exame.repository.ExameRepository;
import com.jaasielsilva.labsystem.features.pedido.dto.PedidoCancelarRequest;
import com.jaasielsilva.labsystem.features.pedido.dto.PedidoItemRequest;
import com.jaasielsilva.labsystem.features.pedido.dto.PedidoRequest;
import com.jaasielsilva.labsystem.features.pedido.dto.PedidoResponse;
import com.jaasielsilva.labsystem.features.pedido.dto.PedidoSummaryResponse;
import com.jaasielsilva.labsystem.features.pedido.entity.Pedido;
import com.jaasielsilva.labsystem.features.pedido.entity.PedidoItem;
import com.jaasielsilva.labsystem.features.pedido.entity.PedidoStatus;
import com.jaasielsilva.labsystem.features.pedido.mapper.PedidoMapper;
import com.jaasielsilva.labsystem.features.pedido.repository.PedidoRepository;
import com.jaasielsilva.labsystem.features.pedido.service.PedidoService;
import com.jaasielsilva.labsystem.features.resultado.service.ResultadoSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.jaasielsilva.labsystem.features.audit.service.AuditService;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class PedidoServiceImpl implements PedidoService {

    private final PedidoRepository repository;
    private final PedidoMapper mapper;
    private final TenantContext tenantContext;
    private final EmpresaRepository empresaRepository;
    private final ClienteRepository clienteRepository;
    private final ExameRepository exameRepository;
    private final ResultadoSyncService resultadoSyncService;
    private final AuditService auditService;

    @Override
    @Transactional(readOnly = true)
    public Page<PedidoSummaryResponse> findAll(Pageable pageable, String search) {
        Long empresaId = tenantContext.requireTenantEmpresaId();

        Page<Pedido> page;
        if (search == null || search.isBlank()) {
            log.info("Buscando pedidos paginados para empresaId={}", empresaId);
            page = repository.findAllByEmpresaId(empresaId, pageable);
        } else {
            String term = search.trim();
            String digits = term.replaceAll("\\D", "");
            log.info("Buscando pedidos com filtro para empresaId={}", empresaId);
            page = repository.searchByTermAndEmpresaId(empresaId, term, digits, pageable);
        }

        return page.map(mapper::toSummaryResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PedidoResponse findById(Long id) {
        Long empresaId = tenantContext.requireTenantEmpresaId();
        log.info("Buscando pedido id={} empresaId={}", id, empresaId);
        Pedido pedido = findPedido(id, empresaId);
        return mapper.toResponse(pedido);
    }

    @Override
    @Transactional
    public PedidoResponse create(PedidoRequest request) {
        Long empresaId = tenantContext.requireTenantEmpresaId();
        log.info("Criando pedido para empresaId={}", empresaId);

        Cliente cliente = resolveCliente(request.clienteId(), empresaId);
        List<PedidoItem> itens = buildItens(request.itens(), empresaId);

        Empresa empresa = empresaRepository.getReferenceById(empresaId);
        Pedido pedido = Pedido.builder()
                .empresa(empresa)
                .cliente(cliente)
                .status(PedidoStatus.ABERTO)
                .observacao(normalizeObservacao(request.observacao()))
                .build();
        pedido.replaceItens(itens);

        Pedido saved = repository.save(pedido);
        resultadoSyncService.provisionForPedido(findPedido(saved.getId(), empresaId), empresaId);
        return mapper.toResponse(findPedido(saved.getId(), empresaId));
    }

    @Override
    @Transactional
    public PedidoResponse update(Long id, PedidoRequest request) {
        Long empresaId = tenantContext.requireTenantEmpresaId();
        log.info("Atualizando pedido id={} empresaId={}", id, empresaId);

        Pedido pedido = findPedido(id, empresaId);
        ensureEditable(pedido);

        Cliente cliente = resolveCliente(request.clienteId(), empresaId);
        List<PedidoItem> itens = buildItens(request.itens(), empresaId);

        pedido.setCliente(cliente);
        pedido.setObservacao(normalizeObservacao(request.observacao()));
        pedido.replaceItens(itens);

        Pedido updated = repository.save(pedido);
        resultadoSyncService.provisionForPedido(findPedido(updated.getId(), empresaId), empresaId);
        return mapper.toResponse(findPedido(updated.getId(), empresaId));
    }

    @Override
    @Transactional
    public PedidoResponse concluir(Long id) {
        Long empresaId = tenantContext.requireTenantEmpresaId();
        log.info("Concluindo pedido id={} empresaId={}", id, empresaId);

        Pedido pedido = findPedido(id, empresaId);
        if (pedido.getStatus() == PedidoStatus.CONCLUIDO) {
            throw new BusinessException("O pedido já está concluído.");
        }
        if (pedido.getStatus() == PedidoStatus.CANCELADO) {
            throw new BusinessException("Pedido cancelado não pode ser concluído.");
        }

        pedido.setStatus(PedidoStatus.CONCLUIDO);
        Pedido updated = repository.save(pedido);
        return mapper.toResponse(findPedido(updated.getId(), empresaId));
    }

    @Override
    @Transactional
    public PedidoResponse cancelar(Long id, PedidoCancelarRequest request) {
        Long empresaId = tenantContext.requireTenantEmpresaId();
        log.info("Cancelando pedido id={} empresaId={}", id, empresaId);

        Pedido pedido = findPedido(id, empresaId);
        if (pedido.getStatus() == PedidoStatus.CONCLUIDO) {
            throw new BusinessException("Pedido concluído não pode ser cancelado.");
        }
        if (pedido.getStatus() == PedidoStatus.CANCELADO) {
            throw new BusinessException("O pedido já está cancelado.");
        }

        pedido.setStatus(PedidoStatus.CANCELADO);
        pedido.setMotivoCancelamento(request.motivo().trim());
        resultadoSyncService.cancelByPedido(pedido, empresaId);
        Pedido updated = repository.save(pedido);
        return mapper.toResponse(findPedido(updated.getId(), empresaId));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Long empresaId = tenantContext.requireTenantEmpresaId();
        log.info("Removendo pedido id={} empresaId={}", id, empresaId);

        Pedido pedido = findPedido(id, empresaId);
        if (pedido.getStatus() != PedidoStatus.ABERTO) {
            throw new BusinessException("Somente pedidos abertos podem ser removidos.");
        }

        repository.delete(pedido);

        //AUDIT: Log de auditoria para remoção de pedido
        auditService.log(
                "DELETE",
                "PEDIDO",
                pedido.getId(),
                "Pedido removido: " + pedido.getId()
        );
    }

    private Pedido findPedido(Long id, Long empresaId) {
        return repository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado com ID: " + id));
    }

    private Cliente resolveCliente(Long clienteId, Long empresaId) {
        Cliente cliente = clienteRepository.findByIdAndEmpresaId(clienteId, empresaId)
                .orElseThrow(() -> new BusinessException("Cliente não encontrado."));
        if (!cliente.isAtivo()) {
            throw new BusinessException("O cliente selecionado está inativo.");
        }
        return cliente;
    }

    private List<PedidoItem> buildItens(List<PedidoItemRequest> requests, Long empresaId) {
        if (requests == null || requests.isEmpty()) {
            throw new BusinessException("Adicione pelo menos um exame ao pedido.");
        }

        Set<Long> exameIds = new HashSet<>();
        for (PedidoItemRequest itemRequest : requests) {
            if (!exameIds.add(itemRequest.exameId())) {
                throw new BusinessException("Não é permitido repetir o mesmo exame no pedido.");
            }
        }

        return requests.stream()
                .map(itemRequest -> toPedidoItem(itemRequest, empresaId))
                .toList();
    }

    private PedidoItem toPedidoItem(PedidoItemRequest request, Long empresaId) {
        Exame exame = exameRepository.findByIdAndEmpresaId(request.exameId(), empresaId)
                .orElseThrow(() -> new BusinessException("Exame não encontrado: ID " + request.exameId()));
        if (!exame.isAtivo()) {
            throw new BusinessException("O exame " + exame.getNome() + " está inativo.");
        }

        BigDecimal valor = exame.getValor() != null ? exame.getValor() : BigDecimal.ZERO;
        return PedidoItem.builder()
                .exame(exame)
                .valorUnitario(valor)
                .build();
    }

    private void ensureEditable(Pedido pedido) {
        if (pedido.getStatus() == PedidoStatus.CONCLUIDO) {
            throw new BusinessException("Pedido concluído não pode ser alterado.");
        }
        if (pedido.getStatus() == PedidoStatus.CANCELADO) {
            throw new BusinessException("Pedido cancelado não pode ser alterado.");
        }
    }

    private String normalizeObservacao(String observacao) {
        if (observacao == null || observacao.isBlank()) {
            return null;
        }
        return observacao.trim();
    }
}
