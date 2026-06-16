import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap, catchError, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../../shared/models/api-response.model';
import { LoginRequest, LoginResponse } from '../models/auth.model';
import { Perfil, Usuario } from '../models/usuario.model';
import { TenantContextService } from './tenant-context.service';

const ACCESS_TOKEN_KEY = 'labsystem_access_token';
const REFRESH_TOKEN_KEY = 'labsystem_refresh_token';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);
  private tenant = inject(TenantContextService);
  private apiUrl = `${environment.apiUrl}/auth`;

  readonly usuarioAtual = signal<Usuario | null>(null);

  login(credentials: LoginRequest): Observable<ApiResponse<LoginResponse>> {
    return this.http.post<ApiResponse<LoginResponse>>(`${this.apiUrl}/login`, credentials).pipe(
      tap((response) => {
        if (response.success && response.data) {
          this.persistSession(response.data);
        }
      })
    );
  }

  refreshSession(): Observable<ApiResponse<LoginResponse>> {
    const refreshToken = this.getRefreshToken();
    if (!refreshToken) {
      return throwError(() => new Error('Sem refresh token'));
    }
    return this.http.post<ApiResponse<LoginResponse>>(`${this.apiUrl}/refresh`, { refreshToken }).pipe(
      tap((response) => {
        if (response.success && response.data) {
          this.persistSession(response.data);
        }
      })
    );
  }

  loadMe(): Observable<ApiResponse<Usuario>> {
    return this.http.get<ApiResponse<Usuario>>(`${this.apiUrl}/me`).pipe(
      tap((response) => {
        if (response.success && response.data) {
          this.syncUsuario(response.data);
        }
      })
    );
  }

  logout(): void {
    localStorage.removeItem(ACCESS_TOKEN_KEY);
    localStorage.removeItem(REFRESH_TOKEN_KEY);
    this.usuarioAtual.set(null);
    this.tenant.clear();
    if (!this.router.url.startsWith('/login')) {
      this.router.navigate(['/login']);
    }
  }

  getAccessToken(): string | null {
    return localStorage.getItem(ACCESS_TOKEN_KEY);
  }

  getRefreshToken(): string | null {
    return localStorage.getItem(REFRESH_TOKEN_KEY);
  }

  isAuthenticated(): boolean {
    return !!this.getAccessToken();
  }

  hasRole(...perfis: Perfil[]): boolean {
    const usuario = this.usuarioAtual();
    return !!usuario && perfis.includes(usuario.perfil);
  }

  isSuperAdmin(): boolean {
    return this.hasRole('SUPER_ADMIN');
  }

  isTenantUser(): boolean {
    const usuario = this.usuarioAtual();
    return !!usuario && usuario.perfil !== 'SUPER_ADMIN';
  }

  getHomeRoute(): string {
    return this.isSuperAdmin() ? '/plataforma/laboratorios' : '/clientes';
  }

  canEdit(): boolean {
    return this.hasRole('ADMIN', 'OPERADOR', 'SUPER_ADMIN');
  }

  canDelete(): boolean {
    return this.hasRole('ADMIN', 'SUPER_ADMIN');
  }

  private persistSession(data: LoginResponse): void {
    localStorage.setItem(ACCESS_TOKEN_KEY, data.accessToken);
    localStorage.setItem(REFRESH_TOKEN_KEY, data.refreshToken);
    this.usuarioAtual.set(data.usuario);
    this.tenant.syncFromUsuario(data.usuario);
  }

  private syncUsuario(usuario: Usuario): void {
    this.usuarioAtual.set(usuario);
    this.tenant.syncFromUsuario(usuario);
  }
}
