import { Component, Input, Output, EventEmitter, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AdminBrandService } from './admin-brand.service';

@Component({
  selector: 'app-brand-form',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  template: `
    <div class="modal-backdrop" (click)="onCancel()">
      <div class="modal-content" (click)="$event.stopPropagation()">
        <div class="modal-header">
          <h2>{{ brand ? 'Sửa thương hiệu' : 'Thêm thương hiệu mới' }}</h2>
          <button class="btn-close" (click)="onCancel()">&times;</button>
        </div>
        <form [formGroup]="brandForm" (ngSubmit)="onSubmit()">
          <div class="modal-body">
            <div class="form-group">
              <label>Tên thương hiệu *</label>
              <input type="text" formControlName="name" placeholder="Ví dụ: Nike, Adidas">
              <div *ngIf="brandForm.get('name')?.invalid && brandForm.get('name')?.touched" class="error-message">
                Tên thương hiệu là bắt buộc
              </div>
            </div>

            <!-- Upload Logo Area -->
            <div class="form-group">
              <label>Logo thương hiệu</label>
              <div class="logo-upload-area" (click)="fileInput.click()"
                   [class.has-logo]="logoPreview || brandForm.get('logoUrl')?.value">
                <input type="file" #fileInput (change)="onFileSelected($event)" accept="image/*" hidden>

                <!-- Hiển thị preview -->
                <div *ngIf="logoPreview || brandForm.get('logoUrl')?.value" class="logo-preview">
                  <img [src]="logoPreview || brandForm.get('logoUrl')?.value" alt="Logo preview">
                  <button type="button" class="btn-remove-logo" (click)="removeLogo($event)">&times;</button>
                </div>

                <!-- Placeholder khi chưa có logo -->
                <div *ngIf="!logoPreview && !brandForm.get('logoUrl')?.value" class="logo-placeholder">
                  <i class="bi bi-cloud-upload"></i>
                  <p>Nhấp để tải lên logo</p>
                  <small>PNG, JPG, WEBP (tối đa 5MB)</small>
                </div>
              </div>

              <!-- Upload status -->
              <div *ngIf="uploadError" class="error-message">
                <i class="bi bi-exclamation-circle"></i> {{ uploadError }}
              </div>
              <div *ngIf="isUploading" class="uploading">
                <i class="bi bi-arrow-repeat spin"></i> Đang tải lên...
              </div>
              <div *ngIf="uploadSuccess" class="success-message">
                <i class="bi bi-check-circle"></i> Tải lên thành công
              </div>
            </div>

            <div class="form-group">
              <label>Mô tả</label>
              <textarea formControlName="description" rows="3" placeholder="Mô tả về thương hiệu..."></textarea>
            </div>

            <div class="form-group">
              <label>Website (tùy chọn)</label>
              <input type="url" formControlName="websiteUrl" placeholder="https://example.com">
            </div>

            <div class="form-group">
              <label>Quốc gia (tùy chọn)</label>
              <input type="text" formControlName="countryOfOrigin" placeholder="Ví dụ: USA, Vietnam">
            </div>

            <div class="form-group">
              <label>Số thứ tự hiển thị</label>
              <input type="number" formControlName="sortOrder" placeholder="0">
            </div>

            <div class="form-group">
              <label class="checkbox-label">
                <input type="checkbox" formControlName="active"> Kích hoạt thương hiệu
              </label>
            </div>

            <div class="form-group">
              <label class="checkbox-label">
                <input type="checkbox" formControlName="featured"> Thương hiệu nổi bật
              </label>
            </div>
          </div>

          <div class="modal-footer">
            <button type="button" class="btn btn-outline" (click)="onCancel()">Hủy</button>
            <button type="submit" class="btn btn-primary" [disabled]="brandForm.invalid || isUploading">
              {{ isUploading ? 'Đang xử lý...' : 'Lưu thay đổi' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  `,
  styles: [`
    .modal-backdrop {
      position: fixed;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      background: rgba(0,0,0,0.5);
      display: flex;
      align-items: center;
      justify-content: center;
      z-index: 1000;
    }
    .modal-content {
      background: white;
      border-radius: 8px;
      width: 90%;
      max-width: 600px;
      max-height: 90vh;
      overflow-y: auto;
    }
    .modal-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 1rem 1.5rem;
      border-bottom: 1px solid #dee2e6;
    }
    .modal-body {
      padding: 1.5rem;
    }
    .modal-footer {
      padding: 1rem 1.5rem;
      border-top: 1px solid #dee2e6;
      display: flex;
      justify-content: flex-end;
      gap: 0.5rem;
    }
    .form-group {
      margin-bottom: 1rem;
    }
    .form-group label {
      display: block;
      margin-bottom: 0.5rem;
      font-weight: 500;
    }
    .form-group input:not([type="checkbox"]),
    .form-group textarea {
      width: 100%;
      padding: 0.5rem;
      border: 1px solid #dee2e6;
      border-radius: 4px;
    }
    .logo-upload-area {
      border: 2px dashed #dee2e6;
      border-radius: 8px;
      cursor: pointer;
      transition: all 0.3s;
      min-height: 150px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: #f8f9fa;
    }
    .logo-upload-area:hover {
      border-color: #0d6efd;
      background: #f1f3f5;
    }
    .logo-placeholder {
      text-align: center;
      padding: 2rem;
    }
    .logo-placeholder i {
      font-size: 48px;
      color: #adb5bd;
    }
    .logo-placeholder p {
      margin: 10px 0 5px;
      color: #495057;
    }
    .logo-placeholder small {
      color: #6c757d;
    }
    .logo-preview {
      position: relative;
      width: 100%;
      padding: 1rem;
      text-align: center;
    }
    .logo-preview img {
      max-height: 120px;
      max-width: 100%;
      object-fit: contain;
    }
    .btn-remove-logo {
      position: absolute;
      top: 5px;
      right: 5px;
      background: #dc3545;
      color: white;
      border: none;
      border-radius: 50%;
      width: 24px;
      height: 24px;
      cursor: pointer;
      font-size: 16px;
      display: flex;
      align-items: center;
      justify-content: center;
    }
    .btn-remove-logo:hover {
      background: #c82333;
    }
    .error-message {
      color: #dc3545;
      font-size: 12px;
      margin-top: 5px;
    }
    .uploading {
      color: #0d6efd;
      font-size: 12px;
      margin-top: 5px;
    }
    .success-message {
      color: #28a745;
      font-size: 12px;
      margin-top: 5px;
    }
    .checkbox-label {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      cursor: pointer;
    }
    .checkbox-label input {
      width: auto;
      margin: 0;
    }
    .btn {
      padding: 0.5rem 1rem;
      border-radius: 4px;
      cursor: pointer;
      border: none;
    }
    .btn-outline {
      background: transparent;
      border: 1px solid #dee2e6;
    }
    .btn-primary {
      background: #0d6efd;
      color: white;
    }
    .btn-primary:disabled {
      opacity: 0.6;
      cursor: not-allowed;
    }
    .btn-close {
      background: none;
      border: none;
      font-size: 24px;
      cursor: pointer;
    }
    @keyframes spin {
      from { transform: rotate(0deg); }
      to { transform: rotate(360deg); }
    }
    .spin {
      animation: spin 1s linear infinite;
      display: inline-block;
    }
  `]
})
export class BrandFormComponent implements OnInit {
  @Input() brand: any = null;
  @Output() save = new EventEmitter<any>();
  @Output() cancel = new EventEmitter<void>();

  private fb = inject(FormBuilder);
  private brandService = inject(AdminBrandService);

  brandForm!: FormGroup;
  logoPreview: string | null = null;
  isUploading = false;
  uploadError: string | null = null;
  uploadSuccess = false;

  ngOnInit() {
    this.brandForm = this.fb.group({
      name: [this.brand?.name || '', [Validators.required]],
      description: [this.brand?.description || ''],
      logoUrl: [this.brand?.logoUrl || ''],
      websiteUrl: [this.brand?.websiteUrl || ''],
      countryOfOrigin: [this.brand?.countryOfOrigin || ''],
      active: [this.brand ? this.brand.active : true],
      featured: [this.brand ? this.brand.featured : false],
      sortOrder: [this.brand?.sortOrder || 0]
    });

    // Set preview nếu có logoUrl
    if (this.brand?.logoUrl) {
      this.logoPreview = this.brand.logoUrl;
    }
  }

  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];

    if (!file) return;

    // Validate file
    const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/webp'];
    if (!allowedTypes.includes(file.type)) {
      this.uploadError = 'Chỉ chấp nhận file JPG, PNG, WEBP';
      return;
    }

    if (file.size > 5 * 1024 * 1024) {
      this.uploadError = 'Kích thước file tối đa 5MB';
      return;
    }

    this.uploadError = null;
    this.uploadSuccess = false;
    this.isUploading = true;

    // Tạo preview ngay lập tức
    const reader = new FileReader();
    reader.onload = () => {
      this.logoPreview = reader.result as string;
    };
    reader.readAsDataURL(file);

    // Upload lên server
    this.brandService.uploadLogo(file, 'brands').subscribe({
      next: (response: any) => {
        // Response từ FileUploadResponse có url và publicId
        const logoUrl = response.url || response.secure_url;
        this.brandForm.patchValue({ logoUrl: logoUrl });
        this.isUploading = false;
        this.uploadSuccess = true;

        // Ẩn thông báo success sau 3 giây
        setTimeout(() => {
          this.uploadSuccess = false;
        }, 3000);
      },
      error: (error) => {
        console.error('Upload error:', error);
        this.uploadError = error.error?.message || 'Tải lên thất bại, vui lòng thử lại';
        this.isUploading = false;
        // Không xóa preview nếu upload thất bại, để user thấy ảnh đã chọn
      }
    });
  }

  removeLogo(event: Event) {
    event.stopPropagation();
    this.logoPreview = null;
    this.brandForm.patchValue({ logoUrl: '' });
    this.uploadSuccess = false;
    this.uploadError = null;
  }

  onSubmit() {
    if (this.brandForm.valid && !this.isUploading) {
      // Chuẩn bị dữ liệu gửi đi
      const formValue = this.brandForm.value;

      // Đảm bảo các field boolean có giá trị đúng
      const submitData = {
        ...formValue,
        isFeatured: formValue.featured,  // Backend dùng isFeatured
        isActive: formValue.active       // Backend dùng isActive
      };

      // Xóa các field không cần thiết
      delete submitData.featured;
      delete submitData.active;

      this.save.emit(submitData);
    }
  }

  onCancel() {
    this.cancel.emit();
  }
}
