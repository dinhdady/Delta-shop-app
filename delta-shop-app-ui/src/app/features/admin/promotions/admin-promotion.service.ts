import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export type DiscountType = 'PERCENTAGE' | 'FIXED_AMOUNT' | 'FREE_SHIPPING' | 'BUY_X_GET_Y';

export interface Promotion {
  id: string;
  name: string;
  code: string;
  description?: string;
  type: DiscountType;
  value: number;
  minOrderAmount?: number;
  maxDiscountAmount?: number;
  usageLimit?: number;
  usagePerUser?: number;
  usedCount?: number;
  appliesTo?: string;
  startsAt: string;
  endsAt?: string;
  active: boolean;
  stackable: boolean;
  createdAt?: string;
}

export interface PageResponse<T> {
  content: T[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
}

@Injectable({ providedIn: 'root' })
export class AdminPromotionService {
  private apiUrl = '/api/promotions';

  constructor(private http: HttpClient) {}

  getPromotions(page = 0, size = 20): Observable<PageResponse<Promotion>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<PageResponse<Promotion>>(this.apiUrl, { params });
  }

  createPromotion(payload: any): Observable<Promotion> {
    return this.http.post<Promotion>(this.apiUrl, payload);
  }

  updatePromotion(id: string, payload: any): Observable<Promotion> {
    return this.http.put<Promotion>(`${this.apiUrl}/${id}`, payload);
  }

  activatePromotion(id: string): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/${id}/activate`, {});
  }

  deactivatePromotion(id: string): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/${id}/deactivate`, {});
  }

  deletePromotion(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
