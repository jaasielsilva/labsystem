package com.jaasielsilva.labsystem.features.empresa.mapper;

import com.jaasielsilva.labsystem.features.empresa.dto.EmpresaRequest;
import com.jaasielsilva.labsystem.features.empresa.dto.EmpresaResponse;
import com.jaasielsilva.labsystem.features.empresa.entity.Empresa;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface EmpresaMapper {
    Empresa toEntity(EmpresaRequest request);
    EmpresaResponse toResponse(Empresa entity);
    void updateEntity(EmpresaRequest request, @MappingTarget Empresa entity);
}
