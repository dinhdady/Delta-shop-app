import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
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
    if (this.isMockSession()) {
      const order = this.createMockOrder(orderData);
      const orders = this.readMockOrders();
      localStorage.setItem('mockAdminOrders', JSON.stringify([order, ...orders]));
      return of(order);
    }
    return this.http.post<OrderDetail>(this.apiUrl, orderData);
  }

  getOrder(id: string): Observable<OrderDetail> {
    if (this.isMockSession()) {
      const order = this.readMockOrders().find(item => item.id === id);
      return of(order || this.createMockOrder({ items: [], shippingName: 'Admin Delta', shippingPhone: '0901234567', shippingProvince: 'TP. Hồ Chí Minh', shippingDistrict: 'Quận 1', shippingWard: 'Phường Bến Nghé', shippingAddress: '123 Nguyễn Huệ', paymentMethod: 'COD' }));
    }
    return this.http.get<OrderDetail>(`${this.apiUrl}/me/${id}`);
  }

  getMyOrders(page = 0, size = 10): Observable<PageResponse<OrderSummary>> {
    if (this.isMockSession()) {
      const orders = this.readMockOrders();
      return of({
        content: orders.slice(page * size, page * size + size).map(order => ({
          id: order.id,
          orderNumber: order.orderNumber,
          createdAt: order.createdAt,
          status: order.status,
          paymentStatus: order.paymentStatus,
          paymentMethod: order.paymentMethod,
          totalAmount: order.totalAmount,
          totalItems: order.items?.reduce((sum, item) => sum + item.quantity, 0) || 0,
          shippingName: order.shippingName || '',
          shippingPhone: order.shippingPhone || '',
          shippingAddress: order.shippingAddress
        })),
        pageNumber: page,
        pageSize: size,
        totalElements: orders.length,
        totalPages: Math.max(1, Math.ceil(orders.length / size)),
        first: page === 0,
        last: (page + 1) * size >= orders.length
      });
    }
    return this.http.get<PageResponse<OrderSummary>>(`${this.apiUrl}/me?page=${page}&size=${size}`);
  }

  cancelOrder(orderId: string, reason: string): Observable<OrderDetail> {
    if (this.isMockSession()) {
      const orders = this.readMockOrders();
      const order = orders.find(item => item.id === orderId) || this.createMockOrder({ items: [], shippingName: 'Admin Delta', shippingPhone: '0901234567', shippingProvince: 'TP. Hồ Chí Minh', shippingDistrict: 'Quận 1', shippingWard: 'Phường Bến Nghé', shippingAddress: '123 Nguyễn Huệ', paymentMethod: 'COD' });
      order.status = 'CANCELLED';
      localStorage.setItem('mockAdminOrders', JSON.stringify(orders));
      return of(order);
    }
    return this.http.post<OrderDetail>(`${this.apiUrl}/${orderId}/cancel?reason=${encodeURIComponent(reason)}`, {});
  }

  private isMockSession(): boolean {
    return localStorage.getItem('accessToken') === 'mock-admin-access-token';
  }

  private readMockOrders(): OrderDetail[] {
    try {
      return JSON.parse(localStorage.getItem('mockAdminOrders') || '[]');
    } catch {
      return [];
    }
  }

  private createMockOrder(orderData: CreateOrderRequest): OrderDetail {
    const now = new Date();
    const totalAmount = orderData.items.reduce((sum, item: any) => {
      return sum + Number(item.totalPrice || 0);
    }, 0);

    return {
      id: `mock-order-${now.getTime()}`,
      orderNumber: `MOCK-${now.getTime().toString().slice(-6)}`,
      totalAmount,
      status: 'CONFIRMED',
      paymentStatus: orderData.paymentMethod === 'COD' ? 'PENDING' : 'PAID',
      paymentMethod: orderData.paymentMethod,
      createdAt: now.toISOString(),
      shippingAddress: orderData.shippingAddress,
      shippingProvince: orderData.shippingProvince,
      shippingDistrict: orderData.shippingDistrict,
      shippingWard: orderData.shippingWard,
      shippingName: orderData.shippingName,
      shippingPhone: orderData.shippingPhone,
      subtotal: totalAmount,
      shippingFee: 0,
      discountAmount: 0,
      notes: orderData.notes,
      items: orderData.items.map((item: any, index) => ({
        id: `mock-order-item-${index + 1}`,
        productName: 'Sản phẩm Delta Sports',
        variantName: item.variantId,
        quantity: item.quantity,
        unitPrice: Number(item.unitPrice || 0),
        totalPrice: Number(item.totalPrice || 0)
      }))
    };
  }
}
