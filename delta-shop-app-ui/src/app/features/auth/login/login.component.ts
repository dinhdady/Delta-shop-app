import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { LucideAngularModule, Mail, Shield, Eye, EyeOff, AlertCircle } from 'lucide-angular';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule, 
    ReactiveFormsModule, 
    RouterModule, 
    LucideAngularModule
  ],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {
  loginForm!: FormGroup;
  isLoading = false;
  errorMessage = '';
  showPassword = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      remember: [false]
    });

    const savedEmail = localStorage.getItem('rememberedEmail');
    if (savedEmail) {
      this.loginForm.patchValue({ email: savedEmail, remember: true });
    }
  }

  togglePassword(): void {
    this.showPassword = !this.showPassword;
  }

  onSubmit(): void {
    if (this.loginForm.invalid) {
      Object.keys(this.loginForm.controls).forEach(key => {
        this.loginForm.get(key)?.markAsTouched();
      });
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    const { email, password, remember } = this.loginForm.value;

    if (remember) {
      localStorage.setItem('rememberedEmail', email);
    } else {
      localStorage.removeItem('rememberedEmail');
    }

    this.authService.login(email, password).subscribe({
      next: (response) => {
        this.isLoading = false;
        // Dùng trực tiếp response.role — không cần gọi getCurrentUser()
        this.navigateBasedOnRole(response.role);
      },
      error: (error) => {
        this.isLoading = false;
        this.errorMessage = this.getLoginErrorMessage(error);
      }
    });
  }
  private navigateBasedOnRole(role: string): void {
    if (this.authService.hasAdminAccess(role)) {
      this.router.navigate(['/admin/dashboard']);
    } else {
      this.router.navigate(['/']);
    }
  }

  private getLoginErrorMessage(error: HttpErrorResponse): string {
    if (error.status === 0) {
      return 'Không thể kết nối đến máy chủ (http://localhost:8080). Vui lòng chạy backend và kiểm tra CORS/network.';
    }

    return error.error?.message || 'Email hoặc mật khẩu không chính xác.';
  }
}
