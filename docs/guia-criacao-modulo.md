# Guia para Criação de Novos Módulos — LabSystem

> Documento de referência para desenvolvimento de features.
> Siga **sempre** nesta ordem. Não pule etapas.
> Referência de código vivo: `features/cliente/` (back e front).

---

## 0. Antes de Codar (Obrigatório)

Responda antes de criar qualquer arquivo. Se a resposta for vaga, **não comece**.

| Pergunta | Exemplo para o módulo Exame |
|---|---|
| **Quem usa?** | Operador cadastra; Visualizador consulta |
| **Qual dor resolve?** | Laboratório precisa de catálogo padronizado de exames |
| **Como medir sucesso?** | Operador consegue cadastrar um exame em menos de 2 min |
| **O que é MVP?** | CRUD básico com nome, código e valor de referência |
| **O que fica para v2?** | Upload de PDF, integração com equipamento |

Feature que não passa nessa triagem **não entra no sprint**.

### Multi-tenant (estado atual)

O Labsystem **já é multi-tenant por laboratório** (`empresa_id` = tenant). Todo módulo de negócio deve:

- Ter coluna `empresa_id NOT NULL` na migration
- Filtrar queries pelo `empresaId` do JWT (`TenantContext.requireTenantEmpresaId()`)
- Usar `@tenantAccess.read()` / `.write()` / `.admin()` no controller (não só `hasRole`)
- **Nunca** aceitar `empresaId` no body ou query para autorizar

> **Planos + billing** (limites, assinatura) são **Fase 4** — camada comercial futura, ortogonal ao isolamento por `empresa_id`. Ver `docs/ARQUITETURA.md` e skill `/labsystem` seção 6.1.

---

## 1. Ordem de Execução

```
BACKEND
  1. Entity (JPA)
  2. Migration SQL (Flyway)
  3. Repository
  4. DTOs (Request + Response)
  5. Mapper (MapStruct)
  6. Service (interface + impl)
  7. Controller (REST)
  8. Testes unitários do Service

FRONTEND
  9.  Model (TypeScript)
  10. Service Angular
  11. List Component (page + html + css)
  12. Form Component (page + html + css)
  13. Routes
  14. app.routes.ts (lazy load)
  15. nav.config.ts (menu)

VALIDAÇÃO
  16. Reiniciar backend → testar endpoints no Swagger/Postman
  17. Testar frontend ponta a ponta
  18. Atualizar ARQUITETURA.md
```

> **Regra:** backend funcionando e testado antes de tocar no frontend.

---

## 2. Backend (Spring Boot)

### 2.1 Estrutura de Diretórios

```
src/main/java/com/jaasielsilva/labsystem/features/[modulo]/
├── controller/
│   └── [Modulo]Controller.java
├── service/
│   ├── [Modulo]Service.java
│   └── impl/
│       └── [Modulo]ServiceImpl.java
├── repository/
│   └── [Modulo]Repository.java
├── mapper/
│   └── [Modulo]Mapper.java
├── dto/
│   ├── [Modulo]Request.java
│   └── [Modulo]Response.java
└── entity/
    └── [Modulo].java
```

### 2.2 Entity (JPA)

**Arquivo:** `entity/[Modulo].java`

```java
package com.jaasielsilva.labsystem.features.[modulo].entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "[modulos]")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class [Modulo] {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Column(nullable = false)
    @Builder.Default
    private boolean ativo = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

### 2.3 Migration Flyway

**Arquivo:** `src/main/resources/db/migration/V[N]__create_table_[modulos].sql`

```sql
CREATE TABLE [modulos] (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id BIGINT        NOT NULL,
    nome       VARCHAR(150)  NOT NULL,
    ativo      BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP     NULL ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_[modulos]_empresa FOREIGN KEY (empresa_id) REFERENCES empresas(id)
);

CREATE INDEX idx_[modulos]_empresa_id ON [modulos](empresa_id);
CREATE INDEX idx_[modulos]_nome ON [modulos](nome);
```

> **Regras de migration:** tabelas em `snake_case` plural; toda tabela de **negócio** tem `empresa_id`, `id`, `created_at`, `updated_at`; nunca alterar banco manualmente; `ddl-auto: validate` em produção.

### 2.4 DTOs

**Arquivo:** `dto/[Modulo]Request.java`

```java
package com.jaasielsilva.labsystem.features.[modulo].dto;

import jakarta.validation.constraints.*;

public record [Modulo]Request(

    @NotBlank(message = "O nome é obrigatório")
    @Size(max = 150, message = "O nome não pode exceder 150 caracteres")
    String nome,

    @NotNull(message = "O status é obrigatório")
    Boolean ativo

) {}
```

**Arquivo:** `dto/[Modulo]Response.java`

```java
package com.jaasielsilva.labsystem.features.[modulo].dto;

import java.time.LocalDateTime;

public record [Modulo]Response(
    Long id,
    String nome,
    boolean ativo,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
```

### 2.5 Mapper (MapStruct)

**Arquivo:** `mapper/[Modulo]Mapper.java`

```java
package com.jaasielsilva.labsystem.features.[modulo].mapper;

import com.jaasielsilva.labsystem.features.[modulo].dto.[Modulo]Request;
import com.jaasielsilva.labsystem.features.[modulo].dto.[Modulo]Response;
import com.jaasielsilva.labsystem.features.[modulo].entity.[Modulo];
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface [Modulo]Mapper {
    [Modulo] toEntity([Modulo]Request request);
    [Modulo]Response toResponse([Modulo] entity);
    void updateEntity([Modulo]Request request, @MappingTarget [Modulo] entity);
}
```

### 2.6 Repository

**Arquivo:** `repository/[Modulo]Repository.java`

```java
package com.jaasielsilva.labsystem.features.[modulo].repository;

import com.jaasielsilva.labsystem.features.[modulo].entity.[Modulo];
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface [Modulo]Repository extends JpaRepository<[Modulo], Long> {

    Page<[Modulo]> findAllByEmpresaId(Long empresaId, Pageable pageable);

    Optional<[Modulo]> findByIdAndEmpresaId(Long id, Long empresaId);

    @Query("""
            SELECT e FROM [Modulo] e
            WHERE e.empresa.id = :empresaId
              AND LOWER(e.nome) LIKE LOWER(CONCAT('%', :term, '%'))
            """)
    Page<[Modulo]> searchByTermAndEmpresaId(
            @Param("empresaId") Long empresaId,
            @Param("term") String term,
            Pageable pageable);
}
```

### 2.7 Service

**Arquivo:** `service/[Modulo]Service.java`

```java
package com.jaasielsilva.labsystem.features.[modulo].service;

import com.jaasielsilva.labsystem.features.[modulo].dto.[Modulo]Request;
import com.jaasielsilva.labsystem.features.[modulo].dto.[Modulo]Response;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface [Modulo]Service {
    Page<[Modulo]Response> findAll(Pageable pageable, String search);
    [Modulo]Response findById(Long id);
    [Modulo]Response create([Modulo]Request request);
    [Modulo]Response update(Long id, [Modulo]Request request);
    void delete(Long id);
}
```

**Arquivo:** `service/impl/[Modulo]ServiceImpl.java`

```java
package com.jaasielsilva.labsystem.features.[modulo].service.impl;

import com.jaasielsilva.labsystem.common.TenantContext;
import com.jaasielsilva.labsystem.exception.ResourceNotFoundException;
import com.jaasielsilva.labsystem.features.empresa.entity.Empresa;
import com.jaasielsilva.labsystem.features.empresa.repository.EmpresaRepository;
import com.jaasielsilva.labsystem.features.[modulo].dto.[Modulo]Request;
import com.jaasielsilva.labsystem.features.[modulo].dto.[Modulo]Response;
import com.jaasielsilva.labsystem.features.[modulo].entity.[Modulo];
import com.jaasielsilva.labsystem.features.[modulo].mapper.[Modulo]Mapper;
import com.jaasielsilva.labsystem.features.[modulo].repository.[Modulo]Repository;
import com.jaasielsilva.labsystem.features.[modulo].service.[Modulo]Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class [Modulo]ServiceImpl implements [Modulo]Service {

    private final [Modulo]Repository repository;
    private final [Modulo]Mapper mapper;
    private final TenantContext tenantContext;
    private final EmpresaRepository empresaRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<[Modulo]Response> findAll(Pageable pageable, String search) {
        Long empresaId = tenantContext.requireTenantEmpresaId();
        if (search == null || search.isBlank()) {
            log.info("Buscando [modulos] paginados empresaId={}", empresaId);
            return repository.findAllByEmpresaId(empresaId, pageable).map(mapper::toResponse);
        }
        log.info("Buscando [modulos] com filtro empresaId={}", empresaId);
        return repository.searchByTermAndEmpresaId(empresaId, search.trim(), pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public [Modulo]Response findById(Long id) {
        Long empresaId = tenantContext.requireTenantEmpresaId();
        log.info("Buscando [modulo] id={} empresaId={}", id, empresaId);
        return mapper.toResponse(findOrThrow(id, empresaId));
    }

    @Override
    @Transactional
    public [Modulo]Response create([Modulo]Request request) {
        Long empresaId = tenantContext.requireTenantEmpresaId();
        log.info("Criando [modulo] empresaId={}", empresaId);
        [Modulo] entity = mapper.toEntity(request);
        Empresa empresa = empresaRepository.getReferenceById(empresaId);
        entity.setEmpresa(empresa);
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional
    public [Modulo]Response update(Long id, [Modulo]Request request) {
        Long empresaId = tenantContext.requireTenantEmpresaId();
        log.info("Atualizando [modulo] id={} empresaId={}", id, empresaId);
        [Modulo] entity = findOrThrow(id, empresaId);
        mapper.updateEntity(request, entity);
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Long empresaId = tenantContext.requireTenantEmpresaId();
        log.info("Deletando [modulo] id={} empresaId={}", id, empresaId);
        repository.delete(findOrThrow(id, empresaId));
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private [Modulo] findOrThrow(Long id, Long empresaId) {
        return repository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "[Modulo] não encontrado com ID: " + id));
    }
}
```

> **Quando usar `BusinessException`:** regras de negócio violadas (ex: exame com código duplicado, pedido já finalizado não pode ser editado). `ResourceNotFoundException` é para "entidade não existe". `BusinessException` é para "entidade existe mas a operação é inválida".

### 2.8 Controller

**Arquivo:** `controller/[Modulo]Controller.java`

```java
package com.jaasielsilva.labsystem.features.[modulo].controller;

import com.jaasielsilva.labsystem.common.ApiResponse;
import com.jaasielsilva.labsystem.features.[modulo].dto.[Modulo]Request;
import com.jaasielsilva.labsystem.features.[modulo].dto.[Modulo]Response;
import com.jaasielsilva.labsystem.features.[modulo].service.[Modulo]Service;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/[modulos]")
@RequiredArgsConstructor
public class [Modulo]Controller {

    private final [Modulo]Service service;

    @GetMapping
    @PreAuthorize("@tenantAccess.read()")
    public ResponseEntity<ApiResponse<Page<[Modulo]Response>>> getAll(
            @RequestParam(defaultValue = "0")    int page,
            @RequestParam(defaultValue = "10")   int size,
            @RequestParam(defaultValue = "nome") String sortBy,
            @RequestParam(defaultValue = "asc")  String sortDir,
            @RequestParam(required = false)      String q) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        return ResponseEntity.ok(ApiResponse.ok(
                service.findAll(PageRequest.of(page, size, sort), q)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@tenantAccess.read()")
    public ResponseEntity<ApiResponse<[Modulo]Response>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(service.findById(id)));
    }

    @PostMapping
    @PreAuthorize("@tenantAccess.write()")
    public ResponseEntity<ApiResponse<[Modulo]Response>> create(
            @Valid @RequestBody [Modulo]Request request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("[Modulo] criado com sucesso", service.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@tenantAccess.write()")
    public ResponseEntity<ApiResponse<[Modulo]Response>> update(
            @PathVariable Long id,
            @Valid @RequestBody [Modulo]Request request) {
        return ResponseEntity.ok(
                ApiResponse.ok("[Modulo] atualizado com sucesso", service.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@tenantAccess.admin()")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("[Modulo] removido com sucesso", null));
    }
}
```

### 2.9 Testes Unitários do Service

**Arquivo:** `src/test/java/.../features/[modulo]/service/[Modulo]ServiceImplTest.java`

```java
@ExtendWith(MockitoExtension.class)
class [Modulo]ServiceImplTest {

    @Mock [Modulo]Repository repository;
    @Mock [Modulo]Mapper mapper;
    @InjectMocks [Modulo]ServiceImpl service;

    // happy path — create
    @Test
    void create_deveRetornarResponse_quandoDadosValidos() {
        var request  = new [Modulo]Request("Nome Teste", true);
        var entity   = new [Modulo]();
        var response = new [Modulo]Response(1L, "Nome Teste", true, null, null);

        when(mapper.toEntity(request)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toResponse(entity)).thenReturn(response);

        var result = service.create(request);

        assertThat(result.nome()).isEqualTo("Nome Teste");
        verify(repository).save(entity);
    }

    // erro de negócio — findById com ID inexistente
    @Test
    void findById_deveLancarException_quandoNaoEncontrado() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }
}
```

> Cubra sempre: happy path de cada operação + pelo menos um erro de negócio por método crítico.

---

## 3. Frontend (Angular)

### 3.1 Estrutura de Diretórios

```
frontend/src/app/features/[modulo]/
├── pages/
│   ├── [modulo]-list/
│   │   ├── [modulo]-list.component.ts
│   │   ├── [modulo]-list.component.html
│   │   └── [modulo]-list.component.css
│   └── [modulo]-form/
│       ├── [modulo]-form.component.ts
│       ├── [modulo]-form.component.html
│       └── [modulo]-form.component.css
├── services/
│   └── [modulo].service.ts
├── models/
│   └── [modulo].model.ts
└── [modulo].routes.ts
```

### 3.2 Model

**Arquivo:** `models/[modulo].model.ts`

```typescript
export interface [Modulo] {
  id?: number;
  nome: string;
  // ... campos específicos do módulo
  ativo: boolean;
  createdAt?: string;
  updatedAt?: string;
}
```

### 3.3 Service Angular

**Arquivo:** `services/[modulo].service.ts`

```typescript
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { [Modulo] } from '../models/[modulo].model';

interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

interface PageResponse<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
}

@Injectable({ providedIn: 'root' })
export class [Modulo]Service {
  private http   = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/[modulos]`;

  getAll(page: number, size: number, sortBy: string, sortDir: string, search: string)
      : Observable<ApiResponse<PageResponse<[Modulo]>>> {
    return this.http.get<ApiResponse<PageResponse<[Modulo]>>>(
      `${this.apiUrl}?page=${page}&size=${size}&sortBy=${sortBy}&sortDir=${sortDir}&search=${search}`
    );
  }

  getById(id: number): Observable<ApiResponse<[Modulo]>> {
    return this.http.get<ApiResponse<[Modulo]>>(`${this.apiUrl}/${id}`);
  }

  create(data: [Modulo]): Observable<ApiResponse<[Modulo]>> {
    return this.http.post<ApiResponse<[Modulo]>>(this.apiUrl, data);
  }

  update(id: number, data: [Modulo]): Observable<ApiResponse<[Modulo]>> {
    return this.http.put<ApiResponse<[Modulo]>>(`${this.apiUrl}/${id}`, data);
  }

  delete(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/${id}`);
  }
}
```

### 3.4 List Component

**Arquivo:** `pages/[modulo]-list/[modulo]-list.component.ts`

```typescript
import { Component, ElementRef, OnDestroy, OnInit, ViewChild, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { Subject, debounceTime, distinctUntilChanged, finalize, takeUntil, timeout } from 'rxjs';
import { [Modulo]Service } from '../../services/[modulo].service';
import { [Modulo] } from '../../models/[modulo].model';
import { AuthService } from '../../../../core/services/auth.service';
import { ToastService } from '../../../../core/services/toast.service';

@Component({
  selector: 'app-[modulo]-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './[modulo]-list.component.html',
  styleUrls: ['./[modulo]-list.component.css']
})
export class [Modulo]ListComponent implements OnInit, OnDestroy {
  @ViewChild('searchInput') searchInput?: ElementRef<HTMLInputElement>;

  private [modulo]Service = inject([Modulo]Service);
  private toast           = inject(ToastService);
  private router          = inject(Router);
  protected auth          = inject(AuthService);

  private readonly destroy$       = new Subject<void>();
  private readonly searchChanges$ = new Subject<string>();

  [modulos]: [Modulo][] = [];
  currentPage   = 0;
  pageSize      = 10;
  totalPages    = 0;
  totalElements = 0;
  sortBy        = 'nome';
  sortDir       = 'asc';
  searchQuery   = '';

  loading        = signal(false);
  editingLoading = signal(false);

  private editNavigateTimer?: ReturnType<typeof setTimeout>;
  private static readonly EDIT_LOADING_MS = 2000;

  confirmDeleteId: number | null = null;

  private readonly sortLabels: Record<string, string> = {
    nome: 'nome'
  };

  get sortLabel(): string   { return this.sortLabels[this.sortBy] ?? this.sortBy; }
  get hasActiveSearch(): boolean { return this.searchQuery.trim().length > 0; }

  get fillerRows(): null[] {
    if (this.loading() || this.editingLoading() || this.[modulos].length === 0) return [];
    const missing = this.pageSize - this.[modulos].length;
    return missing > 0 ? Array(missing).fill(null) : [];
  }

  ngOnInit(): void {
    this.searchChanges$
      .pipe(debounceTime(350), distinctUntilChanged(), takeUntil(this.destroy$))
      .subscribe(term => { this.searchQuery = term; this.currentPage = 0; this.load[Modulos](); });

    this.load[Modulos]();
  }

  ngOnDestroy(): void {
    clearTimeout(this.editNavigateTimer);
    this.destroy$.next();
    this.destroy$.complete();
  }

  load[Modulos](): void {
    this.loading.set(true);
    this.[modulo]Service
      .getAll(this.currentPage, this.pageSize, this.sortBy, this.sortDir, this.searchQuery)
      .pipe(timeout(15000), finalize(() => this.loading.set(false)))
      .subscribe({
        next: res => {
          if (res.success && res.data) {
            this.[modulos]      = res.data.content      ?? [];
            this.totalPages    = res.data.totalPages    ?? 0;
            this.totalElements = res.data.totalElements ?? 0;
          } else {
            this.toast.error(res.message || 'Falha ao carregar [modulos].');
          }
        },
        error: err => {
          this.toast.error(
            err.name === 'TimeoutError'
              ? 'O servidor demorou para responder.'
              : err.error?.message || 'Erro ao se conectar com o servidor.'
          );
        }
      });
  }

  onEdit(id: number): void {
    if (this.editingLoading() || this.loading()) return;
    this.editingLoading.set(true);
    let prefetched: [Modulo] | null = null;

    this.[modulo]Service.getById(id).subscribe({
      next: res => { if (res.success && res.data) prefetched = res.data; }
    });

    this.editNavigateTimer = setTimeout(() => {
      this.router.navigate(['/[modulos]/editar', id], { state: { [modulo]: prefetched } });
      this.editingLoading.set(false);
    }, [Modulo]ListComponent.EDIT_LOADING_MS);
  }

  onSearchInput(event: Event): void {
    const value = (event.target as HTMLInputElement).value;
    this.searchQuery = value;
    this.searchChanges$.next(value);
  }

  clearSearch(): void {
    this.searchQuery = '';
    if (this.searchInput?.nativeElement) this.searchInput.nativeElement.value = '';
    this.currentPage = 0;
    this.load[Modulos]();
  }

  changePage(page: number): void {
    if (page >= 0 && page < this.totalPages) { this.currentPage = page; this.load[Modulos](); }
  }

  setSort(column: string): void {
    this.sortDir = this.sortBy === column && this.sortDir === 'asc' ? 'desc' : 'asc';
    this.sortBy  = column;
    this.currentPage = 0;
    this.load[Modulos]();
  }

  askDelete(id: number): void    { this.confirmDeleteId = id; }
  cancelDelete(): void           { this.confirmDeleteId = null; }

  delete[Modulo](id: number): void {
    this.[modulo]Service.delete(id).subscribe({
      next: res => {
        if (res.success) {
          this.toast.success('[Modulo] removido com sucesso.');
          this.confirmDeleteId = null;
          this.load[Modulos]();
        } else {
          this.toast.error(res.message || 'Erro ao remover [modulo].');
        }
      },
      error: () => this.toast.error('Erro ao remover [modulo].')
    });
  }
}
```

**Arquivo:** `pages/[modulo]-list/[modulo]-list.component.html`

```html
<div class="page-content animate-fade-in">
  <header class="page-header">
    <div class="title-group">
      <h1 class="page-title-sm">[Modulos]</h1>
      <p class="page-subtitle">Descrição do módulo</p>
    </div>
    <button class="btn btn-primary btn-sm" routerLink="/[modulos]/novo" *ngIf="auth.canEdit()">
      <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none"
           stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
        <line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/>
      </svg>
      Novo [modulo]
    </button>
  </header>

  <div class="glass-card table-wrapper">
    <!-- Toolbar -->
    <div class="table-toolbar">
      <div class="table-search">
        <svg class="search-icon" xmlns="http://www.w3.org/2000/svg" width="16" height="16"
             viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
             stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
          <circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/>
        </svg>
        <input #searchInput type="search" class="form-control table-search-input"
               placeholder="Buscar..." (input)="onSearchInput($event)"
               aria-label="Buscar [modulos]"/>
        <button type="button" class="search-clear" *ngIf="hasActiveSearch"
                (click)="clearSearch()" aria-label="Limpar busca">×</button>
      </div>
      <div class="table-toolbar-end">
        <span class="table-toolbar-meta">
          @if (loading()) { Carregando... }
          @else if (totalElements === 0 && hasActiveSearch) { Nenhum resultado }
          @else if (totalElements === 0) { Nenhum registro }
          @else { {{ totalElements }} {{ totalElements === 1 ? '[modulo]' : '[modulos]' }} }
        </span>
        @if (totalElements > 0 && !loading()) {
          <span class="stat-chip">Ordenado por {{ sortLabel }}</span>
        }
      </div>
    </div>

    <!-- Loading overlay -->
    <div class="loader-overlay" *ngIf="loading() || editingLoading()">
      <div class="loader-content">
        <div class="spinner spinner-sm-table"></div>
        <p class="loader-label" *ngIf="editingLoading()">Abrindo edição...</p>
      </div>
    </div>

    <!-- Tabela -->
    <table class="premium-table dense">
      <thead>
        <tr>
          <th (click)="setSort('nome')" class="sortable">
            Nome
            <span class="sort-icon" *ngIf="sortBy === 'nome'">{{ sortDir === 'asc' ? '▲' : '▼' }}</span>
          </th>
          <th>Status</th>
          <th class="actions-header">Ações</th>
        </tr>
      </thead>
      <tbody>
        <tr *ngFor="let m of [modulos]" [class.inactive-row]="!m.ativo">
          <td><span class="cell-name">{{ m.nome }}</span></td>
          <td>
            <span class="badge" [ngClass]="m.ativo ? 'badge-green' : 'badge-gray'">
              {{ m.ativo ? 'Ativo' : 'Inativo' }}
            </span>
          </td>
          <td>
            <div class="action-buttons" *ngIf="confirmDeleteId !== m.id">
              <button type="button" class="action-btn edit-btn" *ngIf="auth.canEdit()"
                      (click)="onEdit(m.id!)" [disabled]="editingLoading()"
                      title="Editar" aria-label="Editar">
                <svg xmlns="http://www.w3.org/2000/svg" width="15" height="15" viewBox="0 0 24 24"
                     fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
                  <path d="M18.5 2.5a2.121 2.121 0 1 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
                </svg>
              </button>
              <button type="button" class="action-btn delete-btn" *ngIf="auth.canDelete()"
                      (click)="askDelete(m.id!)" title="Remover" aria-label="Remover">
                <svg xmlns="http://www.w3.org/2000/svg" width="15" height="15" viewBox="0 0 24 24"
                     fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <polyline points="3 6 5 6 21 6"/>
                  <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
                </svg>
              </button>
            </div>
            <div class="confirm-delete-actions animate-fade-in" *ngIf="confirmDeleteId === m.id">
              <span class="confirm-text">Remover?</span>
              <button type="button" class="confirm-btn yes-btn" (click)="delete[Modulo](m.id!)">Sim</button>
              <button type="button" class="confirm-btn no-btn"  (click)="cancelDelete()">Não</button>
            </div>
          </td>
        </tr>

        <!-- Filler rows para manter altura fixa -->
        <tr *ngFor="let _ of fillerRows" class="row-filler" aria-hidden="true">
          <td><span class="cell-name filler-spacer">&nbsp;</span></td>
          <td><span class="badge badge-gray filler-spacer">&nbsp;</span></td>
          <td><span class="filler-spacer action-spacer">&nbsp;</span></td>
        </tr>

        <!-- Empty state -->
        <tr *ngIf="[modulos].length === 0 && !loading()">
          <td colspan="3">
            <div class="empty-state">
              <div class="empty-state-icon">
                <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24"
                     fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                  <rect x="3" y="3" width="18" height="18" rx="2"/><line x1="3" y1="9" x2="21" y2="9"/>
                  <line x1="9" y1="21" x2="9" y2="9"/>
                </svg>
              </div>
              @if (hasActiveSearch) {
                <p>Nenhum [modulo] encontrado para <strong>{{ searchQuery }}</strong>.</p>
                <button type="button" class="empty-cta btn-link" (click)="clearSearch()">Limpar busca</button>
              } @else {
                <p>Nenhum [modulo] cadastrado.</p>
                <a *ngIf="auth.canEdit()" routerLink="/[modulos]/novo" class="empty-cta">
                  Cadastrar primeiro [modulo]
                </a>
              }
            </div>
          </td>
        </tr>
      </tbody>
    </table>

    <!-- Paginação -->
    <footer class="table-footer" *ngIf="totalElements > 0">
      <span class="pagination-info">
        Página {{ currentPage + 1 }} de {{ totalPages || 1 }}
        · {{ [modulos].length }} de {{ totalElements }}
      </span>
      <div class="pagination-controls">
        <button type="button" class="page-btn btn-sm"
                [disabled]="currentPage === 0 || loading()"
                (click)="changePage(currentPage - 1)">Anterior</button>
        <button type="button" class="page-btn btn-sm"
                [disabled]="currentPage >= totalPages - 1 || loading()"
                (click)="changePage(currentPage + 1)">Próxima</button>
      </div>
    </footer>
  </div>
</div>
```

### 3.5 Form Component

**Arquivo:** `pages/[modulo]-form/[modulo]-form.component.ts`

```typescript
import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { [Modulo]Service } from '../../services/[modulo].service';
import { [Modulo] } from '../../models/[modulo].model';
import { ToastService } from '../../../../core/services/toast.service';

@Component({
  selector: 'app-[modulo]-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './[modulo]-form.component.html',
  styleUrls: ['./[modulo]-form.component.css']
})
export class [Modulo]FormComponent implements OnInit {
  private fb             = inject(FormBuilder);
  private [modulo]Service = inject([Modulo]Service);
  private toast          = inject(ToastService);
  private router         = inject(Router);
  private route          = inject(ActivatedRoute);

  [modulo]Form!: FormGroup;
  isEditMode = false;
  [modulo]Id?: number;
  loading = false;
  saving  = false;

  ngOnInit(): void {
    this.initForm();
    this.checkEditMode();
  }

  private initForm(): void {
    this.[modulo]Form = this.fb.group({
      nome: ['', [Validators.required, Validators.maxLength(150)]],
      // ... campos específicos do módulo
      ativo: [true]
    });
  }

  private checkEditMode(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (!idParam) return;

    this.isEditMode = true;
    this.[modulo]Id  = Number(idParam);

    const prefetched = history.state?.['[modulo]'] as [Modulo] | null | undefined;
    prefetched ? this.apply[Modulo](prefetched) : this.load[Modulo](this.[modulo]Id);
  }

  private apply[Modulo](data: [Modulo]): void {
    this.[modulo]Form.patchValue({ nome: data.nome, ativo: data.ativo });
    this.loading = false;
  }

  private load[Modulo](id: number): void {
    this.loading = true;
    this.[modulo]Service.getById(id)
      .pipe(finalize(() => this.loading = false))
      .subscribe({
        next: res => {
          if (res.success && res.data) this.apply[Modulo](res.data);
          else this.toast.error(res.message || 'Erro ao carregar [modulo].');
        },
        error: () => this.toast.error('Erro ao se conectar com o servidor.')
      });
  }

  onSubmit(): void {
    if (this.[modulo]Form.invalid) { this.[modulo]Form.markAllAsTouched(); return; }

    this.saving = true;
    const data: [Modulo] = this.[modulo]Form.value;

    const request$ = this.isEditMode && this.[modulo]Id
      ? this.[modulo]Service.update(this.[modulo]Id, data)
      : this.[modulo]Service.create(data);

    request$.subscribe({
      next: res => {
        if (res.success) {
          this.toast.success(this.isEditMode ? '[Modulo] atualizado com sucesso.' : '[Modulo] cadastrado com sucesso.');
          this.router.navigate(['/[modulos]']);
        } else {
          this.toast.error(res.message || 'Erro ao salvar [modulo].');
          this.saving = false;
        }
      },
      error: err => {
        this.toast.error(err.error?.message || 'Ocorreu um erro inesperado no servidor.');
        this.saving = false;
      }
    });
  }

  get f() { return this.[modulo]Form.controls; }
}
```

### 3.6 Routes

**Arquivo:** `[modulo].routes.ts`

```typescript
import { Routes } from '@angular/router';
import { [Modulo]ListComponent } from './pages/[modulo]-list/[modulo]-list.component';
import { [Modulo]FormComponent }  from './pages/[modulo]-form/[modulo]-form.component';
import { roleGuard } from '../../core/guards/role.guard';

export const [MODULO]_ROUTES: Routes = [
  { path: '',           component: [Modulo]ListComponent },
  { path: 'novo',       component: [Modulo]FormComponent, canActivate: [roleGuard('ADMIN', 'OPERADOR')] },
  { path: 'editar/:id', component: [Modulo]FormComponent, canActivate: [roleGuard('ADMIN', 'OPERADOR')] }
];
```

### 3.7 Registrar no App

**`frontend/src/app/app.routes.ts`** — adicionar dentro do children do AppShell:

```typescript
{
  path: '[modulos]',
  loadChildren: () => import('./features/[modulo]/[modulo].routes').then(m => m.[MODULO]_ROUTES)
}
```

**`frontend/src/app/core/navigation/nav.config.ts`** — adicionar item de menu:

```typescript
{
  id:    '[modulo]',
  label: '[Modulos]',
  route: '/[modulos]',
  icon:  '🔷',           // substituir por SVG inline se disponível
  roles: ['ADMIN', 'OPERADOR', 'VISUALIZADOR']
}
```

---

## 4. Padrões de Referência Rápida

### Segurança (RBAC + tenant)

| Operação | Backend | Roles (tenant) |
|---|---|---|
| Listar / Visualizar | `@tenantAccess.read()` | ADMIN, OPERADOR, VISUALIZADOR (+ SUPER_ADMIN em impersonação) |
| Criar / Editar | `@tenantAccess.write()` | ADMIN, OPERADOR |
| Deletar | `@tenantAccess.admin()` | ADMIN |

- Permissão validada **sempre no backend** — `@tenantAccess` considera impersonação
- Frontend: `auth.canEdit()` / `canDelete()` e `hasTenantAccess()` no menu
- Backend rejeita com 403 se burlar; isolamento por `empresa_id` do JWT, nunca do body

### Exceções

| Situação | Exceção |
|---|---|
| Entidade não encontrada | `ResourceNotFoundException` |
| Regra de negócio violada | `BusinessException` |

### Toast (feedback obrigatório)

```typescript
this.toast.success('...');  // CRUD ok — 4s
this.toast.error('...');    // falha de API — 6s
this.toast.warning('...');  // aviso de negócio — 5s
```

Nunca duplicar com `alert` inline para o mesmo evento.

### UX — estados obrigatórios em toda tela

| Estado | Implementação |
|---|---|
| Loading | Spinner + desabilitar ações |
| Empty | Mensagem + CTA ("Cadastrar primeiro") |
| Error | `toast.error()` com mensagem clara, sem stack trace |
| Success | `toast.success()` após salvar / excluir |

---

## 5. Definition of Done (DoD)

Só marque o módulo como pronto quando **todos** os itens estiverem ✅.

### Produto
- [ ] Resolve a dor definida na seção 0
- [ ] Usuário completa o fluxo sem ajuda

### Código
- [ ] Ordem de execução da seção 1 seguida sem pular etapas
- [ ] Testes unitários: happy path + erro de negócio por operação crítica
- [ ] Migration testada e aplicada com sucesso
- [ ] Nenhuma Entity exposta diretamente na API

### UX
- [ ] Loading, empty, error e success implementados
- [ ] Validação inline no formulário (campo + mensagem após `touched`)
- [ ] Textos em português, sem jargão técnico

### Segurança
- [ ] `@tenantAccess` em todos os endpoints de negócio
- [ ] Queries filtradas por `empresa_id` do JWT (`TenantContext`)
- [ ] Frontend esconde ações sem permissão
- [ ] Nenhum secret hardcoded

### Operação
- [ ] Logs SLF4J nas operações relevantes
- [ ] `ARQUITETURA.md` atualizado (tabela de módulos)
- [ ] Skill atualizada (seção "Estado atual")

---

## 6. Anti-padrões (nunca fazer)

- Backend completo sem tela para o usuário
- Permissão só no frontend
- `System.out.println` em vez de SLF4J
- Alterar banco manualmente (sem Flyway)
- Alert inline para feedback de ação (use toast)
- `ddl-auto: create` ou `update` em produção
- Emojis em ícones de ação (use SVG)
- Começar a codar sem responder a seção 0
- Módulo de negócio sem `empresa_id` na migration
- Query sem filtro por `empresa_id`
- Aceitar `empresaId` do body do cliente para decidir tenant