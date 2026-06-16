package com.jaasielsilva.labsystem.features.empresa.service.impl;

import com.jaasielsilva.labsystem.exception.BusinessException;
import com.jaasielsilva.labsystem.exception.ResourceNotFoundException;
import com.jaasielsilva.labsystem.features.cliente.repository.ClienteRepository;
import com.jaasielsilva.labsystem.features.empresa.dto.EmpresaRequest;
import com.jaasielsilva.labsystem.features.empresa.dto.EmpresaResponse;
import com.jaasielsilva.labsystem.features.empresa.entity.Empresa;
import com.jaasielsilva.labsystem.features.empresa.mapper.EmpresaMapper;
import com.jaasielsilva.labsystem.features.empresa.repository.EmpresaRepository;
import com.jaasielsilva.labsystem.features.empresa.service.EmpresaService;
import com.jaasielsilva.labsystem.features.exame.repository.ExameRepository;
import com.jaasielsilva.labsystem.features.auth.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmpresaServiceImpl implements EmpresaService {

    private final EmpresaRepository repository;
    private final EmpresaMapper mapper;
    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final ExameRepository exameRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<EmpresaResponse> findAll(Pageable pageable, String search) {
        if (search == null || search.isBlank()) {
            log.info("Buscando empresas paginadas");
            return repository.findAll(pageable).map(mapper::toResponse);
        }

        String term = search.trim();
        String digits = term.replaceAll("\\D", "");
        log.info("Buscando empresas com filtro");
        return repository.searchByTerm(term, digits, pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public EmpresaResponse findById(Long id) {
        log.info("Buscando empresa por id: {}", id);
        Empresa empresa = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada com ID: " + id));
        return mapper.toResponse(empresa);
    }

    @Override
    @Transactional
    public EmpresaResponse create(EmpresaRequest request) {
        log.info("Criando nova empresa");

        if (repository.existsByCnpj(request.cnpj())) {
            throw new BusinessException("CNPJ já cadastrado no sistema.");
        }

        Empresa empresa = mapper.toEntity(request);
        Empresa saved = repository.save(empresa);
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public EmpresaResponse update(Long id, EmpresaRequest request) {
        log.info("Atualizando empresa com id: {}", id);

        Empresa empresa = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada com ID: " + id));

        if (repository.existsByCnpjAndIdNot(request.cnpj(), id)) {
            throw new BusinessException("CNPJ já cadastrado por outra empresa.");
        }

        mapper.updateEntity(request, empresa);
        Empresa updated = repository.save(empresa);
        return mapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.info("Deletando empresa com id: {}", id);

        Empresa empresa = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada com ID: " + id));

        if (repository.count() <= 1) {
            throw new BusinessException("Não é possível remover a única empresa do sistema.");
        }

        if (usuarioRepository.countByEmpresa_Id(id) > 0) {
            throw new BusinessException("Não é possível remover empresa com usuários vinculados.");
        }
        if (clienteRepository.countByEmpresa_Id(id) > 0) {
            throw new BusinessException("Não é possível remover empresa com clientes vinculados.");
        }
        if (exameRepository.countByEmpresa_Id(id) > 0) {
            throw new BusinessException("Não é possível remover empresa com exames vinculados.");
        }

        repository.delete(empresa);
    }
}
