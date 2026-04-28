// core/interceptors/auth.interceptor.ts
import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const token = authService.getToken();

  console.log('🔵 Interceptor - Request URL:', req.url);
  console.log('🔵 Interceptor - Token exists:', !!token);

  if (token) {
    const cloned = req.clone({
      headers: req.headers.set('Authorization', `Bearer ${token}`)
    });
    console.log('🔵 Interceptor - Added Authorization header');
    return next(cloned);
  }

  console.log('🔵 Interceptor - No token found');
  return next(req);
};
