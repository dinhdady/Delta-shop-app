import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class ShopProductService {
  private apiUrl = '/api/products';

  constructor(private http: HttpClient) {}

  getProducts(params: any): Observable<any> {
    let httpParams = new HttpParams()
      .set('page', params.page || 0)
      .set('size', params.size || 12);
    if (params.keyword) httpParams = httpParams.set('keyword', params.keyword);
    if (params.categoryId) httpParams = httpParams.set('categoryId', params.categoryId);
    if (params.brandId) httpParams = httpParams.set('brandId', params.brandId);
    if (params.minPrice) httpParams = httpParams.set('minPrice', params.minPrice);
    if (params.maxPrice) httpParams = httpParams.set('maxPrice', params.maxPrice);
    if (params.sortBy) httpParams = httpParams.set('sortBy', params.sortBy);
    if (params.sortDir) httpParams = httpParams.set('sortDir', params.sortDir);
    return this.http.get(this.apiUrl, { params: httpParams });
  }

  getFeatured(limit: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/featured?limit=${limit}`);
  }

  getNewArrivals(limit: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/new-arrivals?limit=${limit}`);
  }

  getBestSellers(limit: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/best-sellers?limit=${limit}`);
  }

  getProductBySlug(slug: string): Observable<any> {
    return this.http.get(`${this.apiUrl}/${slug}`);
  }

  getRelatedProducts(productId: string, limit: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/${productId}/related?limit=${limit}`);
  }
}
