import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { OrderDetail, OrderService, OrderSummary, PageResponse } from '../../core/services/order.service';
import { ToastrService } from 'ngx-toastr';
import { FormsModule } from '@angular/forms';
import { ReviewService } from '../../core/services/review.service';

@Component({
  selector: 'app-orders',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <section class="orders-page">
      <div class="page-header">
        <div class="container">
          <h1>Đơn hàng của tôi</h1>
          <p>Theo dõi trạng thái xử lý, thanh toán và giao hàng.</p>
        </div>
      </div>

      <div class="container">
        <div class="account-wrapper">
          <main class="orders-content">
            <div class="content-header">
              <div>
                <h2>Lịch sử đơn hàng</h2>
                <p>Quản lý và theo dõi các đơn hàng của bạn</p>
              </div>
            </div>

        <div class="filter-card">
          <div class="filters-row">
            <div class="search-box">
              <i class="bi bi-search"></i>
              <input
                [(ngModel)]="searchQuery"
                type="text"
                (keyup.enter)="onSearch()"
                placeholder="Tìm kiếm mã đơn hàng...">
            </div>
            <button class="btn-filter" (click)="onSearch()">Lọc</button>
          </div>
        </div>

        <div class="toolbar">
          <div class="summary">
            <strong>{{ totalElements() }}</strong> đơn hàng
          </div>
          <div class="pager" *ngIf="totalPages() > 1">
            <button class="btn-page" (click)="changePage(currentPage() - 1)" [disabled]="currentPage() === 0">Trước</button>
            <span>Trang {{ currentPage() + 1 }} / {{ totalPages() }}</span>
            <button class="btn-page" (click)="changePage(currentPage() + 1)" [disabled]="currentPage() + 1 >= totalPages()">Sau</button>
          </div>
        </div>

        <div class="empty-state" *ngIf="!loading() && orders().length === 0">
          <h2>Chưa có đơn hàng</h2>
          <p>Đơn hàng mới của bạn sẽ xuất hiện tại đây.</p>
        </div>

        <div class="orders-list" *ngIf="orders().length > 0">
          <article class="order-card" *ngFor="let order of orders()">
            <div class="order-head">
              <div>
                <div class="order-number">{{ order.orderNumber }}</div>
                <div class="order-date">{{ formatDate(order.createdAt) }}</div>
              </div>
              <div class="badges">
                <span class="badge" [class]="statusClass(order.status)">{{ labelStatus(order.status) }}</span>
                <span class="badge payment" [class]="paymentClass(order.paymentStatus)">{{ labelPayment(order.paymentStatus) }}</span>
              </div>
            </div>

            <div class="order-meta">
              <div><span>Người nhận</span><strong>{{ order.shippingName }}</strong></div>
              <div><span>Tổng tiền</span><strong>{{ formatCurrency(order.totalAmount) }}</strong></div>
              <div><span>Thanh toán</span><strong>{{ order.paymentMethod }}</strong></div>
              <div><span>Số sản phẩm</span><strong>{{ order.totalItems }}</strong></div>
            </div>

            <div class="order-actions">
              <button class="btn-primary" (click)="openDetails(order.id)">Xem chi tiết</button>
              <button
                class="btn-secondary"
                *ngIf="canCancel(order.status)"
                (click)="cancelOrder(order.id)"
              >
                Hủy đơn
              </button>
            </div>
          </article>
        </div>
          </main>
        </div>
      </div>

      <div class="modal-backdrop" *ngIf="selectedOrder()" (click)="closeDetails()">
        <div class="modal-panel" (click)="$event.stopPropagation()">
          <div class="modal-head">
            <div>
              <h2>{{ selectedOrder()?.orderNumber }}</h2>
              <p>{{ formatDate(selectedOrder()?.createdAt || '') }}</p>
            </div>
            <button class="close-btn" (click)="closeDetails()">x</button>
          </div>

          <div class="modal-grid" *ngIf="selectedOrder() as order">
            <div class="detail-block">
              <h3>Giao hàng</h3>
              <p>{{ order.shippingName }}</p>
              <p>{{ order.shippingPhone }}</p>
              <p>{{ fullAddress(order) }}</p>
            </div>

            <div class="detail-block">
              <h3>Thanh toán</h3>
              <p>Trạng thái: <strong>{{ labelPayment(order.paymentStatus) }}</strong></p>
              <p>Phương thức: <strong>{{ order.paymentMethod }}</strong></p>
              <p>Tổng tiền: <strong>{{ formatCurrency(order.totalAmount) }}</strong></p>
            </div>
          </div>

          <div class="items" *ngIf="selectedOrder()?.items?.length">
            <div class="item-row" *ngFor="let item of selectedOrder()?.items">
              <div>
                <strong>{{ item.productName }}</strong>
                <div class="muted" *ngIf="item.variantName">{{ item.variantName }}</div>
              </div>
              <div class="qty">x{{ item.quantity }}</div>
              <div class="price price-action-col">
                <div>{{ formatCurrency(item.totalPrice) }}</div>
                <button 
                  *ngIf="canReviewOrder(selectedOrder()) && !item.reviewed"
                  class="btn-review-item"
                  (click)="openReviewModal(item)">
                  Đánh giá
                </button>
                <span *ngIf="canReviewOrder(selectedOrder()) && item.reviewed" class="reviewed-label">
                  <i class="bi bi-check-circle-fill"></i>
                  Đã đánh giá
                </span>
              </div>
            </div>
          </div>

          <div class="totals" *ngIf="selectedOrder() as order">
            <div><span>Tạm tính</span><strong>{{ formatCurrency(order.subtotal || 0) }}</strong></div>
            <div><span>Giảm giá</span><strong>{{ formatCurrency(order.discountAmount || 0) }}</strong></div>
            <div><span>Phí vận chuyển</span><strong>{{ formatCurrency(order.shippingFee || 0) }}</strong></div>
            <div class="grand-total"><span>Tổng cộng</span><strong>{{ formatCurrency(order.totalAmount) }}</strong></div>
          </div>
        </div>
      </div>

      <!-- Write Review Modal -->
      <div class="modal-backdrop review-modal" *ngIf="reviewingItem" (click)="closeReviewModal()">
        <div class="modal-panel review-panel" (click)="$event.stopPropagation()">
          <div class="modal-head">
            <div>
              <h2>Đánh giá sản phẩm</h2>
              <p>{{ reviewingItem.productName }}</p>
            </div>
            <button class="close-btn" (click)="closeReviewModal()">x</button>
          </div>

          <form (ngSubmit)="submitReview()" class="review-form">
            <!-- Star Selection -->
            <div class="form-group-star">
              <label class="form-label">Chất lượng sản phẩm *</label>
              <div class="star-rating">
                <span 
                  *ngFor="let star of [1,2,3,4,5]" 
                  class="star-select" 
                  [class.active]="star <= reviewRating"
                  (click)="setReviewRating(star)">
                  ★
                </span>
              </div>
            </div>

            <!-- Title -->
            <div class="form-group">
              <label class="form-label">Tiêu đề đánh giá</label>
              <input 
                type="text" 
                name="title" 
                [(ngModel)]="reviewTitle" 
                class="form-control" 
                placeholder="Nhập tiêu đề đánh giá ngắn gọn">
            </div>

            <!-- Body -->
            <div class="form-group">
              <label class="form-label">Nhận xét chi tiết *</label>
              <textarea 
                name="body" 
                [(ngModel)]="reviewBody" 
                class="form-control" 
                rows="4" 
                placeholder="Hãy chia sẻ nhận xét của bạn về sản phẩm này..."></textarea>
            </div>

            <!-- Image Link -->
            <div class="form-group">
              <label class="form-label">Đường dẫn hình ảnh (tùy chọn)</label>
              <input 
                type="text" 
                name="imageUrl" 
                [(ngModel)]="reviewImageUrl" 
                class="form-control" 
                placeholder="Nhập URL hình ảnh nếu muốn chia sẻ hình ảnh thực tế">
            </div>

            <div class="form-actions review-actions-row">
              <button type="submit" class="btn-primary" [disabled]="submittingReview || reviewRating === 0 || !reviewBody.trim()">
                {{ submittingReview ? 'Đang gửi...' : 'Gửi đánh giá' }}
              </button>
              <button type="button" class="btn-secondary" (click)="closeReviewModal()">Hủy</button>
            </div>
          </form>
        </div>
      </div>
    </section>
  `,
  // styles property của OrdersComponent - thay thế styles hiện tại
  styles: [`
    /* ========== TONE MÀU CHÍNH ========== */
    :host {
      --color-black: #0a0a0a;
      --color-dark: #111827;
      --color-gray-50: #faf9f8;
      --color-gray-100: #f3f2f0;
      --color-gray-200: #e5e3e0;
      --color-gray-300: #d1ceca;
      --color-gray-400: #a8a49e;
      --color-gray-500: #8b8580;
      --color-gray-600: #6b6560;
      --color-gray-700: #554f4a;
      --color-gray-800: #3a3430;
      --color-gray-900: #1f1c1a;
      --color-orange-50: #fff7ed;
      --color-orange-100: #ffedd5;
      --color-orange-200: #fed7aa;
      --color-orange-300: #fdba74;
      --color-orange-400: #fb923c;
      --color-orange-500: #f97316;
      --color-orange-600: #ea580c;
      --color-orange-700: #c2410c;
      --color-orange-800: #9a3412;
      --color-orange-900: #7c2d12;
      --shadow-sm: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
      --shadow-md: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
      --shadow-lg: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
      --shadow-xl: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
    }

    /* ========== BODY BACKGROUND ========== */
    .orders-page {
      min-height: 100vh;
      background: linear-gradient(135deg, #ffffff 0%, #fffaf5 50%, #fff5ed 100%);
    }

    /* ========== HEADER ĐEN ========== */
    .page-header {
      background: linear-gradient(135deg, #0a0a0a 0%, #1a1a1a 50%, #0d0d0d 100%);
      color: #ffffff;
      padding: 3rem 0;
      position: relative;
      overflow: hidden;
      border-bottom: 3px solid var(--color-orange-500);
    }

    .page-header::before {
      content: '';
      position: absolute;
      top: 0;
      right: 0;
      width: 40%;
      height: 100%;
      background: linear-gradient(135deg, transparent 0%, rgba(249, 115, 22, 0.08) 100%);
      pointer-events: none;
    }

    .page-header h1 {
      margin: 0 0 0.75rem;
      font-size: 2.5rem;
      font-weight: 700;
      letter-spacing: -0.02em;
      background: linear-gradient(135deg, #ffffff 0%, var(--color-orange-200) 100%);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
    }

    .page-header p {
      margin: 0;
      color: #a8a8a8;
      font-size: 1rem;
    }

    /* ========== CONTAINER ========== */
    .container {
      max-width: 1280px;
      margin: 0 auto;
      padding: 0 1.5rem;
      position: relative;
      z-index: 1;
    }

    .content {
      padding: 2.5rem 1.5rem 4rem;
    }

    /* ========== TOOLBAR ========== */
    .filter-card {
      padding: 1rem 1.5rem;
      margin-bottom: 1.5rem;
      background: white;
      border: 1px solid var(--color-gray-200);
      border-radius: 8px;
      box-shadow: var(--shadow-sm);
    }

    .filters-row {
      display: flex;
      align-items: center;
      gap: 1rem;
    }

    .search-box {
      position: relative;
      flex: 1;
    }

    .search-box i {
      position: absolute;
      left: 1rem;
      top: 50%;
      transform: translateY(-50%);
      color: var(--color-gray-500);
      pointer-events: none;
    }

    .search-box input {
      width: 100%;
      padding: 0.75rem 1rem 0.75rem 2.5rem;
      border: 1px solid var(--color-gray-300);
      border-radius: 8px;
      outline: none;
      transition: all 0.2s ease;
    }

    .search-box input:focus {
      border-color: var(--color-orange-500);
    }

    .btn-filter {
      padding: 0.75rem 1.5rem;
      border: 1px solid var(--color-gray-300);
      border-radius: 8px;
      background: white;
      color: var(--color-gray-700);
      font-weight: 500;
      cursor: pointer;
      transition: all 0.2s ease;
    }

    .btn-filter:hover {
      border-color: var(--color-orange-500);
      color: var(--color-orange-600);
    }

    .toolbar {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 1.75rem;
      gap: 1rem;
      flex-wrap: wrap;
    }

    .summary {
      font-size: 0.875rem;
      color: var(--color-gray-600);
      background: white;
      padding: 0.5rem 1rem;
      border-radius: 999px;
      box-shadow: var(--shadow-sm);
    }

    .summary strong {
      color: var(--color-orange-600);
      font-weight: 700;
      font-size: 1rem;
    }

    .pager {
      display: flex;
      align-items: center;
      gap: 0.75rem;
    }

    .btn-page {
      border: 1px solid var(--color-gray-200);
      background: white;
      padding: 0.5rem 1rem;
      border-radius: 8px;
      font-size: 0.875rem;
      font-weight: 500;
      color: var(--color-gray-700);
      cursor: pointer;
      transition: all 0.2s ease;
    }

    .btn-page:hover:not(:disabled) {
      background: var(--color-orange-50);
      border-color: var(--color-orange-300);
      color: var(--color-orange-700);
    }

    .btn-page:disabled {
      opacity: 0.4;
      cursor: not-allowed;
    }

    .pager span {
      font-size: 0.875rem;
      color: var(--color-gray-600);
    }

    /* ========== EMPTY STATE ========== */
    .empty-state {
      background: white;
      border: 1px solid var(--color-gray-200);
      border-radius: 20px;
      padding: 4rem 3rem;
      text-align: center;
      box-shadow: var(--shadow-sm);
    }

    .empty-state h2 {
      font-size: 1.5rem;
      color: var(--color-gray-800);
      margin-bottom: 0.75rem;
    }

    .empty-state p {
      color: var(--color-gray-500);
    }

    /* ========== ORDERS LIST ========== */
    .orders-list {
      display: flex;
      flex-direction: column;
      gap: 1.25rem;
    }

    .order-card {
      background: white;
      border: 1px solid var(--color-gray-200);
      border-radius: 20px;
      padding: 1.5rem;
      transition: all 0.3s ease;
      box-shadow: var(--shadow-sm);
    }

    .order-card:hover {
      transform: translateY(-2px);
      box-shadow: var(--shadow-lg);
      border-color: var(--color-orange-200);
    }

    /* ========== ORDER HEADER ========== */
    .order-head {
      display: flex;
      justify-content: space-between;
      gap: 1rem;
      align-items: flex-start;
      margin-bottom: 1.25rem;
      flex-wrap: wrap;
    }

    .order-number {
      font-size: 1.125rem;
      font-weight: 700;
      color: var(--color-gray-900);
      letter-spacing: -0.01em;
    }

    .order-date {
      color: var(--color-gray-500);
      font-size: 0.813rem;
      margin-top: 0.25rem;
    }

    /* ========== BADGES ========== */
    .badges {
      display: flex;
      flex-wrap: wrap;
      gap: 0.5rem;
      justify-content: flex-end;
    }

    .badge {
      padding: 0.375rem 0.875rem;
      border-radius: 999px;
      font-size: 0.75rem;
      font-weight: 600;
      letter-spacing: 0.01em;
    }

    .badge.pending {
      background: var(--color-orange-50);
      color: var(--color-orange-700);
      border: 1px solid var(--color-orange-200);
    }

    .badge.confirmed, .badge.delivered {
      background: #ecfdf3;
      color: #027a48;
      border: 1px solid #abefc6;
    }

    .badge.processing, .badge.shipped {
      background: #eff8ff;
      color: #175cd3;
      border: 1px solid #b2ddff;
    }

    .badge.cancelled {
      background: #fef3f2;
      color: #b42318;
      border: 1px solid #fecdca;
    }

    /* Payment Badges */
    .badge.payment {
      background: var(--color-gray-100);
      color: var(--color-gray-600);
      border: 1px solid var(--color-gray-200);
    }

    .badge.payment.pending {
      background: #f2f4f7;
      color: #344054;
    }

    .badge.payment.paid {
      background: #ecfdf3;
      color: #027a48;
      border-color: #abefc6;
    }

    .badge.payment.failed,
    .badge.payment.refunded,
    .badge.payment.partially_refunded {
      background: #fef3f2;
      color: #b42318;
      border-color: #fecdca;
    }

    /* ========== ORDER META ========== */
    .order-meta {
      display: grid;
      grid-template-columns: repeat(4, 1fr);
      gap: 1rem;
      padding: 1.25rem 0;
      border-top: 1px solid var(--color-gray-100);
      border-bottom: 1px solid var(--color-gray-100);
    }

    .order-meta div span {
      display: block;
      color: var(--color-gray-500);
      font-size: 0.75rem;
      text-transform: uppercase;
      letter-spacing: 0.05em;
      margin-bottom: 0.375rem;
    }

    .order-meta div strong {
      font-size: 1rem;
      color: var(--color-gray-800);
    }

    /* ========== ORDER ACTIONS ========== */
    .order-actions {
      display: flex;
      gap: 0.75rem;
      justify-content: flex-end;
      padding-top: 1.25rem;
    }

    .btn-primary {
      background: linear-gradient(135deg, var(--color-orange-500) 0%, var(--color-orange-600) 100%);
      color: white;
      border: none;
      padding: 0.625rem 1.25rem;
      border-radius: 10px;
      font-size: 0.875rem;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.2s ease;
      box-shadow: var(--shadow-sm);
    }

    .btn-primary:hover {
      transform: translateY(-1px);
      background: linear-gradient(135deg, var(--color-orange-600) 0%, var(--color-orange-700) 100%);
      box-shadow: var(--shadow-md);
    }

    .btn-secondary {
      background: white;
      border: 1px solid var(--color-gray-300);
      color: var(--color-gray-700);
      padding: 0.625rem 1.25rem;
      border-radius: 10px;
      font-size: 0.875rem;
      font-weight: 500;
      cursor: pointer;
      transition: all 0.2s ease;
    }

    .btn-secondary:hover {
      background: var(--color-gray-50);
      border-color: var(--color-orange-300);
      color: var(--color-orange-600);
    }

    /* ========== MODAL ========== */
    .modal-backdrop {
      position: fixed;
      inset: 0;
      background: rgba(10, 10, 10, 0.75);
      backdrop-filter: blur(4px);
      display: grid;
      place-items: center;
      padding: 1rem;
      z-index: 1000;
      animation: fadeIn 0.2s ease;
    }

    @keyframes fadeIn {
      from { opacity: 0; }
      to { opacity: 1; }
    }

    .modal-panel {
      width: min(960px, 100%);
      max-height: 90vh;
      overflow: auto;
      background: white;
      border-radius: 24px;
      padding: 1.5rem;
      box-shadow: var(--shadow-xl);
      animation: slideUp 0.3s ease;
    }

    @keyframes slideUp {
      from {
        opacity: 0;
        transform: translateY(20px);
      }
      to {
        opacity: 1;
        transform: translateY(0);
      }
    }

    .modal-head {
      display: flex;
      justify-content: space-between;
      gap: 1rem;
      align-items: flex-start;
      margin-bottom: 1.5rem;
      padding-bottom: 1rem;
      border-bottom: 2px solid var(--color-orange-100);
    }

    .modal-head h2 {
      margin: 0;
      font-size: 1.35rem;
      color: var(--color-gray-900);
    }

    .modal-head p {
      margin: 0.35rem 0 0;
      color: var(--color-gray-500);
      font-size: 0.875rem;
    }

    .close-btn {
      background: var(--color-gray-100);
      border: none;
      width: 36px;
      height: 36px;
      border-radius: 50%;
      cursor: pointer;
      font-size: 1.25rem;
      color: var(--color-gray-600);
      transition: all 0.2s ease;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .close-btn:hover {
      background: var(--color-orange-100);
      color: var(--color-orange-700);
      transform: rotate(90deg);
    }

    /* ========== MODAL GRID ========== */
    .modal-grid {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 1rem;
      margin-bottom: 1.5rem;
    }

    .detail-block {
      background: linear-gradient(135deg, var(--color-gray-50) 0%, white 100%);
      border: 1px solid var(--color-gray-200);
      border-radius: 16px;
      padding: 1.25rem;
    }

    .detail-block h3 {
      margin: 0 0 1rem;
      font-size: 1rem;
      font-weight: 600;
      color: var(--color-orange-700);
      display: flex;
      align-items: center;
      gap: 0.5rem;
    }

    .detail-block h3::before {
      content: '';
      width: 4px;
      height: 18px;
      background: var(--color-orange-500);
      border-radius: 2px;
    }

    .detail-block p {
      margin: 0.5rem 0;
      font-size: 0.875rem;
      color: var(--color-gray-700);
    }

    /* ========== ITEMS ========== */
    .items {
      border: 1px solid var(--color-gray-200);
      border-radius: 16px;
      overflow: hidden;
      margin-bottom: 1.5rem;
    }

    .item-row {
      display: grid;
      grid-template-columns: 1fr auto auto;
      gap: 1rem;
      align-items: center;
      padding: 1rem 1.25rem;
      border-bottom: 1px solid var(--color-gray-100);
      transition: background 0.2s;
    }

    .item-row:hover {
      background: var(--color-orange-50);
    }

    .item-row:last-child {
      border-bottom: none;
    }

    .item-row strong {
      color: var(--color-gray-800);
      font-size: 0.938rem;
    }

    .muted {
      color: var(--color-gray-500);
      font-size: 0.813rem;
      margin-top: 0.25rem;
    }

    .qty {
      color: var(--color-gray-600);
      font-size: 0.875rem;
      white-space: nowrap;
    }

    .price {
      font-weight: 600;
      color: var(--color-orange-600);
      white-space: nowrap;
    }

    /* ========== TOTALS ========== */
    .totals {
      display: flex;
      flex-direction: column;
      gap: 0.75rem;
      background: var(--color-gray-50);
      border-radius: 16px;
      padding: 1.25rem;
    }

    .totals > div {
      display: flex;
      justify-content: space-between;
      align-items: center;
      font-size: 0.875rem;
    }

    .totals > div span {
      color: var(--color-gray-600);
    }

    .totals > div strong {
      font-weight: 600;
      color: var(--color-gray-800);
    }

    .grand-total {
      padding-top: 0.75rem;
      border-top: 2px dashed var(--color-orange-200);
      font-size: 1rem !important;
    }

    .grand-total span {
      font-weight: 600;
      color: var(--color-gray-800);
    }

    .grand-total strong {
      font-size: 1.125rem;
      color: var(--color-orange-600) !important;
    }

    /* ========== RESPONSIVE ========== */
    @media (max-width: 860px) {
      .order-meta {
        grid-template-columns: repeat(2, 1fr);
      }
      .modal-grid {
        grid-template-columns: 1fr;
      }
    }

    @media (max-width: 640px) {
      .page-header {
        padding: 2rem 0;
      }
      .page-header h1 {
        font-size: 1.75rem;
      }
      .toolbar {
        flex-direction: column;
        align-items: stretch;
      }
      .filters-row {
        flex-direction: column;
        align-items: stretch;
      }
      .order-head {
        flex-direction: column;
      }
      .badges {
        justify-content: flex-start;
      }
      .order-meta {
        grid-template-columns: 1fr;
      }
      .order-actions {
        flex-direction: column;
      }
      .order-actions button {
        width: 100%;
      }
      .modal-panel {
        padding: 1rem;
      }
      .item-row {
        grid-template-columns: 1fr;
        gap: 0.5rem;
      }
      .qty, .price {
        text-align: left;
      }
    }

    /* Review Button & Modal Styles */
    .price-action-col {
      display: flex;
      flex-direction: column;
      align-items: flex-end;
      gap: 6px;
    }
    .btn-review-item {
      background: #cd4631;
      color: white;
      border: none;
      padding: 6px 12px;
      border-radius: 6px;
      font-size: 12px;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.2s ease;
      margin-top: 4px;
    }
    .btn-review-item:hover {
      background: #b83a26;
      transform: translateY(-1px);
    }

    .reviewed-label {
      display: inline-flex;
      align-items: center;
      justify-content: flex-end;
      gap: 6px;
      margin-top: 8px;
      color: #15803d;
      font-size: 13px;
      font-weight: 700;
    }
    .review-panel {
      width: min(550px, 100%) !important;
    }
    .review-form {
      display: flex;
      flex-direction: column;
      gap: 16px;
      margin-top: 10px;
    }
    .form-group-star {
      display: flex;
      flex-direction: column;
      gap: 6px;
    }
    .form-label {
      font-size: 13px;
      font-weight: 600;
      color: #333;
    }
    .star-rating {
      display: flex;
      gap: 8px;
      font-size: 28px;
      color: #ddd;
    }
    .star-select {
      cursor: pointer;
      transition: color 0.2s ease;
    }
    .star-select:hover,
    .star-select.active {
      color: #ffc107;
    }
    .form-group {
      display: flex;
      flex-direction: column;
      gap: 6px;
    }
    .form-control {
      padding: 10px 12px;
      border: 1px solid #ddd;
      border-radius: 8px;
      font-size: 14px;
      font-family: inherit;
    }
    .form-control:focus {
      outline: none;
      border-color: #cd4631;
      box-shadow: 0 0 0 2px rgba(205, 70, 49, 0.1);
    }
    .review-actions-row {
      display: flex;
      gap: 12px;
      margin-top: 8px;
    }

    /* ========== PROFILE-ALIGNED LAYOUT ========== */
    :host {
      --color-orange-50: #fff5f6;
      --color-orange-100: #ffe4e8;
      --color-orange-200: #fecdd5;
      --color-orange-300: #fda4b2;
      --color-orange-400: #e85a70;
      --color-orange-500: #c41e3a;
      --color-orange-600: #a71630;
      --color-orange-700: #8b0000;
      --color-orange-800: #710018;
      --color-orange-900: #590012;
    }

    .orders-page {
      background: #f5f5f5;
    }

    .page-header {
      padding: 60px 0;
      text-align: center;
      background: linear-gradient(135deg, #1a1a1a 0%, #2c2c2c 100%);
      border-bottom: 0;
    }

    .page-header::before {
      display: none;
    }

    .page-header h1 {
      margin: 0 0 16px;
      color: #fff;
      font-size: 48px;
      letter-spacing: 0;
      background: none;
      -webkit-text-fill-color: initial;
    }

    .page-header p {
      color: rgba(255, 255, 255, 0.8);
      font-size: 18px;
    }

    .container {
      max-width: 1200px;
      padding: 0 20px;
    }

    .account-wrapper {
      display: block;
      padding: 60px 0;
    }

    .orders-content {
      background: #fff;
      border-radius: 12px;
      box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
    }

    .orders-content {
      min-width: 0;
      padding: 30px;
    }

    .content-header {
      margin-bottom: 30px;
      padding-bottom: 20px;
      border-bottom: 2px solid #c41e3a;
    }

    .content-header h2 {
      margin: 0 0 6px;
      color: #333;
      font-size: 24px;
      font-weight: 600;
      text-transform: none;
    }

    .content-header p {
      margin: 0;
      color: #777;
      font-size: 14px;
    }

    .filter-card {
      padding: 0;
      border: 0;
      border-radius: 0;
      box-shadow: none;
    }

    .search-box input {
      border-radius: 6px;
    }

    .search-box input:focus {
      border-color: #c41e3a;
      box-shadow: 0 0 0 3px rgba(196, 30, 58, 0.1);
    }

    .btn-filter,
    .btn-page {
      border-radius: 6px;
    }

    .summary {
      padding: 0;
      border-radius: 0;
      background: transparent;
      box-shadow: none;
    }

    .summary strong {
      color: #c41e3a;
    }

    .order-card {
      border-color: #e0e0e0;
      border-radius: 8px;
      box-shadow: none;
    }

    .order-card:hover {
      border-color: #c41e3a;
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
    }

    .btn-primary {
      border-radius: 6px;
      background: #c41e3a;
      box-shadow: none;
    }

    .btn-primary:hover {
      background: #8b0000;
    }

    .btn-secondary {
      border-radius: 6px;
    }

    .modal-panel,
    .detail-block,
    .items,
    .totals {
      border-radius: 12px;
    }

    @media (max-width: 992px) {
      .account-wrapper {
        padding: 40px 0;
      }
    }

    @media (max-width: 640px) {
      .page-header {
        padding: 40px 0;
      }

      .page-header h1 {
        font-size: 32px;
      }

      .page-header p {
        font-size: 15px;
      }

      .account-wrapper {
        padding: 24px 0;
      }

      .orders-content {
        padding: 20px;
      }
    }
  `]
})
export class OrdersComponent implements OnInit {
  private orderService = inject(OrderService);
  private toastr = inject(ToastrService);
  private route = inject(ActivatedRoute);
  private reviewService = inject(ReviewService);

  orders = signal<OrderSummary[]>([]);
  selectedOrder = signal<OrderDetail | null>(null);
  currentPage = signal(0);
  totalPages = signal(0);
  totalElements = signal(0);
  loading = signal(false);
  searchQuery = '';

  // Review states
  reviewingItem: any = null;
  reviewRating = 5;
  reviewTitle = '';
  reviewBody = '';
  reviewImageUrl = '';
  submittingReview = false;

  ngOnInit(): void {
    this.loadOrders();
    
    // Auto-focus on an order if orderId query parameter is provided
    this.route.queryParams.subscribe(params => {
      const orderId = params['orderId'];
      if (orderId) {
        this.openDetails(orderId);
      }
    });
  }

  loadOrders(): void {
    this.loading.set(true);
    this.orderService.getMyOrders(this.currentPage(), 10, this.searchQuery.trim() || undefined).subscribe({
      next: (response: PageResponse<OrderSummary>) => {
        this.orders.set(response.content);
        this.totalPages.set(response.totalPages);
        this.totalElements.set(response.totalElements);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.toastr.error('Khong the tai danh sach don hang');
      }
    });
  }

  onSearch(): void {
    this.currentPage.set(0);
    this.loadOrders();
  }

  changePage(page: number): void {
    if (page < 0 || page >= this.totalPages()) {
      return;
    }
    this.currentPage.set(page);
    this.loadOrders();
  }

  openDetails(orderId: string): void {
    this.orderService.getOrder(orderId).subscribe({
      next: order => this.selectedOrder.set(order),
      error: () => this.toastr.error('Khong the tai chi tiet don hang')
    });
  }

  closeDetails(): void {
    this.selectedOrder.set(null);
  }

  cancelOrder(orderId: string): void {
    this.orderService.cancelOrder(orderId, 'Nguoi dung huy don').subscribe({
      next: () => {
        this.toastr.success('Da huy don hang');
        this.closeDetails();
        this.loadOrders();
      },
      error: () => this.toastr.error('Khong the huy don hang')
    });
  }

  canCancel(status: string): boolean {
    return status === 'PENDING' || status === 'CONFIRMED';
  }

  labelStatus(status: string): string {
    return ({
      PENDING: 'Cho xu ly',
      CONFIRMED: 'Da xac nhan',
      PROCESSING: 'Dang xu ly',
      SHIPPED: 'Dang giao',
      DELIVERED: 'Da giao',
      CANCELLED: 'Da huy',
      REFUNDED: 'Da hoan tien'
    } as Record<string, string>)[status] ?? status;
  }

  labelPayment(status: string): string {
    return ({
      PENDING: 'Cho thanh toan',
      PAID: 'Da thanh toan',
      FAILED: 'That bai',
      REFUNDED: 'Da hoan tien',
      PARTIALLY_REFUNDED: 'Hoan tien mot phan'
    } as Record<string, string>)[status] ?? status;
  }

  statusClass(status: string): string {
    return status.toLowerCase();
  }

  paymentClass(status: string): string {
    return status.toLowerCase();
  }

  fullAddress(order: OrderDetail): string {
    return [order.shippingAddress, order.shippingWard, order.shippingDistrict, order.shippingProvince].filter(Boolean).join(', ');
  }

  formatDate(date: string): string {
    return new Date(date).toLocaleString('vi-VN');
  }

  formatCurrency(value: number): string {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND', maximumFractionDigits: 0 }).format(value);
  }

  openReviewModal(item: any): void {
    if (!this.canReviewOrder(this.selectedOrder()) || item.reviewed) {
      return;
    }

    this.reviewService.getReviewEligibility(item.id).subscribe({
      next: eligibility => {
        item.reviewed = eligibility.reviewed;
        if (!eligibility.canReview || !eligibility.productId) {
          this.toastr.info(eligibility.reason || 'Sản phẩm này chưa đủ điều kiện đánh giá');
          return;
        }

        this.reviewingItem = { ...item, productId: eligibility.productId };
        this.reviewRating = 5;
        this.reviewTitle = '';
        this.reviewBody = '';
        this.reviewImageUrl = '';
      },
      error: err => this.toastr.error(this.getReviewErrorMessage(err))
    });
  }

  closeReviewModal(): void {
    this.reviewingItem = null;
  }

  setReviewRating(rating: number): void {
    this.reviewRating = rating;
  }

  canReviewOrder(order: OrderDetail | null): boolean {
    return !!order
      && order.paymentStatus === 'PAID'
      && order.status !== 'CANCELLED'
      && order.status !== 'REFUNDED';
  }

  submitReview(): void {
    if (!this.reviewingItem || !this.selectedOrder()) return;

    this.submittingReview = true;
    const imagesList: string[] = [];
    if (this.reviewImageUrl.trim()) {
      imagesList.push(this.reviewImageUrl.trim());
    }

    const payload = {
      productId: this.reviewingItem.productId,
      orderItemId: this.reviewingItem.id,
      rating: this.reviewRating,
      title: this.reviewTitle.trim(),
      body: this.reviewBody.trim(),
      images: imagesList
    };

    this.reviewService.createReview(payload).subscribe({
      next: () => {
        this.toastr.success('Cảm ơn bạn đã gửi đánh giá sản phẩm!');
        this.submittingReview = false;
        this.closeReviewModal();
        // Reload details
        const orderId = this.selectedOrder()?.id;
        if (orderId) {
          this.openDetails(orderId);
        }
      },
      error: (err) => {
        this.submittingReview = false;
        this.toastr.error(this.getReviewErrorMessage(err));
      }
    });
  }

  private getReviewErrorMessage(error: any): string {
    if (error?.error?.message) {
      return error.error.message;
    }
    const validationErrors = error?.error?.errors;
    if (validationErrors) {
      return Object.values(validationErrors)[0] as string;
    }
    return 'Không thể kết nối tới hệ thống đánh giá';
  }
}
