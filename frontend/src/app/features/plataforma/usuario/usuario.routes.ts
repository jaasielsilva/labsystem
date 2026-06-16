import { Routes } from '@angular/router';
import { PlatformUsuarioListComponent } from './pages/platform-usuario-list/platform-usuario-list.component';
import { PlatformUsuarioFormComponent } from './pages/platform-usuario-form/platform-usuario-form.component';
import { superAdminGuard } from '../../../core/guards/super-admin.guard';

export const PLATFORM_USUARIO_ROUTES: Routes = [
  { path: '', component: PlatformUsuarioListComponent },
  { path: 'novo', component: PlatformUsuarioFormComponent, canActivate: [superAdminGuard] },
  { path: 'editar/:id', component: PlatformUsuarioFormComponent, canActivate: [superAdminGuard] }
];
