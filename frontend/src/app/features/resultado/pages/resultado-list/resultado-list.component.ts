import { Component, ElementRef, OnDestroy, OnInit, ViewChild, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Subject, debounceTime, distinctUntilChanged, finalize, takeUntil, timeout } from 'rxjs';

import { ResultadoService } from '../../services/resultado.service';
import { ResultadoStatus, ResultadoSummary } from '../../models/resultado.model';
import { AuthService } from '../../../../core/services/auth.service';
import { ToastService } from '../../../../core/services/toast.service';

@Component({
  selector: 'app-resultado-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './resultado-list.component.html',
  styleUrls: ['./resultado-list.component.css']
})
export class ResultadoListComponent implements OnInit, OnDestroy {
  @ViewChild('searchInput') searchInput?: ElementRef<HTMLInputElement>;

  private resultadoService = inject(ResultadoService);
  private toast = inject(ToastService);
  protected auth = inject(AuthService);

  private readonly destroy$ = new Subject<void>();
  private readonly searchChanges$ = new Subject<string>();

  resultados: ResultadoSummary[] = [];
  currentPage = 0;
  pageSize = 10;
  totalPages = 0;
  totalElements = 0;
  sortBy = 'createdAt';
  sortDir = 'desc';
  searchQuery = '';
  loading = signal(false);

  ngOnInit(): void {
    this.searchChanges$
      .pipe(debounceTime(350), distinctUntilChanged(), takeUntil(this.destroy$))
      .subscribe((term) => {
        this.searchQuery = term;
        this.currentPage = 0;
        this.loadResultados();
      });
    this.loadResultados();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  get hasActiveSearch(): boolean {
    return this.searchQuery.trim().length > 0;
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
    this.loadResultados();
  }

  loadResultados(): void {
    this.loading.set(true);
    this.resultadoService
      .getAll(this.currentPage, this.pageSize, this.sortBy, this.sortDir, this.searchQuery)
      .pipe(timeout(15000), finalize(() => this.loading.set(false)))
      .subscribe({
        next: (response) => {
          if (response.success && response.data) {
            this.resultados = response.data.content ?? [];
            this.totalPages = response.data.totalPages ?? 0;
            this.totalElements = response.data.totalElements ?? 0;
          } else {
            this.toast.error(response.message || 'Falha ao carregar resultados.');
          }
        },
        error: (err) => this.toast.error(err.error?.message || 'Erro ao se conectar com o servidor.')
      });
  }

  changePage(page: number): void {
    if (page >= 0 && page < this.totalPages) {
      this.currentPage = page;
      this.loadResultados();
    }
  }

  formatDate(value?: string): string {
    if (!value) return '—';
    return new Date(value).toLocaleString('pt-BR');
  }

  statusLabel(status: ResultadoStatus): string {
    const labels: Record<ResultadoStatus, string> = {
      PENDENTE: 'Pendente',
      EM_ANALISE: 'Em análise',
      DISPONIVEL: 'Disponível',
      CANCELADO: 'Cancelado'
    };
    return labels[status];
  }

  statusClass(status: ResultadoStatus): string {
    const classes: Record<ResultadoStatus, string> = {
      PENDENTE: 'badge-gray',
      EM_ANALISE: 'badge-yellow',
      DISPONIVEL: 'badge-green',
      CANCELADO: 'badge-rose'
    };
    return classes[status];
  }
}
