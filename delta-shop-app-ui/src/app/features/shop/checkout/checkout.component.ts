import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { finalize } from 'rxjs/operators';

interface PaymentRequest {
  orderId: string;
  paymentMethod: string;
  returnUrl?: string;
  bankCode?: string;
}

interface PaymentUrlResponse {
  paymentUrl: string;
  paymentId: string;
  orderId: string;
  orderNumber: string;
  gateway: string;
  amount: number;
  transactionNo: string;
}

@Component({
  selector: 'app-checkout',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="checkout-container">
      <h2>Checkout</h2>

      <div class="payment-methods">
        <h3>Phương thức thanh toán</h3>
        <button
          *ngFor="let method of paymentMethods"
          (click)="processPayment(method.value)"
          [disabled]="isProcessing"
          class="payment-btn">
          {{ method.label }}
        </button>
      </div>

      <div *ngIf="isProcessing" class="loading">
        Đang xử lý thanh toán...
      </div>

      <div *ngIf="error" class="error">
        {{ error }}
      </div>
    </div>
  `,
  styles: [`
    .checkout-container { padding: 20px; max-width: 600px; margin: 0 auto; }
    .payment-methods { margin-top: 20px; }
    .payment-btn {
      margin: 10px;
      padding: 12px 24px;
      font-size: 16px;
      cursor: pointer;
      background-color: #007bff;
      color: white;
      border: none;
      border-radius: 4px;
    }
    .payment-btn:disabled { background-color: #ccc; cursor: not-allowed; }
    .loading { color: blue; margin-top: 20px; }
    .error { color: red; margin-top: 20px; }
  `]
})
export class CheckoutComponent {
  paymentMethods = [
    { label: 'Thanh toán VNPAY', value: 'VNPAY' },
    { label: 'Thanh toán MOMO', value: 'MOMO' },
    { label: 'Chuyển khoản ngân hàng', value: 'BANK_TRANSFER' }
  ];

  isProcessing = false;
  error: string | null = null;

  // Giả sử orderId từ service hoặc route param
  private orderId = '123e4567-e89b-12d3-a456-426614174000'; // TODO: Lấy từ cart/order service

  constructor(private http: HttpClient) {}

  processPayment(paymentMethod: string): void {
    this.isProcessing = true;
    this.error = null;

    const paymentRequest: PaymentRequest = {
      orderId: this.orderId,
      paymentMethod: paymentMethod
    };

    let endpoint = '';
    switch (paymentMethod) {
      case 'VNPAY':
        endpoint = '/api/payments/vnpay';
        break;
      case 'MOMO':
        endpoint = '/api/payments/momo';
        break;
      default:
        endpoint = '/api/payments/bank-transfer';
    }

    this.http.post<PaymentUrlResponse>(endpoint, paymentRequest)
      .pipe(finalize(() => this.isProcessing = false))
      .subscribe({
        next: (response) => {
          if (response.paymentUrl) {
            // Chuyển hướng đến cổng thanh toán
            window.location.href = response.paymentUrl;
          } else {
            this.error = 'Không nhận được URL thanh toán';
          }
        },
        error: (err) => {
          console.error('Payment error:', err);
          this.error = err.error?.message || 'Có lỗi xảy ra khi khởi tạo thanh toán';
        }
      });
  }
}
