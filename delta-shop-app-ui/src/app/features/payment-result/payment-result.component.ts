// payment-result.component.ts
import { CommonModule } from '@angular/common';
import { Component, computed, inject, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { OrderService } from '../../core/services/order.service';
import { ToastrService } from 'ngx-toastr';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-payment-result',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <section class="payment-result">
      <div class="result-card" [class.failed]="!isSuccess()">
        <div class="status-badge">{{ isSuccess() ? 'THANH TOÁN THÀNH CÔNG' : 'THANH TOÁN THẤT BẠI' }}</div>
        <h1>{{ isSuccess() ? 'Giao dịch đã được xác nhận' : 'Giao dịch chưa hoàn tất' }}</h1>
        <p class="message">
          {{ message() }}
        </p>

        <div class="meta" *ngIf="orderNumber() || transactionNo()">
          <div class="meta-row" *ngIf="orderNumber()">
            <span>Mã đơn hàng</span>
            <strong>{{ orderNumber() }}</strong>
          </div>
          <div class="meta-row" *ngIf="transactionNo()">
            <span>Mã giao dịch nội bộ</span>
            <strong>{{ transactionNo() }}</strong>
          </div>
          <div class="meta-row" *ngIf="gatewayTxnId()">
            <span>Mã giao dịch VNPay</span>
            <strong>{{ gatewayTxnId() }}</strong>
          </div>
        </div>

        <div class="actions">
          <a routerLink="/" class="btn btn-primary">Về trang chủ</a>
          <a *ngIf="orderId()" [routerLink]="['/orders']" class="btn btn-secondary">Xem đơn hàng</a>
          <button *ngIf="!isSuccess()" (click)="retryPayment()" class="btn btn-warning">Thử lại</button>
        </div>
      </div>
    </section>
  `,
  styles: [`
    .payment-result {
      min-height: calc(100vh - 140px);
      display: grid;
      place-items: center;
      padding: 2rem 1rem 4rem;
      background: #f8f9fa;
    }
    .result-card {
      width: min(100%, 720px);
      background: #fff;
      border: 1px solid #dfe3e8;
      border-top: 4px solid #1f7a1f;
      border-radius: 12px;
      padding: 2rem;
      box-shadow: 0 18px 50px rgba(15, 23, 42, 0.08);
    }
    .result-card.failed {
      border-top-color: #c62828;
    }
    .status-badge {
      display: inline-block;
      margin-bottom: 1rem;
      padding: 0.5rem 0.75rem;
      border-radius: 999px;
      font-size: 0.875rem;
      font-weight: 700;
      background: rgba(31, 122, 31, 0.08);
      color: #1f7a1f;
    }
    .failed .status-badge {
      background: rgba(198, 40, 40, 0.08);
      color: #c62828;
    }
    h1 {
      margin: 0 0 0.75rem;
      font-size: 2rem;
    }
    .message {
      margin: 0 0 1.5rem;
      color: #475467;
      line-height: 1.6;
    }
    .meta {
      display: grid;
      gap: 0.75rem;
      margin-bottom: 1.75rem;
      padding: 1rem;
      background: #f8fafc;
      border-radius: 10px;
    }
    .meta-row {
      display: flex;
      justify-content: space-between;
      gap: 1rem;
    }
    .actions {
      display: flex;
      flex-wrap: wrap;
      gap: 0.75rem;
    }
    .btn-warning {
      background-color: #ffc107;
      color: #000;
      padding: 0.5rem 1rem;
      border-radius: 4px;
      text-decoration: none;
      border: none;
      cursor: pointer;
    }
  `]
})
export class PaymentResultComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private orderService = inject(OrderService);
  private toastr = inject(ToastrService);
  private http = inject(HttpClient);

  status = computed(() => this.route.snapshot.queryParamMap.get('status') ?? 'FAILED');
  orderId = computed(() => this.route.snapshot.queryParamMap.get('orderId'));
  orderNumber = computed(() => this.route.snapshot.queryParamMap.get('orderNumber'));
  transactionNo = computed(() => this.route.snapshot.queryParamMap.get('transactionNo'));
  gatewayTxnId = computed(() => this.route.snapshot.queryParamMap.get('gatewayTxnId'));
  errorMessage = computed(() => this.route.snapshot.queryParamMap.get('message'));

  isSuccess = computed(() => this.status() === 'PAID' || this.status() === 'paid');

  message = computed(() => {
    if (this.isSuccess()) {
      return 'Đơn hàng đã được ghi nhận và thanh toán qua VNPay. Bạn có thể theo dõi trạng thái giao hàng trong tài khoản.';
    }
    return this.errorMessage() ?? 'VNPay không xác nhận được giao dịch. Bạn có thể thử lại hoặc chọn phương thức thanh toán khác.';
  });

  ngOnInit() {
  console.log('All query params:', this.route.snapshot.queryParamMap);
  console.log('Payment result:', {
    status: this.status(),
    orderId: this.orderId(),
    transactionNo: this.transactionNo(),
    gatewayTxnId: this.gatewayTxnId()
  });

  // KHÔNG gọi API kiểm tra order nữa vì đã có params từ URL
  // Chỉ hiển thị thông tin từ URL
  if (!this.orderId() && !this.isSuccess()) {
    const pendingOrder = sessionStorage.getItem('pendingOrder');
    if (pendingOrder) {
      const order = JSON.parse(pendingOrder);
      console.log('Found pending order:', order);
      // Cập nhật UI với thông tin từ storage
      // Không gọi API nếu không cần thiết
    }
  }
}

  checkPaymentStatus(orderId: string) {
    this.http.get(`/api/orders/${orderId}`).subscribe({
      next: (order: any) => {
        console.log('Order status:', order);
        if (order.paymentStatus === 'PAID') {
          this.toastr.success('Thanh toán thành công!');
          // Reload page với đúng params
          this.router.navigate(['/payment-result'], {
            queryParams: {
              status: 'PAID',
              orderId: order.id,
              orderNumber: order.orderNumber,
              transactionNo: order.payment?.transactionNo
            }
          });
        }
      },
      error: (err) => {
        console.error('Error checking order:', err);
      }
    });
  }

  retryPayment() {
    // Lấy lại order info từ sessionStorage
    const pendingOrder = sessionStorage.getItem('pendingOrder');
    if (pendingOrder) {
      const order = JSON.parse(pendingOrder);
      this.router.navigate(['/checkout'], {
        queryParams: { retryOrderId: order.orderId }
      });
    } else {
      this.router.navigate(['/cart']);
    }
  }
}
