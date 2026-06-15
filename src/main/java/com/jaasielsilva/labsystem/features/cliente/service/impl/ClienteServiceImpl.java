package com.jaasielsilva.labsystem.features.cliente.service.impl;

import com.jaasielsilva.labsystem.exception.BusinessException;
import com.jaasielsilva.labsystem.exception.ResourceNotFoundException;
import com.jaasielsilva.labsystem.features.cliente.dto.ClienteRequest;
import com.jaasielsilva.labsystem.features.cliente.dto.ClienteResponse;
import com.jaasielsilva.labsystem.features.cliente.entity.Cliente;
import com.jaasielsilva.labsystem.features.cliente.mapper.ClienteMapper;
import com.jaasielsilva.labsystem.features.cliente.repository.ClienteRepository;
import com.jaasielsilva.labsystem.features.cliente.service.ClienteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository repository;
    private final ClienteMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public Page<ClienteResponse> findAll(Pageable pageable, String search) {
        if (search == null || search.isBlank()) {
            log.info("Buscando clientes paginados");
            return repository.findAll(pageable).map(mapper::toResponse);
        }

        String term = search.trim();
        String digits = term.replaceAll("\\D", "");
        log.info("Buscando clientes com filtro");
        return repository.searchByTerm(term, digits, pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponse findById(Long id) {
        log.info("Buscando cliente por id: {}", id);
        Cliente cliente = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado com ID: " + id));
        return mapper.toResponse(cliente);
    }

    @Override
    @Transactional
    public ClienteResponse create(ClienteRequest request) {
        log.info("Criando novo cliente");
        
        if (repository.existsByCpf(request.cpf())) {
            throw new BusinessException("CPF já cadastrado no sistema.");
        }
        if (repository.existsByEmail(request.email())) {
            throw new BusinessException("E-mail já cadastrado no sistema.");
        }

        Cliente cliente = mapper.toEntity(request);
        Cliente saved = repository.save(cliente);
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ClienteResponse update(Long id, ClienteRequest request) {
        log.info("Atualizando cliente com id: {}", id);
        
        Cliente cliente = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado com ID: " + id));

        if (repository.existsByCpfAndIdNot(request.cpf(), id)) {
            throw new BusinessException("CPF já cadastrado por outro cliente.");
        }
        if (repository.existsByEmailAndIdNot(request.email(), id)) {
            throw new BusinessException("E-mail já cadastrado por outro cliente.");
        }

        mapper.updateEntity(request, cliente);
        Cliente updated = repository.save(cliente);
        return mapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.info("Deletando cliente com id: {}", id);
        Cliente cliente = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado com ID: " + id));
        repository.delete(cliente);
    }
}
