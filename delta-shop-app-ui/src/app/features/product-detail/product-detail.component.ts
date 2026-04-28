import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { ProductService } from '../../core/services/product.service';
import { CartService } from '../../core/services/cart.service';
import { ToastrService } from 'ngx-toastr';
import { SkeletonLoaderComponent } from '../../shared/components/skeleton-loader/skeleton-loader.component';
import { FormsModule } from '@angular/forms';
import { LucideAngularModule, ShoppingCart, Check, Heart, Shield, Truck, RefreshCw, Star, TruckIcon, Package, Ruler, Weight, Tag, Info } from 'lucide-angular';

interface SizeGuide {
  size: string;
  label: string;
  description: string;
  measurement: string;
  stockQuantity: number;
  priceModifier: number;
  sku: string;
}

interface ProductImage {
  url: string;
  publicId?: string;
  altText?: string | null;
  sortOrder?: number;
  primary?: boolean;
}

interface ProductData {
  id: string;
  name: string;
  slug: string;
  sku: string;
  description: string;
  shortDescription?: string;
  basePrice: number;
  comparePrice?: number;
  discountPercentage?: number;
  stockQuantity: number;
  status: string;
  featured: boolean;
  newArrival: boolean;
  bestSeller: boolean;
  averageRating?: number;
  reviewCount?: number;
  totalSold?: number;
  totalViews?: number;
  tags?: string[];
  sportTypes?: string[];
  images: ProductImage[];
  galleryImages?: ProductImage[];
  category?: {
    id: string;
    name: string;
    slug: string;
  };
  brand?: {
    id: string;
    name: string;
    slug: string;
    logo?: string;
  };
  weight?: number;
  length?: number;
  width?: number;
  height?: number;
  costPrice?: number;
  sizeGuides?: SizeGuide[];
  specifications?: Record<string, string>;
  createdAt?: string;
}

interface ProductDetailApiResponse {
  product: ProductData;
  variants: any[];
  galleryImages: ProductImage[];
  relatedProducts: any[];
}

@Component({
  selector: 'app-product-detail',
  standalone: true,
  imports: [CommonModule, SkeletonLoaderComponent, FormsModule, RouterModule, LucideAngularModule],
  template: `
    <section class="product-detail-section">
      <div class="container">
        <!-- Breadcrumb -->
        <nav class="breadcrumb" *ngIf="!loading && product">
          <a routerLink="/">Trang chủ</a>
          <span>/</span>
          <a routerLink="/products">Sản phẩm</a>
          <span>/</span>
          <a *ngIf="product.category" [routerLink]="['/category', product.category.slug]">{{ product.category.name }}</a>
          <span *ngIf="product.category">/</span>
          <span class="current">{{ product.name }}</span>
        </nav>

        @if (loading) {
          <app-skeleton-loader type="detail"></app-skeleton-loader>
        } @else if (product) {
          <div class="product-detail-grid">
            <!-- Left Column - Image Gallery -->
            <div class="product-gallery">
              <div class="main-image-container">
                <img
                  *ngIf="selectedImage"
                  [src]="selectedImage"
                  [alt]="product.name"
                  class="main-image"
                  (click)="openLightbox()"
                  (error)="onMainImageError()"
                >
                <div *ngIf="!selectedImage" class="no-image">
                  <span>Không có ảnh</span>
                </div>

                <!-- Badges -->
                <div class="image-badges">
                  <span class="badge new" *ngIf="product.newArrival">Mới</span>
                  <span class="badge sale" *ngIf="product.discountPercentage && product.discountPercentage > 0">
                    -{{ product.discountPercentage }}%
                  </span>
                  <span class="badge best" *ngIf="product.bestSeller">Best Seller</span>
                </div>
              </div>

              <!-- Thumbnail Gallery -->
              <div class="thumbnail-list" *ngIf="productImages.length > 0">
                <div
                  class="thumbnail-item"
                  *ngFor="let image of productImages; let i = index"
                  [class.active]="selectedImage === image.url"
                  (click)="selectImage(image)"
                >
                  <img
                    [src]="image.url"
                    [alt]="product.name + ' - Ảnh ' + (i + 1)"
                    (error)="onThumbnailError($event, i)"
                  >
                </div>
              </div>
            </div>

            <!-- Right Column - Product Info -->
            <div class="product-info">
              <h1 class="product-title">{{ product.name }}</h1>

              <!-- Brand -->
              <div class="brand-info" *ngIf="product.brand">
                <span class="brand-label">Thương hiệu:</span>
                <a [routerLink]="['/brand', product.brand.slug]" class="brand-name">{{ product.brand.name }}</a>
              </div>

              <!-- SKU -->
              <div class="sku-info" *ngIf="product.sku">
                <span class="sku-label">Mã SKU:</span>
                <span class="sku-value">{{ product.sku }}</span>
              </div>

              <!-- Rating -->
              <div class="rating-section" *ngIf="product.averageRating">
                <div class="stars">
                  <span *ngFor="let star of [1,2,3,4,5]" class="star" [class.filled]="star <= (product.averageRating || 0)">
                    ★
                  </span>
                </div>
                <span class="rating-value">{{ (product.averageRating || 0).toFixed(1) }}</span>
                <span class="review-count">({{ product.reviewCount || 0 }} đánh giá)</span>
                <span class="sold-count" *ngIf="product.totalSold">| Đã bán: {{ product.totalSold | number }}</span>
              </div>

              <!-- Price -->
              <div class="price-wrapper">
                <div class="current-price">
                  {{ (selectedVariant ? (product.basePrice + selectedVariant.priceModifier) : product.basePrice) | currency:'VND':'symbol':'1.0-0' }}₫
                </div>
                <div class="old-price" *ngIf="product.comparePrice && product.comparePrice > product.basePrice">
                  {{ product.comparePrice | currency:'VND':'symbol':'1.0-0' }}₫
                </div>
                <div class="discount-badge" *ngIf="product.discountPercentage && product.discountPercentage > 0">
                  Tiết kiệm {{ ((product.comparePrice || 0) - product.basePrice) | currency:'VND':'symbol':'1.0-0' }}₫
                </div>
              </div>

              <!-- Short Description -->
              <div class="short-description" *ngIf="product.shortDescription">
                <p>{{ product.shortDescription }}</p>
              </div>

              <!-- Size Guide Section -->
              <div class="size-guide-section" *ngIf="product.sizeGuides && product.sizeGuides.length > 0">
                <h3 class="section-subtitle">Chọn size</h3>
                <div class="size-options">
                  <button
                    *ngFor="let size of product.sizeGuides"
                    class="size-btn"
                    [class.active]="selectedSize === size"
                    [class.out-of-stock]="size.stockQuantity === 0"
                    (click)="selectSize(size)"
                    [disabled]="size.stockQuantity === 0">
                    {{ size.label || size.size }}
                    <span class="size-stock" *ngIf="size.stockQuantity > 0 && size.stockQuantity < 10">
                      (còn {{ size.stockQuantity }})
                    </span>
                  </button>
                </div>
                <div class="size-description" *ngIf="selectedSize?.description">
                  <span class="info-icon">ℹ️</span>
                  <span>{{ selectedSize?.description || '' }}</span>
                </div>
              </div>

              <!-- Variants Section -->
              <div class="variant-section" *ngIf="variants && variants.length > 0 && (!product.sizeGuides || product.sizeGuides.length === 0)">
                <h3 class="section-subtitle">Phân loại</h3>
                <div class="variant-options">
                  <button
                    *ngFor="let variant of variants"
                    class="variant-btn"
                    [class.active]="selectedVariant?.id === variant.id"
                    [disabled]="!variant.inStock"
                    (click)="selectVariant(variant)">
                    {{ variant.name }}
                    <span class="variant-price" *ngIf="variant.priceModifier !== 0">
                      {{ variant.priceModifier > 0 ? '+' : '' }}{{ variant.priceModifier | currency:'VND':'symbol':'1.0-0' }}
                    </span>
                  </button>
                </div>
              </div>

              <!-- Quantity Selector -->
              <div class="quantity-section">
                <h3 class="section-subtitle">Số lượng</h3>
                <div class="quantity-control">
                  <button (click)="decreaseQuantity()" [disabled]="quantity <= 1">-</button>
                  <input type="number" [(ngModel)]="quantity" (change)="validateQuantity()" min="1" [max]="maxStock">
                  <button (click)="increaseQuantity()" [disabled]="quantity >= maxStock">+</button>
                </div>
                <div class="stock-status" [class.out-of-stock]="maxStock === 0">
                  <span *ngIf="maxStock > 0">
                    ✔️ Còn hàng ({{ maxStock }} sản phẩm)
                  </span>
                  <span *ngIf="maxStock === 0">
                    ❌ Hết hàng
                  </span>
                </div>
              </div>

              <!-- Action Buttons -->
              <div class="action-buttons">
                <button
                  class="btn btn-primary btn-add-to-cart"
                  (click)="addToCart()"
                  [disabled]="maxStock === 0 || (!selectedVariant && variants.length > 0 && (!product.sizeGuides || product.sizeGuides.length === 0))">
                  <lucide-icon name="shopping-cart" [size]="20"></lucide-icon>
                  THÊM VÀO GIỎ
                </button>
                <button class="btn btn-outline btn-buy-now" (click)="buyNow()" [disabled]="maxStock === 0">
                  MUA NGAY
                </button>
                <button class="btn btn-icon btn-wishlist" aria-label="Yêu thích">
                  <lucide-icon name="heart" [size]="20"></lucide-icon>
                </button>
              </div>

              <!-- Product Meta -->
              <div class="product-meta">
                <div class="meta-item" *ngIf="product.category">
                  <span class="meta-label">Danh mục:</span>
                  <a [routerLink]="['/category', product.category.slug]">{{ product.category.name }}</a>
                </div>
                <div class="meta-item" *ngIf="product.tags && product.tags.length > 0">
                  <span class="meta-label">Thẻ:</span>
                  <div class="tags-list">
                    <a *ngFor="let tag of product.tags" [routerLink]="['/products']" [queryParams]="{tag: tag}" class="tag">
                      {{ tag }}
                    </a>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Product Tabs -->
          <div class="product-tabs">
            <div class="tab-headers">
              <button
                *ngFor="let tab of tabs"
                class="tab-btn"
                [class.active]="activeTab === tab.id"
                (click)="activeTab = tab.id">
                {{ tab.label }}
              </button>
            </div>

            <div class="tab-content">
              <!-- Description Tab -->
              <div *ngIf="activeTab === 'description'" class="tab-pane">
                <div class="description-content" [innerHTML]="product.description"></div>
              </div>

              <!-- Specifications Tab -->
              <div *ngIf="activeTab === 'specifications'" class="tab-pane">
                <h3>Thông số kỹ thuật</h3>
                <div class="specs-grid">
                  <div class="spec-row" *ngIf="product.sku">
                    <div class="spec-label">Mã sản phẩm</div>
                    <div class="spec-value">{{ product.sku }}</div>
                  </div>
                  <div class="spec-row" *ngIf="product.brand">
                    <div class="spec-label">Thương hiệu</div>
                    <div class="spec-value">{{ product.brand.name }}</div>
                  </div>
                  <div class="spec-row" *ngIf="product.category">
                    <div class="spec-label">Danh mục</div>
                    <div class="spec-value">{{ product.category.name }}</div>
                  </div>
                  <div class="spec-row" *ngIf="product.weight">
                    <div class="spec-label">Trọng lượng</div>
                    <div class="spec-value">{{ product.weight }} kg</div>
                  </div>
                  <div class="spec-row" *ngIf="product.length || product.width || product.height">
                    <div class="spec-label">Kích thước</div>
                    <div class="spec-value">
                      {{ product.length || 0 }} x {{ product.width || 0 }} x {{ product.height || 0 }} cm
                    </div>
                  </div>
                  <div class="spec-row" *ngIf="product.sportTypes && product.sportTypes.length > 0">
                    <div class="spec-label">Môn thể thao</div>
                    <div class="spec-value">{{ product.sportTypes.join(', ') }}</div>
                  </div>
                  <!-- Dynamic specifications from JSON -->
                  <div class="spec-row" *ngFor="let spec of product.specifications | keyvalue">
                    <div class="spec-label">{{ spec.key }}</div>
                    <div class="spec-value">{{ spec.value }}</div>
                  </div>
                </div>
              </div>

              <!-- Size Guide Tab -->
              <div *ngIf="activeTab === 'sizeguide'" class="tab-pane">
                <h3>Bảng hướng dẫn chọn size</h3>
                <div class="size-chart" *ngIf="product.sizeGuides && product.sizeGuides.length > 0">
                  <table class="size-table">
                    <thead>
                      <tr>
                        <th>Size</th>
                        <th>Mô tả</th>
                        <th>Số đo tham khảo</th>
                        <th>Tồn kho</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr *ngFor="let size of product.sizeGuides">
                        <td><strong>{{ size.label || size.size }}</strong></td>
                        <td>{{ size.description || '—' }}</td>
                        <td>{{ size.measurement || '—' }}</td>
                        <td [class.out-of-stock]="size.stockQuantity === 0">
                          {{ size.stockQuantity > 0 ? size.stockQuantity + ' sản phẩm' : 'Hết hàng' }}
                        </td>
                      </tr>
                    </tbody>
                  </table>
                </div>
                <div *ngIf="!product.sizeGuides?.length" class="no-data">
                  <p>Chưa có hướng dẫn chọn size cho sản phẩm này.</p>
                </div>
              </div>

              <!-- Reviews Tab -->
              <div *ngIf="activeTab === 'reviews'" class="tab-pane">
                <h3>Đánh giá sản phẩm</h3>
                <div class="reviews-summary">
                  <div class="rating-overall">
                    <div class="rating-number">{{ product.averageRating?.toFixed(1) || 0 }}</div>
                    <div class="rating-stars">
                      <span *ngFor="let star of [1,2,3,4,5]" class="star" [class.filled]="star <= (product.averageRating || 0)">★</span>
                    </div>
                    <div class="rating-count">{{ product.reviewCount || 0 }} đánh giá</div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Related Products -->
          <div class="related-products" *ngIf="relatedProducts.length > 0">
            <h2 class="section-title">Sản phẩm liên quan</h2>
            <div class="related-products-grid">
              <div class="related-product-card" *ngFor="let related of relatedProducts">
                <a [routerLink]="['/product', related.slug]">
                  <img [src]="related.primaryImage || 'assets/images/placeholder.jpg'" [alt]="related.name">
                  <h4>{{ related.name }}</h4>
                  <div class="price">
                    <span class="current-price">{{ related.basePrice | currency:'VND':'symbol':'1.0-0' }}₫</span>
                    <span class="old-price" *ngIf="related.comparePrice">{{ related.comparePrice | currency:'VND':'symbol':'1.0-0' }}₫</span>
                  </div>
                </a>
              </div>
            </div>
          </div>
        } @else {
          <div class="error-state">
            <h2>Không tìm thấy sản phẩm</h2>
            <p>Sản phẩm này không tồn tại hoặc đã bị xóa.</p>
            <a routerLink="/products" class="btn btn-primary">Xem tất cả sản phẩm</a>
          </div>
        }
      </div>
    </section>
  `,
  styles: [`
    .product-detail-section {
      padding: 40px 0;
      background: #fff;
    }

    .container {
      max-width: 1280px;
      margin: 0 auto;
      padding: 0 20px;
    }

    /* Breadcrumb */
    .breadcrumb {
      margin-bottom: 30px;
      font-size: 14px;
      color: #666;
    }
    .breadcrumb a {
      color: #0066cc;
      text-decoration: none;
    }
    .breadcrumb a:hover {
      text-decoration: underline;
    }
    .breadcrumb span {
      margin: 0 8px;
    }
    .breadcrumb .current {
      color: #333;
      font-weight: 500;
    }

    /* Product Grid */
    .product-detail-grid {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 60px;
      margin-bottom: 60px;
    }

    @media (max-width: 992px) {
      .product-detail-grid {
        grid-template-columns: 1fr;
        gap: 40px;
      }
    }

    /* Gallery */
    .product-gallery {
      position: sticky;
      top: 20px;
    }
    .main-image-container {
      position: relative;
      width: 100%;
      aspect-ratio: 1;
      background: #f8f9fa;
      border: 1px solid #eee;
      border-radius: 12px;
      overflow: hidden;
      margin-bottom: 16px;
      cursor: pointer;
    }
    .main-image {
      width: 100%;
      height: 100%;
      object-fit: contain;
      transition: transform 0.3s;
    }
    .main-image:hover {
      transform: scale(1.05);
    }
    .image-badges {
      position: absolute;
      top: 12px;
      left: 12px;
      display: flex;
      gap: 8px;
      z-index: 1;
    }
    .badge {
      padding: 4px 12px;
      border-radius: 20px;
      font-size: 12px;
      font-weight: 600;
    }
    .badge.new {
      background: #e3f2fd;
      color: #1976d2;
    }
    .badge.sale {
      background: #ffebee;
      color: #c62828;
    }
    .badge.best {
      background: #fff3e0;
      color: #ef6c00;
    }
    .thumbnail-list {
      display: flex;
      gap: 12px;
      overflow-x: auto;
    }
    .thumbnail-item {
      flex-shrink: 0;
      width: 80px;
      height: 80px;
      border: 2px solid transparent;
      border-radius: 8px;
      overflow: hidden;
      cursor: pointer;
      transition: all 0.2s;
    }
    .thumbnail-item.active {
      border-color: #0066cc;
    }
    .thumbnail-item img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
    .no-image {
      width: 100%;
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #999;
    }

    /* Product Info */
    .product-info {
      display: flex;
      flex-direction: column;
      gap: 16px;
    }
    .product-title {
      font-size: 28px;
      font-weight: 700;
      color: #111;
      margin: 0;
    }
    .brand-info, .sku-info {
      font-size: 14px;
      color: #666;
    }
    .brand-label, .sku-label {
      font-weight: 500;
      margin-right: 8px;
    }
    .brand-name {
      color: #0066cc;
      text-decoration: none;
    }
    .rating-section {
      display: flex;
      align-items: center;
      gap: 12px;
      flex-wrap: wrap;
    }
    .stars {
      display: flex;
      gap: 2px;
    }
    .star {
      font-size: 16px;
      color: #ddd;
    }
    .star.filled {
      color: #ffc107;
    }
    .rating-value, .review-count, .sold-count {
      font-size: 14px;
      color: #666;
    }
    .price-wrapper {
      display: flex;
      align-items: baseline;
      gap: 12px;
      flex-wrap: wrap;
      padding: 16px 0;
      border-top: 1px solid #eee;
      border-bottom: 1px solid #eee;
    }
    .current-price {
      font-size: 32px;
      font-weight: 700;
      color: #c62828;
    }
    .old-price {
      font-size: 18px;
      color: #999;
      text-decoration: line-through;
    }
    .discount-badge {
      font-size: 14px;
      color: #2e7d32;
      background: #e8f5e9;
      padding: 4px 8px;
      border-radius: 4px;
    }
    .short-description {
      color: #666;
      line-height: 1.6;
      padding: 8px 0;
    }

    /* Size & Variant */
    .section-subtitle {
      font-size: 16px;
      font-weight: 600;
      margin-bottom: 12px;
      color: #333;
    }
    .size-options, .variant-options {
      display: flex;
      gap: 12px;
      flex-wrap: wrap;
      margin-bottom: 16px;
    }
    .size-btn, .variant-btn {
      min-width: 60px;
      padding: 10px 20px;
      border: 2px solid #ddd;
      background: white;
      border-radius: 8px;
      cursor: pointer;
      font-weight: 500;
      transition: all 0.2s;
    }
    .size-btn:hover, .variant-btn:hover {
      border-color: #0066cc;
    }
    .size-btn.active, .variant-btn.active {
      border-color: #0066cc;
      background: #0066cc;
      color: white;
    }
    .size-btn.out-of-stock, .variant-btn:disabled {
      opacity: 0.5;
      cursor: not-allowed;
      text-decoration: line-through;
    }
    .size-stock {
      font-size: 11px;
      font-weight: normal;
      margin-left: 4px;
    }
    .variant-price {
      font-size: 12px;
      margin-left: 6px;
    }
    .size-description {
      background: #e3f2fd;
      padding: 10px 14px;
      border-radius: 8px;
      font-size: 14px;
      display: flex;
      align-items: center;
      gap: 8px;
    }

    /* Quantity */
    .quantity-section {
      margin: 8px 0;
    }
    .quantity-control {
      display: inline-flex;
      align-items: center;
      border: 1px solid #ddd;
      border-radius: 8px;
      overflow: hidden;
    }
  button{
    background: white;
    font-size: 1.25rem;
     cursor: pointer;
   }
    .quantity-control button {
      width: 40px;
      height: 40px;
      border: none;
      background: white;
      cursor: pointer;
      font-size: 18px;
    }
    .quantity-control button:hover:not(:disabled) {
      background: #f5f5f5;
    }
    .quantity-control input {
      width: 60px;
      height: 40px;
      text-align: center;
      border: none;
      border-left: 1px solid #ddd;
      border-right: 1px solid #ddd;
    }
    .stock-status {
      margin-left: 16px;
      display: inline-block;
      font-size: 14px;
      font-weight: 500;
      color: #2e7d32;
    }
    .stock-status.out-of-stock {
      color: #c62828;
    }

    /* Actions */
    .action-buttons {
      display: flex;
      gap: 12px;
      margin: 16px 0;
    }
    .btn {
      padding: 14px 24px;
      border-radius: 8px;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.2s;
      display: flex;
      align-items: center;
      gap: 8px;
    }
    .btn-primary {
      background: #111;
      color: white;
      border: none;
    }
    .btn-primary:hover:not(:disabled) {
      background: #C52B36;
    }
    .btn-outline {
      background: white;
      border: 2px solid #111;
      color: #111;
    }
    .btn-outline:hover:not(:disabled) {
      background: #111;
      color: white;
    }
    .btn-icon {
      width: 52px;
      padding: 14px;
      justify-content: center;
      border: 1px solid #ddd;
      background: white;
    }
    .btn-icon:hover {
      border-color: #111;
    }
    .btn:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }
    .btn-add-to-cart {
      background-color: #E63946;;
      flex: 1;
    }

    /* Product Meta */
    .product-meta {
      margin-top: 16px;
      padding-top: 16px;
      border-top: 1px solid #eee;
    }
    .meta-item {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 8px;
      font-size: 14px;
    }
    .meta-label {
      font-weight: 600;
      color: #333;
    }
    .tags-list {
      display: flex;
      gap: 8px;
      flex-wrap: wrap;
    }
    .tag {
      background: #f0f0f0;
      padding: 4px 12px;
      border-radius: 20px;
      text-decoration: none;
      color: #666;
      font-size: 12px;
    }
    .tag:hover {
      background: #e0e0e0;
    }

    /* Tabs */
    .product-tabs {
      margin-top: 48px;
      border-top: 1px solid #eee;
    }
    .tab-headers {
      display: flex;
      gap: 4px;
      border-bottom: 1px solid #eee;
    }
    .tab-btn {
      padding: 12px 24px;
      background: none;
      border: none;
      cursor: pointer;
      font-weight: 500;
      transition: all 0.2s;
    }
    .tab-btn.active {
      color: #0066cc;
      border-bottom: 2px solid #0066cc;
    }
    .tab-content {
      padding: 24px 0;
    }
    .tab-pane h3 {
      margin-bottom: 20px;
      font-size: 20px;
    }
    .description-content {
      line-height: 1.8;
      color: #444;
    }
    .specs-grid {
      display: flex;
      flex-direction: column;
      gap: 12px;
    }
    .spec-row {
      display: flex;
      padding: 12px;
      background: #f8f9fa;
      border-radius: 8px;
    }
    .spec-label {
      width: 180px;
      font-weight: 600;
      color: #333;
    }
    .spec-value {
      flex: 1;
      color: #666;
    }
    .size-table {
      width: 100%;
      border-collapse: collapse;
    }
    .size-table th, .size-table td {
      padding: 12px;
      text-align: left;
      border-bottom: 1px solid #eee;
    }
    .size-table th {
      background: #f8f9fa;
      font-weight: 600;
    }
    .size-table td.out-of-stock {
      color: #c62828;
    }
    .no-data {
      text-align: center;
      padding: 40px;
      color: #999;
    }

    /* Related Products */
    .related-products {
      margin-top: 48px;
    }
    .section-title {
      font-size: 24px;
      font-weight: 700;
      margin-bottom: 24px;
    }
    .related-products-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
      gap: 24px;
    }
    .related-product-card {
      border: 1px solid #eee;
      border-radius: 12px;
      padding: 12px;
      transition: transform 0.2s;
    }
    .related-product-card:hover {
      transform: translateY(-4px);
      box-shadow: 0 4px 12px rgba(0,0,0,0.1);
    }
    .related-product-card a {
      text-decoration: none;
      color: inherit;
    }
    .related-product-card img {
      width: 100%;
      height: 180px;
      object-fit: cover;
      border-radius: 8px;
    }
    .related-product-card h4 {
      margin: 12px 0 8px;
      font-size: 14px;
    }
    .related-product-card .price {
      display: flex;
      gap: 8px;
      align-items: center;
    }
    .related-product-card .current-price {
      font-size: 16px;
      font-weight: 600;
      color: #c62828;
    }
    .related-product-card .old-price {
      font-size: 12px;
      color: #999;
      text-decoration: line-through;
    }

    /* Error State */
    .error-state {
      text-align: center;
      padding: 60px 20px;
    }
    .error-state h2 {
      margin-bottom: 12px;
    }
    .error-state p {
      color: #666;
      margin-bottom: 24px;
    }
  `]
})
export class ProductDetailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private productService = inject(ProductService);
  private cartService = inject(CartService);
  private toastr = inject(ToastrService);
  private http = inject(HttpClient);

  product: ProductData | null = null;
  variants: any[] = [];
  relatedProducts: any[] = [];
  productImages: ProductImage[] = [];
  loading = true;

  selectedImage = '';
  selectedVariant: any = null;
  selectedSize: SizeGuide | null = null;
  quantity = 1;
  activeTab = 'description';

  tabs = [
    { id: 'description', label: 'Mô tả sản phẩm' },
    { id: 'specifications', label: 'Thông số kỹ thuật' },
    { id: 'sizeguide', label: 'Hướng dẫn chọn size' },
    { id: 'reviews', label: 'Đánh giá' }
  ];

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      const productId = params['id'] || params['slug'];
      if (productId) {
        this.loadProduct(productId);
      }
    });
  }

  get maxStock(): number {
    if (this.selectedSize) {
      return this.selectedSize.stockQuantity || 0;
    }
    if (this.selectedVariant) {
      return this.selectedVariant.availableQuantity || 0;
    }
    return this.product?.stockQuantity || 0;
  }

  loadProduct(id: string): void {
    const apiUrl = `http://localhost:8080/api/products/id/${id}`;
    this.loading = true;

    this.http.get<ProductDetailApiResponse>(apiUrl).subscribe({
      next: (response) => {
        this.product = response.product;
        this.variants = response.variants || [];
        this.relatedProducts = response.relatedProducts || [];

        this.processImages(response);
        this.selectDefaultVariant();
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading product:', error);
        this.loading = false;
      }
    });
  }

  processImages(response: ProductDetailApiResponse): void {
    this.productImages = [];

    if (response.product.images?.length > 0) {
      this.productImages = response.product.images;
    } else if (response.galleryImages?.length > 0) {
      this.productImages = response.galleryImages;
    }

    if (this.productImages.length > 0) {
      const primary = this.productImages.find(img => img.primary);
      this.selectedImage = primary?.url || this.productImages[0].url;
    }
  }

  selectDefaultVariant(): void {
    if (this.product?.sizeGuides && this.product.sizeGuides.length > 0) {
      const inStockSize = this.product.sizeGuides.find(s => s.stockQuantity > 0);
      if (inStockSize) {
        this.selectedSize = inStockSize;
      }
    } else if (this.variants.length > 0) {
      const inStockVariant = this.variants.find(v => v.inStock);
      if (inStockVariant) {
        this.selectedVariant = inStockVariant;
      }
    }
  }

  selectImage(image: ProductImage): void {
    this.selectedImage = image.url;
  }

  selectSize(size: SizeGuide): void {
    this.selectedSize = size;
    this.selectedVariant = null;
    this.quantity = 1;
  }

  selectVariant(variant: any): void {
    this.selectedVariant = variant;
    this.selectedSize = null;
    this.quantity = 1;
    if (variant.imageUrl) {
      this.selectedImage = variant.imageUrl;
    }
  }

  increaseQuantity(): void {
    if (this.quantity < this.maxStock) {
      this.quantity++;
    }
  }

  decreaseQuantity(): void {
    if (this.quantity > 1) {
      this.quantity--;
    }
  }

  validateQuantity(): void {
    if (this.quantity > this.maxStock) {
      this.quantity = this.maxStock;
    }
    if (this.quantity < 1 || isNaN(this.quantity)) {
      this.quantity = 1;
    }
  }

  addToCart(): void {
    if (!this.selectedVariant && this.variants.length > 0 && (!this.product?.sizeGuides?.length)) {
      this.toastr.warning('Vui lòng chọn phân loại sản phẩm');
      return;
    }

    const variantId = this.selectedVariant?.id || this.variants[0]?.id;
    if (!variantId) {
      this.toastr.warning('Không thể thêm vào giỏ hàng');
      return;
    }

    const payload = { variantId, quantity: this.quantity };
    this.cartService.addToCart(payload).subscribe({
      next: () => {
        this.toastr.success(`Đã thêm ${this.quantity} sản phẩm vào giỏ hàng`);
      },
      error: (err) => {
        if (err.status === 401) {
          this.toastr.error('Vui lòng đăng nhập để thêm vào giỏ hàng');
        } else {
          this.toastr.error(err.error?.message || 'Không thể thêm vào giỏ hàng');
        }
      }
    });
  }

  buyNow(): void {
    this.addToCart();
    // Navigate to checkout
    // this.router.navigate(['/checkout']);
  }

  openLightbox(): void {
    // Implement lightbox functionality
  }

  onMainImageError(): void {
    this.selectedImage = 'https://via.placeholder.com/600x600?text=Image+Not+Found';
  }

  onThumbnailError(event: Event, index: number): void {
    const img = event.target as HTMLImageElement;
    img.src = 'https://via.placeholder.com/80x80?text=Error';
  }
}
