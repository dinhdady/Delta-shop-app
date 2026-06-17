// categories.component.ts
import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClientModule, HttpClient } from '@angular/common/http';
import { RouterModule, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { debounceTime, distinctUntilChanged, Subject, takeUntil } from 'rxjs';

// Interfaces
export interface CategoryResponse {
  id: string;
  name: string;
  slug: string;
  description: string;
  imageUrl: string;
  iconClass: string;
  sortOrder: number;
  active: boolean;
  parentId: string | null;
  parentName: string | null;
  children: CategoryResponse[];
  productCount?: number;
}

export interface PageResponse {
  content: CategoryResponse[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
  first: boolean;
}

@Component({
  selector: 'app-categories',
  standalone: true,
  imports: [CommonModule, HttpClientModule, RouterModule, FormsModule],
  template: `
    <div class="categories-container">
      <!-- Header -->
      <div class="page-header">
        <div class="container">
          <h1 class="page-title">Danh Mục Sản Phẩm</h1>
          <p class="page-description">Khám phá các danh mục sản phẩm đa dạng của chúng tôi</p>
        </div>
      </div>

      <!-- Search Bar -->
      <div class="search-wrapper">
        <div class="container">
          <div class="search-container">
            <i class="fas fa-search search-icon"></i>
            <input
              type="text"
              [(ngModel)]="searchTerm"
              (ngModelChange)="onSearchChange()"
              placeholder="Tìm kiếm danh mục..."
              class="search-input"
            />
            <button
              *ngIf="searchTerm"
              (click)="clearSearch()"
              class="clear-btn"
            >
              <i class="fas fa-times"></i>
            </button>
          </div>
        </div>
      </div>

      <!-- Loading -->
      <div *ngIf="loading" class="loading-wrapper">
        <div class="spinner"></div>
        <p>Đang tải danh mục...</p>
      </div>

      <!-- Categories Grid -->
      <div *ngIf="!loading && categories.length > 0" class="categories-wrapper">
        <div class="container">
          <div class="categories-grid">
            <div *ngFor="let category of categories" class="category-card" (click)="viewCategoryDetail(category)">
              <div class="card-image">
                <img *ngIf="category.imageUrl" [src]="getImageUrl(category)" [alt]="category.name" (error)="onImageError(category)">
                <div *ngIf="!category.imageUrl" class="image-placeholder">
                  <i class="fas fa-folder-open"></i>
                </div>
                <div class="card-overlay">
                  <span class="view-btn">Xem chi tiết</span>
                </div>
              </div>
              <div class="card-body">
                <div class="category-icon-wrapper">
                  <i [class]="(category.iconClass ? category.iconClass : 'fas fa-tag') + ' category-icon'"></i>
                </div>
                <h3 class="category-name">{{ category.name }}</h3>
                <p class="category-description" *ngIf="category.description">
                  {{ truncateText(category.description, 100) }}
                </p>
                <div class="category-info">
                  <span class="info-item">
                    <i class="fas fa-link"></i> {{ category.slug }}
                  </span>
                  <span class="info-item" *ngIf="category.productCount !== undefined">
                    <i class="fas fa-box"></i> {{ category.productCount }} sản phẩm
                  </span>
                </div>
                <div class="card-footer">
                  <span class="status" [class.active]="category.active" [class.inactive]="!category.active">
                    {{ category.active ? 'Đang hoạt động' : 'Tạm ẩn' }}
                  </span>
                  <span class="sort-order" *ngIf="category.sortOrder !== undefined">
                    <i class="fas fa-sort"></i> {{ category.sortOrder }}
                  </span>
                </div>
              </div>
            </div>
          </div>

          <!-- Pagination -->
          <div class="pagination" *ngIf="totalPages > 1">
            <button class="page-btn" (click)="firstPage()" [disabled]="currentPage === 0">
              <i class="fas fa-angle-double-left"></i>
            </button>
            <button class="page-btn" (click)="previousPage()" [disabled]="currentPage === 0">
              <i class="fas fa-angle-left"></i>
            </button>

            <div class="page-numbers">
              <button
                *ngFor="let page of pageNumbers"
                (click)="goToPage(page - 1)"
                [class.active]="currentPage === page - 1"
                class="page-number"
              >
                {{ page }}
              </button>
            </div>

            <button class="page-btn" (click)="nextPage()" [disabled]="currentPage === totalPages - 1">
              <i class="fas fa-angle-right"></i>
            </button>
            <button class="page-btn" (click)="lastPage()" [disabled]="currentPage === totalPages - 1">
              <i class="fas fa-angle-double-right"></i>
            </button>
          </div>

          <!-- Pagination Info -->
          <div class="pagination-info" *ngIf="totalElements > 0">
            Hiển thị {{ getDisplayFrom() }} - {{ getDisplayTo() }} trên tổng số {{ totalElements }} danh mục
          </div>
        </div>
      </div>

      <!-- No Data -->
      <div *ngIf="!loading && categories.length === 0" class="no-data">
        <i class="fas fa-folder-open fa-4x"></i>
        <h3>Không có danh mục nào</h3>
        <p *ngIf="!searchTerm">Hiện tại chưa có danh mục sản phẩm nào.</p>
        <p *ngIf="searchTerm">Không tìm thấy danh mục nào phù hợp với "{{ searchTerm }}"</p>
        <button *ngIf="searchTerm" (click)="clearSearch()" class="clear-search-btn">
          Xóa tìm kiếm
        </button>
      </div>
    </div>
  `,
  // categories.component.ts - Updated styles
styles: [`
  .categories-container {
    min-height: 100vh;
    background: #f8f8f8;
  }

  .container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 20px;
  }

  /* Header - Nền đen thuần */
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

  .page-title {
    font-size: 48px;
    font-weight: 800;
    color: #ffffff;
    margin-bottom: 16px;
    letter-spacing: -0.5px;
  }

  .page-title span {
    color: #cd4631;
  }

  .page-description {
    font-size: 18px;
    color: #a0a0a0;
    max-width: 600px;
    margin: 0 auto;
  }

  /* Search Bar */
  .search-wrapper {
    padding: 30px 0;
    background: #ffffff;
    border-bottom: 1px solid #eee;
  }

  .search-container {
    position: relative;
    max-width: 500px;
    margin: 0 auto;
  }

  .search-icon {
    position: absolute;
    left: 15px;
    top: 50%;
    transform: translateY(-50%);
    color: #999;
    font-size: 16px;
  }

  .search-input {
    width: 100%;
    padding: 12px 40px 12px 45px;
    border: 1px solid #e0e0e0;
    border-radius: 30px;
    font-size: 16px;
    transition: all 0.3s ease;
    outline: none;
    background: #ffffff;
    color: #333;
  }

  .search-input:focus {
    border-color: #cd4631;
    box-shadow: 0 0 0 3px rgba(205, 70, 49, 0.1);
  }

  .clear-btn {
    position: absolute;
    right: 15px;
    top: 50%;
    transform: translateY(-50%);
    background: none;
    border: none;
    color: #999;
    cursor: pointer;
  }

  .clear-btn:hover {
    color: #cd4631;
  }

  /* Loading */
  .loading-wrapper {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    min-height: 400px;
  }

  .spinner {
    width: 50px;
    height: 50px;
    border: 3px solid #f0f0f0;
    border-top: 3px solid #cd4631;
    border-radius: 50%;
    animation: spin 1s linear infinite;
  }

  .loading-wrapper p {
    margin-top: 20px;
    color: #cd4631;
  }

  /* Categories Wrapper */
  .categories-wrapper {
    padding: 60px 0;
  }

  /* Grid Layout */
  .categories-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 30px;
    margin-bottom: 50px;
  }

  /* Category Card */
  .category-card {
    background: #ffffff;
    border-radius: 12px;
    overflow: hidden;
    cursor: pointer;
    transition: all 0.3s ease;
    box-shadow: 0 2px 8px rgba(0,0,0,0.04);
    border: 1px solid #eaeaea;
  }

  .category-card:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0,0,0,0.08);
    border-color: #cd4631;
  }

  .card-image {
    height: 200px;
    overflow: hidden;
    position: relative;
    background: #f5f5f5;
  }

  .card-image img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.4s ease;
  }

  .category-card:hover .card-image img {
    transform: scale(1.05);
  }

  .image-placeholder {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #fafafa;
  }

  .image-placeholder i {
    font-size: 56px;
    color: #cd4631;
    opacity: 0.5;
  }

  .card-overlay {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0,0,0,0.7);
    display: flex;
    align-items: center;
    justify-content: center;
    opacity: 0;
    transition: opacity 0.3s ease;
  }

  .category-card:hover .card-overlay {
    opacity: 1;
  }

  .view-btn {
    padding: 8px 22px;
    background: #cd4631;
    color: white;
    border-radius: 30px;
    font-weight: 600;
    font-size: 13px;
    transition: all 0.2s ease;
  }

  .view-btn:hover {
    background: #b83a26;
    transform: scale(1.02);
  }

  .card-body {
    padding: 20px;
  }

  .category-icon-wrapper {
    margin-bottom: 12px;
  }

  .category-icon {
    font-size: 26px;
    color: #cd4631;
  }

  .category-name {
    font-size: 19px;
    font-weight: 700;
    color: #1a1a1a;
    margin: 0 0 8px 0;
  }

  .category-description {
    font-size: 13px;
    color: #777;
    line-height: 1.5;
    margin-bottom: 12px;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  .category-info {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 12px;
    padding: 8px 0;
    border-top: 1px solid #f0f0f0;
    border-bottom: 1px solid #f0f0f0;
  }

  .info-item {
    font-size: 11px;
    color: #999;
  }

  .info-item i {
    margin-right: 4px;
    color: #cd4631;
  }

  .card-footer {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  .status {
    padding: 3px 10px;
    border-radius: 20px;
    font-size: 10px;
    font-weight: 600;
  }

  .status.active {
    background: #e8f5e9;
    color: #2e7d32;
  }

  .status.inactive {
    background: #ffebee;
    color: #c62828;
  }

  .sort-order {
    font-size: 11px;
    color: #cd4631;
  }

  .sort-order i {
    margin-right: 4px;
  }

  /* Pagination */
  .pagination {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 10px;
    margin-bottom: 20px;
    flex-wrap: wrap;
  }

  .page-btn {
    padding: 8px 14px;
    background: #ffffff;
    border: 1px solid #e0e0e0;
    border-radius: 8px;
    color: #555;
    cursor: pointer;
    transition: all 0.2s ease;
    font-weight: 500;
  }

  .page-btn:hover:not(:disabled) {
    background: #cd4631;
    color: white;
    border-color: #cd4631;
  }

  .page-btn:disabled {
    opacity: 0.4;
    cursor: not-allowed;
  }

  .page-numbers {
    display: flex;
    gap: 6px;
  }

  .page-number {
    width: 38px;
    height: 38px;
    border-radius: 8px;
    border: 1px solid #e0e0e0;
    background: #ffffff;
    color: #555;
    cursor: pointer;
    transition: all 0.2s ease;
  }

  .page-number:hover {
    background: #cd4631;
    color: white;
    border-color: #cd4631;
  }

  .page-number.active {
    background: #cd4631;
    color: white;
    border-color: #cd4631;
  }

  .pagination-info {
    text-align: center;
    color: #888;
    font-size: 13px;
  }

  /* No Data */
  .no-data {
    text-align: center;
    padding: 80px 20px;
    background: #ffffff;
    margin: 60px auto;
    max-width: 500px;
    border-radius: 12px;
    box-shadow: 0 2px 12px rgba(0,0,0,0.04);
    border: 1px solid #eee;
  }

  .no-data i {
    color: #cd4631;
    margin-bottom: 20px;
    opacity: 0.5;
  }

  .no-data h3 {
    font-size: 22px;
    color: #333;
    margin-bottom: 10px;
  }

  .no-data p {
    color: #888;
    margin-bottom: 20px;
  }

  .clear-search-btn {
    padding: 10px 24px;
    background: #cd4631;
    color: white;
    border: none;
    border-radius: 30px;
    cursor: pointer;
    font-weight: 600;
    transition: all 0.2s ease;
  }

  .clear-search-btn:hover {
    background: #b83a26;
  }

  /* Animations */
  @keyframes spin {
    to { transform: rotate(360deg); }
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

  .category-card {
    animation: fadeInUp 0.4s ease forwards;
    opacity: 0;
  }

  .category-card:nth-child(1) { animation-delay: 0.05s; }
  .category-card:nth-child(2) { animation-delay: 0.1s; }
  .category-card:nth-child(3) { animation-delay: 0.15s; }
  .category-card:nth-child(4) { animation-delay: 0.2s; }
  .category-card:nth-child(5) { animation-delay: 0.25s; }
  .category-card:nth-child(6) { animation-delay: 0.3s; }

  /* Responsive */
  @media (max-width: 1024px) {
    .categories-grid {
      grid-template-columns: repeat(2, 1fr);
      gap: 25px;
    }
  }

  @media (max-width: 768px) {
    .page-header {
      padding: 40px 0;
    }
    .page-title {
      font-size: 32px;
    }
    .page-description {
      font-size: 14px;
    }
    .categories-grid {
      grid-template-columns: 1fr;
      gap: 20px;
    }
    .categories-wrapper {
      padding: 40px 0;
    }
    .page-number {
      width: 34px;
      height: 34px;
    }
  }

  @media (max-width: 480px) {
    .page-title {
      font-size: 28px;
    }
    .container {
      padding: 0 16px;
    }
    .card-body {
      padding: 16px;
    }
    .category-name {
      font-size: 17px;
    }
  }
`]
})
export class CategoriesComponent implements OnInit, OnDestroy {
  private http = inject(HttpClient);
  private router = inject(Router);

  categories: CategoryResponse[] = [];
  loading: boolean = true;

  // Pagination
  currentPage: number = 0;
  pageSize: number = 6; // 6 categories per page (2 rows x 3 columns)
  totalPages: number = 0;
  totalElements: number = 0;

  // Search
  searchTerm: string = '';
  private searchSubject = new Subject<string>();
  private destroy$ = new Subject<void>();

  // API URL - Thay đổi theo backend của bạn
  private apiUrl = 'http://localhost:8080/api';

  ngOnInit(): void {
    this.loadCategories();

    // Debounce search
    this.searchSubject.pipe(
      debounceTime(500),
      distinctUntilChanged(),
      takeUntil(this.destroy$)
    ).subscribe(() => {
      this.currentPage = 0;
      this.loadCategories();
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadCategories(): void {
    this.loading = true;

    let url = `${this.apiUrl}/categories/paginated?page=${this.currentPage}&size=${this.pageSize}&sortBy=sortOrder&sortDir=asc`;
    if (this.searchTerm.trim()) {
      url += `&keyword=${encodeURIComponent(this.searchTerm.trim())}`;
    }

    this.http.get<PageResponse>(url)
      .subscribe({
        next: (response: PageResponse) => {
          this.categories = response.content;
          this.totalPages = response.totalPages;
          this.totalElements = response.totalElements;
          this.loading = false;

          // Debug log
          console.log('Loaded categories:', this.categories.map(c => c.name));
        },
        error: (error) => {
          console.error('Error loading categories:', error);
          this.loading = false;
        }
      });
  }

  getImageUrl(category: CategoryResponse): string {
    if (category.imageUrl) {
      if (category.imageUrl.startsWith('/uploads')) {
        return 'http://localhost:8080' + category.imageUrl;
      }
      return category.imageUrl;
    }
    return `https://via.placeholder.com/400x300?text=${encodeURIComponent(category.name)}`;
  }

  onImageError(category: CategoryResponse): void {
    category.imageUrl = `https://via.placeholder.com/400x300?text=${encodeURIComponent(category.name)}`;
  }

  getDisplayFrom(): number {
    if (this.categories.length === 0) return 0;
    return this.currentPage * this.pageSize + 1;
  }

  getDisplayTo(): number {
    return Math.min((this.currentPage + 1) * this.pageSize, this.totalElements);
  }

  get pageNumbers(): number[] {
    const pages: number[] = [];
    const maxVisible = 5;
    let start = Math.max(0, this.currentPage - Math.floor(maxVisible / 2));
    let end = Math.min(this.totalPages, start + maxVisible);

    if (end - start < maxVisible) {
      start = Math.max(0, end - maxVisible);
    }

    for (let i = start; i < end; i++) {
      pages.push(i + 1);
    }
    return pages;
  }

  onSearchChange(): void {
    this.searchSubject.next(this.searchTerm);
  }

  clearSearch(): void {
    this.searchTerm = '';
    this.currentPage = 0;
    this.loadCategories();
  }

  firstPage(): void {
    if (this.currentPage !== 0) {
      this.currentPage = 0;
      this.loadCategories();
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }
  }

  previousPage(): void {
    if (this.currentPage > 0) {
      this.currentPage--;
      this.loadCategories();
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages - 1) {
      this.currentPage++;
      this.loadCategories();
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }
  }

  lastPage(): void {
    if (this.currentPage !== this.totalPages - 1) {
      this.currentPage = this.totalPages - 1;
      this.loadCategories();
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }
  }

  goToPage(page: number): void {
    if (page !== this.currentPage && page >= 0 && page < this.totalPages) {
      this.currentPage = page;
      this.loadCategories();
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }
  }

  truncateText(text: string, maxLength: number): string {
    if (!text) return '';
    if (text.length <= maxLength) return text;
    return text.substring(0, maxLength) + '...';
  }

  viewCategoryDetail(category: CategoryResponse): void {
    this.router.navigate(['/products'], { queryParams: { category: category.slug } });
  }
}
