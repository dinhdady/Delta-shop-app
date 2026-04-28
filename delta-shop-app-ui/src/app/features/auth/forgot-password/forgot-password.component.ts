import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { LucideAngularModule, Mail, CheckCircle, AlertCircle, ArrowLeft } from 'lucide-angular';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [
    CommonModule, 
    ReactiveFormsModule, 
    RouterModule, 
    LucideAngularModule
  ],
  templateUrl: './forgot-password.component.html',
  styleUrls: ['./forgot-password.component.scss']
})
export class ForgotPasswordComponent {
  forgotForm!: FormGroup;
  isLoading = false;
  errorMessage = '';
  successMessage = '';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService
  ) {
    this.initForm();
  }

  initForm(): void {
    this.forgotForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]]
    });
  }

  onSubmit(): void {
    if (this.forgotForm.invalid) {
      this.forgotForm.get('email')?.markAsTouched();
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';
    this.successMessage = '';

    const { email } = this.forgotForm.value;

    this.authService.forgotPassword(email).subscribe({
      next: () => {
        this.isLoading = false;
        this.successMessage = 'Link đặt lại mật khẩu đã được gửi đến email của bạn. Vui lòng kiểm tra hộp thư.';
        this.forgotForm.reset();
      },
      error: (error) => {
        this.isLoading = false;
        this.successMessage = 'Nếu email tồn tại trong hệ thống, bạn sẽ nhận được link đặt lại mật khẩu.';
        this.forgotForm.reset();
      }
    });
  }
}
