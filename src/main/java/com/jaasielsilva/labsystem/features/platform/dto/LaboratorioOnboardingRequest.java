package com.jaasielsilva.labsystem.features.platform.dto;

import com.jaasielsilva.labsystem.features.empresa.dto.EmpresaRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record LaboratorioOnboardingRequest(
    @NotNull(message = "Os dados do laboratório são obrigatórios")
    @Valid
    EmpresaRequest laboratorio,

    @NotNull(message = "Os dados do administrador são obrigatórios")
    @Valid
    PrimeiroAdminRequest admin
) {}
