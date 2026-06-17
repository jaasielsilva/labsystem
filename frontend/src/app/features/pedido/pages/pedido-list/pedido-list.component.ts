import { Component, ElementRef, OnDestroy, OnInit, ViewChild, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { Subject, debounceTime, distinctUntilChanged, finalize, takeUntil, timeout } from 'rxjs';

import { PedidoService } from '../../services/pedido.service';
import { PedidoStatus, PedidoSummary, Pedido } from '../../models/pedido.model';
import { AuthService } from '../../../../core/services/auth.service';
import { ToastService } from '../../../../core/services/toast.service';

@Component({
  selector: 'app-pedido-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './pedido-list.component.html',
  styleUrls: ['./pedido-list.component.css']
})
export class PedidoListComponent implements OnInit, OnDestroy {
  @ViewChild('searchInput') searchInput?: ElementRef<HTMLInputElement>;

  private pedidoService = inject(PedidoService);
  private toast = inject(ToastService);
  private router = inject(Router);
  protected auth = inject(AuthService);

  private readonly destroy$ = new Subject<void>();
  private readonly searchChanges$ = new Subject<string>();

  pedidos: PedidoSummary[] = [];

  currentPage = 0;
  pageSize = 10;
  totalPages = 0;
  totalElements = 0;

  sortBy = 'dataPedido';
  sortDir = 'desc';

  searchQuery = '';

  loading = signal(false);
  editingLoading = signal(false);

  private editNavigateTimer?: ReturnType<typeof setTimeout>;
  private static readonly EDIT_LOADING_MS = 2000;

  confirmDeleteId: number | null = null;

  get hasActiveSearch(): boolean {
    return this.searchQuery.trim().length > 0;
  }

  get fillerRows(): null[] {
    if (this.loading() || this.editingLoading() || this.pedidos.length === 0) {
      return [];
    }
    const missing = this.pageSize - this.pedidos.length;
    return missing > 0 ? Array.from({ length: missing }, () => null) : [];
  }

  ngOnInit(): void {
    this.searchChanges$
      .pipe(debounceTime(350), distinctUntilChanged(), takeUntil(this.destroy$))
      .subscribe((term) => {
        this.searchQuery = term;
        this.currentPage = 0;
        this.loadPedidos();
      });

    this.loadPedidos();
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

  clearSearch(): void {
    this.searchQuery = '';
    if (this.searchInput?.nativeElement) {
      this.searchInput.nativeElement.value = '';
    }
    this.currentPage = 0;
    this.loadPedidos();
  }

  loadPedidos(): void {
    this.loading.set(true);

    this.pedidoService
      .getAll(this.currentPage, this.pageSize, this.sortBy, this.sortDir, this.searchQuery)
      .pipe(timeout(15000), finalize(() => this.loading.set(false)))
      .subscribe({
        next: (response) => {
          if (response.success && response.data) {
            this.pedidos = response.data.content ?? [];
            this.totalPages = response.data.totalPages ?? 0;
            this.totalElements = response.data.totalElements ?? 0;
          } else {
            this.toast.error(response.message || 'Falha ao carregar pedidos.');
          }
        },
        error: (err) => {
          this.toast.error(err.error?.message || 'Erro ao se conectar com o servidor.');
        }
      });
  }

  changePage(page: number): void {
    if (page >= 0 && page < this.totalPages) {
      this.currentPage = page;
      this.loadPedidos();
    }
  }

  setSort(column: string): void {
    if (this.sortBy === column) {
      this.sortDir = this.sortDir === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortBy = column;
      this.sortDir = column === 'dataPedido' ? 'desc' : 'asc';
    }
    this.currentPage = 0;
    this.loadPedidos();
  }

  onView(id: number): void {
    if (this.editingLoading() || this.loading()) {
      return;
    }

    this.editingLoading.set(true);
    let prefetched: Pedido | null = null;

    this.pedidoService.getById(id).subscribe({
      next: (response) => {
        if (response.success && response.data) {
          prefetched = response.data;
        }
      }
    });

    this.editNavigateTimer = setTimeout(() => {
      this.router.navigate(['/pedidos/editar', id], { state: { pedido: prefetched } });
      this.editingLoading.set(false);
    }, PedidoListComponent.EDIT_LOADING_MS);
  }

  askDelete(id: number): void {
    this.confirmDeleteId = id;
  }

  cancelDelete(): void {
    this.confirmDeleteId = null;
  }

  deletePedido(id: number): void {
    this.pedidoService.delete(id).subscribe({
      next: (response) => {
        if (response.success) {
          this.toast.success('Pedido removido com sucesso.');
          this.confirmDeleteId = null;
          this.loadPedidos();
        } else {
          this.toast.error(response.message || 'Erro ao remover pedido.');
        }
      },
      error: (err) => {
        this.toast.error(err.error?.message || 'Erro ao remover pedido.');
      }
    });
  }

  formatDate(value: string): string {
    if (!value) return '—';
    return new Date(value).toLocaleString('pt-BR');
  }

  formatCurrency(value: number | undefined): string {
    return (value ?? 0).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
  }

  statusLabel(status: PedidoStatus): string {
    const labels: Record<PedidoStatus, string> = {
      ABERTO: 'Aberto',
      EM_ANDAMENTO: 'Em andamento',
      CONCLUIDO: 'Concluído',
      CANCELADO: 'Cancelado'
    };
    return labels[status] ?? status;
  }

  statusClass(status: PedidoStatus): string {
    const classes: Record<PedidoStatus, string> = {
      ABERTO: 'badge-blue',
      EM_ANDAMENTO: 'badge-yellow',
      CONCLUIDO: 'badge-green',
      CANCELADO: 'badge-gray'
    };
    return classes[status] ?? 'badge-gray';
  }

  canDeletePedido(status: PedidoStatus): boolean {
    return status === 'ABERTO' && this.auth.canDelete();
  }
}
