import { Component, ElementRef, OnDestroy, OnInit, ViewChild, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { Subject, debounceTime, distinctUntilChanged, finalize, takeUntil, timeout } from 'rxjs';

import { ExameService } from '../../services/exame.service';
import { Exame } from '../../models/exame.model';
import { AuthService } from '../../../../core/services/auth.service';
import { ToastService } from '../../../../core/services/toast.service';

@Component({
  selector: 'app-exame-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './exame-list.component.html',
  styleUrls: ['./exame-list.component.css']
})
export class ExameListComponent implements OnInit, OnDestroy {
  @ViewChild('searchInput') searchInput?: ElementRef<HTMLInputElement>;

  private exameService = inject(ExameService);
  private toast = inject(ToastService);
  private router = inject(Router);
  protected auth = inject(AuthService);

  private readonly destroy$ = new Subject<void>();
  private readonly searchChanges$ = new Subject<string>();

  exames: Exame[] = [];

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
    codigo: 'código',
    categoria: 'categoria'
  };

  get sortLabel(): string {
    return this.sortLabels[this.sortBy] ?? this.sortBy;
  }

  get hasActiveSearch(): boolean {
    return this.searchQuery.trim().length > 0;
  }

  /** Linhas vazias para manter altura fixa do grid entre páginas */
  get fillerRows(): null[] {
    if (this.loading() || this.editingLoading() || this.exames.length === 0) {
      return [];
    }
    const missing = this.pageSize - this.exames.length;
    return missing > 0 ? Array.from({ length: missing }, () => null) : [];
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
        this.loadExames();
      });

    this.loadExames();
  }

  ngOnDestroy(): void {
    if (this.editNavigateTimer) {
      clearTimeout(this.editNavigateTimer);
    }
    this.destroy$.next();
    this.destroy$.complete();
  }

  onSearchInput(event: Event): void {
    const value = (event.target as HTMLInputElement).value;
    this.searchQuery = value;
    this.searchChanges$.next(value);
  }

  onEdit(id: number): void {
    if (this.editingLoading() || this.loading()) {
      return;
    }

    this.editingLoading.set(true);
    let prefetchedExame: Exame | null = null;

    this.exameService.getById(id).subscribe({
      next: (response) => {
        if (response.success && response.data) {
          prefetchedExame = response.data;
        }
      }
    });

    this.editNavigateTimer = setTimeout(() => {
      this.router.navigate(['/exames/editar', id], {
        state: { exame: prefetchedExame }
      });
      this.editingLoading.set(false);
    }, ExameListComponent.EDIT_LOADING_MS);
  }

  clearSearch(): void {
    this.searchQuery = '';
    if (this.searchInput?.nativeElement) {
      this.searchInput.nativeElement.value = '';
    }
    this.currentPage = 0;
    this.loadExames();
  }

  loadExames(): void {
    this.loading.set(true);

    this.exameService
      .getAll(this.currentPage, this.pageSize, this.sortBy, this.sortDir, this.searchQuery)
      .pipe(
        timeout(15000),
        finalize(() => this.loading.set(false))
      )
      .subscribe({
        next: (response) => {
          if (response.success && response.data) {
            this.exames = response.data.content ?? [];
            this.totalPages = response.data.totalPages ?? 0;
            this.totalElements = response.data.totalElements ?? 0;
          } else {
            this.toast.error(response.message || 'Falha ao carregar exames.');
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
      this.loadExames();
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
    this.loadExames();
  }

  askDelete(id: number): void {
    this.confirmDeleteId = id;
  }

  cancelDelete(): void {
    this.confirmDeleteId = null;
  }

  deleteExame(id: number): void {
    this.exameService.delete(id).subscribe({
      next: (response) => {
        if (response.success) {
          this.toast.success('Exame removido com sucesso.');
          this.confirmDeleteId = null;
          this.loadExames();
        } else {
          this.toast.error(response.message || 'Erro ao remover exame.');
        }
      },
      error: () => {
        this.toast.error('Erro ao remover exame.');
      }
    });
  }

  formatTipoAmostra(tipo: string): string {
    const labels: Record<string, string> = {
      SANGUE: 'Sangue',
      URINA: 'Urina',
      FEZES: 'Fezes',
      OUTRO: 'Outro'
    };
    return labels[tipo] ?? tipo;
  }
}