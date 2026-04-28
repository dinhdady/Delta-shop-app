import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class AdminCategoryService {
  private apiUrl = '/api/categories';

  constructor(private http: HttpClient) {}

  getCategories(): Observable<any> {
    return this.http.get(this.apiUrl);
  }

  getCategoryTree(): Observable<any> {
    return this.http.get(`${this.apiUrl}/tree`);
  }

  createCategory(category: any): Observable<any> {
    return this.http.post(this.apiUrl, category);
  }

  updateCategory(id: string, category: any): Observable<any> {
    return this.http.put(`${this.apiUrl}/${id}`, category);
  }

  uploadCategoryImage(id: string, file: File): Observable<any> {
    const formData = new FormData();
    formData.append('file', file);  // Key là "file" giống product
    return this.http.post(`${this.apiUrl}/${id}/upload-image`, formData);
  }

  deleteCategoryImage(id: string): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}/image`);
  }

  deleteCategory(id: string): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`);
  }

  toggleStatus(id: string): Observable<any> {
    return this.http.patch(`${this.apiUrl}/${id}/toggle`, {});
  }

  reorderCategories(ids: string[]): Observable<any> {
    return this.http.post(`${this.apiUrl}/reorder`, ids);
  }
}
