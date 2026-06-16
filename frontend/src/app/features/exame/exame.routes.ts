import { Routes } from '@angular/router';
import { ExameListComponent } from './pages/exame-list/exame-list.component';
import { ExameFormComponent } from './pages/exame-form/exame-form.component';
import { roleGuard } from '../../core/guards/role.guard';

export const EXAME_ROUTES: Routes = [
  { path: '', component: ExameListComponent },
  { path: 'novo', component: ExameFormComponent, canActivate: [roleGuard('ADMIN', 'OPERADOR')] },
  { path: 'editar/:id', component: ExameFormComponent, canActivate: [roleGuard('ADMIN', 'OPERADOR')] }
];