import { Injectable, signal } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface ProductSummary {
  id: string;
  name: string;
  slug: string;
  primaryImage: string;
  basePrice: number;
  comparePrice: number;
  discountPercentage: number;
  averageRating: number;
  reviewCount: number;
  totalSold: number;
  inStock: boolean;
}

export interface Product {
  id: string;
  name: string;
  slug: string;
  sku: string;
  category: { id: string, name: string };
  brand: { id: string, name: string };
  shortDescription: string;
  description: string;
  basePrice: number;
  comparePrice: number;
  discountPercentage: number;
  averageRating: number;
  reviewCount: number;
  images: string[];
  featured: boolean;
}

export interface ProductDetail {
  product: Product;
  variants: any[];
  galleryImages: string[];
  relatedProducts: ProductSummary[];
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

@Injectable({
  providedIn: 'root'
})
export class ProductService {
  private apiUrl = '/api/products';

  constructor(private http: HttpClient) { }

  getProducts(params?: any): Observable<PageResponse<ProductSummary>> {
    let httpParams = new HttpParams();
    if (params) {
      Object.keys(params).forEach(key => {
        const value = params[key];
        if (value !== null && value !== undefined && value !== '' && value !== 'null' && value !== 'undefined') {
          httpParams = httpParams.set(key, String(value).trim());
        }
      });
    }
    const hasKeyword = params?.keyword && params.keyword !== 'null' && params.keyword !== 'undefined';
    const endpoint = hasKeyword ? `${this.apiUrl}/search` : this.apiUrl;
    return this.http.get<PageResponse<ProductSummary>>(endpoint, { params: httpParams });
  }

  getProductById(id: string): Observable<ProductDetail> {
    return this.http.get<ProductDetail>(`${this.apiUrl}/id/${id}`);
  }

  getFeaturedProducts(): Observable<ProductSummary[]> {
    return this.http.get<ProductSummary[]>(`${this.apiUrl}/featured`);
  }

  getCategories(): Observable<any[]> {
    return this.http.get<any[]>(`${environment.apiUrl}/categories`);
  }
}
