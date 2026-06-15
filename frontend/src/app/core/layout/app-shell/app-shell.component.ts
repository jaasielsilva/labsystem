import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { NavService } from '../../navigation/nav.service';
import { TenantContextService } from '../../services/tenant-context.service';
import { Perfil } from '../../models/usuario.model';

const PERFIL_LABELS: Record<Perfil, string> = {
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

  sidebarOpen = signal(false);

  perfilLabel(perfil: Perfil): string {
    return PERFIL_LABELS[perfil] ?? perfil;
  }

  logout(): void {
    this.auth.logout();
  }

  toggleSidebar(): void {
    this.sidebarOpen.update((open) => !open);
  }

  closeSidebar(): void {
    this.sidebarOpen.set(false);
  }

}
