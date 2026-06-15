package com.jaasielsilva.labsystem.features.cliente.mapper;

import com.jaasielsilva.labsystem.features.cliente.dto.ClienteRequest;
import com.jaasielsilva.labsystem.features.cliente.dto.ClienteResponse;
import com.jaasielsilva.labsystem.features.cliente.entity.Cliente;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ClienteMapper {
    Cliente toEntity(ClienteRequest request);
    ClienteResponse toResponse(Cliente entity);
    void updateEntity(ClienteRequest request, @MappingTarget Cliente entity);
}
