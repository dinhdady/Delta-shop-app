import { Component, Input, Output, EventEmitter, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';

@Component({
  selector: 'app-category-form',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  template: `
    <div class="modal-backdrop" (click)="onCancel()">
      <div class="modal-content" (click)="$event.stopPropagation()">
        <div class="modal-header">
          <h2>{{ category ? 'Sửa danh mục' : 'Thêm danh mục mới' }}</h2>
          <button class="btn-close" (click)="onCancel()">&times;</button>
        </div>
        <form [formGroup]="categoryForm" (ngSubmit)="onSubmit()">
          <div class="modal-body">
            <div class="form-group">
              <label>Tên danh mục *</label>
              <input type="text" formControlName="name" placeholder="Ví dụ: Giày đá bóng" class="form-control">
            </div>

            <div class="form-group">
              <label>Danh mục cha (nếu có)</label>
              <select formControlName="parentId" class="form-control">
                <option [value]="null">Không có</option>
                <option *ngFor="let cat of filteredCategories" [value]="cat.id">{{ cat.name }}</option>
              </select>
            </div>

            <div class="form-group">
              <label>Mô tả</label>
              <textarea formControlName="description" rows="3" class="form-control"></textarea>
            </div>

            <!-- Preview ảnh hiện tại -->
            <div class="form-group" *ngIf="imagePreview && !selectedFile">
              <label>Hình ảnh hiện tại</label>
              <div class="current-image">
                <img [src]="imagePreview" alt="Current">
                <button type="button" class="btn-remove-image" (click)="removeCurrentImage()">Xóa ảnh</button>
              </div>
            </div>

            <!-- Upload ảnh mới -->
            <div class="form-group">
              <label>Đổi ảnh danh mục</label>
              <div class="image-upload">
                <div class="upload-area" (click)="fileInput.click()">
                  <i class="bi bi-cloud-upload"></i>
                  <p>Click để chọn ảnh mới</p>
                  <small>PNG, JPG, JPEG (Tối đa 5MB)</small>
                </div>
                <div class="image-preview" *ngIf="selectedFile">
                  <img [src]="newImagePreview" alt="Preview">
                  <button type="button" class="remove-image" (click)="removeNewImage()">×</button>
                </div>
                <input
                  type="file"
                  #fileInput
                  (change)="onFileSelected($event)"
                  accept="image/jpeg,image/jpg,image/png,image/webp"
                  style="display: none"
                >
              </div>
            </div>

            <div class="form-group">
              <label>URL hình ảnh (hoặc nhập link)</label>
              <input type="text" formControlName="imageUrl" placeholder="https://example.com/image.jpg" class="form-control">
            </div>

            <div class="form-group">
              <label>Icon class (Bootstrap Icons)</label>
              <input type="text" formControlName="icon" placeholder="bi-star" class="form-control">
            </div>

            <div class="form-group">
              <label>Thứ tự sắp xếp</label>
              <input type="number" formControlName="sortOrder" placeholder="0" class="form-control">
            </div>

            <div class="form-group">
              <label class="checkbox-label">
                <input type="checkbox" formControlName="active"> Kích hoạt danh mục
              </label>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" (click)="onCancel()">Hủy</button>
            <button type="submit" class="btn btn-primary" [disabled]="categoryForm.invalid">Lưu thay đổi</button>
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
      background: rgba(0, 0, 0, 0.5);
      display: flex;
      align-items: center;
      justify-content: center;
      z-index: 9999;
    }

    .modal-content {
      background: white;
      border-radius: 12px;
      width: 90%;
      max-width: 600px;
      max-height: 90vh;
      overflow-y: auto;
      box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
    }

    .modal-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 1.5rem;
      border-bottom: 1px solid #e0e0e0;
      position: sticky;
      top: 0;
      background: white;
      z-index: 10;

      h2 {
        margin: 0;
        font-size: 1.5rem;
        color: #1a1a1a;
      }

      .btn-close {
        background: none;
        border: none;
        font-size: 1.5rem;
        cursor: pointer;
        color: #999;
        transition: color 0.2s;

        &:hover {
          color: #ff4400;
        }
      }
    }

    .modal-body {
      padding: 1.5rem;
    }

    .modal-footer {
      padding: 1rem 1.5rem;
      border-top: 1px solid #e0e0e0;
      display: flex;
      justify-content: flex-end;
      gap: 1rem;
      position: sticky;
      bottom: 0;
      background: white;
    }

    .form-group {
      margin-bottom: 1.25rem;

      label {
        display: block;
        font-weight: 600;
        margin-bottom: 0.5rem;
        color: #1a1a1a;
        font-size: 0.875rem;
      }
    }

    .form-control {
      width: 100%;
      padding: 0.625rem 0.875rem;
      border: 1px solid #e0e0e0;
      border-radius: 6px;
      font-size: 0.875rem;
      transition: all 0.2s;
      font-family: inherit;

      &:focus {
        outline: none;
        border-color: #ff4400;
        box-shadow: 0 0 0 3px rgba(255, 68, 0, 0.1);
      }
    }

    textarea.form-control {
      resize: vertical;
      min-height: 80px;
    }

    select.form-control {
      cursor: pointer;
    }

    .checkbox-label {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      cursor: pointer;

      input {
        width: 16px;
        height: 16px;
        margin: 0;
        cursor: pointer;
      }
    }

    .current-image {
      display: flex;
      align-items: center;
      gap: 1rem;

      img {
        width: 80px;
        height: 80px;
        object-fit: cover;
        border-radius: 8px;
        border: 1px solid #e0e0e0;
      }

      .btn-remove-image {
        padding: 0.25rem 0.75rem;
        background: #dc3545;
        color: white;
        border: none;
        border-radius: 4px;
        cursor: pointer;
        font-size: 0.75rem;

        &:hover {
          background: #c82333;
        }
      }
    }

    .image-upload {
      border: 2px dashed #e0e0e0;
      border-radius: 8px;
      overflow: hidden;
      cursor: pointer;
      transition: border-color 0.2s;

      &:hover {
        border-color: #ff4400;
      }
    }

    .image-preview {
      position: relative;

      img {
        width: 100%;
        height: 200px;
        object-fit: cover;
      }

      .remove-image {
        position: absolute;
        top: 10px;
        right: 10px;
        background: rgba(0, 0, 0, 0.6);
        color: white;
        border: none;
        width: 30px;
        height: 30px;
        border-radius: 50%;
        cursor: pointer;
        font-size: 18px;
        display: flex;
        align-items: center;
        justify-content: center;
        transition: background 0.2s;

        &:hover {
          background: #dc3545;
        }
      }
    }

    .upload-area {
      text-align: center;
      padding: 2rem;
      background: #f9f9f9;

      i {
        font-size: 48px;
        color: #999;
      }

      p {
        margin: 0.5rem 0;
        color: #666;
        font-size: 0.875rem;
      }

      small {
        color: #999;
        font-size: 0.75rem;
      }
    }

    .btn {
      padding: 0.625rem 1.25rem;
      border: none;
      border-radius: 6px;
      font-size: 0.875rem;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.2s;
      font-family: inherit;

      &:disabled {
        opacity: 0.6;
        cursor: not-allowed;
      }
    }

    .btn-primary {
      background: #ff4400;
      color: white;

      &:hover:not(:disabled) {
        background: #e63c00;
      }
    }

    .btn-secondary {
      background: #f0f0f0;
      color: #666;

      &:hover {
        background: #e0e0e0;
      }
    }

    @media (max-width: 576px) {
      .modal-content {
        width: 95%;
        max-height: 85vh;
      }

      .modal-header, .modal-body {
        padding: 1rem;
      }
    }
  `]
})
export class CategoryFormComponent implements OnInit {
  @Input() category: any = null;
  @Input() allCategories: any[] = [];
  @Output() save = new EventEmitter<any>();
  @Output() cancel = new EventEmitter<void>();

  private fb = inject(FormBuilder);
  categoryForm!: FormGroup;
  imagePreview: string | null = null;
  selectedFile: File | null = null;
  newImagePreview: string | null = null;

  get filteredCategories() {
    return this.allCategories.filter(c => !this.category || c.id !== this.category.id);
  }

  ngOnInit() {
    this.categoryForm = this.fb.group({
      name: [this.category?.name || '', []],
      parentId: [this.category?.parentId || null],
      description: [this.category?.description || ''],
      imageUrl: [this.category?.imageUrl || ''],
      icon: [this.category?.icon || ''],
      sortOrder: [this.category?.sortOrder || 0],
      active: [this.category ? this.category.active : true]
    });

    if (this.category?.imageUrl) {
      this.imagePreview = this.category.imageUrl;
    }
  }

  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files[0]) {
      this.selectedFile = input.files[0];

      const reader = new FileReader();
      reader.onload = (e) => {
        this.newImagePreview = e.target?.result as string;
      };
      reader.readAsDataURL(this.selectedFile);
    }
  }

  removeNewImage() {
    this.selectedFile = null;
    this.newImagePreview = null;
  }

  removeCurrentImage() {
    this.imagePreview = null;
    this.categoryForm.patchValue({ imageUrl: '' });
  }

  onSubmit() {
    if (this.categoryForm.valid) {
      // Tạo object JSON để gửi
      const categoryData = {
        name: this.categoryForm.get('name')?.value,
        parentId: this.categoryForm.get('parentId')?.value,
        description: this.categoryForm.get('description')?.value,
        icon: this.categoryForm.get('icon')?.value,
        sortOrder: this.categoryForm.get('sortOrder')?.value || 0,
        active: this.categoryForm.get('active')?.value,
        imageUrl: this.categoryForm.get('imageUrl')?.value || null
      };

      // Emit cả data và file ảnh (nếu có)
      this.save.emit({
        data: categoryData,
        image: this.selectedFile,
        removeImage: this.imagePreview === null && this.category?.imageUrl !== null
      });
    }
  }

  onCancel() {
    this.cancel.emit();
  }
}
