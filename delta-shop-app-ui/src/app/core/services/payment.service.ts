// payment.service.ts - Đảm bảo có method này
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

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
    return this.http.post<PaymentUrlResponse>(`${this.apiUrl}/payments/vnpay`, request);
  }
}
