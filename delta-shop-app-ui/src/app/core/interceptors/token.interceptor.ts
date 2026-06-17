// src/app/core/interceptors/token.interceptor.ts
import { Injectable, inject } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent, HttpErrorResponse, HttpClient } from '@angular/common/http';
import { Observable, throwError, BehaviorSubject } from 'rxjs';
import { catchError, filter, take, switchMap } from 'rxjs/operators';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';

@Injectable()
export class TokenInterceptor implements HttpInterceptor {
  private router = inject(Router);
  private toastr = inject(ToastrService);
  private http = inject(HttpClient); // Thêm inject HttpClient ở đây
  private isRefreshing = false;
  private refreshTokenSubject = new BehaviorSubject<string | null>(null);

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    let authReq = req;
    const token = localStorage.getItem('accessToken') || localStorage.getItem('token');

    if (token) {
      authReq = this.addTokenToRequest(req, token);
    }

    return next.handle(authReq).pipe(
      catchError((error: HttpErrorResponse) => {
        if (error.status === 401 || error.status === 403) {
          return this.handle401Error(authReq, next);
        }
        return throwError(() => error);
      })
    );
  }

  private addTokenToRequest(request: HttpRequest<any>, token: string): HttpRequest<any> {
    return request.clone({
      setHeaders: {
        'Authorization': `Bearer ${token}`
      }
    });
  }

  private handle401Error(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    if (!this.isRefreshing) {
      this.isRefreshing = true;
      this.refreshTokenSubject.next(null);

      const refreshToken = localStorage.getItem('refreshToken');

      if (refreshToken) {
        return this.refreshAccessToken(refreshToken).pipe(
          switchMap((response: any) => {
            this.isRefreshing = false;
            const newToken = response.accessToken;
            this.refreshTokenSubject.next(newToken);
            localStorage.setItem('accessToken', newToken);
            localStorage.setItem('token', newToken);

            // Retry the failed request with new token
            return next.handle(this.addTokenToRequest(request, newToken));
          }),
          catchError((refreshError) => {
            this.isRefreshing = false;
            this.handleLogout();
            return throwError(() => refreshError);
          })
        );
      } else {
        this.handleLogout();
        return throwError(() => new Error('No refresh token available'));
      }
    } else {
      return this.refreshTokenSubject.pipe(
        filter(token => token !== null),
        take(1),
        switchMap(token => next.handle(this.addTokenToRequest(request, token!)))
      );
    }
  }

  private refreshAccessToken(refreshToken: string): Observable<any> {
    // Gọi API refresh token
    return this.http.post('/api/auth/refresh', { refreshToken });
  }

  private handleLogout(): void {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('token');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('user');
    this.toastr.error('Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại');
    this.router.navigate(['/auth/login']);
  }
}
