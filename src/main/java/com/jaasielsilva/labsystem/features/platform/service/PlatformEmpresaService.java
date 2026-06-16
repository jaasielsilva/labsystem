package com.jaasielsilva.labsystem.features.platform.service;

import com.jaasielsilva.labsystem.features.empresa.dto.EmpresaRequest;
import com.jaasielsilva.labsystem.features.empresa.dto.EmpresaResponse;
import com.jaasielsilva.labsystem.features.platform.dto.LaboratorioOnboardingRequest;
import com.jaasielsilva.labsystem.features.platform.dto.LaboratorioOnboardingResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PlatformEmpresaService {

    Page<EmpresaResponse> findAll(Pageable pageable, String search);

    EmpresaResponse findById(Long id);

    EmpresaResponse create(EmpresaRequest request);

    LaboratorioOnboardingResponse onboard(LaboratorioOnboardingRequest request);

    EmpresaResponse update(Long id, EmpresaRequest request);

    void delete(Long id);
}
