// error.interceptor.ts
import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ToastrService } from 'ngx-toastr';

@Injectable()
export class ErrorInterceptor implements HttpInterceptor {
  constructor(private toastr: ToastrService) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(req).pipe(
      catchError((error: HttpErrorResponse) => {
        let errorMessage = 'Có lỗi xảy ra';

        if (error.error instanceof ErrorEvent) {
          errorMessage = error.error.message;
        } else if (error.status === 400) {
          errorMessage = error.error?.message || 'Dữ liệu không hợp lệ';
        } else if (error.status === 500) {
          errorMessage = 'Lỗi máy chủ, vui lòng thử lại sau';
        }

        this.toastr.error(errorMessage);
        return throwError(() => error);
      })
    );
  }
}
