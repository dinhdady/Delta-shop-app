// core/interceptors/auth.interceptor.ts
import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { catchError, switchMap, throwError } from 'rxjs';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const token = authService.getToken();
  const isRefreshRequest = req.url.includes('/auth/refresh');
  const isAuthRequest = req.url.includes('/auth/');

  if (isAuthRequest) {
    return next(req);
  }

  if (token) {
    const cloned = req.clone({
      headers: req.headers.set('Authorization', `Bearer ${token}`)
    });
    return next(cloned).pipe(
      catchError(error => {
        if (error.status !== 401 && error.status !== 403) {
          return throwError(() => error);
        }

        return authService.refreshSession().pipe(
          switchMap(response => {
            const retryReq = req.clone({
              headers: req.headers.set('Authorization', `Bearer ${response.accessToken}`)
            });
            return next(retryReq);
          }),
          catchError(refreshError => {
            authService.clearLocalSession();
            return throwError(() => refreshError);
          })
        );
      })
    );
  }

  const refreshToken = authService.getRefreshToken();
  if (!refreshToken) {
    authService.syncAuthState();
    return next(req);
  }

  return authService.refreshSession().pipe(
    switchMap(response => {
      const cloned = req.clone({
        headers: req.headers.set('Authorization', `Bearer ${response.accessToken}`)
      });
      return next(cloned);
    }),
    catchError(error => {
      authService.clearLocalSession();
      return next(req);
    })
  );
};
