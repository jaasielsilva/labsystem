import { Component, ElementRef, OnDestroy, OnInit, ViewChild, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { Subject, debounceTime, distinctUntilChanged, finalize, takeUntil, timeout } from 'rxjs';
import { EmpresaService } from '../../services/empresa.service';
import { Empresa } from '../../models/empresa.model';
import { AuthService } from '../../../../../core/services/auth.service';
import { ToastService } from '../../../../../core/services/toast.service';
import { maskCnpj } from '../../../../../shared/utils/cnpj.utils';

@Component({
  selector: 'app-empresa-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './empresa-list.component.html',
  styleUrls: ['./empresa-list.component.css']
})
export class EmpresaListComponent implements OnInit, OnDestroy {
  @ViewChild('searchInput') searchInput?: ElementRef<HTMLInputElement>;

  private empresaService = inject(EmpresaService);
  private toast = inject(ToastService);
  private router = inject(Router);
  protected auth = inject(AuthService);

  private readonly destroy$ = new Subject<void>();
  private readonly searchChanges$ = new Subject<string>();

  empresas: Empresa[] = [];

  currentPage = 0;
  pageSize = 10;
  totalPages = 0;
  totalElements = 0;

  sortBy = 'nome';
  sortDir = 'asc';

  searchQuery = '';

  loading = signal(false);
  editingLoading = signal(false);

  confirmDeleteId: number | null = null;

  private editNavigateTimer?: ReturnType<typeof setTimeout>;
  private static readonly EDIT_LOADING_MS = 2000;

  private readonly sortLabels: Record<string, string> = {
    nome: 'nome',
    cnpj: 'CNPJ',
    email: 'e-mail'
  };

  get sortLabel(): string {
    return this.sortLabels[this.sortBy] ?? this.sortBy;
  }

  get hasActiveSearch(): boolean {
    return this.searchQuery.trim().length > 0;
  }

  get fillerRows(): null[] {
    if (this.loading() || this.editingLoading() || this.empresas.length === 0) {
      return [];
    }

    const missing = this.pageSize - this.empresas.length;

    return missing > 0
      ? Array.from({ length: missing }, () => null)
      : [];
  }

  ngOnInit(): void {
    this.searchChanges$
      .pipe(
        debounceTime(350),
        distinctUntilChanged(),
        takeUntil(this.destroy$)
      )
      .subscribe((term) => {
        this.searchQuery = term;
        this.currentPage = 0;
        this.loadEmpresas();
      });

    this.loadEmpresas();
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

    let prefetchedEmpresa: Empresa | null = null;

    this.empresaService.getById(id).subscribe({
      next: (response) => {
        if (response.success && response.data) {
          prefetchedEmpresa = response.data;
        }
      }
    });

    this.editNavigateTimer = setTimeout(() => {
      this.router.navigate(['/plataforma/laboratorios/editar', id], {
        state: { empresa: prefetchedEmpresa }
      });

      this.editingLoading.set(false);
    }, EmpresaListComponent.EDIT_LOADING_MS);
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
    this.loadEmpresas();
  }

  loadEmpresas(): void {
    this.loading.set(true);

    this.empresaService
      .getAll(
        this.currentPage,
        this.pageSize,
        this.sortBy,
        this.sortDir,
        this.searchQuery
      )
      .pipe(
        timeout(15000),
        finalize(() => this.loading.set(false))
      )
      .subscribe({
        next: (response) => {
          if (response.success && response.data) {
            this.empresas = response.data.content ?? [];
            this.totalPages = response.data.totalPages ?? 0;
            this.totalElements = response.data.totalElements ?? 0;
          } else {
            this.toast.error(
              response.message || 'Falha ao carregar empresas.'
            );
          }
        },
        error: (err) => {
          if (err.name === 'TimeoutError') {
            this.toast.error('O servidor demorou para responder. Verifique se o backend está rodando.');
          } else {
            this.toast.error(
              err.error?.message ||
              'Erro ao se conectar com o servidor.'
            );
          }
        }
      });
  }

  changePage(page: number): void {
    if (page >= 0 && page < this.totalPages) {
      this.currentPage = page;
      this.loadEmpresas();
    }
  }

  setSort(column: string): void {
    if (this.sortBy === column) {
      this.sortDir = this.sortDir === 'asc'
        ? 'desc'
        : 'asc';
    } else {
      this.sortBy = column;
      this.sortDir = 'asc';
    }

    this.currentPage = 0;
    this.loadEmpresas();
  }

  askDelete(id: number): void {
    this.confirmDeleteId = id;
  }

  cancelDelete(): void {
    this.confirmDeleteId = null;
  }

  deleteEmpresa(id: number): void {
    this.empresaService.delete(id).subscribe({
      next: (response) => {
        if (response.success) {
          this.toast.success(
            'Empresa removida com sucesso.'
          );

          this.confirmDeleteId = null;
          this.loadEmpresas();
        } else {
          this.toast.error(
            response.message || 'Erro ao remover empresa.'
          );
        }
      },
      error: () => {
        this.toast.error('Erro ao remover empresa.');
      }
    });
  }
  formatPhone(phone?: string): string {
    if (!phone) return '-';

    if (phone.length === 11) {
      return phone.replace(
        /(\d{2})(\d{5})(\d{4})/,
        '($1) $2-$3'
      );
    }

    if (phone.length === 10) {
      return phone.replace(
        /(\d{2})(\d{4})(\d{4})/,
        '($1) $2-$3'
      );
    }

    return phone;
  }

  maskCnpj = maskCnpj;
}