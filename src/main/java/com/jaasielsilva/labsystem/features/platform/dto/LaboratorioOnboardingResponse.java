package com.jaasielsilva.labsystem.features.platform.dto;

import com.jaasielsilva.labsystem.features.empresa.dto.EmpresaResponse;
import com.jaasielsilva.labsystem.features.usuario.dto.UsuarioResponse;

public record LaboratorioOnboardingResponse(
    EmpresaResponse laboratorio,
    UsuarioResponse admin
) {}
