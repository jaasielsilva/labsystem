package com.jaasielsilva.labsystem.features.exame.controller;

import com.jaasielsilva.labsystem.common.ApiResponse;
import com.jaasielsilva.labsystem.features.exame.dto.ExameRequest;
import com.jaasielsilva.labsystem.features.exame.dto.ExameResponse;
import com.jaasielsilva.labsystem.features.exame.service.ExameService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/exames")
@RequiredArgsConstructor
public class ExameController {

    private final ExameService service;

    @GetMapping
    @PreAuthorize("@tenantAccess.read()")
    public ResponseEntity<ApiResponse<Page<ExameResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "nome") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String q) {

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ?
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ExameResponse> response = service.findAll(pageable, q);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@tenantAccess.read()")
    public ResponseEntity<ApiResponse<ExameResponse>> getById(@PathVariable Long id) {
        ExameResponse response = service.findById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping
    @PreAuthorize("@tenantAccess.write()")
    public ResponseEntity<ApiResponse<ExameResponse>> create(@Valid @RequestBody ExameRequest request) {
        ExameResponse response = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Exame criado com sucesso", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@tenantAccess.write()")
    public ResponseEntity<ApiResponse<ExameResponse>> update(
            @PathVariable Long id, 
            @Valid @RequestBody ExameRequest request) {
        ExameResponse response = service.update(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Exame atualizado com sucesso", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@tenantAccess.admin()")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Exame removido com sucesso", null));
    }
}
