import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class BrandService {
  private apiUrl = '/api/brands';

  constructor(private http: HttpClient) {}

  getActiveBrands(): Observable<any> {
    return this.http.get(`${this.apiUrl}/active`);
  }

  getFeaturedBrands(): Observable<any> {
    return this.http.get(`${this.apiUrl}/featured`);
  }
}
