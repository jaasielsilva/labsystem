package com.jaasielsilva.labsystem.features.pedido.controller;

import com.jaasielsilva.labsystem.common.ApiResponse;
import com.jaasielsilva.labsystem.features.pedido.dto.PedidoCancelarRequest;
import com.jaasielsilva.labsystem.features.pedido.dto.PedidoRequest;
import com.jaasielsilva.labsystem.features.pedido.dto.PedidoResponse;
import com.jaasielsilva.labsystem.features.pedido.dto.PedidoSummaryResponse;
import com.jaasielsilva.labsystem.features.pedido.service.PedidoService;
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
@RequestMapping("/api/v1/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService service;

    @GetMapping
    @PreAuthorize("@tenantAccess.read()")
    public ResponseEntity<ApiResponse<Page<PedidoSummaryResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "dataPedido") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String q) {

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<PedidoSummaryResponse> response = service.findAll(pageable, q);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@tenantAccess.read()")
    public ResponseEntity<ApiResponse<PedidoResponse>> getById(@PathVariable Long id) {
        PedidoResponse response = service.findById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping
    @PreAuthorize("@tenantAccess.write()")
    public ResponseEntity<ApiResponse<PedidoResponse>> create(@Valid @RequestBody PedidoRequest request) {
        PedidoResponse response = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Pedido criado com sucesso", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@tenantAccess.write()")
    public ResponseEntity<ApiResponse<PedidoResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody PedidoRequest request) {
        PedidoResponse response = service.update(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Pedido atualizado com sucesso", response));
    }

    @PostMapping("/{id}/concluir")
    @PreAuthorize("@tenantAccess.write()")
    public ResponseEntity<ApiResponse<PedidoResponse>> concluir(@PathVariable Long id) {
        PedidoResponse response = service.concluir(id);
        return ResponseEntity.ok(ApiResponse.ok("Pedido concluído com sucesso", response));
    }

    @PostMapping("/{id}/cancelar")
    @PreAuthorize("@tenantAccess.write()")
    public ResponseEntity<ApiResponse<PedidoResponse>> cancelar(
            @PathVariable Long id,
            @Valid @RequestBody PedidoCancelarRequest request) {
        PedidoResponse response = service.cancelar(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Pedido cancelado com sucesso", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@tenantAccess.admin()")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Pedido removido com sucesso", null));
    }
}
