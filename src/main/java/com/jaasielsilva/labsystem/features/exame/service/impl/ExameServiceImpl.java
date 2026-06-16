package com.jaasielsilva.labsystem.features.exame.service.impl;

import com.jaasielsilva.labsystem.common.TenantContext;
import com.jaasielsilva.labsystem.exception.BusinessException;
import com.jaasielsilva.labsystem.exception.ResourceNotFoundException;
import com.jaasielsilva.labsystem.features.empresa.entity.Empresa;
import com.jaasielsilva.labsystem.features.empresa.repository.EmpresaRepository;
import com.jaasielsilva.labsystem.features.exame.dto.ExameRequest;
import com.jaasielsilva.labsystem.features.exame.dto.ExameResponse;
import com.jaasielsilva.labsystem.features.exame.entity.Exame;
import com.jaasielsilva.labsystem.features.exame.mapper.ExameMapper;
import com.jaasielsilva.labsystem.features.exame.repository.ExameRepository;
import com.jaasielsilva.labsystem.features.exame.service.ExameService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExameServiceImpl implements ExameService {

    private final ExameRepository repository;
    private final ExameMapper mapper;
    private final TenantContext tenantContext;
    private final EmpresaRepository empresaRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<ExameResponse> findAll(Pageable pageable, String search) {
        Long empresaId = tenantContext.requireTenantEmpresaId();

        if (search == null || search.isBlank()) {
            log.info("Buscando exames paginados para empresaId={}", empresaId);
            return repository.findAllByEmpresaId(empresaId, pageable).map(mapper::toResponse);
        }

        String term = search.trim();
        log.info("Buscando exames com filtro para empresaId={}", empresaId);
        return repository.searchByTermAndEmpresaId(empresaId, term, pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ExameResponse findById(Long id) {
        Long empresaId = tenantContext.requireTenantEmpresaId();
        log.info("Buscando exame por id={} empresaId={}", id, empresaId);
        Exame exame = repository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Exame não encontrado com ID: " + id));
        return mapper.toResponse(exame);
    }

    @Override
    @Transactional
    public ExameResponse create(ExameRequest request) {
        Long empresaId = tenantContext.requireTenantEmpresaId();
        log.info("Criando novo exame para empresaId={}", empresaId);

        if (repository.existsByCodigoAndEmpresaId(request.codigo(), empresaId)) {
            throw new BusinessException("Código já cadastrado no sistema.");
        }

        Empresa empresa = empresaRepository.getReferenceById(empresaId);
        Exame exame = mapper.toEntity(request);
        exame.setEmpresa(empresa);
        Exame saved = repository.save(exame);
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ExameResponse update(Long id, ExameRequest request) {
        Long empresaId = tenantContext.requireTenantEmpresaId();
        log.info("Atualizando exame id={} empresaId={}", id, empresaId);

        Exame exame = repository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Exame não encontrado com ID: " + id));

        if (repository.existsByCodigoAndEmpresaIdAndIdNot(request.codigo(), empresaId, id)) {
            throw new BusinessException("Código já cadastrado por outro exame.");
        }

        mapper.updateEntity(request, exame);
        Exame updated = repository.save(exame);
        return mapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Long empresaId = tenantContext.requireTenantEmpresaId();
        log.info("Deletando exame id={} empresaId={}", id, empresaId);
        Exame exame = repository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Exame não encontrado com ID: " + id));
        repository.delete(exame);
    }
}
