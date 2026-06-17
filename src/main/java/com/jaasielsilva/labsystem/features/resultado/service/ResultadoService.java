package com.jaasielsilva.labsystem.features.resultado.service;

import com.jaasielsilva.labsystem.features.resultado.dto.ResultadoResponse;
import com.jaasielsilva.labsystem.features.resultado.dto.ResultadoSummaryResponse;
import com.jaasielsilva.labsystem.features.resultado.dto.ResultadoUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ResultadoService {

    Page<ResultadoSummaryResponse> findAll(Pageable pageable, String search);

    ResultadoResponse findById(Long id);

    ResultadoResponse update(Long id, ResultadoUpdateRequest request);

    ResultadoResponse iniciarAnalise(Long id);

    ResultadoResponse liberar(Long id, ResultadoUpdateRequest request);

    ResultadoResponse cancelar(Long id);
}
