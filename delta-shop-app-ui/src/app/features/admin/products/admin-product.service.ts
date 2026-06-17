import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class AdminProductService {
  private apiUrl = '/api/products';

  constructor(private http: HttpClient) {}

  getProducts(page: number, size: number, keyword?: string, status?: string): Observable<any> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (keyword) params = params.set('keyword', keyword);
    if (status) params = params.set('status', status);
    return this.http.get(this.apiUrl, { params });
  }

  getProductById(id: string): Observable<any> {
    return this.http.get(`${this.apiUrl}/id/${id}`);
  }

  createProduct(product: any): Observable<any> {
    return this.http.post(this.apiUrl, product);
  }

  updateProduct(id: string, product: any): Observable<any> {
    return this.http.put(`${this.apiUrl}/${id}`, product);
  }

  uploadProductImages(files: File[]): Observable<any[]> {
    const formData = new FormData();
    files.forEach(file => formData.append('files', file));
    formData.append('folder', 'products');
    return this.http.post<any[]>('/api/files/upload/multiple/images', formData);
  }

  deleteProduct(id: string): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`);
  }

  updateStatus(id: string, status: string): Observable<any> {
    return this.http.patch(`${this.apiUrl}/${id}/status?status=${status}`, {});
  }

  updateFeatured(id: string, featured: boolean): Observable<any> {
    return this.http.patch(`${this.apiUrl}/${id}/featured?featured=${featured}`, {});
  }

  adjustStock(id: string, stockQuantity: number, note?: string): Observable<any> {
    return this.http.patch(`${this.apiUrl}/${id}/stock`, { stockQuantity, note });
  }
}
