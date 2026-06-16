package com.jaasielsilva.labsystem.features.exame.service;

import com.jaasielsilva.labsystem.features.exame.dto.ExameRequest;
import com.jaasielsilva.labsystem.features.exame.dto.ExameResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ExameService {
    Page<ExameResponse> findAll(Pageable pageable, String search);
    ExameResponse findById(Long id);
    ExameResponse create(ExameRequest request);
    ExameResponse update(Long id, ExameRequest request);
    void delete(Long id);
}
