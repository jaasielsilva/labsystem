import { Routes } from '@angular/router';
import { UsuarioListComponent } from './pages/usuario-list/usuario-list.component';
import { UsuarioFormComponent } from './pages/usuario-form/usuario-form.component';
import { roleGuard } from '../../../core/guards/role.guard';

export const USUARIO_ROUTES: Routes = [
  { path: '', component: UsuarioListComponent },
  { path: 'novo', component: UsuarioFormComponent, canActivate: [roleGuard('ADMIN')] },
  { path: 'editar/:id', component: UsuarioFormComponent, canActivate: [roleGuard('ADMIN')] }
];