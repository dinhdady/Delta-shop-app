import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { LucideAngularModule, Mail, Phone, Shield, CheckCircle, Eye, EyeOff, AlertCircle } from 'lucide-angular';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule,
    LucideAngularModule
  ],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss']
})
export class RegisterComponent implements OnInit {
  registerForm!: FormGroup;
  isLoading = false;
  errorMessage = '';
  showPassword = false;
  showConfirmPassword = false;

  // Thêm property passwordStrength
  passwordStrength = {
    hasMinLength: false,
    hasUpperCase: false,
    hasLowerCase: false,
    hasNumber: false
  };

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.initForm();

    // Theo dõi password changes để cập nhật strength
    this.registerForm.get('password')?.valueChanges.subscribe((value: string) => {
      this.updatePasswordStrength(value);
    });
  }

  initForm(): void {
    this.registerForm = this.fb.group({
      firstName: ['', [Validators.required, Validators.minLength(1), Validators.maxLength(50)]],
      lastName: ['', [Validators.required, Validators.minLength(1), Validators.maxLength(50)]],
      email: ['', [Validators.required, Validators.email]],
      phone: ['', [Validators.required, Validators.pattern(/^(0|\+84)[0-9]{9,10}$/)]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', [Validators.required]],
      agreeTerms: [false, [Validators.requiredTrue]]
    }, {
      validators: this.passwordMatchValidator
    });
  }

  // Cập nhật password strength
  updatePasswordStrength(password: string): void {
    this.passwordStrength = {
      hasMinLength: password.length >= 6,
      hasUpperCase: /[A-Z]/.test(password),
      hasLowerCase: /[a-z]/.test(password),
      hasNumber: /[0-9]/.test(password)
    };
  }

  passwordMatchValidator(group: AbstractControl): ValidationErrors | null {
    const password = group.get('password')?.value;
    const confirmPassword = group.get('confirmPassword')?.value;
    return password === confirmPassword ? null : { passwordMismatch: true };
  }

  togglePassword(): void {
    this.showPassword = !this.showPassword;
  }

  toggleConfirmPassword(): void {
    this.showConfirmPassword = !this.showConfirmPassword;
  }

  getConfirmPasswordError(): string {
    const control = this.registerForm.get('confirmPassword');
    const formErrors = this.registerForm.errors;

    if (control?.touched) {
      if (control.errors?.['required']) {
        return 'Vui lòng xác nhận mật khẩu';
      }
      if (formErrors?.['passwordMismatch']) {
        return 'Mật khẩu xác nhận không khớp';
      }
    }
    return '';
  }

  hasError(fieldName: string): boolean {
    const control = this.registerForm.get(fieldName);
    return !!control && control.invalid && control.touched;
  }

  getFieldError(fieldName: string): string {
    const control = this.registerForm.get(fieldName);

    if (!control || !control.touched || control.valid) {
      return '';
    }

    if (control.errors) {
      if (control.errors['required']) {
        switch(fieldName) {
          case 'firstName': return 'Vui lòng nhập họ';
          case 'lastName': return 'Vui lòng nhập tên';
          case 'email': return 'Vui lòng nhập email';
          case 'phone': return 'Vui lòng nhập số điện thoại';
          case 'password': return 'Vui lòng nhập mật khẩu';
          case 'confirmPassword': return 'Vui lòng xác nhận mật khẩu';
          case 'agreeTerms': return 'Vui lòng đồng ý với điều khoản';
          default: return 'Trường này không được để trống';
        }
      }

      if (control.errors['email']) {
        return 'Email không đúng định dạng (ví dụ: name@example.com)';
      }

      if (control.errors['minlength']) {
        const requiredLength = control.errors['minlength'].requiredLength;
        return `Tối thiểu ${requiredLength} ký tự`;
      }

      if (control.errors['pattern']) {
        return 'Số điện thoại không đúng định dạng (ví dụ: 0912345678)';
      }
    }

    return '';
  }

  onSubmit(): void {
    // Mark all fields as touched
    Object.keys(this.registerForm.controls).forEach(key => {
      this.registerForm.get(key)?.markAsTouched();
    });

    if (this.registerForm.invalid) {
      console.log('Form invalid:', this.registerForm.errors);
      this.errorMessage = 'Vui lòng điền đầy đủ thông tin hợp lệ';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    // Lấy tất cả giá trị từ form
    const formValue = this.registerForm.value;

    // Tạo request body với confirmPassword
    const requestBody = {
      firstName: formValue.firstName,
      lastName: formValue.lastName,
      email: formValue.email,
      phone: formValue.phone,
      password: formValue.password,
      confirmPassword: formValue.confirmPassword  // Quan trọng: phải gửi confirmPassword
    };

    console.log('Sending request:', requestBody);

    this.authService.register(requestBody).subscribe({
      next: (response: any) => {
        console.log('Register success:', response);
        this.isLoading = false;
        this.router.navigate(['/auth/verify-email'], {
          queryParams: { email: formValue.email }
        });
      },
      error: (error: any) => {
        console.error('Register error:', error);
        this.isLoading = false;

        // Hiển thị lỗi chi tiết từ backend
        if (error.error) {
          if (typeof error.error === 'string') {
            this.errorMessage = error.error;
          } else if (error.error.message) {
            this.errorMessage = error.error.message;
          } else if (error.error.errors) {
            // Validation errors
            const errors = Object.values(error.error.errors).join(', ');
            this.errorMessage = errors;
          } else {
            this.errorMessage = JSON.stringify(error.error);
          }
        } else {
          this.errorMessage = 'Đăng ký thất bại, vui lòng thử lại';
        }
      }
    });
  }
}
