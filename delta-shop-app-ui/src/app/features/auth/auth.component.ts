import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { AuthService } from '../../core/services/auth.service';
import { ToastrService } from 'ngx-toastr';

type AuthMode = 'login' | 'register' | 'forgot-password';

@Component({
  selector: 'app-auth',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div class="auth-page">
      <div class="auth-container">
        <!-- Debug: if you see this, the component is rendering -->
        <div style="display:none">Auth Component Loaded</div>

        <!-- Tab Switcher -->
        @if (mode !== 'forgot-password') {
          <div class="auth-tabs">
            <button [class.active]="mode === 'login'" (click)="setMode('login')">Đăng nhập</button>
            <button [class.active]="mode === 'register'" (click)="setMode('register')">Đăng ký</button>
          </div>
        }

        <div class="auth-header">
          <h2>{{ getTitle() }}</h2>
          <p>{{ getSubtitle() }}</p>
        </div>

        <!-- Login Form -->
        @if (mode === 'login') {
          <form [formGroup]="loginForm" (ngSubmit)="onLogin()" class="auth-form">
            <div class="form-group">
              <label>Email</label>
              <input type="email" formControlName="email" placeholder="Email của bạn" [class.is-invalid]="isInvalid(loginForm, 'email')">
              @if (isInvalid(loginForm, 'email')) {
                <div class="error-message">Vui lòng nhập email hợp lệ</div>
              }
            </div>

            <div class="form-group">
              <label>Mật khẩu</label>
              <input type="password" formControlName="password" placeholder="Mật khẩu" [class.is-invalid]="isInvalid(loginForm, 'password')">
              @if (isInvalid(loginForm, 'password')) {
                <div class="error-message">Vui lòng nhập mật khẩu</div>
              }
            </div>

            <div class="form-actions">
               <button type="button" class="link-btn" (click)="setMode('forgot-password')">Quên mật khẩu?</button>
            </div>

            <button type="submit" class="btn btn-primary btn-full" [disabled]="loginForm.invalid || isSubmitting">
              {{ isSubmitting ? 'ĐANG XỬ LÝ...' : 'ĐĂNG NHẬP' }}
            </button>
          </form>
        }

        <!-- Register Form -->
        @else if (mode === 'register') {
          <form [formGroup]="registerForm" (ngSubmit)="onRegister()" class="auth-form">
            <div class="form-grid">
              <div class="form-group">
                <label>Họ</label>
                <input type="text" formControlName="firstName" placeholder="Họ" [class.is-invalid]="isInvalid(registerForm, 'firstName')">
              </div>
              <div class="form-group">
                <label>Tên</label>
                <input type="text" formControlName="lastName" placeholder="Tên" [class.is-invalid]="isInvalid(registerForm, 'lastName')">
              </div>
            </div>

            <div class="form-group">
              <label>Email</label>
              <input type="email" formControlName="email" placeholder="Email" [class.is-invalid]="isInvalid(registerForm, 'email')">
              @if (isInvalid(registerForm, 'email')) {
                <div class="error-message">Vui lòng nhập email hợp lệ</div>
              }
            </div>

            <div class="form-group">
              <label>Số điện thoại</label>
              <input type="tel" formControlName="phone" placeholder="Số điện thoại" [class.is-invalid]="isInvalid(registerForm, 'phone')">
              @if (isInvalid(registerForm, 'phone')) {
                <div class="error-message">Vui lòng nhập số điện thoại (10-11 số)</div>
              }
            </div>

            <div class="form-group">
              <label>Mật khẩu</label>
              <input type="password" formControlName="password" placeholder="Mật khẩu từ 6 ký tự" [class.is-invalid]="isInvalid(registerForm, 'password')">
              @if (isInvalid(registerForm, 'password')) {
                <div class="error-message">Mật khẩu phải từ 6-50 ký tự</div>
              }
            </div>

            <button type="submit" class="btn btn-primary btn-full" [disabled]="registerForm.invalid || isSubmitting">
              {{ isSubmitting ? 'ĐANG XỬ LÝ...' : 'ĐĂNG KÝ TÀI KHOẢN' }}
            </button>
          </form>
        }

        <!-- Forgot Password Form -->
        @else if (mode === 'forgot-password') {
          <form [formGroup]="forgotPasswordForm" (ngSubmit)="onForgotPassword()" class="auth-form">
            <div class="form-group">
              <label>Nhập email của bạn</label>
              <input type="email" formControlName="email" placeholder="Nhập email đã đăng ký" [class.is-invalid]="isInvalid(forgotPasswordForm, 'email')">
              @if (isInvalid(forgotPasswordForm, 'email')) {
                <div class="error-message">Vui lòng nhập email hợp lệ</div>
              }
            </div>

            <button type="submit" class="btn btn-primary btn-full" [disabled]="forgotPasswordForm.invalid || isSubmitting">
              {{ isSubmitting ? 'ĐANG GỬI...' : 'GỬI YÊU CẦU KHÔI PHỤC' }}
            </button>

            <button type="button" class="btn btn-outline btn-full back-btn" (click)="setMode('login')">
              QUAY LẠI ĐĂNG NHẬP
            </button>
          </form>
        }
      </div>
    </div>
  `,
  styles: [`
    .auth-page {
      min-height: 80vh;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 4rem 1rem;
      background-color: var(--color-light, #f8f8f8);
    }

    .auth-container {
      width: 100%;
      max-width: 480px;
      background: white;
      border: 2px solid #000; /* Force visible border for debugging */
      border-radius: var(--radius, 8px);
      padding: 3rem 2.5rem;
      box-shadow: 0 4px 15px rgba(0,0,0,0.05);
    }

    .auth-tabs {
      display: flex;
      margin-bottom: 2.5rem;
      border-bottom: 2px solid var(--color-light, #f8f8f8);

      button {
        flex: 1;
        background: none;
        border: none;
        padding: 1rem;
        font-family: var(--font-heading, sans-serif);
        font-size: 1.125rem;
        font-weight: 700;
        text-transform: uppercase;
        color: var(--color-gray, #666);
        cursor: pointer;
        position: relative;
        transition: all 0.3s ease;

        &::after {
          content: '';
          position: absolute;
          bottom: -2px;
          left: 0;
          right: 0;
          height: 3px;
          background-color: transparent;
        }

        &.active {
          color: var(--color-primary, #e63946);

          &::after {
            background-color: var(--color-primary, #e63946);
          }
        }
      }
    }

    .auth-header {
      text-align: center;
      margin-bottom: 2.5rem;

      h2 {
        font-family: var(--font-heading, sans-serif);
        font-size: 2rem;
        margin-bottom: 0.5rem;
        color: var(--color-dark, #0d0d0d);
      }

      p {
        color: var(--color-gray, #666);
        font-size: 0.95rem;
      }
    }

    .auth-form {
      .form-grid {
        display: grid;
        grid-template-columns: 1fr 1fr;
        gap: 1rem;
      }

      .form-actions {
        display: flex;
        justify-content: flex-end;
        margin-bottom: 1.5rem;
      }

      .link-btn {
        background: none;
        border: none;
        color: var(--color-gray, #666);
        font-size: 0.875rem;
        cursor: pointer;
        &:hover {
          color: var(--color-primary, #e63946);
          text-decoration: underline;
        }
      }

      .btn {
        margin-top: 1rem;
      }

      .back-btn {
        margin-top: 1rem;
      }
    }
  `]
})
export class AuthComponent implements OnInit {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private toastr = inject(ToastrService);

  mode: AuthMode = 'login';
  isSubmitting = false;
  returnUrl = '/';

  loginForm: FormGroup = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', Validators.required]
  });

  registerForm: FormGroup = this.fb.group({
    firstName: ['', Validators.required],
    lastName: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    phone: ['', [Validators.required, Validators.pattern(/^[0-9]{10,11}$/)]],
    password: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(50)]]
  });

  forgotPasswordForm: FormGroup = this.fb.group({
    email: ['', [Validators.required, Validators.email]]
  });

  ngOnInit() {
    this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/';

    // Safety check
    if (this.authService.isAuthenticated()) {
      this.router.navigate([this.returnUrl]);
    }
  }

  setMode(newMode: AuthMode) {
    this.mode = newMode;
  }

  getTitle() {
    switch (this.mode) {
      case 'login': return 'ĐĂNG NHẬP';
      case 'register': return 'ĐĂNG KÝ';
      case 'forgot-password': return 'QUÊN MẬT KHẨU';
    }
  }

  getSubtitle() {
    switch (this.mode) {
      case 'login': return 'Chào mừng bạn quay trở lại với Delta Sports';
      case 'register': return 'Tạo tài khoản để mua sắm dễ dàng hơn';
      case 'forgot-password': return 'Hãy nhập email để khôi phục mật khẩu';
    }
  }

  isInvalid(form: FormGroup, field: string): boolean {
    const control = form.get(field);
    return !!(control && control.invalid && (control.dirty || control.touched));
  }

  onLogin() {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    this.authService.login(this.loginForm.value).subscribe({
      next: (response) => {                    // ← nhận response
        this.toastr.success('Đăng nhập thành công!');
        this.isSubmitting = false;
        this.navigateBasedOnRole(response.role); // ← navigate theo role
      },
      error: (err) => {
        this.isSubmitting = false;
        const msg = this.getLoginErrorMessage(err);
        this.toastr.error(msg);
      }
    });
  }
  private navigateBasedOnRole(role: string): void {
    if (this.authService.hasAdminAccess(role)) {
      this.router.navigate(['/admin/dashboard']);
    } else {
      this.router.navigate([this.returnUrl]);
    }
  }

  private getLoginErrorMessage(error: HttpErrorResponse): string {
    if (error.status === 0) {
      return 'Không thể kết nối đến máy chủ (http://localhost:8080). Vui lòng chạy backend và kiểm tra CORS/network.';
    }
    return error.error?.message || 'Email hoặc mật khẩu không chính xác';
  }
  onRegister() {
    if (this.registerForm.invalid) {
      this.registerForm.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    this.authService.register(this.registerForm.value).subscribe({
      next: () => {
        this.toastr.success('Đăng ký thành công! Vui lòng đăng nhập.');
        this.setMode('login');
        this.isSubmitting = false;
      },
      error: (err) => {
        this.isSubmitting = false;
        const msg = err.error?.message || 'Có lỗi xảy ra trong quá trình đăng ký';
        this.toastr.error(msg);
      }
    });
  }

  onForgotPassword() {
    if (this.forgotPasswordForm.invalid) {
      this.forgotPasswordForm.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    this.authService.forgotPassword(this.forgotPasswordForm.value.email).subscribe({
      next: () => {
        this.toastr.success('Yêu cầu đã được gửi! Vui lòng kiểm tra email.');
        this.isSubmitting = false;
        this.setMode('login');
      },
      error: (err) => {
        this.isSubmitting = false;
        const msg = err.error?.message || 'Không thể gửi yêu cầu, vui lòng thử lại';
        this.toastr.error(msg);
      }
    });
  }
}
