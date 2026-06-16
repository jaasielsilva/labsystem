import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { NavService } from '../../navigation/nav.service';
import { TenantContextService } from '../../services/tenant-context.service';
import { ToastService } from '../../services/toast.service';
import { Perfil } from '../../models/usuario.model';

const PERFIL_LABELS: Record<Perfil, string> = {
  SUPER_ADMIN: 'Super Admin',
  ADMIN: 'Administrador',
  OPERADOR: 'Operador',
  VISUALIZADOR: 'Visualizador'
};

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './app-shell.component.html',
  styleUrl: './app-shell.component.css'
})
export class AppShellComponent {
  protected auth = inject(AuthService);
  protected nav = inject(NavService);
  protected tenant = inject(TenantContextService);
  private router = inject(Router);
  private toast = inject(ToastService);

  sidebarOpen = signal(false);
  exitingImpersonation = signal(false);

  perfilLabel(perfil: Perfil): string {
    return PERFIL_LABELS[perfil] ?? perfil;
  }

  logout(): void {
    this.auth.logout();
  }

  exitImpersonation(): void {
    if (this.exitingImpersonation()) {
      return;
    }

    this.exitingImpersonation.set(true);

    this.auth.exitImpersonation().subscribe({
      next: (response) => {
        if (response.success) {
          this.toast.success('Modo suporte encerrado.');
          this.router.navigate(['/plataforma/laboratorios']);
        } else {
          this.toast.error(response.message || 'Erro ao sair do modo suporte.');
        }
        this.exitingImpersonation.set(false);
      },
      error: (err) => {
        this.toast.error(err.error?.message || 'Erro ao sair do modo suporte.');
        this.exitingImpersonation.set(false);
      }
    });
  }

  toggleSidebar(): void {
    this.sidebarOpen.update((open) => !open);
  }

  closeSidebar(): void {
    this.sidebarOpen.set(false);
  }

}
