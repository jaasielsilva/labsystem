package com.jaasielsilva.labsystem.features.audit.service;

import com.jaasielsilva.labsystem.features.audit.dto.AuditResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AuditService {

    Page<AuditResponse> findAll(Pageable pageable, String search);

    AuditResponse findById(Long id);

    void log(String action, String entidade, Long entidadeId, String detalhes);
}
