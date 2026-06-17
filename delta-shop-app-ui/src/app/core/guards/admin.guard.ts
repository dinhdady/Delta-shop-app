import { Injectable, inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { catchError, map, of } from 'rxjs';

export const adminGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isAuthenticated() && authService.isAdmin()) {
    return true;
  }

  if (authService.getToken()) {
    authService.syncAuthState();
    if (authService.isAdmin()) {
      return true;
    }
    return router.createUrlTree(['/']);
  }

  if (authService.getRefreshToken()) {
    return authService.refreshSession().pipe(
      map(() => {
        if (authService.isAdmin()) {
          return true;
        }
        return router.createUrlTree(['/']);
      }),
      catchError(() => {
        authService.clearLocalSession();
        return of(router.createUrlTree(['/auth/login'], { queryParams: { returnUrl: state.url } }));
      })
    );
  }

  authService.clearLocalSession();
  return router.createUrlTree(['/auth/login'], { queryParams: { returnUrl: state.url } });
};
