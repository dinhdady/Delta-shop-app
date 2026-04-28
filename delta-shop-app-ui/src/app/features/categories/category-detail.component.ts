import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { LucideAngularModule, ArrowLeft } from 'lucide-angular';

@Component({
  selector: 'app-category-detail',
  standalone: true,
  imports: [CommonModule, RouterModule, LucideAngularModule],
  template: `
    <div class="category-header">
      <div class="container">
        <a routerLink="/categories" class="back-link">
          <lucide-icon name="arrow-left" [size]="16"></lucide-icon>
          Quay lại danh mục
        </a>
        <h1>{{ category?.name }}</h1>
        <p *ngIf="category?.description">{{ category.description }}</p>
      </div>
    </div>

    <section class="section">
      <div class="container">
        <!-- Products Grid -->
        <div class="products-grid">
          <div *ngFor="let product of products" class="product-card" (click)="viewProduct(product.id)">
            <img [src]="product.images[0]?.url || 'https://via.placeholder.com/300'" [alt]="product.name">
            <div class="product-info">
              <h3>{{ product.name }}</h3>
              <div class="price">{{ product.basePrice | currency:'VND':'symbol':'1.0-0' }}</div>
            </div>
          </div>
        </div>

        <div *ngIf="products.length === 0 && !loading" class="empty-state">
          <p>Chưa có sản phẩm nào trong danh mục này</p>
        </div>

        <div *ngIf="loading" class="loading-state">
          <div class="spinner"></div>
        </div>
      </div>
    </section>
  `,
  styles: [`
    .category-header {
      background: linear-gradient(135deg, var(--color-dark) 0%, #1a1a1a 100%);
      padding: 3rem 0;
      color: white;

      .back-link {
        display: inline-flex;
        align-items: center;
        gap: 0.5rem;
        color: #ccc;
        text-decoration: none;
        margin-bottom: 1.5rem;
        transition: color 0.3s ease;

        &:hover {
          color: var(--color-primary);
        }
      }

      h1 {
        font-size: 2.5rem;
        margin-bottom: 0.5rem;
      }

      p {
        color: #ccc;
        font-size: 1rem;
      }
    }

    .products-grid {
      display: grid;
      grid-template-columns: repeat(4, 1fr);
      gap: 1.5rem;

      @media (max-width: 992px) {
        grid-template-columns: repeat(3, 1fr);
      }

      @media (max-width: 768px) {
        grid-template-columns: repeat(2, 1fr);
      }

      @media (max-width: 576px) {
        grid-template-columns: 1fr;
      }
    }

    .product-card {
      background: white;
      border-radius: 8px;
      overflow: hidden;
      cursor: pointer;
      transition: all 0.3s ease;
      border: 1px solid #e0e0e0;

      &:hover {
        transform: translateY(-5px);
        box-shadow: 0 8px 20px rgba(0,0,0,0.1);
      }

      img {
        width: 100%;
        height: 250px;
        object-fit: cover;
      }

      .product-info {
        padding: 1rem;

        h3 {
          font-size: 1rem;
          margin-bottom: 0.5rem;
        }

        .price {
          font-size: 1.125rem;
          font-weight: 700;
          color: var(--color-primary);
        }
      }
    }

    .loading-state {
      text-align: center;
      padding: 3rem;

      .spinner {
        width: 40px;
        height: 40px;
        border: 3px solid #f3f3f3;
        border-top: 3px solid var(--color-primary);
        border-radius: 50%;
        animation: spin 1s linear infinite;
        margin: 0 auto;
      }
    }

    .empty-state {
      text-align: center;
      padding: 3rem;
      color: #666;
    }

    @keyframes spin {
      0% { transform: rotate(0deg); }
      100% { transform: rotate(360deg); }
    }
  `]
})
export class CategoryDetailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private http = inject(HttpClient);

  category: any = null;
  products: any[] = [];
  loading = true;

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      const slug = params['slug'];
      if (slug) {
        this.loadCategory(slug);
        this.loadProducts(slug);
      }
    });
  }

  loadCategory(slug: string): void {
    this.http.get(`http://localhost:8080/api/categories/slug/${slug}`)
      .subscribe({
        next: (category: any) => {
          this.category = category;
        },
        error: (error) => {
          console.error('Error loading category:', error);
        }
      });
  }

  loadProducts(slug: string): void {
    this.http.get(`http://localhost:8080/api/products?category=${slug}`)
      .subscribe({
        next: (response: any) => {
          this.products = response.content || response;
          this.loading = false;
        },
        error: (error) => {
          console.error('Error loading products:', error);
          this.loading = false;
        }
      });
  }

  viewProduct(productId: string): void {
    window.location.href = `/products/${productId}`;
  }
}
