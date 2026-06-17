import { Routes } from '@angular/router';
import { ResultadoListComponent } from './pages/resultado-list/resultado-list.component';
import { ResultadoFormComponent } from './pages/resultado-form/resultado-form.component';

export const RESULTADO_ROUTES: Routes = [
  { path: '', component: ResultadoListComponent },
  { path: 'laudo/:id', component: ResultadoFormComponent }
];
