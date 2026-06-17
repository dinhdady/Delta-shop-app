import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface PromotionValidation {
  valid: boolean;
  code: string;
  name?: string;
  message?: string;
  discountAmount?: number;
  discountType?: string;
  minOrderAmount?: number;
  remainingUses?: number;
  stackable?: boolean;
}

@Injectable({ providedIn: 'root' })
export class PromotionService {
  private apiUrl = '/api/promotions';

  constructor(private http: HttpClient) {}

  validate(code: string, subtotal: number): Observable<PromotionValidation> {
    const params = new HttpParams()
      .set('code', code.trim())
      .set('subtotal', String(subtotal));
    return this.http.get<PromotionValidation>(`${this.apiUrl}/validate`, { params });
  }
}
