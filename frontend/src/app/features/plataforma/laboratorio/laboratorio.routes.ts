import { Routes } from '@angular/router';
import { EmpresaListComponent } from '../../governanca/empresa/pages/empresa-list/empresa-list.component';
import { EmpresaFormComponent } from '../../governanca/empresa/pages/empresa-form/empresa-form.component';
import { superAdminGuard } from '../../../core/guards/super-admin.guard';

export const LABORATORIO_ROUTES: Routes = [
  { path: '', component: EmpresaListComponent },
  { path: 'novo', component: EmpresaFormComponent, canActivate: [superAdminGuard] },
  { path: 'editar/:id', component: EmpresaFormComponent, canActivate: [superAdminGuard] }
];
