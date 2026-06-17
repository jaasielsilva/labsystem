import { Routes } from '@angular/router';
import { AuditListComponent } from './pages/audit-list/audit-list.component';
import { AuditDetailComponent } from './pages/audit-detail/audit-detail.component';

export const AUDIT_ROUTES: Routes = [
  { path: '', component: AuditListComponent },
  { path: ':id', component: AuditDetailComponent }
];
