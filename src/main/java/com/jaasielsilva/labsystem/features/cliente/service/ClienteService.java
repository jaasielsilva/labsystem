package com.jaasielsilva.labsystem.features.cliente.service;

import com.jaasielsilva.labsystem.features.cliente.dto.ClienteRequest;
import com.jaasielsilva.labsystem.features.cliente.dto.ClienteResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ClienteService {
    Page<ClienteResponse> findAll(Pageable pageable, String search);
    ClienteResponse findById(Long id);
    ClienteResponse create(ClienteRequest request);
    ClienteResponse update(Long id, ClienteRequest request);
    void delete(Long id);
}
