import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class AdminBrandService {
  private apiUrl = '/api/brands';
  private fileApiUrl = '/api/files';  // Thêm API cho file

  constructor(private http: HttpClient) {}

  getBrands(page: number, size: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/paginated?page=${page}&size=${size}`);
  }

  getAllBrands(): Observable<any> {
    return this.http.get(this.apiUrl);
  }

  createBrand(brand: any): Observable<any> {
    return this.http.post(this.apiUrl, brand);
  }

  updateBrand(id: string, brand: any): Observable<any> {
    return this.http.put(`${this.apiUrl}/${id}`, brand);
  }

  deleteBrand(id: string): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`);
  }

  toggleStatus(id: string): Observable<any> {
    return this.http.patch(`${this.apiUrl}/${id}/status`, {});
  }

  toggleFeatured(id: string): Observable<any> {
    return this.http.patch(`${this.apiUrl}/${id}/featured`, {});
  }

  // Upload logo - Sử dụng đúng endpoint từ FileController
  uploadLogo(file: File, folder: string = 'brands'): Observable<any> {
    const formData = new FormData();
    formData.append('file', file);
    if (folder) {
      formData.append('folder', folder);
    }
    // Sử dụng endpoint /files/upload/image (upload image chuyên biệt)
    return this.http.post(`${this.fileApiUrl}/upload/image`, formData);
  }
}
