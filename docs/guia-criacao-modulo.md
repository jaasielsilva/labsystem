# Guia para Criação de Novos Módulos

Este documento descreve o fluxo padrão para criar novos módulos no sistema LabSystem, cobrindo tanto o frontend (Angular) quanto o backend (Spring Boot).

---

## FRONTEND (Angular)

### 1. Estrutura de Diretórios

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

### 2. Criar Model (TypeScript)

**Arquivo**: `frontend/src/app/features/[modulo]/models/[modulo].model.ts`

```typescript
export interface [Modulo] {
  id?: number;
  campo1: string;
  campo2: number;
  // ... outros campos
  ativo: boolean;
  createdAt?: string;
  updatedAt?: string;
}
```

### 3. Criar Service

**Arquivo**: `frontend/src/app/features/[modulo]/services/[modulo].service.ts`

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

@Injectable({
  providedIn: 'root'
})
export class [Modulo]Service {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/[modulos]`;

  getAll(
    page: number,
    size: number,
    sortBy: string,
    sortDir: string,
    search: string
  ): Observable<ApiResponse<PageResponse<[Modulo]>>> {
    return this.http.get<ApiResponse<PageResponse<[Modulo]>>>(
      `${this.apiUrl}?page=${page}&size=${size}&sortBy=${sortBy}&sortDir=${sortDir}&search=${search}`
    );
  }

  getById(id: number): Observable<ApiResponse<[Modulo]>> {
    return this.http.get<ApiResponse<[Modulo]>>(`${this.apiUrl}/${id}`);
  }

  create([modulo]: [Modulo]): Observable<ApiResponse<[Modulo]>> {
    return this.http.post<ApiResponse<[Modulo]>>(this.apiUrl, [modulo]);
  }

  update(id: number, [modulo]: [Modulo]): Observable<ApiResponse<[Modulo]>> {
    return this.http.put<ApiResponse<[Modulo]>>(`${this.apiUrl}/${id}`, [modulo]);
  }

  delete(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/${id}`);
  }
}
```

### 4. Criar List Component

**Arquivo**: `frontend/src/app/features/[modulo]/pages/[modulo]-list/[modulo]-list.component.ts`

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
  private toast = inject(ToastService);
  private router = inject(Router);
  protected auth = inject(AuthService);

  private readonly destroy$ = new Subject<void>();
  private readonly searchChanges$ = new Subject<string>();

  [modulos]: [Modulo][] = [];
  currentPage = 0;
  pageSize = 10;
  totalPages = 0;
  totalElements = 0;
  sortBy = 'nome';
  sortDir = 'asc';
  searchQuery = '';

  loading = signal(false);
  editingLoading = signal(false);

  private editNavigateTimer?: ReturnType<typeof setTimeout>;
  private static readonly EDIT_LOADING_MS = 2000;

  confirmDeleteId: number | null = null;

  private readonly sortLabels: Record<string, string> = {
    nome: 'nome',
    codigo: 'código'
  };

  get sortLabel(): string {
    return this.sortLabels[this.sortBy] ?? this.sortBy;
  }

  get hasActiveSearch(): boolean {
    return this.searchQuery.trim().length > 0;
  }

  get fillerRows(): null[] {
    if (this.loading() || this.editingLoading() || this.[modulos].length === 0) {
      return [];
    }
    const missing = this.pageSize - this.[modulos].length;
    return missing > 0 ? Array.from({ length: missing }, () => null) : [];
  }

  ngOnInit(): void {
    this.searchChanges$
      .pipe(debounceTime(350), distinctUntilChanged(), takeUntil(this.destroy$))
      .subscribe((term) => {
        this.searchQuery = term;
        this.currentPage = 0;
        this.load[Modulos]();
      });

    this.load[Modulos]();
  }

  ngOnDestroy(): void {
    if (this.editNavigateTimer) {
      clearTimeout(this.editNavigateTimer);
    }
    this.destroy$.next();
    this.destroy$.complete();
  }

  onEdit(id: number): void {
    if (this.editingLoading() || this.loading()) {
      return;
    }

    this.editingLoading.set(true);
    let prefetched[Modulo]: [Modulo] | null = null;

    this.[modulo]Service.getById(id).subscribe({
      next: (response) => {
        if (response.success && response.data) {
          prefetched[Modulo] = response.data;
        }
      }
    });

    this.editNavigateTimer = setTimeout(() => {
      this.router.navigate(['/[modulos]/editar', id], {
        state: { [modulo]: prefetched[Modulo] }
      });
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
    if (this.searchInput?.nativeElement) {
      this.searchInput.nativeElement.value = '';
    }
    this.currentPage = 0;
    this.load[Modulos]();
  }

  load[Modulos](): void {
    this.loading.set(true);
    this.[modulo]Service
      .getAll(this.currentPage, this.pageSize, this.sortBy, this.sortDir, this.searchQuery)
      .pipe(
        timeout(15000),
        finalize(() => this.loading.set(false))
      )
      .subscribe({
        next: (response) => {
          if (response.success && response.data) {
            this.[modulos] = response.data.content ?? [];
            this.totalPages = response.data.totalPages ?? 0;
            this.totalElements = response.data.totalElements ?? 0;
          } else {
            this.toast.error(response.message || 'Falha ao carregar [modulos].');
          }
        },
        error: (err) => {
          if (err.name === 'TimeoutError') {
            this.toast.error('O servidor demorou para responder. Verifique se o backend está rodando.');
          } else {
            this.toast.error(err.error?.message || 'Erro ao se conectar com o servidor.');
          }
        }
      });
  }

  changePage(page: number): void {
    if (page >= 0 && page < this.totalPages) {
      this.currentPage = page;
      this.load[Modulos]();
    }
  }

  setSort(column: string): void {
    if (this.sortBy === column) {
      this.sortDir = this.sortDir === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortBy = column;
      this.sortDir = 'asc';
    }
    this.currentPage = 0;
    this.load[Modulos]();
  }

  askDelete(id: number): void {
    this.confirmDeleteId = id;
  }

  cancelDelete(): void {
    this.confirmDeleteId = null;
  }

  delete[Modulo](id: number): void {
    this.[modulo]Service.delete(id).subscribe({
      next: (response) => {
        if (response.success) {
          this.toast.success('[Modulo] removido com sucesso.');
          this.confirmDeleteId = null;
          this.load[Modulos]();
        } else {
          this.toast.error(response.message || 'Erro ao remover [modulo].');
        }
      },
      error: () => {
        this.toast.error('Erro ao remover [modulo].');
      }
    });
  }
}
```

**Arquivo**: `frontend/src/app/features/[modulo]/pages/[modulo]-list/[modulo]-list.component.html`

```html
<div class="page-content animate-fade-in">
  <header class="page-header">
    <div class="title-group">
      <h1 class="page-title-sm">[Modulos]</h1>
      <p class="page-subtitle">Descrição do módulo</p>
    </div>
    <button class="btn btn-primary btn-sm" routerLink="/[modulos]/novo" *ngIf="auth.canEdit()">
      <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><line x1="12" y1="5" x2="12" y2="19"></line><line x1="5" y1="12" x2="19" y2="12"></line></svg>
      Novo [modulo]
    </button>
  </header>

  <div class="glass-card table-wrapper">
    <div class="table-toolbar">
      <div class="table-search">
        <svg class="search-icon" xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><circle cx="11" cy="11" r="8"></circle><line x1="21" y1="21" x2="16.65" y2="16.65"></line></svg>
        <input
          #searchInput
          type="search"
          class="form-control table-search-input"
          placeholder="Buscar..."
          (input)="onSearchInput($event)"
          aria-label="Buscar [modulos]"
        />
        <button
          type="button"
          class="search-clear"
          *ngIf="hasActiveSearch"
          (click)="clearSearch()"
          aria-label="Limpar busca"
        >
          ×
        </button>
      </div>
      <div class="table-toolbar-end">
        <span class="table-toolbar-meta">
          @if (loading()) {
            Carregando...
          } @else if (totalElements === 0 && hasActiveSearch) {
            Nenhum resultado
          } @else if (totalElements === 0) {
            Nenhum registro
          } @else {
            {{ totalElements }} {{ totalElements === 1 ? '[modulo]' : '[modulos]' }}
          }
        </span>
        @if (totalElements > 0 && !loading()) {
          <span class="stat-chip">Ordenado por {{ sortLabel }}</span>
        }
      </div>
    </div>

    <div class="loader-overlay" *ngIf="loading() || editingLoading()">
      <div class="loader-content">
        <div class="spinner spinner-sm-table"></div>
        <p class="loader-label" *ngIf="editingLoading()">Abrindo edição...</p>
      </div>
    </div>

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
              <button
                type="button"
                class="action-btn edit-btn"
                *ngIf="auth.canEdit()"
                (click)="onEdit(m.id!)"
                [disabled]="editingLoading()"
                title="Editar"
                aria-label="Editar"
              >
                <svg xmlns="http://www.w3.org/2000/svg" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path><path d="M18.5 2.5a2.121 2.121 0 1 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path></svg>
              </button>
              <button type="button" class="action-btn delete-btn" *ngIf="auth.canDelete()" (click)="askDelete(m.id!)" title="Remover" aria-label="Remover">
                <svg xmlns="http://www.w3.org/2000/svg" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="3 6 5 6 21 6"></polyline><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path></svg>
              </button>
            </div>
            <div class="confirm-delete-actions animate-fade-in" *ngIf="confirmDeleteId === m.id">
              <span class="confirm-text">Remover?</span>
              <button type="button" class="confirm-btn yes-btn" (click)="delete[Modulo](m.id!)">Sim</button>
              <button type="button" class="confirm-btn no-btn" (click)="cancelDelete()">Não</button>
            </div>
          </td>
        </tr>
        <tr *ngFor="let _ of fillerRows" class="row-filler" aria-hidden="true">
          <td><span class="cell-name filler-spacer">&nbsp;</span></td>
          <td><span class="badge badge-gray filler-spacer">&nbsp;</span></td>
          <td><span class="filler-spacer action-spacer">&nbsp;</span></td>
        </tr>
        <tr *ngIf="[modulos].length === 0 && !loading()">
          <td colspan="3">
            <div class="empty-state">
              <div class="empty-state-icon">{{ hasActiveSearch ? '🔍' : '📋' }}</div>
              @if (hasActiveSearch) {
                <p>Nenhum [modulo] encontrado para <strong>{{ searchQuery }}</strong>.</p>
                <button type="button" class="empty-cta btn-link" (click)="clearSearch()">Limpar busca</button>
              } @else {
                <p>Nenhum [modulo] cadastrado.</p>
                <a *ngIf="auth.canEdit()" routerLink="/[modulos]/novo" class="empty-cta">Cadastrar primeiro [modulo]</a>
              }
            </div>
          </td>
        </tr>
      </tbody>
    </table>

    <footer class="table-footer" *ngIf="totalElements > 0">
      <span class="pagination-info">
        Página {{ currentPage + 1 }} de {{ totalPages || 1 }}
        · {{ [modulos].length }} de {{ totalElements }}
      </span>
      <div class="pagination-controls">
        <button type="button" class="page-btn btn-sm" [disabled]="currentPage === 0 || loading()" (click)="changePage(currentPage - 1)">
          Anterior
        </button>
        <button type="button" class="page-btn btn-sm" [disabled]="currentPage >= totalPages - 1 || loading()" (click)="changePage(currentPage + 1)">
          Próxima
        </button>
      </div>
    </footer>
  </div>
</div>
```

### 5. Criar Form Component

**Arquivo**: `frontend/src/app/features/[modulo]/pages/[modulo]-form/[modulo]-form.component.ts`

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
  private fb = inject(FormBuilder);
  private [modulo]Service = inject([Modulo]Service);
  private toast = inject(ToastService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  [modulo]Form!: FormGroup;
  isEditMode = false;
  [modulo]Id?: number;
  loading = false;
  saving = false;

  ngOnInit(): void {
    this.initForm();
    this.checkEditMode();
  }

  private initForm(): void {
    this.[modulo]Form = this.fb.group({
      nome: ['', [Validators.required, Validators.maxLength(150)]],
      // outros campos...
      ativo: [true]
    });
  }

  private checkEditMode(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (!idParam) {
      return;
    }

    this.isEditMode = true;
    this.[modulo]Id = Number(idParam);

    const prefetched = history.state?.['[modulo]'] as [Modulo] | null | undefined;
    if (prefetched) {
      this.apply[Modulo](prefetched);
      return;
    }

    this.load[Modulo](this.[modulo]Id);
  }

  private apply[Modulo]([modulo]Data: [Modulo]): void {
    this.[modulo]Form.patchValue({
      nome: [modulo]Data.nome,
      // outros campos...
      ativo: [modulo]Data.ativo
    });
    this.loading = false;
  }

  private load[Modulo](id: number): void {
    this.loading = true;
    this.[modulo]Service.getById(id)
      .pipe(finalize(() => { this.loading = false; }))
      .subscribe({
        next: (response) => {
          if (response.success && response.data) {
            this.apply[Modulo](response.data);
          } else {
            this.toast.error(response.message || 'Erro ao carregar dados do [modulo].');
          }
        },
        error: () => {
          this.toast.error('Erro ao se conectar com o servidor.');
        }
      });
  }

  onSubmit(): void {
    if (this.[modulo]Form.invalid) {
      this.[modulo]Form.markAllAsTouched();
      return;
    }

    this.saving = true;
    const [modulo]Data: [Modulo] = this.[modulo]Form.value;

    const request$ = this.isEditMode && this.[modulo]Id
      ? this.[modulo]Service.update(this.[modulo]Id, [modulo]Data)
      : this.[modulo]Service.create([modulo]Data);

    request$.subscribe({
      next: (response) => {
        if (response.success) {
          this.toast.success(
            this.isEditMode ? '[Modulo] atualizado com sucesso.' : '[Modulo] cadastrado com sucesso.'
          );
          this.router.navigate(['/[modulos]']);
        } else {
          this.toast.error(response.message || 'Erro ao salvar [modulo].');
          this.saving = false;
        }
      },
      error: (err) => {
        this.toast.error(err.error?.message || 'Ocorreu um erro inesperado no servidor.');
        this.saving = false;
      }
    });
  }

  get f() {
    return this.[modulo]Form.controls;
  }
}
```

### 6. Criar Routes

**Arquivo**: `frontend/src/app/features/[modulo]/[modulo].routes.ts`

```typescript
import { Routes } from '@angular/router';
import { [Modulo]ListComponent } from './pages/[modulo]-list/[modulo]-list.component';
import { [Modulo]FormComponent } from './pages/[modulo]-form/[modulo]-form.component';
import { roleGuard } from '../../core/guards/role.guard';

export const [MODULO]_ROUTES: Routes = [
  { path: '', component: [Modulo]ListComponent },
  { path: 'novo', component: [Modulo]FormComponent, canActivate: [roleGuard('ADMIN', 'OPERADOR')] },
  { path: 'editar/:id', component: [Modulo]FormComponent, canActivate: [roleGuard('ADMIN', 'OPERADOR')] }
];
```

### 7. Adicionar Rota Principal

**Arquivo**: `frontend/src/app/app.routes.ts`

```typescript
{
  path: '[modulos]',
  loadChildren: () => import('./features/[modulo]/[modulo].routes').then(m => m.[MODULO]_ROUTES)
}
```

### 8. Adicionar Menu

**Arquivo**: `frontend/src/app/core/navigation/nav.config.ts`

```typescript
{
  id: '[modulo]',
  label: '[Modulos]',
  route: '/[modulos]',
  icon: '🔷',
  roles: ['ADMIN', 'OPERADOR', 'VISUALIZADOR']
}
```

---

## BACKEND (Spring Boot)

### 1. Estrutura de Diretórios

```
src/main/java/com/.../features/[modulo]/
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

### 2. Criar Entity (JPA)

**Arquivo**: `src/main/java/com/.../features/[modulo]/entity/[Modulo].java`

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

### 3. Criar DTOs

**Arquivo**: `src/main/java/com/.../features/[modulo]/dto/[Modulo]Request.java`

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

**Arquivo**: `src/main/java/com/.../features/[modulo]/dto/[Modulo]Response.java`

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

### 4. Criar Mapper (MapStruct)

**Arquivo**: `src/main/java/com/.../features/[modulo]/mapper/[Modulo]Mapper.java`

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

### 5. Criar Repository

**Arquivo**: `src/main/java/com/.../features/[modulo]/repository/[Modulo]Repository.java`

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
    @Query("""
            SELECT e FROM [Modulo] e WHERE
            LOWER(e.nome) LIKE LOWER(CONCAT('%', :term, '%'))
            """)
    Page<[Modulo]> searchByTerm(@Param("term") String term, Pageable pageable);
}
```

### 6. Criar Service

**Arquivo**: `src/main/java/com/.../features/[modulo]/service/[Modulo]Service.java`

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

**Arquivo**: `src/main/java/com/.../features/[modulo]/service/impl/[Modulo]ServiceImpl.java`

```java
package com.jaasielsilva.labsystem.features.[modulo].service.impl;

import com.jaasielsilva.labsystem.exception.ResourceNotFoundException;
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

    @Override
    @Transactional(readOnly = true)
    public Page<[Modulo]Response> findAll(Pageable pageable, String search) {
        if (search == null || search.isBlank()) {
            log.info("Buscando [modulos] paginados");
            return repository.findAll(pageable).map(mapper::toResponse);
        }

        String term = search.trim();
        log.info("Buscando [modulos] com filtro");
        return repository.searchByTerm(term, pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public [Modulo]Response findById(Long id) {
        log.info("Buscando [modulo] por id: {}", id);
        [Modulo] [modulo] = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("[Modulo] não encontrado com ID: " + id));
        return mapper.toResponse([modulo]);
    }

    @Override
    @Transactional
    public [Modulo]Response create([Modulo]Request request) {
        log.info("Criando novo [modulo]");
        [Modulo] [modulo] = mapper.toEntity(request);
        [Modulo] saved = repository.save([modulo]);
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public [Modulo]Response update(Long id, [Modulo]Request request) {
        log.info("Atualizando [modulo] com id: {}", id);
        [Modulo] [modulo] = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("[Modulo] não encontrado com ID: " + id));

        mapper.updateEntity(request, [modulo]);
        [Modulo] updated = repository.save([modulo]);
        return mapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.info("Deletando [modulo] com id: {}", id);
        [Modulo] [modulo] = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("[Modulo] não encontrado com ID: " + id));
        repository.delete([modulo]);
    }
}
```

### 7. Criar Controller

**Arquivo**: `src/main/java/com/.../features/[modulo]/controller/[Modulo]Controller.java`

```java
package com.jaasielsilva.labsystem.features.[modulo].controller;

import com.jaasielsilva.labsystem.common.ApiResponse;
import com.jaasielsilva.labsystem.features.[modulo].dto.[Modulo]Request;
import com.jaasielsilva.labsystem.features.[modulo].dto.[Modulo]Response;
import com.jaasielsilva.labsystem.features.[modulo].service.[Modulo]Service;
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
@RequestMapping("/api/v1/[modulos]")
@RequiredArgsConstructor
public class [Modulo]Controller {

    private final [Modulo]Service service;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR', 'VISUALIZADOR')")
    public ResponseEntity<ApiResponse<Page<[Modulo]Response>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "nome") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String q) {

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ?
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<[Modulo]Response> response = service.findAll(pageable, q);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR', 'VISUALIZADOR')")
    public ResponseEntity<ApiResponse<[Modulo]Response>> getById(@PathVariable Long id) {
        [Modulo]Response response = service.findById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR')")
    public ResponseEntity<ApiResponse<[Modulo]Response>> create(@Valid @RequestBody [Modulo]Request request) {
        [Modulo]Response response = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("[Modulo] criado com sucesso", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR')")
    public ResponseEntity<ApiResponse<[Modulo]Response>> update(
            @PathVariable Long id, 
            @Valid @RequestBody [Modulo]Request request) {
        [Modulo]Response response = service.update(id, request);
        return ResponseEntity.ok(ApiResponse.ok("[Modulo] atualizado com sucesso", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("[Modulo] removido com sucesso", null));
    }
}
```

### 8. Criar Migration (Flyway)

**Arquivo**: `src/main/resources/db/migration/V[N]__create_table_[modulos].sql`

```sql
CREATE TABLE [modulos] (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP
);
```

---

## Ordem de Execução

1. **Backend primeiro**: Entity → DTOs → Mapper → Repository → Service → Controller → Migration
2. **Frontend depois**: Model → Service → List Component → Form Component → Routes → Menu Principal
3. **Testar**: Reiniciar backend, testar endpoints, depois testar frontend

---

## Padrões Importantes

### Segurança
- Usar `@PreAuthorize` com roles apropriadas
- ADMIN: acesso total
- OPERADOR: criar, editar, visualizar
- VISUALIZADOR: apenas visualizar

### Validação
- Backend: usar `@Valid`, `@NotBlank`, `@Size`, `@Pattern` etc.
- Frontend: usar Validators do ReactiveForms

### Padrão de Resposta
- Sempre usar `ApiResponse<T>` no backend
- Estrutura: `{ success, message, data, timestamp }`

### Paginação
- Backend: usar `Page<T>` com `Pageable`
- Frontend: implementar paginação com `currentPage`, `pageSize`, `totalPages`

### Busca
- Backend: implementar `searchByTerm` com query customizada JPQL
- Frontend: debounce de 350ms na busca

### Frontend Padrões
- Usar signals para loading states
- Implementar `editingLoading` para prefetch em edição
- Usar SVGs em vez de emojis
- Implementar `fillerRows` para manter altura fixa da tabela
- Adicionar `aria-label` para acessibilidade
- Verificar permissões com `auth.canEdit()`, `auth.canDelete()`

### Backend Padrões
- Usar Lombok para reduzir código boilerplate
- Usar MapStruct para mapeamento Entity ↔ DTO
- Usar `@Transactional` nos métodos de serviço
- Implementar logs com `@Slf4j`
- Tratar exceções com `ResourceNotFoundException` e `BusinessException`
