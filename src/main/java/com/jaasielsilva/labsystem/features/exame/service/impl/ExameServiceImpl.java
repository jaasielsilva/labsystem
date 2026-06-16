package com.jaasielsilva.labsystem.features.exame.service.impl;

import com.jaasielsilva.labsystem.exception.BusinessException;
import com.jaasielsilva.labsystem.exception.ResourceNotFoundException;
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

    @Override
    @Transactional(readOnly = true)
    public Page<ExameResponse> findAll(Pageable pageable, String search) {
        if (search == null || search.isBlank()) {
            log.info("Buscando exames paginados");
            return repository.findAll(pageable).map(mapper::toResponse);
        }

        String term = search.trim();
        log.info("Buscando exames com filtro");
        return repository.searchByTerm(term, pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ExameResponse findById(Long id) {
        log.info("Buscando exame por id: {}", id);
        Exame exame = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exame não encontrado com ID: " + id));
        return mapper.toResponse(exame);
    }

    @Override
    @Transactional
    public ExameResponse create(ExameRequest request) {
        log.info("Criando novo exame");
        
        if (repository.existsByCodigo(request.codigo())) {
            throw new BusinessException("Código já cadastrado no sistema.");
        }

        Exame exame = mapper.toEntity(request);
        Exame saved = repository.save(exame);
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ExameResponse update(Long id, ExameRequest request) {
        log.info("Atualizando exame com id: {}", id);
        
        Exame exame = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exame não encontrado com ID: " + id));

        if (repository.existsByCodigoAndIdNot(request.codigo(), id)) {
            throw new BusinessException("Código já cadastrado por outro exame.");
        }

        mapper.updateEntity(request, exame);
        Exame updated = repository.save(exame);
        return mapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.info("Deletando exame com id: {}", id);
        Exame exame = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exame não encontrado com ID: " + id));
        repository.delete(exame);
    }
}
