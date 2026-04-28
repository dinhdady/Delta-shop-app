import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
export interface CreateOrderItemRequest {
  variantId: string;
  quantity: number;
}

export interface CreateOrderRequest {
  items: CreateOrderItemRequest[];
  shippingName: string;
  shippingPhone: string;
  shippingProvince: string;
  shippingDistrict: string;
  shippingWard: string;
  shippingAddress: string;
  paymentMethod: string;
  notes?: string;
  promotionCode?: string;
  loyaltyPointsToUse?: number;
}

export interface OrderDetail {
  id: string;
  orderNumber: string;
  totalAmount: number;
  status: string;
  paymentStatus: string;
  paymentMethod: string;
  createdAt: string;
  shippingAddress: string;
  shippingProvince: string;
  shippingDistrict: string;
  shippingWard: string;
  shippingName?: string;
  shippingPhone?: string;
  subtotal?: number;
  shippingFee?: number;
  discountAmount?: number;
  notes?: string;
  trackingNumber?: string;
  items?: Array<{
    id: string;
    productName: string;
    variantName?: string;
    productImage?: string;
    quantity: number;
    unitPrice: number;
    totalPrice: number;
  }>;
}

export interface PageResponse<T> {
  content: T[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export interface OrderSummary {
  id: string;
  orderNumber: string;
  createdAt: string;
  status: string;
  paymentStatus: string;
  paymentMethod: string;
  totalAmount: number;
  totalItems: number;
  shippingName: string;
  shippingPhone: string;
  shippingAddress: string;
}

@Injectable({
  providedIn: 'root'
})
export class OrderService {
  private apiUrl = `${environment.apiUrl}/orders`;

  constructor(private http: HttpClient) { }

  createOrder(orderData: CreateOrderRequest): Observable<OrderDetail> {
    return this.http.post<OrderDetail>(this.apiUrl, orderData);
  }

  getOrder(id: string): Observable<OrderDetail> {
    return this.http.get<OrderDetail>(`${this.apiUrl}/me/${id}`);
  }

  getMyOrders(page = 0, size = 10): Observable<PageResponse<OrderSummary>> {
    return this.http.get<PageResponse<OrderSummary>>(`${this.apiUrl}/me?page=${page}&size=${size}`);
  }

  cancelOrder(orderId: string, reason: string): Observable<OrderDetail> {
    return this.http.post<OrderDetail>(`${this.apiUrl}/${orderId}/cancel?reason=${encodeURIComponent(reason)}`, {});
  }
}
