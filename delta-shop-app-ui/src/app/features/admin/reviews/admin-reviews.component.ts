import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ReviewService, Review } from '../../../core/services/review.service';
import { ToastrService } from 'ngx-toastr';
import { LucideAngularModule } from 'lucide-angular';

@Component({
  selector: 'app-admin-reviews',
  standalone: true,
  imports: [CommonModule, FormsModule, LucideAngularModule],
  template: `
    <div class="admin-reviews-page">
      <div class="page-header">
        <h2>Duyệt Đánh Giá Sản Phẩm</h2>
        <p>Quản lý và phê duyệt nhận xét từ khách hàng.</p>
      </div>

      <div class="reviews-container">
        @if (loading()) {
          <div class="state-box">
            <div class="spinner"></div>
            <p>Đang tải danh sách đánh giá...</p>
          </div>
        } @else if (reviews().length === 0) {
          <div class="state-box empty">
            <p>Không có đánh giá nào đang chờ duyệt.</p>
          </div>
        } @else {
          <div class="reviews-table-wrapper">
            <table class="admin-table">
              <thead>
                <tr>
                  <th>Sản phẩm</th>
                  <th>Khách hàng</th>
                  <th>Đánh giá</th>
                  <th>Nội dung</th>
                  <th>Ngày tạo</th>
                  <th class="actions-col">Thao tác</th>
                </tr>
              </thead>
              <tbody>
                @for (rev of reviews(); track rev.id) {
                  <tr class="review-row">
                    <td>
                      <div class="product-info">
                        <strong>{{ rev.productName }}</strong>
                        <span class="product-id">ID: {{ rev.productId | slice:0:8 }}</span>
                      </div>
                    </td>
                    <td>
                      <div class="user-info">
                        <img #avatarImg [src]="rev.userAvatar || 'assets/images/default-avatar.png'" alt="" class="user-avatar" (error)="avatarImg.src = 'https://via.placeholder.com/32x32?text=U'">
                        <strong>{{ rev.userName }}</strong>
                      </div>
                    </td>
                    <td>
                      <div class="stars-rating">
                        @for (s of [1,2,3,4,5]; track s) {
                          <span class="star" [class.filled]="s <= rev.rating">★</span>
                        }
                      </div>
                      <span class="purchase-badge" *ngIf="rev.verifiedPurchase">Đã mua hàng</span>
                    </td>
                    <td>
                      <div class="review-content-cell">
                        <strong class="review-title" *ngIf="rev.title">{{ rev.title }}</strong>
                        <p class="review-body">{{ rev.body }}</p>
                        @if (rev.images && rev.images.length > 0) {
                          <div class="review-images-row">
                            @for (img of rev.images; track img) {
                              <img [src]="img" alt="Ảnh thực tế" class="review-thumb" (click)="viewFullImage(img)">
                            }
                          </div>
                        }
                      </div>
                    </td>
                    <td>
                      <span class="date-text">{{ rev.createdAt | date:'dd/MM/yyyy HH:mm' }}</span>
                    </td>
                    <td>
                      <div class="action-buttons-group">
                        <button class="btn-action approve" (click)="startApproval(rev)">Duyệt</button>
                        <button class="btn-action reject" (click)="rejectReview(rev.id)">Từ chối</button>
                      </div>
                    </td>
                  </tr>
                }
              </tbody>
            </table>
          </div>

          <!-- Pagination -->
          <div class="pagination-row" *ngIf="totalPages() > 1">
            <button class="btn-page" (click)="changePage(currentPage() - 1)" [disabled]="currentPage() === 0">Trước</button>
            <span>Trang {{ currentPage() + 1 }} / {{ totalPages() }}</span>
            <button class="btn-page" (click)="changePage(currentPage() + 1)" [disabled]="currentPage() + 1 >= totalPages()">Sau</button>
          </div>
        }
      </div>

      <!-- Approve with Reply Modal -->
      <div class="modal-backdrop" *ngIf="activeApprovalReview()">
        <div class="modal-panel" (click)="$event.stopPropagation()">
          <div class="modal-head">
            <h3>Phê duyệt đánh giá</h3>
            <button class="close-btn" (click)="cancelApproval()">×</button>
          </div>
          <div class="modal-body">
            <p>Phản hồi của cửa hàng đối với đánh giá của <strong>{{ activeApprovalReview()?.userName }}</strong>:</p>
            <textarea 
              [(ngModel)]="adminReplyText" 
              class="form-control" 
              rows="4" 
              placeholder="Nhập nội dung phản hồi của cửa hàng (tùy chọn)..."></textarea>
          </div>
          <div class="modal-foot">
            <button class="btn-submit" (click)="approveReview()" [disabled]="submitting()">
              {{ submitting() ? 'Đang duyệt...' : 'Xác nhận & Duyệt' }}
            </button>
            <button class="btn-cancel" (click)="cancelApproval()">Hủy</button>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .admin-reviews-page {
      padding: 24px;
      background: #faf9f6;
      min-height: 100vh;
    }
    .page-header {
      margin-bottom: 24px;
      padding-bottom: 16px;
      border-bottom: 2px solid #cd4631;
    }
    .page-header h2 {
      margin: 0;
      font-size: 24px;
      color: #1a1a1a;
      font-weight: 700;
    }
    .page-header p {
      margin: 4px 0 0;
      color: #666;
      font-size: 14px;
    }
    .reviews-container {
      background: #ffffff;
      border-radius: 16px;
      box-shadow: 0 4px 20px rgba(0,0,0,0.05);
      padding: 20px;
    }
    .state-box {
      text-align: center;
      padding: 60px 20px;
      color: #888;
    }
    .state-box.empty {
      font-size: 15px;
    }
    .spinner {
      width: 40px;
      height: 40px;
      border: 3px solid #f3f3f3;
      border-top: 3px solid #cd4631;
      border-radius: 50%;
      margin: 0 auto 16px;
      animation: spin 1s linear infinite;
    }
    @keyframes spin {
      0% { transform: rotate(0deg); }
      100% { transform: rotate(360deg); }
    }
    .reviews-table-wrapper {
      overflow-x: auto;
    }
    .admin-table {
      width: 100%;
      border-collapse: collapse;
      text-align: left;
    }
    .admin-table th, .admin-table td {
      padding: 16px 12px;
      border-bottom: 1px solid #f0f0f0;
      font-size: 14px;
      vertical-align: top;
    }
    .admin-table th {
      font-weight: 600;
      color: #333;
      background: #fafafa;
    }
    .product-info, .user-info {
      display: flex;
      flex-direction: column;
      gap: 4px;
    }
    .user-info {
      flex-direction: row;
      align-items: center;
      gap: 8px;
    }
    .user-avatar {
      width: 32px;
      height: 32px;
      border-radius: 50%;
      object-fit: cover;
      background: #eee;
    }
    .product-id {
      font-size: 11px;
      color: #999;
    }
    .stars-rating {
      color: #ddd;
      font-size: 15px;
      white-space: nowrap;
    }
    .stars-rating .star.filled {
      color: #ffc107;
    }
    .purchase-badge {
      display: inline-block;
      margin-top: 6px;
      font-size: 10px;
      background: #e8f5e9;
      color: #2e7d32;
      padding: 2px 8px;
      border-radius: 12px;
      font-weight: 600;
    }
    .review-content-cell {
      max-width: 400px;
    }
    .review-title {
      display: block;
      font-weight: 600;
      color: #222;
      margin-bottom: 4px;
    }
    .review-body {
      margin: 0;
      color: #555;
      line-height: 1.5;
    }
    .review-images-row {
      display: flex;
      gap: 6px;
      margin-top: 8px;
      flex-wrap: wrap;
    }
    .review-thumb {
      width: 48px;
      height: 48px;
      object-fit: cover;
      border-radius: 4px;
      border: 1px solid #ddd;
      cursor: pointer;
      transition: transform 0.2s;
    }
    .review-thumb:hover {
      transform: scale(1.05);
    }
    .date-text {
      font-size: 13px;
      color: #888;
      white-space: nowrap;
    }
    .action-buttons-group {
      display: flex;
      flex-direction: column;
      gap: 8px;
    }
    .btn-action {
      padding: 6px 12px;
      border-radius: 6px;
      font-size: 12px;
      font-weight: 600;
      cursor: pointer;
      border: none;
      transition: all 0.2s;
    }
    .btn-action.approve {
      background: #e8f5e9;
      color: #2e7d32;
    }
    .btn-action.approve:hover {
      background: #2e7d32;
      color: white;
    }
    .btn-action.reject {
      background: #ffebee;
      color: #c62828;
    }
    .btn-action.reject:hover {
      background: #c62828;
      color: white;
    }
    .pagination-row {
      display: flex;
      justify-content: center;
      align-items: center;
      gap: 16px;
      margin-top: 20px;
    }
    .btn-page {
      padding: 6px 16px;
      border: 1px solid #ddd;
      background: white;
      border-radius: 6px;
      cursor: pointer;
    }
    .btn-page:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }
    /* Modal styles */
    .modal-backdrop {
      position: fixed;
      inset: 0;
      background: rgba(0, 0, 0, 0.5);
      display: grid;
      place-items: center;
      z-index: 1000;
      padding: 16px;
    }
    .modal-panel {
      background: white;
      border-radius: 16px;
      width: min(500px, 100%);
      padding: 24px;
      box-shadow: 0 10px 30px rgba(0,0,0,0.15);
    }
    .modal-head {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;
    }
    .modal-head h3 {
      margin: 0;
      font-size: 18px;
      color: #333;
    }
    .close-btn {
      background: none;
      border: none;
      font-size: 24px;
      cursor: pointer;
      color: #888;
    }
    .close-btn:hover {
      color: #cd4631;
    }
    .modal-body {
      margin-bottom: 20px;
    }
    .form-control {
      width: 100%;
      padding: 10px;
      border: 1px solid #ddd;
      border-radius: 8px;
      font-size: 14px;
      font-family: inherit;
    }
    .form-control:focus {
      outline: none;
      border-color: #cd4631;
    }
    .modal-foot {
      display: flex;
      gap: 12px;
      justify-content: flex-end;
    }
    .btn-submit {
      background: #cd4631;
      color: white;
      border: none;
      padding: 8px 16px;
      border-radius: 8px;
      cursor: pointer;
      font-weight: 600;
    }
    .btn-submit:hover {
      background: #b83a26;
    }
    .btn-cancel {
      background: #f5f5f5;
      color: #666;
      border: 1px solid #ddd;
      padding: 8px 16px;
      border-radius: 8px;
      cursor: pointer;
    }
  `]
})
export class AdminReviewsComponent implements OnInit {
  private reviewService = inject(ReviewService);
  private toastr = inject(ToastrService);

  reviews = signal<Review[]>([]);
  loading = signal(false);
  submitting = signal(false);
  currentPage = signal(0);
  totalPages = signal(0);

  // Approval modal states
  activeApprovalReview = signal<Review | null>(null);
  adminReplyText = '';

  ngOnInit(): void {
    this.loadPendingReviews();
  }

  loadPendingReviews(): void {
    this.loading.set(true);
    this.reviewService.getPendingReviews(this.currentPage(), 10).subscribe({
      next: (res) => {
        this.reviews.set(res.content || []);
        this.totalPages.set(res.totalPages || 0);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.toastr.error('Không thể tải danh sách đánh giá chờ duyệt');
      }
    });
  }

  changePage(page: number): void {
    if (page >= 0 && page < this.totalPages()) {
      this.currentPage.set(page);
      this.loadPendingReviews();
    }
  }

  startApproval(review: Review): void {
    this.activeApprovalReview.set(review);
    this.adminReplyText = '';
  }

  cancelApproval(): void {
    this.activeApprovalReview.set(null);
  }

  approveReview(): void {
    const review = this.activeApprovalReview();
    if (!review) return;

    this.submitting.set(true);
    const payload = {
      status: 'APPROVED',
      adminReply: this.adminReplyText.trim() ? this.adminReplyText.trim() : undefined
    };

    this.reviewService.moderateReview(review.id, payload).subscribe({
      next: () => {
        this.toastr.success('Đã duyệt đánh giá thành công');
        this.submitting.set(false);
        this.cancelApproval();
        this.loadPendingReviews();
      },
      error: (err) => {
        this.submitting.set(false);
        this.toastr.error(err.error?.message || 'Lỗi khi duyệt đánh giá');
      }
    });
  }

  rejectReview(reviewId: string): void {
    if (!confirm('Bạn có chắc chắn muốn từ chối đánh giá này?')) return;

    this.reviewService.moderateReview(reviewId, { status: 'REJECTED' }).subscribe({
      next: () => {
        this.toastr.success('Đã từ chối đánh giá');
        this.loadPendingReviews();
      },
      error: (err) => {
        this.toastr.error(err.error?.message || 'Lỗi khi từ chối đánh giá');
      }
    });
  }

  viewFullImage(url: string): void {
    window.open(url, '_blank');
  }
}
