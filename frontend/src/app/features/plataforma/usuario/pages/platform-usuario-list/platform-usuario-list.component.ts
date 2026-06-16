import { Component, ElementRef, OnDestroy, OnInit, ViewChild, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { Subject, debounceTime, distinctUntilChanged, finalize, takeUntil, timeout } from 'rxjs';
import { PlatformUsuarioService } from '../../services/platform-usuario.service';
import { ToastService } from '../../../../../core/services/toast.service';
import { AuthService } from '../../../../../core/services/auth.service';

interface PlatformUsuarioRow {
  id?: number;
  nome: string;
  email: string;
  ativo: boolean;
  perfil: string;
  empresaId?: number;
  empresaNome?: string;
}

@Component({
  selector: 'app-platform-usuario-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './platform-usuario-list.component.html',
  styleUrls: ['./platform-usuario-list.component.css']
})
export class PlatformUsuarioListComponent implements OnInit, OnDestroy {
  @ViewChild('searchInput') searchInput?: ElementRef<HTMLInputElement>;

  private usuarioService = inject(PlatformUsuarioService);
  private toast = inject(ToastService);
  private router = inject(Router);
  protected auth = inject(AuthService);
  private readonly destroy$ = new Subject<void>();
  private readonly searchChanges$ = new Subject<string>();

  usuarios: PlatformUsuarioRow[] = [];
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
  private static readonly EDIT_LOADING_MS = 1200;

  get hasActiveSearch(): boolean {
    return this.searchQuery.trim().length > 0;
  }

  get fillerRows(): null[] {
    if (this.loading() || this.editingLoading() || this.usuarios.length === 0) {
      return [];
    }
    const missing = this.pageSize - this.usuarios.length;
    return missing > 0 ? Array.from({ length: missing }, () => null) : [];
  }

  ngOnInit(): void {
    this.searchChanges$
      .pipe(debounceTime(350), distinctUntilChanged(), takeUntil(this.destroy$))
      .subscribe((term) => {
        this.searchQuery = term;
        this.currentPage = 0;
        this.loadUsuarios();
      });

    this.loadUsuarios();
  }

  ngOnDestroy(): void {
    if (this.editNavigateTimer) {
      clearTimeout(this.editNavigateTimer);
    }
    this.destroy$.next();
    this.destroy$.complete();
  }

  isProtectedUser(usuario: PlatformUsuarioRow): boolean {
    return usuario.perfil === 'SUPER_ADMIN';
  }

  onEdit(id: number): void {
    if (this.editingLoading() || this.loading()) {
      return;
    }

    this.editingLoading.set(true);
    let prefetchedUsuario: PlatformUsuarioRow | null = null;

    this.usuarioService.getById(id).subscribe({
      next: (response) => {
        if (response.success && response.data) {
          prefetchedUsuario = response.data;
        }
      }
    });

    this.editNavigateTimer = setTimeout(() => {
      this.router.navigate(['/plataforma/usuarios/editar', id], {
        state: { usuario: prefetchedUsuario }
      });
      this.editingLoading.set(false);
    }, PlatformUsuarioListComponent.EDIT_LOADING_MS);
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
    this.loadUsuarios();
  }

  loadUsuarios(): void {
    this.loading.set(true);
    this.usuarioService
      .getAll(this.currentPage, this.pageSize, this.sortBy, this.sortDir, this.searchQuery)
      .pipe(timeout(15000), finalize(() => this.loading.set(false)))
      .subscribe({
        next: (response) => {
          if (response.success && response.data) {
            this.usuarios = response.data.content ?? [];
            this.totalPages = response.data.totalPages ?? 0;
            this.totalElements = response.data.totalElements ?? 0;
          } else {
            this.toast.error(response.message || 'Falha ao carregar usuários.');
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
      this.loadUsuarios();
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
    this.loadUsuarios();
  }

  askDelete(id: number): void {
    this.confirmDeleteId = id;
  }

  cancelDelete(): void {
    this.confirmDeleteId = null;
  }

  deleteUsuario(id: number): void {
    this.usuarioService.delete(id).subscribe({
      next: (response) => {
        if (response.success) {
          this.toast.success('Usuário removido com sucesso.');
          this.confirmDeleteId = null;
          this.loadUsuarios();
        } else {
          this.toast.error(response.message || 'Erro ao remover usuário.');
        }
      },
      error: (err) => {
        this.toast.error(err.error?.message || 'Erro ao remover usuário.');
      }
    });
  }

  perfilLabel(perfil: string): string {
    if (perfil === 'SUPER_ADMIN') return 'Super Admin';
    if (perfil === 'ADMIN') return 'Administrador';
    if (perfil === 'OPERADOR') return 'Operador';
    if (perfil === 'VISUALIZADOR') return 'Visualizador';
    return perfil;
  }
}
