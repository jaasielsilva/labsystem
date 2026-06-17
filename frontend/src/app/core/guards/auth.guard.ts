import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { map, catchError, of, timeout } from 'rxjs';

export const authGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (!auth.isAuthenticated()) {
    return router.createUrlTree(['/login']);
  }

  if (auth.usuarioAtual()) {
    return true;
  }

  return auth.loadMe().pipe(
    timeout(10000),
    map(() => true),
    catchError(() => {
      auth.logout();
      return of(router.createUrlTree(['/login']));
    })
  );
};
