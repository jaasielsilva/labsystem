package com.jaasielsilva.labsystem.features.cliente.service.impl;

import com.jaasielsilva.labsystem.common.TenantContext;
import com.jaasielsilva.labsystem.exception.BusinessException;
import com.jaasielsilva.labsystem.exception.ResourceNotFoundException;
import com.jaasielsilva.labsystem.features.cliente.dto.ClienteRequest;
import com.jaasielsilva.labsystem.features.cliente.dto.ClienteResponse;
import com.jaasielsilva.labsystem.features.cliente.entity.Cliente;
import com.jaasielsilva.labsystem.features.cliente.mapper.ClienteMapper;
import com.jaasielsilva.labsystem.features.cliente.repository.ClienteRepository;
import com.jaasielsilva.labsystem.features.cliente.service.ClienteService;
import com.jaasielsilva.labsystem.features.empresa.entity.Empresa;
import com.jaasielsilva.labsystem.features.empresa.repository.EmpresaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.jaasielsilva.labsystem.features.audit.service.AuditService;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository repository;
    private final ClienteMapper mapper;
    private final TenantContext tenantContext;
    private final EmpresaRepository empresaRepository;
    private final AuditService auditService;

    @Override
    @Transactional(readOnly = true)
    public Page<ClienteResponse> findAll(Pageable pageable, String search) {
        Long empresaId = tenantContext.requireTenantEmpresaId();

        if (search == null || search.isBlank()) {
            log.info("Buscando clientes paginados para empresaId={}", empresaId);
            return repository.findAllByEmpresaId(empresaId, pageable).map(mapper::toResponse);
        }

        String term = search.trim();
        String digits = term.replaceAll("\\D", "");
        log.info("Buscando clientes com filtro para empresaId={}", empresaId);
        return repository.searchByTermAndEmpresaId(empresaId, term, digits, pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponse findById(Long id) {
        Long empresaId = tenantContext.requireTenantEmpresaId();
        log.info("Buscando cliente por id={} empresaId={}", id, empresaId);
        Cliente cliente = repository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado com ID: " + id));
        return mapper.toResponse(cliente);
    }

    
@Override
    @Transactional
    public ClienteResponse create(ClienteRequest request) {
        Long empresaId = tenantContext.requireTenantEmpresaId();
        log.info("Criando novo cliente para empresaId={}", empresaId);

        if (repository.existsByCpfAndEmpresaId(request.cpf(), empresaId)) {
            throw new BusinessException("CPF já cadastrado no sistema.");
        }

        if (repository.existsByEmailAndEmpresaId(request.email(), empresaId)) {
            throw new BusinessException("E-mail já cadastrado no sistema.");
        }

        Empresa empresa = empresaRepository.getReferenceById(empresaId);
        Cliente cliente = mapper.toEntity(request);
        cliente.setEmpresa(empresa);

        Cliente saved = repository.save(cliente);

        // ✅ AUDITORIA CREATE
        auditService.log(
                "CREATE",
                "CLIENTE",
                saved.getId(),
                "Cliente criado: " + saved.getNome()
        );

        return mapper.toResponse(saved);
    }


    @Override
    @Transactional
    public ClienteResponse update(Long id, ClienteRequest request) {
        Long empresaId = tenantContext.requireTenantEmpresaId();
        log.info("Atualizando cliente id={} empresaId={}", id, empresaId);

        Cliente cliente = repository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado com ID: " + id));

        if (repository.existsByCpfAndEmpresaIdAndIdNot(request.cpf(), empresaId, id)) {
            throw new BusinessException("CPF já cadastrado por outro cliente.");
        }
        if (repository.existsByEmailAndEmpresaIdAndIdNot(request.email(), empresaId, id)) {
            throw new BusinessException("E-mail já cadastrado por outro cliente.");
        }

        mapper.updateEntity(request, cliente);
        Cliente updated = repository.save(cliente);

        // ✅ AUDITORIA UPDATE
        auditService.log(
                "UPDATE",
                "CLIENTE",
                updated.getId(),
                "Cliente atualizado: " + updated.getNome()
        );
        return mapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Long empresaId = tenantContext.requireTenantEmpresaId();
        log.info("Deletando cliente id={} empresaId={}", id, empresaId);
        Cliente cliente = repository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado com ID: " + id));
        repository.delete(cliente);

        // ✅ AUDITORIA DELETE
        auditService.log(
                "DELETE",
                "CLIENTE",
                cliente.getId(),
                "Cliente deletado: " + cliente.getNome()
        );
    }
}
