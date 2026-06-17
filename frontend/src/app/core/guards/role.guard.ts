import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { Perfil } from '../models/usuario.model';

export const roleGuard = (...perfis: Perfil[]): CanActivateFn => () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (auth.hasTenantAccess(...perfis)) {
    return true;
  }

  return router.createUrlTree([auth.getHomeRoute()]);
};
