import { Routes } from '@angular/router';
import { AuditListComponent } from './pages/audit-list/audit-list.component';
import { roleGuard } from '../../../core/guards/role.guard';

export const AUDIT_ROUTES: Routes = [
  {
    path: '',
    component: AuditListComponent,
    canActivate: [roleGuard('ADMIN')]
  }
];