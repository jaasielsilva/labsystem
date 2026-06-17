package com.jaasielsilva.labsystem.features.audit.mapper;

import com.jaasielsilva.labsystem.features.audit.dto.AuditResponse;
import com.jaasielsilva.labsystem.features.audit.entity.AuditLog;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuditMapper {

    AuditResponse toResponse(AuditLog entity);
}
