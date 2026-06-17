import { Routes } from '@angular/router';
import { PedidoListComponent } from './pages/pedido-list/pedido-list.component';
import { PedidoFormComponent } from './pages/pedido-form/pedido-form.component';
import { roleGuard } from '../../core/guards/role.guard';

export const PEDIDO_ROUTES: Routes = [
  { path: '', component: PedidoListComponent },
  { path: 'novo', component: PedidoFormComponent, canActivate: [roleGuard('ADMIN', 'OPERADOR')] },
  { path: 'editar/:id', component: PedidoFormComponent, canActivate: [roleGuard('ADMIN', 'OPERADOR')] }
];
