import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { CartService ,CartItem } from '../../core/services/cart.service';
import { ToastrService } from 'ngx-toastr';
import { LucideAngularModule, Trash2, ArrowRight } from 'lucide-angular';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-cart',
  standalone: true,
  imports: [CommonModule, RouterModule, LucideAngularModule, FormsModule],
  template: `
    <div class="page-header">
      <div class="container">
        <h1>GIỎ HÀNG</h1>
      </div>
    </div>

    <section class="section">
      <div class="container">
        @if (cartItems().length === 0) {
          <div class="empty-cart">
            <div class="empty-icon">
              <lucide-icon name="shopping-cart" [size]="64"></lucide-icon>
            </div>
            <h2>Giỏ hàng của bạn đang trống</h2>
            <p>Khám phá các sản phẩm tuyệt vời của chúng tôi và thêm vào giỏ hàng nhé.</p>
            <a routerLink="/products" class="btn btn-primary">MUA SẮM NGAY</a>
          </div>
        } @else {
          <div class="cart-layout">
            <div class="cart-items">
              <div class="cart-table-header">
                <div class="col-product">Sản phẩm</div>
                <div class="col-price">Đơn giá</div>
                <div class="col-quantity">Số lượng</div>
                <div class="col-total">Thành tiền</div>
                <div class="col-action"></div>
              </div>

              @for (item of cartItems(); track item.id) {
                <div class="cart-item">
                  <!-- Trong template, sửa phần col-product -->
                    <div class="col-product" (click)="navigateToProduct(item)" style="cursor: pointer;">
                      <div class="product-image">
                        <img
                          [src]="getProductImage(item)"
                          [alt]="item.productName"
                          (error)="onImageError($event, item)"
                        >
                      </div>
                      <div class="product-info">
                        <h3>{{ item.productName }}</h3>
                        @if (item.variantName) {
                          <p class="variant">Phân loại: {{ item.variantName }}</p>
                        }
                        <!-- HIỂN THỊ SIZE NẾU CÓ -->
                        @if (item.selectedSizeLabel) {
                          <p class="size-info">
                            Size: <strong>{{ item.selectedSizeLabel }}</strong>
                            @if (item.selectedSizeMeasurement) {
                              <span class="size-measurement"> ({{ item.selectedSizeMeasurement }})</span>
                            }
                          </p>
                        }
                      </div>
                    </div>
                  <div class="col-price">
                    <span class="mobile-label">Đơn giá:</span>
                    {{ item.unitPrice | currency:'VND':'symbol':'1.0-0' }}
                  </div>
                  <div class="col-quantity">
                    <span class="mobile-label">Số lượng:</span>
                    <div class="quantity-control">
                      <button type="button" (click)="updateQuantity(item.id, item.quantity - 1); $event.stopPropagation()" [disabled]="item.quantity <= 1">-</button>
                      <input type="number" [ngModel]="item.quantity" (ngModelChange)="updateQuantity(item.id, $event); $event.stopPropagation()" min="1" (click)="$event.stopPropagation()">
                      <button type="button" (click)="updateQuantity(item.id, item.quantity + 1); $event.stopPropagation()">+</button>
                    </div>
                  </div>
                  <div class="col-total font-heading text-primary font-bold">
                    <span class="mobile-label">Thành tiền:</span>
                    {{ item.subtotal | currency:'VND':'symbol':'1.0-0' }}
                  </div>
                  <div class="col-action">
                    <button class="remove-btn" (click)="openRemoveConfirm(item); $event.stopPropagation()" aria-label="Xóa">
                      <lucide-icon name="trash-2"></lucide-icon>
                    </button>
                  </div>
                </div>
              }
            </div>

            <div class="cart-summary">
              <h3>TỔNG ĐƠN HÀNG</h3>
              <div class="summary-row">
                <span>Tạm tính</span>
                <span>{{ cartTotal() | currency:'VND':'symbol':'1.0-0' }}</span>
              </div>
              <div class="summary-row">
                <span>Giảm giá</span>
                <span>0 ₫</span>
              </div>
              <hr>
              <div class="summary-row total">
                <span>Tổng cộng</span>
                <span class="text-primary">{{ cartTotal() | currency:'VND':'symbol':'1.0-0' }}</span>
              </div>
              <a routerLink="/checkout" class="btn btn-primary btn-full checkout-btn">
                TIẾN HÀNH THANH TOÁN <lucide-icon name="arrow-right"></lucide-icon>
              </a>
            </div>
          </div>
        }
      </div>
    </section>

    @if (itemPendingRemove) {
      <div class="confirm-backdrop" (click)="closeRemoveConfirm()">
        <div class="confirm-dialog" role="dialog" aria-modal="true" aria-labelledby="remove-dialog-title"
             (click)="$event.stopPropagation()">
          <div class="confirm-icon">
            <lucide-icon name="trash-2" [size]="22"></lucide-icon>
          </div>
          <div class="confirm-content">
            <h2 id="remove-dialog-title">Xóa sản phẩm khỏi giỏ hàng?</h2>
            <p>Sản phẩm sẽ được loại khỏi giỏ hàng hiện tại.</p>

            <div class="confirm-product">
              <img [src]="getProductImage(itemPendingRemove)" [alt]="itemPendingRemove.productName"
                   (error)="onImageError($event, itemPendingRemove)">
              <div>
                <strong>{{ itemPendingRemove.productName }}</strong>
                @if (itemPendingRemove.variantName) {
                  <span>{{ itemPendingRemove.variantName }}</span>
                }
                <span>{{ itemPendingRemove.subtotal | currency:'VND':'symbol':'1.0-0' }}</span>
              </div>
            </div>
          </div>
          <div class="confirm-actions">
            <button type="button" class="cancel-button" [disabled]="removingItem" (click)="closeRemoveConfirm()">
              Giữ lại
            </button>
            <button type="button" class="delete-button" [disabled]="removingItem" (click)="confirmRemoveItem()">
              <lucide-icon name="trash-2" [size]="17"></lucide-icon>
              {{ removingItem ? 'Đang xóa...' : 'Xóa sản phẩm' }}
            </button>
          </div>
        </div>
      </div>
    }
  `,
  styles: [`
    .page-header {
      background-color: var(--color-dark);
      padding: 3rem 0;
      text-align: center;
      h1 {
        color: var(--color-light);
        font-size: 3rem;
        margin: 0;
      }
    }
    .size-info {
      font-size: 0.875rem;
      color: var(--color-gray);
      margin-top: 0.25rem;

      .size-measurement {
        color: #0066cc;
        font-size: 0.8rem;
      }
    }
    .empty-cart {
      text-align: center;
      padding: 5rem 0;

      .empty-icon {
        color: var(--color-gray);
        margin-bottom: 2rem;
      }

      h2 {
        font-size: 2rem;
        margin-bottom: 1rem;
      }

      p {
        color: var(--color-gray);
        margin-bottom: 2rem;
      }
    }

    .cart-layout {
      display: grid;
      grid-template-columns: 2fr 1fr;
      gap: 3rem;

      @media (max-width: 992px) {
        grid-template-columns: 1fr;
      }
    }

    .cart-table-header {
      display: flex;
      padding: 1rem 0;
      border-bottom: 2px solid var(--color-dark);
      font-family: var(--font-heading);
      font-size: 1.125rem;
      text-transform: uppercase;
      font-weight: 600;

      @media (max-width: 768px) {
        display: none;
      }
    }

    .cart-item {
      display: flex;
      padding: 1.5rem 0;
      border-bottom: 1px solid var(--color-border);
      align-items: center;

      @media (max-width: 768px) {
        flex-direction: column;
        align-items: flex-start;
        position: relative;
        padding-right: 3rem;
        border-bottom: 2px solid var(--color-border);
      }
    }

    .col-product {
      flex: 2;
      display: flex;
      gap: 1.5rem;
      align-items: center;
      cursor: pointer;
      transition: opacity 0.2s ease;

      &:hover {
        opacity: 0.8;
      }

      .product-image {
        width: 100px;
        height: 100px;
        flex-shrink: 0;
        border-radius: var(--radius);
        border: 1px solid var(--color-border);
        background: #f8f9fa;
        overflow: hidden;
        display: flex;
        align-items: center;
        justify-content: center;

        img {
          width: 100%;
          height: 100%;
          object-fit: contain;
        }
      }

      .product-info {
        flex: 1;
      }

      h3 {
        font-family: var(--font-body);
        font-size: 1rem;
        text-transform: none;
        margin-bottom: 0.5rem;
      }

      .variant {
        font-size: 0.875rem;
        color: var(--color-gray);
        margin-bottom: 0.25rem;
      }

      @media (max-width: 768px) {
        width: 100%;
        margin-bottom: 1rem;
      }
    }

    .col-price, .col-quantity, .col-total {
      flex: 1;
      text-align: center;

      @media (max-width: 768px) {
        width: 100%;
        text-align: left;
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 0.5rem;
      }
    }

    .mobile-label {
      display: none;
      font-weight: 500;
      color: var(--color-gray);

      @media (max-width: 768px) {
        display: inline-block;
      }
    }

    .quantity-control {
      display: inline-flex;
      align-items: center;
      border: 1px solid var(--color-border);
      border-radius: 4px;
      overflow: hidden;

      button {
        background: white;
        border: none;
        width: 2rem;
        height: 2rem;
        font-size: 1.25rem;
        cursor: pointer;

        &:hover:not(:disabled) {
          background-color: #f0f0f0;
        }

        &:disabled {
          opacity: 0.5;
          cursor: not-allowed;
        }
      }

      input {
        width: 2.5rem;
        height: 2rem;
        border: none;
        border-left: 1px solid var(--color-border);
        border-right: 1px solid var(--color-border);
        text-align: center;
        -moz-appearance: textfield;

        &::-webkit-outer-spin-button,
        &::-webkit-inner-spin-button {
          -webkit-appearance: none;
          margin: 0;
        }
      }
    }

    .font-heading { font-family: var(--font-heading); font-size: 1.25rem; }
    .font-bold { font-weight: 700; }

    .col-action {
      width: 40px;
      text-align: right;

      @media (max-width: 768px) {
        position: absolute;
        top: 1.5rem;
        right: 0;
      }
    }

    .remove-btn {
      background: none;
      border: none;
      color: var(--color-gray);
      cursor: pointer;
      transition: color var(--transition);

      &:hover {
        color: var(--color-primary);
      }
    }

    .cart-summary {
      background-color: var(--color-light);
      padding: 2rem;
      border: 1px solid var(--color-border);
      border-radius: var(--radius);
      height: fit-content;
      position: sticky;
      top: 100px;

      h3 {
        margin-bottom: 1.5rem;
        padding-bottom: 1rem;
        border-bottom: 1px solid var(--color-border);
      }
    }

    .summary-row {
      display: flex;
      justify-content: space-between;
      margin-bottom: 1rem;
      font-size: 1.125rem;

      &.total {
        font-family: var(--font-heading);
        font-size: 1.5rem;
        font-weight: 700;
        margin-top: 1rem;
      }
    }

    hr {
      border: none;
      border-top: 1px solid var(--color-border);
      margin: 1.5rem 0;
    }

    .checkout-btn {
      margin-top: 2rem;
      gap: 0.5rem;
      height: 3.5rem;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .confirm-backdrop {
      position: fixed;
      inset: 0;
      z-index: 2000;
      display: grid;
      place-items: center;
      padding: 1rem;
      background: rgba(17, 24, 39, 0.58);
    }

    .confirm-dialog {
      width: min(100%, 480px);
      padding: 1.5rem;
      border: 1px solid var(--color-border);
      border-radius: 8px;
      background: #fff;
      box-shadow: 0 24px 60px rgba(0, 0, 0, 0.22);
    }

    .confirm-icon {
      width: 44px;
      height: 44px;
      display: grid;
      place-items: center;
      margin-bottom: 1rem;
      border-radius: 50%;
      background: #fff1f2;
      color: #dc2626;
    }

    .confirm-content {
      h2 {
        margin: 0 0 0.5rem;
        font-size: 1.35rem;
        text-transform: none;
      }

      > p {
        margin: 0 0 1.25rem;
        color: var(--color-gray);
      }
    }

    .confirm-product {
      display: flex;
      align-items: center;
      gap: 1rem;
      padding: 1rem;
      border: 1px solid var(--color-border);
      border-radius: 6px;
      background: #f8fafc;

      img {
        width: 64px;
        height: 64px;
        flex: 0 0 64px;
        object-fit: contain;
        border-radius: 4px;
        background: #fff;
      }

      div {
        min-width: 0;
        display: grid;
        gap: 0.25rem;
      }

      strong {
        overflow-wrap: anywhere;
      }

      span {
        color: var(--color-gray);
        font-size: 0.875rem;
      }
    }

    .confirm-actions {
      display: flex;
      justify-content: flex-end;
      gap: 0.75rem;
      margin-top: 1.5rem;

      button {
        min-height: 42px;
        padding: 0 1rem;
        border-radius: 4px;
        cursor: pointer;
        font-weight: 700;
      }

      button:disabled {
        cursor: wait;
        opacity: 0.65;
      }
    }

    .cancel-button {
      border: 1px solid var(--color-border);
      background: #fff;
      color: var(--color-dark);
    }

    .delete-button {
      display: inline-flex;
      align-items: center;
      gap: 0.5rem;
      border: 1px solid #dc2626;
      background: #dc2626;
      color: #fff;
    }

    @media (max-width: 520px) {
      .confirm-actions {
        flex-direction: column-reverse;

        button {
          width: 100%;
          justify-content: center;
        }
      }
    }
  `]
})
export class CartComponent implements OnInit {
  private cartService = inject(CartService);
  private router = inject(Router);
  private toastr = inject(ToastrService);

  readonly Trash2 = Trash2;
  readonly ArrowRight = ArrowRight;

  cartItems = this.cartService.cartItems;
  cartTotal = this.cartService.cartTotal;
  itemPendingRemove: CartItem | null = null;
  removingItem = false;

  ngOnInit(): void {
    // Component initialization
  }

  navigateToProduct(item: CartItem): void {
    if (item.productId) {
      this.router.navigate(['/products', item.productId]);
    } else {
      this.toastr.error('Không thể tìm thấy sản phẩm');
    }
  }

  getProductImage(item: CartItem): string {
    if (item.productImage) {
      if (item.productImage.startsWith('/uploads')) {
        return 'http://localhost:8080' + item.productImage;
      }
      return item.productImage;
    }
    return 'https://via.placeholder.com/100x100?text=No+Image';
  }

  onImageError(event: Event, item: CartItem): void {
    const imgElement = event.target as HTMLImageElement;
    imgElement.src = 'https://via.placeholder.com/100x100?text=No+Image';
  }

  updateQuantity(cartItemId: string, quantity: number) {
    if (quantity > 0) {
      this.cartService.updateQuantity(cartItemId, quantity).subscribe({
        next: () => {
          console.log('Quantity updated successfully');
        },
        error: (err: any) => {
          console.error('Error updating quantity:', err);
          this.toastr.error('Cập nhật số lượng thất bại');
        }
      });
    }
  }

  openRemoveConfirm(item: CartItem): void {
    this.itemPendingRemove = item;
  }

  closeRemoveConfirm(): void {
    if (!this.removingItem) {
      this.itemPendingRemove = null;
    }
  }

  confirmRemoveItem(): void {
    if (!this.itemPendingRemove || this.removingItem) return;

    this.removingItem = true;
    this.cartService.removeFromCart(this.itemPendingRemove.id).subscribe({
      next: () => {
        this.itemPendingRemove = null;
        this.removingItem = false;
        this.toastr.success('Đã xóa sản phẩm khỏi giỏ hàng');
      },
      error: (err: any) => {
        console.error('Error removing item:', err);
        this.removingItem = false;
        this.toastr.error('Xóa sản phẩm thất bại');
      }
    });
  }
}
