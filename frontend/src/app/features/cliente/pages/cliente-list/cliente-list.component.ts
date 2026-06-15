import { Component, ElementRef, OnDestroy, OnInit, ViewChild, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { Subject, debounceTime, distinctUntilChanged, finalize, takeUntil, timeout } from 'rxjs';
import { ClienteService } from '../../services/cliente.service';
import { Cliente } from '../../models/cliente.model';
import { AuthService } from '../../../../core/services/auth.service';
import { ToastService } from '../../../../core/services/toast.service';
import { maskCpf } from '../../../../shared/utils/cpf.utils';

@Component({
  selector: 'app-cliente-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './cliente-list.component.html',
  styleUrls: ['./cliente-list.component.css']
})
export class ClienteListComponent implements OnInit, OnDestroy {
  @ViewChild('searchInput') searchInput?: ElementRef<HTMLInputElement>;

  private clienteService = inject(ClienteService);
  private toast = inject(ToastService);
  private router = inject(Router);
  protected auth = inject(AuthService);
  private readonly destroy$ = new Subject<void>();
  private readonly searchChanges$ = new Subject<string>();

  clientes: Cliente[] = [];
  currentPage = 0;
  pageSize = 10;
  totalPages = 0;
  totalElements = 0;
  sortBy = 'nome';
  sortDir = 'asc';
  searchQuery = '';

  private readonly sortLabels: Record<string, string> = {
    nome: 'nome',
    cpf: 'CPF',
    email: 'e-mail'
  };

  get sortLabel(): string {
    return this.sortLabels[this.sortBy] ?? this.sortBy;
  }

  get hasActiveSearch(): boolean {
    return this.searchQuery.trim().length > 0;
  }

  /** Linhas vazias para manter altura fixa do grid entre páginas */
  get fillerRows(): null[] {
    if (this.loading() || this.editingLoading() || this.clientes.length === 0) {
      return [];
    }
    const missing = this.pageSize - this.clientes.length;
    return missing > 0 ? Array.from({ length: missing }, () => null) : [];
  }

  loading = signal(false);
  editingLoading = signal(false);

  private editNavigateTimer?: ReturnType<typeof setTimeout>;
  private static readonly EDIT_LOADING_MS = 2000;

  confirmDeleteId: number | null = null;

  ngOnInit(): void {
    this.searchChanges$
      .pipe(debounceTime(350), distinctUntilChanged(), takeUntil(this.destroy$))
      .subscribe((term) => {
        this.searchQuery = term;
        this.currentPage = 0;
        this.loadClientes();
      });

    this.loadClientes();
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
    let prefetchedCliente: Cliente | null = null;

    this.clienteService.getById(id).subscribe({
      next: (response) => {
        if (response.success && response.data) {
          prefetchedCliente = response.data;
        }
      }
    });

    this.editNavigateTimer = setTimeout(() => {
      this.router.navigate(['/clientes/editar', id], {
        state: { cliente: prefetchedCliente }
      });
      this.editingLoading.set(false);
    }, ClienteListComponent.EDIT_LOADING_MS);
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
    this.loadClientes();
  }

  loadClientes(): void {
    this.loading.set(true);
    this.clienteService
      .getAll(this.currentPage, this.pageSize, this.sortBy, this.sortDir, this.searchQuery)
      .pipe(
        timeout(15000),
        finalize(() => this.loading.set(false))
      )
      .subscribe({
        next: (response) => {
          if (response.success && response.data) {
            this.clientes = response.data.content ?? [];
            this.totalPages = response.data.totalPages ?? 0;
            this.totalElements = response.data.totalElements ?? 0;
          } else {
            this.toast.error(response.message || 'Falha ao carregar clientes.');
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
      this.loadClientes();
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
    this.loadClientes();
  }

  askDelete(id: number): void {
    this.confirmDeleteId = id;
  }

  cancelDelete(): void {
    this.confirmDeleteId = null;
  }

  deleteCliente(id: number): void {
    this.clienteService.delete(id).subscribe({
      next: (response) => {
        if (response.success) {
          this.toast.success('Cliente removido com sucesso.');
          this.confirmDeleteId = null;
          this.loadClientes();
        } else {
          this.toast.error(response.message || 'Erro ao remover cliente.');
        }
      },
      error: () => {
        this.toast.error('Erro ao remover cliente.');
      }
    });
  }

  maskCpf = maskCpf;

  formatPhone(phone?: string): string {
    if (!phone) return '-';
    if (phone.length === 11) {
      return phone.replace(/(\d{2})(\d{5})(\d{4})/, '($1) $2-$3');
    } else if (phone.length === 10) {
      return phone.replace(/(\d{2})(\d{4})(\d{4})/, '($1) $2-$3');
    }
    return phone;
  }
}
