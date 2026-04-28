// category.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface CategoryResponse {
  id: string;
  name: string;
  slug: string;
  description: string;
  imageUrl: string;
  iconClass: string;
  sortOrder: number;
  active: boolean;
  parentId: string | null;
  parentName: string | null;
  children: CategoryResponse[];
  productCount?: number;
}

export interface PageResponse<T> {
  content: T[];
  pageNo: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class CategoryService {
  private apiUrl = `${environment.apiUrl}/categories`;

  constructor(private http: HttpClient) {}

  getAllCategories(): Observable<CategoryResponse[]> {
    return this.http.get<CategoryResponse[]>(this.apiUrl);
  }

  getActiveCategories(): Observable<CategoryResponse[]> {
    return this.http.get<CategoryResponse[]>(`${this.apiUrl}/active`);
  }

  getCategoryTree(): Observable<CategoryResponse[]> {
    return this.http.get<CategoryResponse[]>(`${this.apiUrl}/tree`);
  }

  getPaginatedCategories(page: number, size: number, sortBy?: string, sortDir?: string): Observable<PageResponse<CategoryResponse>> {
    let params: any = { page, size };
    if (sortBy) params.sortBy = sortBy;
    params.sortDir = sortDir || 'asc';
    return this.http.get<PageResponse<CategoryResponse>>(`${this.apiUrl}/paginated`, { params });
  }

  getCategoryById(id: string): Observable<CategoryResponse> {
    return this.http.get<CategoryResponse>(`${this.apiUrl}/${id}`);
  }

  getCategoryBySlug(slug: string): Observable<CategoryResponse> {
    return this.http.get<CategoryResponse>(`${this.apiUrl}/slug/${slug}`);
  }

  getSubcategories(parentId: string): Observable<CategoryResponse[]> {
    return this.http.get<CategoryResponse[]>(`${this.apiUrl}/${parentId}/subcategories`);
  }
}
