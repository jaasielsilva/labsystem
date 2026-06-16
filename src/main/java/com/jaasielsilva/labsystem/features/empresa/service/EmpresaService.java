package com.jaasielsilva.labsystem.features.empresa.service;

import com.jaasielsilva.labsystem.features.empresa.dto.EmpresaRequest;
import com.jaasielsilva.labsystem.features.empresa.dto.EmpresaResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EmpresaService {
    Page<EmpresaResponse> findAll(Pageable pageable, String search);
    EmpresaResponse findById(Long id);
    EmpresaResponse create(EmpresaRequest request);
    EmpresaResponse update(Long id, EmpresaRequest request);
    void delete(Long id);
}
