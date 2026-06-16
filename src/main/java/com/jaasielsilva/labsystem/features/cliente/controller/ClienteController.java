package com.jaasielsilva.labsystem.features.cliente.controller;

import com.jaasielsilva.labsystem.common.ApiResponse;
import com.jaasielsilva.labsystem.features.cliente.dto.ClienteRequest;
import com.jaasielsilva.labsystem.features.cliente.dto.ClienteResponse;
import com.jaasielsilva.labsystem.features.cliente.service.ClienteService;
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
@RequestMapping("/api/v1/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService service;

    @GetMapping
    @PreAuthorize("@tenantAccess.read()")
    public ResponseEntity<ApiResponse<Page<ClienteResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "nome") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String q) {

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ?
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ClienteResponse> response = service.findAll(pageable, q);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@tenantAccess.read()")
    public ResponseEntity<ApiResponse<ClienteResponse>> getById(@PathVariable Long id) {
        ClienteResponse response = service.findById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping
    @PreAuthorize("@tenantAccess.write()")
    public ResponseEntity<ApiResponse<ClienteResponse>> create(@Valid @RequestBody ClienteRequest request) {
        ClienteResponse response = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Cliente criado com sucesso", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@tenantAccess.write()")
    public ResponseEntity<ApiResponse<ClienteResponse>> update(
            @PathVariable Long id, 
            @Valid @RequestBody ClienteRequest request) {
        ClienteResponse response = service.update(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Cliente atualizado com sucesso", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@tenantAccess.admin()")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Cliente removido com sucesso", null));
    }
}
