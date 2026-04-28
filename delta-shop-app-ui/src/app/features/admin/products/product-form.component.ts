import { Component, Input, Output, EventEmitter, OnInit, OnChanges, SimpleChanges, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators, FormArray } from '@angular/forms';
import { AdminCategoryService } from '../categories/admin-category.service';
import { AdminBrandService } from '../brands/admin-brand.service';
import { AdminProductService } from './admin-product.service';

@Component({
  selector: 'app-product-form',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  template: `
    <div class="modal-backdrop" (click)="onCancel()">
      <div class="modal-content" (click)="$event.stopPropagation()">
        <div class="modal-header">
          <h2>{{ product ? 'Sửa sản phẩm' : 'Thêm sản phẩm mới' }}</h2>
          <button class="btn-close" (click)="onCancel()">&times;</button>
        </div>

        <form [formGroup]="productForm" (ngSubmit)="onSubmit()" class="form-flex">
          <div class="modal-body">
            <div class="form-container">
              <!-- Basic Info Section -->
              <div class="form-section">
                <h3 class="section-title">Thông tin cơ bản</h3>
                <div class="form-grid">
                  <div class="form-group full-width">
                    <label>Tên sản phẩm *</label>
                    <input type="text" formControlName="name" placeholder="Nhập tên sản phẩm">
                    <div class="error-msg" *ngIf="productForm.get('name')?.touched && productForm.get('name')?.errors?.['required']">
                      Tên sản phẩm là bắt buộc
                    </div>
                  </div>

                  <div class="form-group">
                    <label>Danh mục *</label>
                    <select formControlName="categoryId">
                      <option value="">Chọn danh mục</option>
                      <option *ngFor="let cat of categories" [value]="cat.id">{{ cat.name }}</option>
                    </select>
                  </div>

                  <div class="form-group">
                    <label>Thương hiệu</label>
                    <select formControlName="brandId">
                      <option value="">Chọn thương hiệu</option>
                      <option *ngFor="let brand of brands" [value]="brand.id">{{ brand.name }}</option>
                    </select>
                  </div>

                  <div class="form-group">
                    <label>Giá bán (VND) *</label>
                    <input type="number" formControlName="basePrice">
                  </div>

                  <div class="form-group">
                    <label>Giá gốc (VND)</label>
                    <input type="number" formControlName="comparePrice">
                  </div>

                  <div class="form-group">
                    <label>Số lượng kho *</label>
                    <input type="number" formControlName="stockQuantity">
                  </div>

                  <div class="form-group">
                    <label>Mã SKU</label>
                    <input type="text" formControlName="sku">
                  </div>

                  <div class="form-group">
                    <label>Trạng thái</label>
                    <select formControlName="status">
                      <option value="ACTIVE">Đang bán</option>
                      <option value="INACTIVE">Ngừng bán</option>
                      <option value="OUT_OF_STOCK">Hết hàng</option>
                      <option value="DISCONTINUED">Ngừng sản xuất</option>
                    </select>
                  </div>

                  <div class="form-group">
                    <label>Giá vốn (VND)</label>
                    <input type="number" formControlName="costPrice">
                  </div>

                  <div class="form-group">
                    <label>Trọng lượng (kg)</label>
                    <input type="number" formControlName="weight" step="0.1">
                  </div>

                  <div class="form-group-grid full-width">
                    <div class="form-group">
                      <label>Dài (cm)</label>
                      <input type="number" formControlName="length">
                    </div>
                    <div class="form-group">
                      <label>Rộng (cm)</label>
                      <input type="number" formControlName="width">
                    </div>
                    <div class="form-group">
                      <label>Cao (cm)</label>
                      <input type="number" formControlName="height">
                    </div>
                  </div>

                  <div class="form-group full-width">
                    <div class="checkbox-group">
                      <label class="checkbox-item">
                        <input type="checkbox" formControlName="featured">
                        <span>Nổi bật</span>
                      </label>
                      <label class="checkbox-item">
                        <input type="checkbox" formControlName="newArrival">
                        <span>Hàng mới về</span>
                      </label>
                      <label class="checkbox-item">
                        <input type="checkbox" formControlName="bestSeller">
                        <span>Bán chạy</span>
                      </label>
                    </div>
                  </div>

                  <div class="form-group full-width">
                    <label>Tags (ngăn cách bởi dấu phẩy)</label>
                    <input type="text" formControlName="tags" placeholder="giày, thể thao, nam">
                  </div>

                  <div class="form-group full-width">
                    <label>Loại môn thể thao (ngăn cách bởi dấu phẩy)</label>
                    <input type="text" formControlName="sportTypes" placeholder="bóng đá, chạy bộ">
                  </div>
                </div>
              </div>

              <!-- Size Guide Section -->
              <div class="form-section">
                <h3 class="section-title">
                  Hướng dẫn chọn size
                  <button type="button" class="btn-add-size" (click)="addSizeGuide()">
                    <i class="bi bi-plus-circle"></i> Thêm size
                  </button>
                </h3>

                <div class="size-guide-info">
                  <p class="info-text">
                    <i class="bi bi-info-circle"></i>
                    Thêm các size cho sản phẩm (áo: S, M, L, XL; giày: 38, 39, 40, ...)
                  </p>
                </div>

                <div formArrayName="sizeGuides" class="size-guides-container">
                  <div *ngFor="let guide of sizeGuides.controls; let i = index" [formGroupName]="i" class="size-guide-item">
                    <div class="size-guide-header">
                      <span class="size-number">Size #{{ i + 1 }}</span>
                      <button type="button" class="btn-remove-size" (click)="removeSizeGuide(i)">
                        <i class="bi bi-trash"></i>
                      </button>
                    </div>
                    <div class="size-guide-grid">
                      <div class="form-group">
                        <label>Size *</label>
                        <input type="text" formControlName="size" placeholder="VD: S, M, L, XL, 38, 39">
                      </div>
                      <div class="form-group">
                        <label>Nhãn hiển thị</label>
                        <input type="text" formControlName="label" placeholder="VD: Small, Medium, Size 38">
                      </div>
                      <div class="form-group">
                        <label>Mô tả size</label>
                        <input type="text" formControlName="description" placeholder="VD: Cân nặng 45-55kg, Cao 155-165cm">
                      </div>
                      <div class="form-group">
                        <label>Số đo tham khảo</label>
                        <input type="text" formControlName="measurement" placeholder="VD: 45-55kg, 25cm">
                      </div>
                      <div class="form-group">
                        <label>Số lượng tồn</label>
                        <input type="number" formControlName="stockQuantity" placeholder="0" min="0">
                      </div>
                      <div class="form-group">
                        <label>Điều chỉnh giá</label>
                        <input type="number" formControlName="priceModifier" placeholder="0" step="1000">
                        <small class="field-note">Số tiền cộng thêm (đ)</small>
                      </div>
                      <div class="form-group">
                        <label>SKU riêng</label>
                        <input type="text" formControlName="sku" placeholder="Mã SKU cho size này">
                      </div>
                    </div>
                  </div>
                </div>

                <div *ngIf="sizeGuides.length === 0" class="empty-size-guide">
                  <p>Chưa có size nào. Nhấn "Thêm size" để thêm size cho sản phẩm.</p>
                </div>
              </div>

              <!-- Specifications Section -->
              <div class="form-section">
                <h3 class="section-title">
                  Thông số kỹ thuật
                  <button type="button" class="btn-add-size" (click)="addSpecification()">
                    <i class="bi bi-plus-circle"></i> Thêm thông số
                  </button>
                </h3>

                <div formArrayName="specifications" class="specifications-container">
                  <div *ngFor="let spec of specifications.controls; let i = index" [formGroupName]="i" class="spec-item">
                    <div class="spec-row">
                      <div class="form-group spec-key-group">
                        <label>Tên thông số *</label>
                        <input type="text" formControlName="key" placeholder="VD: Chất liệu, Xuất xứ">
                      </div>
                      <div class="form-group spec-value-group">
                        <label>Giá trị *</label>
                        <input type="text" formControlName="value" placeholder="VD: Cotton 100%, Việt Nam">
                      </div>
                      <button type="button" class="btn-remove-spec" (click)="removeSpecification(i)">
                        <i class="bi bi-x-circle"></i>
                      </button>
                    </div>
                  </div>
                </div>

                <div *ngIf="specifications.length === 0" class="empty-spec">
                  <p>Chưa có thông số kỹ thuật nào. Nhấn "Thêm thông số" để thêm.</p>
                </div>
              </div>

              <!-- Description Section -->
              <div class="form-section">
                <h3 class="section-title">Mô tả sản phẩm</h3>
                <div class="form-grid">
                  <div class="form-group full-width">
                    <label>Mô tả ngắn</label>
                    <textarea formControlName="shortDescription" rows="2" placeholder="Mô tả ngắn gọn về sản phẩm"></textarea>
                  </div>

                  <div class="form-group full-width">
                    <label>Mô tả chi tiết</label>
                    <textarea formControlName="description" rows="6" placeholder="Mô tả chi tiết sản phẩm..."></textarea>
                  </div>
                </div>
              </div>

              <!-- Image Upload Section -->
              <div class="form-section">
                <h3 class="section-title">Hình ảnh sản phẩm</h3>
                <div class="upload-area" [class.uploading]="isUploading" (click)="fileInput.click()">
                  <input type="file" #fileInput multiple accept="image/*" class="hidden-input" (change)="onFileSelected($event)">
                  <div class="upload-placeholder" *ngIf="!isUploading">
                    <i class="bi bi-cloud-arrow-up"></i>
                    <p>Nhấp để tải lên hoặc kéo thả nhiều ảnh</p>
                    <span>PNG, JPG, JPEG (Tối đa 5MB)</span>
                  </div>
                  <div class="upload-loader" *ngIf="isUploading">
                    <div class="spinner"></div>
                    <p>Đang tải ảnh lên Cloudinary...</p>
                  </div>
                </div>

                <div class="image-preview-grid" *ngIf="uploadedImages.length > 0">
                  <div class="image-preview-item" *ngFor="let image of uploadedImages; let i = index" [class.is-primary]="image.primary">
                    <img [src]="image.url" alt="Preview">
                    <div class="image-actions">
                      <button type="button" class="btn-action primary-toggle" (click)="setPrimary(i)" [title]="image.primary ? 'Ảnh chính' : 'Đặt làm ảnh chính'">
                        <i class="bi" [ngClass]="image.primary ? 'bi-star-fill' : 'bi-star'"></i>
                      </button>
                      <button type="button" class="btn-action remove-btn" (click)="removeImage(i)" title="Xóa ảnh">
                        <i class="bi bi-trash"></i>
                      </button>
                    </div>
                    <div class="primary-badge" *ngIf="image.primary">Ảnh chính</div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div class="modal-footer">
            <button type="button" class="btn btn-outline" (click)="onCancel()">Hủy</button>
            <button type="submit" class="btn btn-primary" [disabled]="productForm.invalid || isUploading">
              {{ isUploading ? 'Vui lòng đợi...' : (product ? 'Cập nhật sản phẩm' : 'Tạo sản phẩm') }}
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
      background: rgba(0,0,0,0.6);
      display: flex;
      align-items: center;
      justify-content: center;
      z-index: 2000;
      backdrop-filter: blur(8px);
    }
    .modal-content {
      background: white;
      width: 95%;
      max-width: 1100px;
      height: 90vh;
      overflow: hidden;
      border-radius: 16px;
      display: flex;
      flex-direction: column;
      box-shadow: 0 25px 50px -12px rgba(0,0,0,0.25);
      animation: modalSlideUp 0.3s ease-out;
    }
    @keyframes modalSlideUp {
      from { transform: translateY(20px); opacity: 0; }
      to { transform: translateY(0); opacity: 1; }
    }
    .modal-header {
      padding: 1.5rem 2rem;
      border-bottom: 1px solid #f0f0f0;
      display: flex;
      justify-content: space-between;
      align-items: center;
      h2 { margin: 0; font-size: 1.5rem; color: #111; font-weight: 700; }
    }
    .btn-close {
      background: #f5f5f5;
      border: none;
      width: 36px;
      height: 36px;
      border-radius: 50%;
      font-size: 1.5rem;
      cursor: pointer;
      color: #666;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: all 0.2s;
      &:hover { background: #eee; color: #111; }
    }
    .form-flex {
      display: flex;
      flex-direction: column;
      flex: 1;
      min-height: 0;
      overflow: hidden;
    }
    .modal-body {
      padding: 2rem;
      overflow-y: auto;
      flex: 1;
      min-height: 0;
    }
    .form-container {
      display: flex;
      flex-direction: column;
      gap: 2rem;
    }
    .form-section {
      background: #fafafa;
      border-radius: 12px;
      padding: 1.5rem;
      border: 1px solid #efefef;
    }
    .section-title {
      font-size: 1.2rem;
      font-weight: 600;
      margin: 0 0 1rem 0;
      color: #1f2937;
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding-bottom: 0.75rem;
      border-bottom: 2px solid #e5e7eb;
    }
    .modal-footer {
      padding: 1.5rem 2rem;
      border-top: 1px solid #f0f0f0;
      display: flex;
      justify-content: flex-end;
      gap: 1rem;
    }
    .form-grid {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 1.5rem;
    }
    .full-width { grid-column: span 2; }
    .form-group {
      display: flex;
      flex-direction: column;
      gap: 0.5rem;
      label { font-weight: 600; font-size: 0.9rem; color: #374151; }
      input, select, textarea {
        padding: 0.75rem 1rem;
        border: 1px solid #d1d5db;
        border-radius: 8px;
        font-size: 0.95rem;
        transition: border-color 0.2s, box-shadow 0.2s;
        &:focus {
          outline: none;
          border-color: #3b82f6;
          box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
        }
      }
      textarea {
        resize: vertical;
        font-family: inherit;
      }
    }
    .error-msg { color: #ef4444; font-size: 0.8rem; margin-top: -0.25rem; }
    .field-note { font-size: 0.7rem; color: #6b7280; margin-top: -0.25rem; }

    .btn-add-size {
      background: #3b82f6;
      color: white;
      border: none;
      padding: 0.5rem 1rem;
      border-radius: 8px;
      font-size: 0.85rem;
      font-weight: 500;
      cursor: pointer;
      display: inline-flex;
      align-items: center;
      gap: 0.5rem;
      transition: all 0.2s;
      &:hover {
        background: #2563eb;
        transform: translateY(-1px);
      }
    }
    .size-guide-info {
      background: #e8f0fe;
      padding: 0.75rem 1rem;
      border-radius: 8px;
      margin-bottom: 1rem;
      .info-text {
        margin: 0;
        font-size: 0.85rem;
        color: #1e40af;
        display: flex;
        align-items: center;
        gap: 0.5rem;
        i { font-size: 1rem; }
      }
    }
    .size-guides-container {
      display: flex;
      flex-direction: column;
      gap: 1rem;
    }
    .size-guide-item {
      background: white;
      border: 1px solid #e5e7eb;
      border-radius: 12px;
      padding: 1rem;
      transition: box-shadow 0.2s;
      &:hover {
        box-shadow: 0 4px 6px -1px rgba(0,0,0,0.1);
      }
    }
    .size-guide-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 1rem;
      padding-bottom: 0.5rem;
      border-bottom: 1px dashed #e5e7eb;
      .size-number {
        font-weight: 600;
        color: #3b82f6;
        font-size: 0.9rem;
      }
    }
    .btn-remove-size {
      background: #fee2e2;
      border: none;
      padding: 0.4rem 0.8rem;
      border-radius: 6px;
      color: #dc2626;
      cursor: pointer;
      transition: all 0.2s;
      &:hover {
        background: #fecaca;
        transform: scale(1.05);
      }
    }
    .size-guide-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
      gap: 1rem;
    }
    .empty-size-guide, .empty-spec {
      text-align: center;
      padding: 2rem;
      color: #6b7280;
      background: #f9fafb;
      border-radius: 8px;
      p { margin: 0; }
    }

    .specifications-container {
      display: flex;
      flex-direction: column;
      gap: 0.75rem;
    }
    .spec-item {
      background: white;
      border: 1px solid #e5e7eb;
      border-radius: 8px;
      padding: 1rem;
    }
    .spec-row {
      display: flex;
      gap: 1rem;
      align-items: flex-start;
    }
    .spec-key-group {
      flex: 1;
      margin-bottom: 0;
    }
    .spec-value-group {
      flex: 2;
      margin-bottom: 0;
    }
    .btn-remove-spec {
      background: #fee2e2;
      border: none;
      padding: 0.5rem;
      border-radius: 6px;
      color: #dc2626;
      cursor: pointer;
      margin-top: 1.7rem;
      transition: all 0.2s;
      &:hover {
        background: #fecaca;
        transform: scale(1.05);
      }
    }

    .checkbox-group {
      display: flex;
      gap: 2rem;
      padding: 0.5rem 0;
    }
    .checkbox-item {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      cursor: pointer;
      font-weight: 600;
      font-size: 0.9rem;
      color: #374151;
      input { width: auto; }
    }
    .form-group-grid {
      display: grid;
      grid-template-columns: repeat(3, 1fr);
      gap: 1rem;
    }

    .upload-area {
      border: 2px dashed #d1d5db;
      border-radius: 12px;
      padding: 2rem;
      text-align: center;
      cursor: pointer;
      transition: all 0.2s;
      background: #f9fafb;
      &:hover { border-color: #3b82f6; background: #f0f7ff; }
      &.uploading { cursor: not-allowed; opacity: 0.7; }
    }
    .hidden-input { display: none; }
    .upload-placeholder {
      i { font-size: 2.5rem; color: #9ca3af; }
      p { margin: 0.5rem 0 0.25rem; font-weight: 600; color: #4b5563; }
      span { font-size: 0.8rem; color: #6b7280; }
    }
    .upload-loader {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 1rem;
      .spinner {
        width: 32px;
        height: 32px;
        border: 3px solid #f3f3f3;
        border-top: 3px solid #3b82f6;
        border-radius: 50%;
        animation: spin 1s linear infinite;
      }
      p { margin: 0; font-weight: 500; color: #3b82f6; }
    }
    @keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }

    .image-preview-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
      gap: 1rem;
      margin-top: 1.5rem;
    }
    .image-preview-item {
      position: relative;
      aspect-ratio: 1;
      border-radius: 10px;
      overflow: hidden;
      border: 2px solid transparent;
      box-shadow: 0 4px 6px -1px rgba(0,0,0,0.1);
      &.is-primary { border-color: #3b82f6; }
      img { width: 100%; height: 100%; object-fit: cover; }
      &:hover .image-actions { opacity: 1; }
    }
    .image-actions {
      position: absolute;
      inset: 0;
      background: rgba(0,0,0,0.4);
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 0.5rem;
      opacity: 0;
      transition: opacity 0.2s;
    }
    .btn-action {
      width: 32px;
      height: 32px;
      border: none;
      border-radius: 50%;
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      transition: transform 0.2s;
      &:hover { transform: scale(1.1); }
    }
    .primary-toggle { background: #3b82f6; }
    .remove-btn { background: #ef4444; }
    .primary-badge {
      position: absolute;
      bottom: 0;
      left: 0;
      right: 0;
      background: #3b82f6;
      color: white;
      font-size: 0.7rem;
      text-align: center;
      padding: 0.25rem 0;
      font-weight: 600;
    }

    .btn {
      padding: 0.75rem 1.5rem;
      border-radius: 8px;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.2s;
      display: flex;
      align-items: center;
      gap: 0.5rem;
    }
    .btn-primary { background: #111; color: white; border: none; &:hover { background: #333; } &:disabled { opacity: 0.5; cursor: not-allowed; } }
    .btn-outline { background: white; color: #374151; border: 1px solid #d1d5db; &:hover { background: #f9fafb; } }

    @media (max-width: 768px) {
      .modal-body { padding: 1rem; }
      .form-section { padding: 1rem; }
      .form-grid { grid-template-columns: 1fr; gap: 1rem; }
      .full-width { grid-column: span 1; }
      .size-guide-grid { grid-template-columns: 1fr; }
      .spec-row { flex-direction: column; }
      .btn-remove-spec { margin-top: 0; align-self: flex-end; }
      .section-title { flex-direction: column; gap: 0.5rem; align-items: flex-start; }
    }
  `]
})
export class ProductFormComponent implements OnInit, OnChanges {
  @Input() product: any = null;
  @Input() categories: any[] = [];
  @Input() brands: any[] = [];
  @Output() save = new EventEmitter<any>();
  @Output() cancel = new EventEmitter<void>();

  private fb = inject(FormBuilder);
  private productService = inject(AdminProductService);
  private cdr = inject(ChangeDetectorRef);

  productForm!: FormGroup;
  uploadedImages: Array<{ url: string; publicId: string; primary?: boolean }> = [];
  isUploading = false;

  ngOnInit() {
    this.initForm();
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['product'] && !changes['product'].firstChange && this.productForm) {
      // Reset form arrays
      while (this.sizeGuides.length) {
        this.sizeGuides.removeAt(0);
      }
      while (this.specifications.length) {
        this.specifications.removeAt(0);
      }

      // Update form values
      this.productForm.patchValue({
        name: this.product?.name || '',
        categoryId: this.product?.categoryId || this.product?.category?.id || '',
        brandId: this.product?.brandId || this.product?.brand?.id || '',
        basePrice: this.product?.basePrice || this.product?.price || 0,
        comparePrice: this.product?.comparePrice || this.product?.originalPrice || null,
        stockQuantity: this.product?.stockQuantity || 0,
        sku: this.product?.sku || '',
        status: this.product?.status || 'ACTIVE',
        costPrice: this.product?.costPrice || null,
        weight: this.product?.weight || null,
        length: this.product?.length || null,
        width: this.product?.width || null,
        height: this.product?.height || null,
        featured: this.product?.featured || false,
        newArrival: this.product?.newArrival || false,
        bestSeller: this.product?.bestSeller || false,
        tags: this.product?.tags ? (Array.isArray(this.product.tags) ? this.product.tags.join(', ') : this.product.tags) : '',
        sportTypes: this.product?.sportTypes ? (Array.isArray(this.product.sportTypes) ? this.product.sportTypes.join(', ') : this.product.sportTypes) : '',
        shortDescription: this.product?.shortDescription || '',
        description: this.product?.description || '',
      });

      // Reload size guides
      if (this.product?.sizeGuides && Array.isArray(this.product.sizeGuides)) {
        this.product.sizeGuides.forEach((guide: any) => {
          this.addSizeGuide(guide);
        });
      }

      // Reload specifications
      if (this.product?.specifications && typeof this.product.specifications === 'object') {
        Object.entries(this.product.specifications).forEach(([key, value]) => {
          if (key && value) {
            this.addSpecification({ key, value: String(value) });
          }
        });
      }

      // Reload images
      this.loadExistingImages();
      this.cdr.detectChanges();
    }
  }
get sizeGuides(): FormArray {
  return this.productForm.get('sizeGuides') as FormArray;
}

get specifications(): FormArray {
  return this.productForm.get('specifications') as FormArray;
}

 initForm() {
  // Khởi tạo FormArray trước
  const sizeGuidesArray = this.fb.array([]);
  const specificationsArray = this.fb.array([]);

  this.productForm = this.fb.group({
    name: [this.product?.name || '', [Validators.required]],
    categoryId: [this.product?.categoryId || this.product?.category?.id || '', [Validators.required]],
    brandId: [this.product?.brandId || this.product?.brand?.id || ''],
    basePrice: [this.product?.basePrice || this.product?.price || 0, [Validators.required, Validators.min(0)]],
    comparePrice: [this.product?.comparePrice || this.product?.originalPrice || null],
    stockQuantity: [this.product?.stockQuantity || 0, [Validators.required, Validators.min(0)]],
    sku: [this.product?.sku || ''],
    status: [this.product?.status || 'ACTIVE'],
    costPrice: [this.product?.costPrice || null],
    weight: [this.product?.weight || null],
    length: [this.product?.length || null],
    width: [this.product?.width || null],
    height: [this.product?.height || null],
    featured: [this.product?.featured || false],
    newArrival: [this.product?.newArrival || false],
    bestSeller: [this.product?.bestSeller || false],
    tags: [this.product?.tags ? (Array.isArray(this.product.tags) ? this.product.tags.join(', ') : this.product.tags) : ''],
    sportTypes: [this.product?.sportTypes ? (Array.isArray(this.product.sportTypes) ? this.product.sportTypes.join(', ') : this.product.sportTypes) : ''],
    shortDescription: [this.product?.shortDescription || ''],
    description: [this.product?.description || ''],
    images: [[]],
    sizeGuides: sizeGuidesArray,
    specifications: specificationsArray
  });

  // Load size guides (nếu có)
  if (this.product?.sizeGuides && Array.isArray(this.product.sizeGuides) && this.product.sizeGuides.length > 0) {
    this.product.sizeGuides.forEach((guide: any) => {
      this.addSizeGuide(guide);
    });
  }

  // Load specifications (nếu có)
  if (this.product?.specifications && typeof this.product.specifications === 'object' && !Array.isArray(this.product.specifications)) {
    Object.entries(this.product.specifications).forEach(([key, value]) => {
      if (key && value) {
        this.addSpecification({ key, value: String(value) });
      }
    });
  }

  // Load images
  this.loadExistingImages();

  // Force update view
  this.cdr.detectChanges();
}

  loadExistingImages() {
    if (this.product?.images && this.product.images.length > 0) {
      this.uploadedImages = this.product.images.map((img: any, index: number) => {
        if (typeof img === 'string') {
          return { url: img, publicId: '', primary: index === 0 };
        }
        return {
          url: img.url,
          publicId: img.publicId || '',
          primary: !!img.primary
        };
      });
    } else if (this.product?.mainImage) {
      this.uploadedImages = [{
        url: this.product.mainImage,
        publicId: '',
        primary: true
      }];
    }
    this.syncImagesToForm();
  }

  addSizeGuide(data?: any) {
    const sizeGuideGroup = this.fb.group({
      size: [data?.size || '', Validators.required],
      label: [data?.label || ''],
      description: [data?.description || ''],
      measurement: [data?.measurement || ''],
      stockQuantity: [data?.stockQuantity || 0],
      priceModifier: [data?.priceModifier || 0],
      sku: [data?.sku || '']
    });
    this.sizeGuides.push(sizeGuideGroup);
    this.cdr.detectChanges();
  }

  removeSizeGuide(index: number) {
    if (this.sizeGuides && this.sizeGuides.length > index) {
      this.sizeGuides.removeAt(index);
      this.cdr.detectChanges();
    }
  }

  // addSpecification(data?: { key: string; value: string }) {
  //   const specGroup = this.fb.group({
  //     key: [data?.key || '', Validators.required],
  //     value: [data?.value || '', Validators.required]
  //   });
  //   this.specifications.push(specGroup);
  //   this.cdr.detectChanges();
  // }

  removeSpecification(index: number) {
  const specsArray = this.productForm.get('specifications') as FormArray;
  if (specsArray && specsArray.length > index) {
    specsArray.removeAt(index);
    this.cdr.detectChanges();
    console.log('Removed specification at index:', index);
  }
}

  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (!input.files || input.files.length === 0) return;

    const files = Array.from(input.files);
    this.isUploading = true;

    this.productService.uploadProductImages(files).subscribe({
      next: (responses: any[]) => {
        const newImages = responses.map((item, index) => ({
          url: item.secureUrl || item.url,
          publicId: item.publicId,
          primary: this.uploadedImages.length === 0 && index === 0
        }));
        this.uploadedImages = [...this.uploadedImages, ...newImages];
        this.syncImagesToForm();
        this.isUploading = false;
        input.value = '';
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Upload failed', err);
        alert('Tải ảnh lên thất bại. Vui lòng thử lại.');
        this.isUploading = false;
        input.value = '';
      }
    });
  }

  setPrimary(index: number) {
    this.uploadedImages.forEach((img, i) => img.primary = (i === index));
    this.syncImagesToForm();
    this.cdr.detectChanges();
  }

  removeImage(index: number) {
    const wasPrimary = this.uploadedImages[index].primary;
    this.uploadedImages.splice(index, 1);

    if (wasPrimary && this.uploadedImages.length > 0) {
      this.uploadedImages[0].primary = true;
    }
    this.syncImagesToForm();
    this.cdr.detectChanges();
  }

  private syncImagesToForm() {
    const payload = this.uploadedImages.map((img, index) => ({
      url: img.url,
      publicId: img.publicId,
      primary: !!img.primary,
      sortOrder: index
    }));
    this.productForm.patchValue({ images: payload });
  }

  onSubmit() {
    if (this.productForm.valid) {
      const formValue = this.productForm.value;

      // Xử lý tags
      if (formValue.tags && typeof formValue.tags === 'string') {
        formValue.tags = formValue.tags.split(',').map((t: string) => t.trim()).filter((t: string) => t);
      } else if (!formValue.tags) {
        formValue.tags = [];
      }

      // Xử lý sportTypes
      if (formValue.sportTypes && typeof formValue.sportTypes === 'string') {
        formValue.sportTypes = formValue.sportTypes.split(',').map((s: string) => s.trim()).filter((s: string) => s);
      } else if (!formValue.sportTypes) {
        formValue.sportTypes = [];
      }

      // Xử lý sizeGuides
      if (formValue.sizeGuides && Array.isArray(formValue.sizeGuides)) {
        formValue.sizeGuides = formValue.sizeGuides.filter((guide: any) => guide.size && guide.size.trim() !== '');
      } else {
        formValue.sizeGuides = [];
      }

      // Xử lý specifications
      if (formValue.specifications && Array.isArray(formValue.specifications)) {
        const specsObj: Record<string, any> = {};
        formValue.specifications.forEach((spec: any) => {
          if (spec.key && spec.key.trim() && spec.value && spec.value.trim()) {
            specsObj[spec.key.trim()] = spec.value.trim();
          }
        });
        formValue.specifications = specsObj;
      } else {
        formValue.specifications = {};
      }

      this.save.emit(formValue);
    } else {
      this.productForm.markAllAsTouched();
      console.log('Form invalid:', this.productForm.errors);
    }
  }

  onCancel() {
    this.cancel.emit();
  }
  // Thêm method này vào component
ngAfterViewInit() {
  console.log('========== DEBUG FORM ==========');
  console.log('ProductForm exists:', !!this.productForm);
  console.log('Specifications control exists:', !!this.productForm?.get('specifications'));
  console.log('Specifications type:', this.productForm?.get('specifications') instanceof FormArray);
  console.log('Specifications value:', this.productForm?.get('specifications')?.value);
  console.log('Specifications controls length:', this.specifications?.length);
  console.log('================================');
}

// Debug khi thêm specification
addSpecification(data?: { key: string; value: string }) {
  console.log('🔵 addSpecification called');
  console.log('Data received:', data);
  console.log('ProductForm exists:', !!this.productForm);

  // Kiểm tra control specifications
  const specsControl = this.productForm.get('specifications');
  console.log('Specifications control:', specsControl);
  console.log('Is FormArray:', specsControl instanceof FormArray);

  let specsArray: FormArray;

  if (!specsControl) {
    console.log('⚠️ Specifications control not found, creating new FormArray');
    specsArray = this.fb.array([]);
    this.productForm.setControl('specifications', specsArray);
  } else if (!(specsControl instanceof FormArray)) {
    console.log('⚠️ Specifications control is not FormArray, replacing');
    specsArray = this.fb.array([]);
    this.productForm.setControl('specifications', specsArray);
  } else {
    specsArray = specsControl as FormArray;
  }

  const specGroup = this.fb.group({
    key: [data?.key || '', Validators.required],
    value: [data?.value || '', Validators.required]
  });

  console.log('Pushing new spec group:', specGroup.value);
  specsArray.push(specGroup);

  console.log('✅ New specifications count:', specsArray.length);
  console.log('All specifications:', specsArray.value);

  this.cdr.detectChanges();
}

// Debug khi render template
get specificationsDebug(): string {
  const exists = !!this.productForm?.get('specifications');
  const isArray = this.productForm?.get('specifications') instanceof FormArray;
  const length = this.specifications?.length;
  return `Exists: ${exists}, IsArray: ${isArray}, Length: ${length}`;
}
}

export interface ProductFormMapping {
  name: string;
  categoryId: string;
  brandId?: string;
  basePrice: number;
  comparePrice?: number;
  stockQuantity: number;
  sku?: string;
  shortDescription?: string;
  description?: string;
  images?: Array<{ url: string; publicId?: string; primary?: boolean; sortOrder?: number }>;
  sizeGuides?: Array<{
    size: string;
    label?: string;
    description?: string;
    measurement?: string;
    stockQuantity?: number;
    priceModifier?: number;
    sku?: string;
  }>;
  specifications?: Record<string, any>;
}
