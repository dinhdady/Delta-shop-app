import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { ProductService, ProductSummary } from '../../core/services/product.service';
import { ProductCardComponent } from '../../shared/components/product-card/product-card.component';
import { SkeletonLoaderComponent } from '../../shared/components/skeleton-loader/skeleton-loader.component';
import { FormsModule } from '@angular/forms';
import { LucideAngularModule, Filter } from 'lucide-angular';

@Component({
  selector: 'app-products',
  standalone: true,
  imports: [CommonModule, ProductCardComponent, SkeletonLoaderComponent, FormsModule, LucideAngularModule],
  template: `
    <div class="page-header">
      <div class="container">
        <h1>SẢN PHẨM</h1>
        <p>{{ searchKeyword ? 'Kết quả tìm kiếm cho "' + searchKeyword + '"' : 'Tất cả sản phẩm thể thao chuyên nghiệp' }}</p>
      </div>
    </div>

    <section class="section">
      <div class="container">
        <div class="layout-grid">
          <!-- Sidebar -->
          <aside class="sidebar">
            <div class="filter-header">
              <h3><lucide-icon name="filter"></lucide-icon> BỘ LỌC</h3>
            </div>

            <div class="filter-group">
              <h4>Danh mục</h4>
              <div class="checkbox-group">
                @for (cat of categories; track cat.id) {
                  <label class="checkbox-container">
                    <input type="checkbox"
                           [checked]="selectedCategories.includes(cat.id)"
                           (change)="onCategoryChange(cat.id)">
                    <span class="checkmark"></span>
                    {{ cat.name }}
                  </label>
                }
              </div>
            </div>

            <div class="filter-group">
              <h4>Khoảng giá</h4>
              <div class="price-inputs">
                <input
                  type="text"
                  inputmode="numeric"
                  [ngModel]="formatPriceInput(minPrice)"
                  (ngModelChange)="minPrice = parsePriceInput($event)"
                  placeholder="Từ (₫)">
                <input
                  type="text"
                  inputmode="numeric"
                  [ngModel]="formatPriceInput(maxPrice)"
                  (ngModelChange)="maxPrice = parsePriceInput($event)"
                  placeholder="Đến (₫)">
              </div>
            </div>

            <div class="filter-group">
              <h4>Đánh giá</h4>
              <select class="filter-select" [(ngModel)]="minRating">
                <option [ngValue]="null">Tất cả đánh giá</option>
                <option [ngValue]="4">Từ 4 sao</option>
                <option [ngValue]="3">Từ 3 sao</option>
                <option [ngValue]="2">Từ 2 sao</option>
              </select>
            </div>

            <div class="filter-group">
              <label class="checkbox-container">
                <input type="checkbox" [(ngModel)]="inStockOnly">
                <span class="checkmark"></span>
                Chỉ sản phẩm còn hàng
              </label>
            </div>

            <div class="filter-actions">
              <button type="button" class="apply-filter" (click)="applyFilters()">Áp dụng</button>
              <button type="button" class="reset-filter" (click)="resetFilters()">Đặt lại</button>
            </div>
          </aside>

          <!-- Main Content -->
          <main class="main-content">
            <div class="toolbar">
              <div class="toolbar-left">
                <div class="results-count">
                  Hiển thị {{ totalElements }} sản phẩm
                </div>
                @if (searchKeyword) {
                  <button class="clear-search" type="button" (click)="clearSearch()">
                    Xóa tìm kiếm: "{{ searchKeyword }}"
                  </button>
                }
              </div>
              <div class="sort-control">
                <select [(ngModel)]="sortOption" (change)="onSortChange()">
                  <option value="newest">Mới nhất</option>
                  <option value="oldest">Cũ nhất</option>
                  <option value="priceAsc">Giá tăng dần</option>
                  <option value="priceDesc">Giá giảm dần</option>
                  <option value="ratingDesc">Đánh giá cao nhất</option>
                  <option value="soldDesc">Bán chạy nhất</option>
                </select>
              </div>
            </div>

            @if (loading) {
              <div class="products-grid">
                @for (item of [1,2,3,4,5,6]; track item) {
                  <app-skeleton-loader type="card"></app-skeleton-loader>
                }
              </div>
            } @else {
              <div class="products-grid">
                @for (product of products; track product.id) {
                  <app-product-card [product]="product"></app-product-card>
                }
              </div>

              @if (products.length === 0) {
                <div class="empty-state">
                  <p>Không tìm thấy sản phẩm nào phù hợp.</p>
                </div>
              }
            }
          </main>
        </div>
      </div>
    </section>
  `,
  // products.component.ts - Updated styles
  styles: [`
    .page-header {
      background: #000000;
      padding: 60px 0;
      text-align: center;
      position: relative;
      border-bottom: 1px solid rgba(205, 70, 49, 0.3);
    }

    .page-header::before {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: radial-gradient(circle at 50% 0%, rgba(205, 70, 49, 0.08) 0%, transparent 70%);
      pointer-events: none;
    }

    .page-header h1 {
      font-size: 48px;
      font-weight: 800;
      color: #ffffff;
      margin-bottom: 16px;
      letter-spacing: -0.5px;
    }

    .page-header p {
      font-size: 18px;
      color: #a0a0a0;
      max-width: 600px;
      margin: 0 auto;
    }

    .section {
      padding: 60px 0;
      background: #f8f8f8;
      min-height: calc(100vh - 300px);
    }

    .container {
      max-width: 1200px;
      margin: 0 auto;
      padding: 0 20px;
    }

    .layout-grid {
      display: grid;
      grid-template-columns: 260px 1fr;
      gap: 2.5rem;

      @media (max-width: 992px) {
        grid-template-columns: 1fr;
      }
    }

    /* Sidebar Styles */
    .sidebar {
      background: #ffffff;
      padding: 1.5rem;
      border-radius: 12px;
      border: 1px solid #eaeaea;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
      height: fit-content;
      position: sticky;
      top: 100px;
      animation: fadeInLeft 0.5s ease forwards;
      opacity: 0;

      @media (max-width: 992px) {
        position: static;
      }
    }

    .filter-header {
      margin-bottom: 1.5rem;
      padding-bottom: 1rem;
      border-bottom: 2px solid #cd4631;

      h3 {
        display: flex;
        align-items: center;
        gap: 0.5rem;
        margin: 0;
        font-size: 1.1rem;
        font-weight: 700;
        color: #1a1a1a;
      }

      lucide-icon {
        color: #cd4631;
        width: 18px;
        height: 18px;
      }
    }

    .filter-group {
      margin-bottom: 1.5rem;

      h4 {
        margin-bottom: 1rem;
        font-size: 0.9rem;
        font-weight: 600;
        color: #333;
        text-transform: uppercase;
        letter-spacing: 0.5px;
      }
    }

    .checkbox-group {
      display: flex;
      flex-direction: column;
      gap: 0.75rem;
    }

    .price-inputs {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 0.5rem;
    }

    .price-inputs input,
    .filter-select {
      width: 100%;
      min-width: 0;
      padding: 0.65rem 0.75rem;
      border: 1px solid #e0e0e0;
      border-radius: 6px;
      background: #ffffff;
      outline: none;
    }

    .price-inputs input:focus,
    .filter-select:focus {
      border-color: #cd4631;
    }

    .filter-actions {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 0.5rem;
    }

    .filter-actions button {
      padding: 0.7rem;
      border-radius: 6px;
      cursor: pointer;
      font-weight: 600;
    }

    .apply-filter {
      border: 1px solid #cd4631;
      background: #cd4631;
      color: #ffffff;
    }

    .reset-filter {
      border: 1px solid #dddddd;
      background: #ffffff;
      color: #555555;
    }

    .checkbox-container {
      display: block;
      position: relative;
      padding-left: 28px;
      cursor: pointer;
      font-size: 0.85rem;
      color: #555;
      user-select: none;
      transition: color 0.2s ease;

      &:hover {
        color: #cd4631;
      }

      input {
        position: absolute;
        opacity: 0;
        cursor: pointer;
        height: 0;
        width: 0;
      }

      .checkmark {
        position: absolute;
        top: 0;
        left: 0;
        height: 18px;
        width: 18px;
        background-color: #f5f5f5;
        border: 1px solid #ddd;
        border-radius: 4px;
        transition: all 0.2s ease;

        &:after {
          content: "";
          position: absolute;
          display: none;
          left: 5px;
          top: 1px;
          width: 4px;
          height: 9px;
          border: solid white;
          border-width: 0 2px 2px 0;
          transform: rotate(45deg);
        }
      }

      &:hover input ~ .checkmark {
        background-color: #ffebe0;
        border-color: #cd4631;
      }

      input:checked ~ .checkmark {
        background-color: #cd4631;
        border-color: #cd4631;

        &:after {
          display: block;
        }
      }
    }

    /* Main Content */
    .main-content {
      animation: fadeInRight 0.5s ease forwards;
      opacity: 0;
    }

    .toolbar {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 2rem;
      padding: 1rem 0;
      border-bottom: 1px solid #eaeaea;
      flex-wrap: wrap;
      gap: 1rem;
    }

    .results-count {
      font-size: 0.85rem;
      color: #777;
    }

    .toolbar-left {
      display: flex;
      align-items: center;
      gap: 1rem;
      flex-wrap: wrap;
    }

    .clear-search {
      border: 1px solid rgba(205, 70, 49, 0.25);
      background: #fff5f2;
      color: #cd4631;
      border-radius: 999px;
      padding: 0.4rem 0.8rem;
      cursor: pointer;
      font-size: 0.8rem;
      transition: all 0.2s ease;
    }

    .clear-search:hover {
      background: #cd4631;
      color: #ffffff;
    }

    .sort-control {
      select {
        padding: 0.5rem 1rem;
        border: 1px solid #e0e0e0;
        border-radius: 8px;
        font-size: 0.85rem;
        cursor: pointer;
        background: #ffffff;
        color: #333;
        transition: all 0.2s ease;
        outline: none;

        &:hover {
          border-color: #cd4631;
        }

        &:focus {
          border-color: #cd4631;
          box-shadow: 0 0 0 3px rgba(205, 70, 49, 0.1);
        }
      }
    }

    /* Products Grid */
    .products-grid {
      display: grid;
      grid-template-columns: repeat(3, 1fr);
      gap: 1.5rem;

      @media (max-width: 1200px) {
        grid-template-columns: repeat(2, 1fr);
      }

      @media (max-width: 768px) {
        grid-template-columns: 1fr;
      }
    }

    /* Product Card Animation */
    .products-grid app-product-card {
      display: block;
      opacity: 0;
      animation: fadeInUp 0.5s ease forwards;
    }

    /* Staggered animation delay */
    .products-grid app-product-card:nth-child(1) { animation-delay: 0.05s; }
    .products-grid app-product-card:nth-child(2) { animation-delay: 0.1s; }
    .products-grid app-product-card:nth-child(3) { animation-delay: 0.15s; }
    .products-grid app-product-card:nth-child(4) { animation-delay: 0.2s; }
    .products-grid app-product-card:nth-child(5) { animation-delay: 0.25s; }
    .products-grid app-product-card:nth-child(6) { animation-delay: 0.3s; }
    .products-grid app-product-card:nth-child(7) { animation-delay: 0.35s; }
    .products-grid app-product-card:nth-child(8) { animation-delay: 0.4s; }
    .products-grid app-product-card:nth-child(9) { animation-delay: 0.45s; }
    .products-grid app-product-card:nth-child(10) { animation-delay: 0.5s; }
    .products-grid app-product-card:nth-child(11) { animation-delay: 0.55s; }
    .products-grid app-product-card:nth-child(12) { animation-delay: 0.6s; }

    .empty-state {
      text-align: center;
      padding: 4rem 0;
      color: #999;
      font-size: 0.9rem;
    }

    /* Animations */
    @keyframes fadeInLeft {
      from {
        opacity: 0;
        transform: translateX(-30px);
      }
      to {
        opacity: 1;
        transform: translateX(0);
      }
    }

    @keyframes fadeInRight {
      from {
        opacity: 0;
        transform: translateX(30px);
      }
      to {
        opacity: 1;
        transform: translateX(0);
      }
    }

    @keyframes fadeInUp {
      from {
        opacity: 0;
        transform: translateY(20px);
      }
      to {
        opacity: 1;
        transform: translateY(0);
      }
    }

    @keyframes pulse {
      0%, 100% { transform: scale(1); opacity: 0.5; }
      50% { transform: scale(1.05); opacity: 0.8; }
    }

    /* Responsive */
    @media (max-width: 768px) {
      .page-header {
        padding: 40px 0;
      }

      .page-header h1 {
        font-size: 32px;
      }

      .page-header p {
        font-size: 14px;
      }

      .section {
        padding: 40px 0;
      }

      .sidebar {
        padding: 1rem;
      }

      .toolbar {
        flex-direction: column;
        align-items: flex-start;
      }

      .sort-control select {
        width: 100%;
      }
    }

    @media (max-width: 480px) {
      .page-header h1 {
        font-size: 28px;
      }

      .filter-header h3 {
        font-size: 1rem;
      }
    }
  `]
})
export class ProductsComponent implements OnInit {
  private productService = inject(ProductService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  readonly Filter = Filter;

  products: ProductSummary[] = [];
  categories: any[] = [];
  loading = true;
  totalElements = 0;

  sortBy = 'createdAt';
  sortDir = 'desc';
  sortOption = 'newest';
  selectedCategories: string[] = [];
  searchKeyword = '';
  minPrice: number | null = null;
  maxPrice: number | null = null;
  minRating: number | null = null;
  inStockOnly = false;

  ngOnInit() {
    this.loadCategories();
    this.route.queryParams.subscribe(params => {
      const kw = params['keyword'];
      this.searchKeyword = (kw && kw !== 'null' && kw !== 'undefined') ? kw.trim() : '';
      if (params['category']) {
        this.selectedCategories = Array.isArray(params['category']) ? params['category'] : [params['category']];
      } else {
        this.selectedCategories = [];
      }
      this.minPrice = this.toNumberOrNull(params['minPrice']);
      this.maxPrice = this.toNumberOrNull(params['maxPrice']);
      this.minRating = this.toNumberOrNull(params['minRating']);
      this.inStockOnly = params['inStockOnly'] === 'true';
      this.sortBy = params['sortBy'] || 'createdAt';
      this.sortDir = params['sortDir'] === 'asc' ? 'asc' : 'desc';
      this.sortOption = this.getSortOption(this.sortBy, this.sortDir);
      this.loadProducts();
    });
  }

  loadCategories() {
    this.productService.getCategories().subscribe(res => {
      this.categories = res;
    });
  }

  loadProducts() {
    this.loading = true;
    const params: any = {
      sortBy: this.sortBy,
      sortDir: this.sortDir,
      size: 12
    };

    if (this.selectedCategories.length > 0) {
      params.categoryId = this.selectedCategories[0]; // Assuming backend takes single for now or update logic
    }

    if (this.searchKeyword) {
      params.keyword = this.searchKeyword;
    }

    if (this.minPrice !== null) params.minPrice = this.minPrice;
    if (this.maxPrice !== null) params.maxPrice = this.maxPrice;
    if (this.minRating !== null) params.minRating = this.minRating;
    if (this.inStockOnly) params.inStockOnly = true;

    this.productService.getProducts(params).subscribe({
      next: (res) => {
        this.products = res.content;
        this.totalElements = res.totalElements;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  onCategoryChange(categoryId: string) {
    const index = this.selectedCategories.indexOf(categoryId);
    if (index === -1) {
      this.selectedCategories = [categoryId]; // Toggle single selection for simplicity or use array
    } else {
      this.selectedCategories = [];
    }

    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { category: this.selectedCategories.length > 0 ? this.selectedCategories[0] : null },
      queryParamsHandling: 'merge'
    });
  }

  clearSearch() {
    this.searchKeyword = '';
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { keyword: null },
      queryParamsHandling: 'merge'
    });
  }

  applyFilters() {
    this.updateFilterQueryParams();
  }

  resetFilters() {
    this.selectedCategories = [];
    this.minPrice = null;
    this.maxPrice = null;
    this.minRating = null;
    this.inStockOnly = false;
    this.sortBy = 'createdAt';
    this.sortDir = 'desc';
    this.sortOption = 'newest';

    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: {
        category: null,
        minPrice: null,
        maxPrice: null,
        minRating: null,
        inStockOnly: null,
        sortBy: null,
        sortDir: null
      },
      queryParamsHandling: 'merge'
    });
  }

  onSortChange() {
    const sortMap: Record<string, [string, string]> = {
      newest: ['createdAt', 'desc'],
      oldest: ['createdAt', 'asc'],
      priceAsc: ['price', 'asc'],
      priceDesc: ['price', 'desc'],
      ratingDesc: ['rating', 'desc'],
      soldDesc: ['sold', 'desc']
    };
    [this.sortBy, this.sortDir] = sortMap[this.sortOption] || sortMap['newest'];
    this.updateFilterQueryParams();
  }

  private updateFilterQueryParams() {
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: {
        category: this.selectedCategories[0] || null,
        minPrice: this.minPrice,
        maxPrice: this.maxPrice,
        minRating: this.minRating,
        inStockOnly: this.inStockOnly ? true : null,
        sortBy: this.sortBy === 'createdAt' ? null : this.sortBy,
        sortDir: this.sortDir === 'desc' ? null : this.sortDir
      },
      queryParamsHandling: 'merge'
    });
  }

  private toNumberOrNull(value: unknown): number | null {
    if (value === null || value === undefined || value === '') return null;
    const parsed = Number(value);
    return Number.isFinite(parsed) ? parsed : null;
  }

  formatPriceInput(value: number | null): string {
    return value === null ? '' : new Intl.NumberFormat('vi-VN', { maximumFractionDigits: 0 }).format(value);
  }

  parsePriceInput(value: string): number | null {
    const digits = String(value).replace(/\D/g, '');
    return digits ? Number(digits) : null;
  }

  private getSortOption(sortBy: string, sortDir: string): string {
    if (sortBy === 'price') return sortDir === 'asc' ? 'priceAsc' : 'priceDesc';
    if (sortBy === 'rating') return 'ratingDesc';
    if (sortBy === 'sold') return 'soldDesc';
    return sortDir === 'asc' ? 'oldest' : 'newest';
  }
}
