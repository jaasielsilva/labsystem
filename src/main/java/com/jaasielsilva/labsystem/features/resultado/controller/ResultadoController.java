package com.jaasielsilva.labsystem.features.resultado.controller;

import com.jaasielsilva.labsystem.common.ApiResponse;
import com.jaasielsilva.labsystem.features.resultado.dto.ResultadoResponse;
import com.jaasielsilva.labsystem.features.resultado.dto.ResultadoSummaryResponse;
import com.jaasielsilva.labsystem.features.resultado.dto.ResultadoUpdateRequest;
import com.jaasielsilva.labsystem.features.resultado.service.ResultadoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/resultados")
@RequiredArgsConstructor
public class ResultadoController {

    private final ResultadoService service;

    @GetMapping
    @PreAuthorize("@tenantAccess.read()")
    public ResponseEntity<ApiResponse<Page<ResultadoSummaryResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String q) {

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ResultadoSummaryResponse> response = service.findAll(pageable, q);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@tenantAccess.read()")
    public ResponseEntity<ApiResponse<ResultadoResponse>> getById(@PathVariable Long id) {
        ResultadoResponse response = service.findById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@tenantAccess.write()")
    public ResponseEntity<ApiResponse<ResultadoResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody ResultadoUpdateRequest request) {
        ResultadoResponse response = service.update(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Rascunho salvo com sucesso", response));
    }

    @PostMapping("/{id}/iniciar-analise")
    @PreAuthorize("@tenantAccess.write()")
    public ResponseEntity<ApiResponse<ResultadoResponse>> iniciarAnalise(@PathVariable Long id) {
        ResultadoResponse response = service.iniciarAnalise(id);
        return ResponseEntity.ok(ApiResponse.ok("Análise iniciada com sucesso", response));
    }

    @PostMapping("/{id}/liberar")
    @PreAuthorize("@tenantAccess.write()")
    public ResponseEntity<ApiResponse<ResultadoResponse>> liberar(
            @PathVariable Long id,
            @Valid @RequestBody ResultadoUpdateRequest request) {
        ResultadoResponse response = service.liberar(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Resultado liberado com sucesso", response));
    }

    @PostMapping("/{id}/cancelar")
    @PreAuthorize("@tenantAccess.write()")
    public ResponseEntity<ApiResponse<ResultadoResponse>> cancelar(@PathVariable Long id) {
        ResultadoResponse response = service.cancelar(id);
        return ResponseEntity.ok(ApiResponse.ok("Resultado cancelado com sucesso", response));
    }
}
