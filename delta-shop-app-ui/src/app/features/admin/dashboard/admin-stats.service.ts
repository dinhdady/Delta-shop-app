import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
export interface TopProductsResponse {
  products: TopProductItem[];
  period: string;
  limit: number;
  totalItems: number;
  totalRevenue: number;
  totalQuantitySold: number;
}
export interface TopProductItem {
  productId: string;
  productName: string;
  productSlug: string;
  productImage: string;
  categoryName: string;
  brandName: string;
  totalSold: number;
  totalRevenue: number;
  averagePrice: number;
  percentageOfTotalSales: number;
}
@Injectable({ providedIn: 'root' })
export class AdminStatsService {
  private apiUrl = '/api/dashboard/admin';

  constructor(private http: HttpClient) {}

  getDashboardStats(): Observable<any> {
    return this.http.get(this.apiUrl);
  }

  getSalesOverview(startDate?: string, endDate?: string): Observable<any> {
    let url = `${this.apiUrl}/sales`;
    if (startDate && endDate) {
      url += `?startDate=${startDate}&endDate=${endDate}`;
    }
    return this.http.get(url);
  }



  getSalesChart(period: string = 'monthly'): Observable<any> {
    return this.http.get(`${this.apiUrl}/charts/sales?period=${period}`);
  }

  getOrderStatusChart(): Observable<any> {
    return this.http.get(`${this.apiUrl}/charts/order-status`);
  }
}
