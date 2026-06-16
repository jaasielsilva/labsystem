package com.jaasielsilva.labsystem.features.exame.mapper;

import com.jaasielsilva.labsystem.features.exame.dto.ExameRequest;
import com.jaasielsilva.labsystem.features.exame.dto.ExameResponse;
import com.jaasielsilva.labsystem.features.exame.entity.Exame;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ExameMapper {
    Exame toEntity(ExameRequest request);
    ExameResponse toResponse(Exame entity);
    void updateEntity(ExameRequest request, @MappingTarget Exame entity);
}
