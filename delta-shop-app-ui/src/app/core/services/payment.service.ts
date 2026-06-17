// payment.service.ts - Đảm bảo có method này
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';

export interface VNPayPaymentRequest {
  orderId: string;
  paymentMethod: string;
  returnUrl?: string;
  bankCode?: string;
}

export interface PaymentUrlResponse {
  paymentUrl: string;
  paymentId: string;
  orderId: string;
  orderNumber: string;
  gateway: string;
  amount: number;
  transactionNo: string;
}

@Injectable({ providedIn: 'root' })
export class PaymentService {
  private apiUrl = '/api';

  constructor(private http: HttpClient) {}

  createVNPayPayment(request: VNPayPaymentRequest): Observable<PaymentUrlResponse> {
    if (localStorage.getItem('accessToken') === 'mock-admin-access-token') {
      return of({
        paymentUrl: `/payment-result?status=SUCCESS&orderId=${encodeURIComponent(request.orderId)}&orderNumber=MOCK&transactionNo=MOCK-VNPAY`,
        paymentId: `mock-payment-${Date.now()}`,
        orderId: request.orderId,
        orderNumber: 'MOCK',
        gateway: 'VNPAY',
        amount: 0,
        transactionNo: 'MOCK-VNPAY'
      });
    }
    return this.http.post<PaymentUrlResponse>(`${this.apiUrl}/payments/vnpay`, request);
  }
}
