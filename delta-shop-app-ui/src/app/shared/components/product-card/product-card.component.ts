import { Component, Input, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { LucideAngularModule, ShoppingCart, Eye } from 'lucide-angular';
import { ProductSummary } from '../../../core/services/product.service';
import { CartService } from '../../../core/services/cart.service';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-product-card',
  standalone: true,
  imports: [
    CommonModule, 
    RouterModule, 
    LucideAngularModule
  ],
  template: `
    <div class="product-card">
      <div class="product-image">
        <a [routerLink]="['/products', product.id]">
          <img [src]="product.primaryImage" [alt]="product.name">
        </a>
        
        <div class="badges">
          @if (product.discountPercentage > 0) {
            <span class="badge badge-secondary">Sale -{{ product.discountPercentage | number:'1.0-0' }}%</span>
          }
          @if (!product.inStock) {
            <span class="badge badge-dark">Hết hàng</span>
          }
        </div>

        <div class="card-actions">
           <a [routerLink]="['/products', product.id]" class="action-btn" title="Xem chi tiết">
              <lucide-icon name="eye"></lucide-icon>
           </a>
        </div>
      </div>
      
      <div class="product-info">
        <h3 class="name"><a [routerLink]="['/products', product.id]">{{ product.name }}</a></h3>
        <div class="price-container">
          @if (product.comparePrice && product.comparePrice > product.basePrice) {
            <span class="price new-price">{{ product.basePrice | currency:'VND':'symbol':'1.0-0' }}</span>
            <span class="price old-price">{{ product.comparePrice | currency:'VND':'symbol':'1.0-0' }}</span>
          } @else {
            <span class="price">{{ product.basePrice | currency:'VND':'symbol':'1.0-0' }}</span>
          }
        </div>
        <div class="rating">
           <span class="stars">★ {{ product.averageRating | number:'1.1-1' }}</span>
           <span class="sold">| Đã bán {{ product.totalSold }}</span>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .product-card {
      background: white;
      border: 1px solid var(--color-border);
      border-radius: var(--radius);
      overflow: hidden;
      transition: all var(--transition);
      
      &:hover {
        border-color: var(--color-dark);
        transform: translateY(-5px);
        box-shadow: 0 10px 20px rgba(0,0,0,0.05);
        
        .card-actions {
          opacity: 1;
          transform: translateY(0);
        }
        
        img {
          transform: scale(1.05);
        }
      }
    }

    .product-image {
      position: relative;
      aspect-ratio: 1;
      overflow: hidden;
      background-color: white;
      display: flex;
      align-items: center;
      justify-content: center;
      
      img {
        width: 100%;
        height: 100%;
        object-fit: contain;
        transition: transform 0.5s ease;
      }
    }

    .badges {
      position: absolute;
      top: 1rem;
      left: 1rem;
      display: flex;
      flex-direction: column;
      gap: 0.5rem;
      z-index: 2;
    }

    .card-actions {
      position: absolute;
      bottom: 1rem;
      left: 0;
      right: 0;
      display: flex;
      justify-content: center;
      gap: 1rem;
      opacity: 0;
      transform: translateY(10px);
      transition: all 0.3s ease;
      z-index: 3;
    }

    .action-btn {
      background-color: var(--color-dark);
      color: white;
      width: 40px;
      height: 40px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: all var(--transition);
      
      &:hover {
        background-color: var(--color-primary);
        transform: scale(1.1);
      }
    }

    .product-info {
      padding: 1.25rem;
    }

    .name {
      font-family: var(--font-body);
      font-size: 1rem;
      font-weight: 600;
      text-transform: none;
      margin-bottom: 0.5rem;
      height: 2.4rem;
      
      a {
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
        overflow: hidden;
      }
      
      &:hover a {
        color: var(--color-primary);
      }
    }

    .price-container {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      font-family: var(--font-heading);
      font-size: 1.25rem;
      margin-bottom: 0.5rem;
    }

    .new-price {
      color: var(--color-primary);
      font-weight: 700;
    }

    .old-price {
      color: var(--color-gray);
      text-decoration: line-through;
      font-size: 1rem;
    }

    .rating {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      font-size: 0.75rem;
      color: var(--color-gray);
      
      .stars {
        color: #f4a261;
        font-weight: 600;
      }
    }
  `]
})
export class ProductCardComponent {
  @Input({ required: true }) product!: ProductSummary;
  
  readonly ShoppingCart = ShoppingCart;
  readonly Eye = Eye;
}
