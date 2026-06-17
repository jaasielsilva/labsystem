import { Routes } from '@angular/router';

import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';
import { superAdminGuard } from './core/guards/super-admin.guard';
import { tenantGuard } from './core/guards/tenant.guard';

import { AppShellComponent } from './core/layout/app-shell/app-shell.component';
import { PlaceholderComponent } from './core/pages/placeholder/placeholder.component';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./features/auth/pages/login/login.component').then(m => m.LoginComponent)
  },

  {
    path: '',
    component: AppShellComponent,
    canActivate: [authGuard],
    children: [
      {
        path: 'clientes',
        canActivate: [tenantGuard],
        loadChildren: () => import('./features/cliente/cliente.routes').then(m => m.CLIENTE_ROUTES)
      },

      {
        path: 'exames',
        canActivate: [tenantGuard],
        loadChildren: () => import('./features/exame/exame.routes').then(m => m.EXAME_ROUTES)
      },

      {
        path: 'pedidos',
        canActivate: [tenantGuard],
        loadChildren: () => import('./features/pedido/pedido.routes').then(m => m.PEDIDO_ROUTES)
      },

      {
        path: 'resultados',
        canActivate: [tenantGuard],
        loadChildren: () => import('./features/resultado/resultado.routes').then(m => m.RESULTADO_ROUTES)
      },

      {
        path: 'dev/ui',
        canActivate: [roleGuard('ADMIN', 'SUPER_ADMIN')],
        loadComponent: () => import('./features/dev/ui-playground/ui-playground.component').then(m => m.UiPlaygroundComponent)
      },

      {
        path: 'plataforma/laboratorios',
        canActivate: [superAdminGuard],
        loadChildren: () =>
          import('./features/plataforma/laboratorio/laboratorio.routes')
            .then(m => m.LABORATORIO_ROUTES)
      },

      {
        path: 'plataforma/usuarios',
        canActivate: [superAdminGuard],
        loadChildren: () =>
          import('./features/plataforma/usuario/usuario.routes')
            .then(m => m.PLATFORM_USUARIO_ROUTES)
      },

      {
        path: 'governanca/usuarios',
        canActivate: [roleGuard('ADMIN')],
        loadChildren: () =>
          import('./features/governanca/usuario/usuario.routes')
            .then(m => m.USUARIO_ROUTES)
      },

      {
        path: 'governanca/permissoes',
        canActivate: [roleGuard('ADMIN')],
        component: PlaceholderComponent,
        data: {
          title: 'Perfis e permissões',
          description: 'Controle fino de permissões por recurso e segregação de acesso.',
          icon: '🛡️'
        }
      },

      {
        path: 'governanca/auditoria',
        canActivate: [tenantGuard, roleGuard('ADMIN')],
        loadChildren: () =>
          import('./features/governanca/audit/audit.routes')
            .then(m => m.AUDIT_ROUTES)
      },

      {
        path: '',
        redirectTo: 'clientes',
        pathMatch: 'full'
      }
    ]
  },

  {
    path: '**',
    redirectTo: 'clientes'
  }
];
