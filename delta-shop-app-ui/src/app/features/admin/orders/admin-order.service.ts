import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { OrderDetail, OrderSummary, PageResponse } from '../../../core/services/order.service';

@Injectable({ providedIn: 'root' })
export class AdminOrderService {
  private apiUrl = `${environment.apiUrl}/orders`;

  constructor(private http: HttpClient) {}

  getOrders(page: number, size: number, status?: string): Observable<PageResponse<OrderSummary>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (status) params = params.set('status', status);
    return this.http.get<PageResponse<OrderSummary>>(`${this.apiUrl}/admin`, { params });
  }

  getOrderById(id: string): Observable<OrderDetail> {
    return this.http.get<OrderDetail>(`${this.apiUrl}/admin/${id}`);
  }

  updateStatus(id: string, status: string, note?: string): Observable<OrderDetail> {
    return this.http.patch<OrderDetail>(`${this.apiUrl}/admin/${id}/status`, { status, note });
  }

  updatePaymentStatus(id: string, paymentStatus: string): Observable<OrderDetail> {
    return this.http.patch<OrderDetail>(`${this.apiUrl}/admin/${id}/payment-status?paymentStatus=${paymentStatus}`, {});
  }

  addTrackingNumber(id: string, trackingNumber: string): Observable<OrderDetail> {
    return this.http.post<OrderDetail>(`${this.apiUrl}/admin/${id}/tracking?trackingNumber=${encodeURIComponent(trackingNumber)}`, {});
  }

  deleteOrder(id: string): Observable<any> {
    return this.http.delete(`${this.apiUrl}/admin/${id}`);
  }
}
