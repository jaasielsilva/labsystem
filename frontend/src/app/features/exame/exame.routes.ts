import { Routes } from '@angular/router';
import { ExameListComponent } from './pages/exame-list/exame-list.component';
import { ExameFormComponent } from './pages/exame-form/exame-form.component';

export const EXAME_ROUTES: Routes = [
  {
    path: '',
    component: ExameListComponent
  },
  {
    path: 'novo',
    component: ExameFormComponent
  },
  {
    path: ':id',
    component: ExameFormComponent
  }
];