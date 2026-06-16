package com.jaasielsilva.labsystem.features.platform.controller;

import com.jaasielsilva.labsystem.common.ApiResponse;
import com.jaasielsilva.labsystem.features.empresa.dto.EmpresaRequest;
import com.jaasielsilva.labsystem.features.empresa.dto.EmpresaResponse;
import com.jaasielsilva.labsystem.features.platform.dto.LaboratorioOnboardingRequest;
import com.jaasielsilva.labsystem.features.platform.dto.LaboratorioOnboardingResponse;
import com.jaasielsilva.labsystem.features.platform.service.PlatformEmpresaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/platform/empresas")
@RequiredArgsConstructor
@PreAuthorize("@platformAccess.allow()")
public class PlatformEmpresaController {

    private final PlatformEmpresaService service;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<EmpresaResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "nome") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String q) {

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<EmpresaResponse> response = service.findAll(pageable, q);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EmpresaResponse>> getById(@PathVariable Long id) {
        EmpresaResponse response = service.findById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EmpresaResponse>> create(@Valid @RequestBody EmpresaRequest request) {
        EmpresaResponse response = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Laboratório criado com sucesso", response));
    }

    @PostMapping("/onboarding")
    public ResponseEntity<ApiResponse<LaboratorioOnboardingResponse>> onboard(
            @Valid @RequestBody LaboratorioOnboardingRequest request) {
        LaboratorioOnboardingResponse response = service.onboard(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Laboratório e administrador criados com sucesso", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EmpresaResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody EmpresaRequest request) {
        EmpresaResponse response = service.update(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Laboratório atualizado com sucesso", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Laboratório removido com sucesso", null));
    }
}
