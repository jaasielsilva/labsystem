import { Routes } from '@angular/router';
import { EmpresaListComponent } from './pages/empresa-list/empresa-list.component';
import { EmpresaFormComponent } from './pages/empresa-form/empresa-form.component';

import { roleGuard } from '../../../core/guards/role.guard';

export const EMPRESA_ROUTES: Routes = [
  { path: '', component: EmpresaListComponent },
  { path: 'novo', component: EmpresaFormComponent, canActivate: [roleGuard('ADMIN')] },
  { path: 'editar/:id', component: EmpresaFormComponent, canActivate: [roleGuard('ADMIN')] }
];