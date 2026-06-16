import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const tenantGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (auth.isSuperAdmin() && !auth.isImpersonating()) {
    return router.createUrlTree(['/plataforma/laboratorios']);
  }

  return true;
};
