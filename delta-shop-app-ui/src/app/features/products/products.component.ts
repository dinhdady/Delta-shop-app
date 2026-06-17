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
        <p>Tất cả sản phẩm thể thao chuyên nghiệp</p>
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
          </aside>

          <!-- Main Content -->
          <main class="main-content">
            <div class="toolbar">
              <div class="results-count">
                Hiển thị {{ totalElements }} sản phẩm
              </div>
              <form class="product-search" (ngSubmit)="applySearch()">
                <input
                  type="search"
                  name="keyword"
                  [(ngModel)]="keyword"
                  placeholder="Tìm kiếm sản phẩm..."
                  aria-label="Tìm kiếm sản phẩm">
                @if (keyword) {
                  <button type="button" class="clear-search" (click)="clearSearch()" aria-label="Xóa tìm kiếm">×</button>
                }
                <button type="submit">Tìm</button>
              </form>
              <div class="sort-control">
                <select [(ngModel)]="sortDir" (change)="loadProducts()">
                  <option value="desc">Mới nhất</option>
                  <option value="asc">Cũ nhất</option>
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

    .product-search {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      flex: 1;
      max-width: 420px;
      min-width: 240px;
    }

    .product-search input {
      flex: 1;
      min-width: 0;
      padding: 0.65rem 0.85rem;
      border: 1px solid #e0e0e0;
      border-radius: 8px;
      font-size: 0.9rem;
      outline: none;
      background: #ffffff;
      color: #222;
    }

    .product-search input:focus {
      border-color: #cd4631;
      box-shadow: 0 0 0 3px rgba(205, 70, 49, 0.1);
    }

    .product-search button {
      border: 0;
      border-radius: 8px;
      background: #cd4631;
      color: #ffffff;
      padding: 0.65rem 1rem;
      font-weight: 600;
      cursor: pointer;
      white-space: nowrap;
    }

    .product-search .clear-search {
      width: 36px;
      height: 36px;
      padding: 0;
      background: #eeeeee;
      color: #333333;
      font-size: 1.25rem;
      line-height: 1;
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

      .product-search {
        width: 100%;
        max-width: none;
        min-width: 0;
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

  sortDir = 'desc';
  selectedCategories: string[] = [];
  keyword = '';

  ngOnInit() {
    this.loadCategories();
    this.route.queryParams.subscribe(params => {
      this.keyword = params['keyword'] || '';
      if (params['category']) {
        this.selectedCategories = Array.isArray(params['category']) ? params['category'] : [params['category']];
      } else {
        this.selectedCategories = [];
      }
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
      sortDir: this.sortDir,
      size: 12
    };

    if (this.keyword.trim()) {
      params.keyword = this.keyword.trim();
    }

    if (this.selectedCategories.length > 0) {
      params.categoryId = this.selectedCategories[0]; // Assuming backend takes single for now or update logic
    }

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

  applySearch() {
    const keyword = this.keyword.trim();
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { keyword: keyword || null },
      queryParamsHandling: 'merge'
    });
  }

  clearSearch() {
    this.keyword = '';
    this.applySearch();
  }
}
