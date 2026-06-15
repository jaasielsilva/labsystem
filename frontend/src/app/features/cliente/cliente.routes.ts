import { Routes } from '@angular/router';
import { ClienteListComponent } from './pages/cliente-list/cliente-list.component';
import { ClienteFormComponent } from './pages/cliente-form/cliente-form.component';
import { roleGuard } from '../../core/guards/role.guard';

export const CLIENTE_ROUTES: Routes = [
  { path: '', component: ClienteListComponent },
  { path: 'novo', component: ClienteFormComponent, canActivate: [roleGuard('ADMIN', 'OPERADOR')] },
  { path: 'editar/:id', component: ClienteFormComponent, canActivate: [roleGuard('ADMIN', 'OPERADOR')] }
];
