import { Routes } from '@angular/router';

import { authGuard } from './core/guards/auth.guard';

import { roleGuard } from './core/guards/role.guard';

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
        loadChildren: () => import('./features/cliente/cliente.routes').then(m => m.CLIENTE_ROUTES)
      },

      {
        path: 'exames',
        loadChildren: () => import('./features/exame/exame.routes').then(m => m.EXAME_ROUTES)
      },

      {
        path: 'dev/ui',
        canActivate: [roleGuard('ADMIN')],
        loadComponent: () => import('./features/dev/ui-playground/ui-playground.component').then(m => m.UiPlaygroundComponent)
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
        path: 'governanca/empresa',
        canActivate: [roleGuard('ADMIN')],
        component: PlaceholderComponent,
        data: {
          title: 'Empresa e plano',
          description: 'Dados da empresa, plano SaaS e limites de uso.',
          icon: '🏢'
        }
      },

      {
        path: 'governanca/auditoria',
        canActivate: [roleGuard('ADMIN')],
        component: PlaceholderComponent,
        data: {
          title: 'Auditoria',
          description: 'Trilha de ações e eventos para governança e compliance.',
          icon: '📝'
        }
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

