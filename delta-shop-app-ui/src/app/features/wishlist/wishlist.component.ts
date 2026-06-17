import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { ProductCardComponent } from '../../shared/components/product-card/product-card.component';
import { ProductSummary } from '../../core/services/product.service';
import { WishlistService } from '../../core/services/wishlist.service';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { LucideAngularModule } from 'lucide-angular';

@Component({
  selector: 'app-wishlist',
  standalone: true,
  imports: [CommonModule, ProductCardComponent, LucideAngularModule],
  template: `
    <section class="page-header">
      <div class="container">
        <h1>Sản phẩm yêu thích</h1>
        <p>Lưu lại các sản phẩm bạn quan tâm để mua sau.</p>
      </div>
    </section>

    <section class="section">
      <div class="container">
        <div class="toolbar">
          <span>{{ totalElements }} sản phẩm</span>
          <div class="toolbar-actions" *ngIf="products.length > 0">
            <button type="button" class="btn btn-primary toolbar-button" (click)="moveAllToCart()">
              Thêm tất cả vào giỏ
            </button>
            <button type="button" class="btn btn-outline toolbar-button danger-outline" (click)="clearWishlist()">
              Xóa tất cả
            </button>
          </div>
        </div>

        <div class="products-grid" *ngIf="products.length > 0; else empty">
          <div class="wishlist-item" *ngFor="let product of products">
            <app-product-card
              [product]="product"
              [wishlistMode]="true"
              (addToCart)="moveToCart($event)">
            </app-product-card>
            <div class="item-actions">
              <button type="button" class="btn btn-primary item-action buy-now-button" (click)="buyNow(product.id)">
                Mua ngay
              </button>
              <button
                type="button"
                class="remove-wishlist-button"
                title="Xóa khỏi danh sách yêu thích"
                aria-label="Xóa khỏi danh sách yêu thích"
                (click)="removeFromWishlist(product.id)">
                <lucide-icon name="trash-2" [size]="18"></lucide-icon>
              </button>
            </div>
          </div>
        </div>

        <ng-template #empty>
          <div class="empty-state">
            <h2>Chưa có sản phẩm yêu thích</h2>
            <p>Hãy bấm biểu tượng trái tim ở sản phẩm để thêm vào danh sách.</p>
          </div>
        </ng-template>
      </div>
    </section>
  `,
  styles: [`
    .page-header { background: #000; color: #fff; padding: 56px 0; text-align: center; }
    .page-header h1 { margin: 0 0 .5rem; font-size: 2.5rem; color: #fff }
    .page-header p { margin: 0; color: #cbd5e1; }
    .section { background: #f8fafc; min-height: 60vh; padding: 48px 0; }
    .container { max-width: 1200px; margin: 0 auto; padding: 0 20px; }
    .toolbar {
      display: flex;
      justify-content: space-between;
      align-items: center;
      gap: 1rem;
      margin-bottom: 1.5rem;
      color: #64748b;
    }
    .toolbar > span { font-weight: 600; }
    .toolbar-actions { display: flex; align-items: center; gap: .75rem; }
    .toolbar-button {
      min-height: 42px;
      padding: .65rem 1rem;
      font-family: var(--font-body);
      font-size: .875rem;
      text-transform: none;
      white-space: nowrap;
    }
    .danger-outline {
      border-color: #dc2626 !important;
      color: #dc2626 !important;
    }
    .danger-outline:hover {
      background: #dc2626 !important;
      color: #fff !important;
    }
    .wishlist-item { display: grid; gap: .75rem; align-content: start; min-width: 0; }
    .item-action {
      flex: 1;
      min-height: 44px;
      padding: .7rem 1rem;
      font-family: var(--font-body);
      font-size: .875rem;
      text-transform: none;
    }
    .item-actions {
      display: flex;
      align-items: stretch;
      gap: .5rem;
    }
    .remove-wishlist-button {
      width: 44px;
      min-width: 44px;
      height: 44px;
      display: inline-grid;
      place-items: center;
      border: 1px solid #dc2626;
      border-radius: var(--radius);
      background: #fff;
      color: #dc2626;
      cursor: pointer;
      transition: background-color var(--transition), color var(--transition), transform var(--transition);
    }
    .remove-wishlist-button:hover {
      background: #dc2626;
      color: #fff;
      transform: translateY(-2px);
    }
    .remove-wishlist-button:active {
      transform: translateY(0);
    }
    .buy-now-button {
      background: #dc2626;
    }
    .buy-now-button:hover {
      background: #b91c1c;
    }
    .products-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 1.25rem; }
    .empty-state { text-align: center; background: #fff; border-radius: 16px; padding: 4rem 1rem; color: #64748b; }
    .empty-state h2 { color: #111827; }
    @media (max-width: 1024px) { .products-grid { grid-template-columns: repeat(3, 1fr); } }
    @media (max-width: 768px) {
      .toolbar { align-items: stretch; flex-direction: column; }
      .toolbar-actions { display: grid; grid-template-columns: 1fr 1fr; }
      .toolbar-button { width: 100%; }
      .products-grid { grid-template-columns: repeat(2, 1fr); }
    }
    @media (max-width: 520px) {
      .toolbar-actions { grid-template-columns: 1fr; }
      .products-grid { grid-template-columns: 1fr; }
    }
  `]
})
export class WishlistComponent implements OnInit {
  private wishlistService = inject(WishlistService);
  private router = inject(Router);
  private toastr = inject(ToastrService);
  products: ProductSummary[] = [];
  totalElements = 0;

  ngOnInit(): void {
    this.loadWishlist();
  }

  loadWishlist(): void {
    this.wishlistService.getWishlist().subscribe(res => {
      this.products = res.content || [];
      this.totalElements = res.totalElements || this.products.length;
      this.wishlistService.loadSummary();
    });
  }

  clearWishlist(): void {
    if (!confirm('Xóa toàn bộ danh sách yêu thích?')) return;
    this.wishlistService.clear().subscribe(() => {
      this.products = [];
      this.totalElements = 0;
    });
  }

  moveToCart(productId: string): void {
    this.wishlistService.moveToCart(productId).subscribe({
      next: () => {
        this.toastr.success('Đã thêm sản phẩm vào giỏ hàng');
        this.loadWishlist();
      },
      error: () => this.toastr.error('Không thể thêm sản phẩm vào giỏ hàng')
    });
  }

  buyNow(productId: string): void {
    this.wishlistService.moveToCart(productId).subscribe({
      next: () => this.router.navigate(['/checkout']),
      error: () => this.toastr.error('Không thể mua sản phẩm này lúc này')
    });
  }

  removeFromWishlist(productId: string): void {
    this.wishlistService.remove(productId).subscribe({
      next: () => {
        this.products = this.products.filter(product => product.id !== productId);
        this.totalElements = Math.max(0, this.totalElements - 1);
        this.toastr.success('Đã xóa khỏi danh sách yêu thích');
      },
      error: () => this.toastr.error('Không thể xóa sản phẩm khỏi danh sách yêu thích')
    });
  }

  moveAllToCart(): void {
    this.wishlistService.moveAllToCart().subscribe(() => this.loadWishlist());
  }
}
