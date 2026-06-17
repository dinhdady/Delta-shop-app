import { Injectable, signal } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
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
  private apiUrl = `${environment.apiUrl}/products`;

  constructor(private http: HttpClient) { }

  getProducts(params?: any): Observable<PageResponse<ProductSummary>> {
    let httpParams = new HttpParams();
    if (params) {
      Object.keys(params).forEach(key => {
        if (params[key] !== null && params[key] !== undefined) {
          httpParams = httpParams.set(key, params[key]);
        }
      });
    }
    return this.http.get<PageResponse<ProductSummary>>(this.apiUrl, { params: httpParams }).pipe(
      catchError(() => {
        const filteredProducts = this.getFilteredMockProducts(params);
        return of({
          content: filteredProducts,
          pageNumber: 0,
          pageSize: 10,
          totalElements: filteredProducts.length,
          totalPages: 1,
          first: true,
          last: true
        });
      })
    );
  }

  getProductById(id: string): Observable<ProductDetail> {
    return this.http.get<ProductDetail>(`${this.apiUrl}/id/${id}`).pipe(
      catchError(() => of(this.getMockProductDetail(id)))
    );
  }

  getFeaturedProducts(): Observable<ProductSummary[]> {
    return this.http.get<ProductSummary[]>(`${this.apiUrl}/featured`).pipe(
      catchError(() => of(this.getMockProducts()))
    );
  }

  getCategories(): Observable<any[]> {
    return this.http.get<any[]>(`${environment.apiUrl}/categories`).pipe(
      catchError(() => of([
        { id: '1', name: 'Bóng đá' },
        { id: '2', name: 'Chạy bộ' },
        { id: '3', name: 'Gym & Fitness' }
      ]))
    );
  }

  private getMockProducts(): ProductSummary[] {
    return [
      { id: '1', name: 'Giày Nike Air Zoom Pegasus 40', slug: 'nike-pegasus-40', primaryImage: 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?q=80&w=800&auto=format&fit=crop', basePrice: 2890000, comparePrice: 3500000, discountPercentage: 17, averageRating: 4.8, reviewCount: 120, totalSold: 500, inStock: true },
      { id: '2', name: 'Bóng Đá Adidas World Cup 2022', slug: 'adidas-world-cup', primaryImage: 'https://images.unsplash.com/photo-1614632537190-23e4146777db?q=80&w=800&auto=format&fit=crop', basePrice: 850000, comparePrice: 0, discountPercentage: 0, averageRating: 4.5, reviewCount: 45, totalSold: 230, inStock: true },
      { id: '3', name: 'Áo Tập Gym Under Armour', slug: 'ua-gym-shirt', primaryImage: 'https://images.unsplash.com/photo-1581655353564-df123a1eb820?q=80&w=800&auto=format&fit=crop', basePrice: 550000, comparePrice: 650000, discountPercentage: 15, averageRating: 4.7, reviewCount: 88, totalSold: 150, inStock: true },
      { id: '4', name: 'Tạ Tay Chrome 5kg', slug: 'chrome-dumbbell-5kg', primaryImage: 'https://images.unsplash.com/photo-1586401100295-7a8096fd231a?q=80&w=800&auto=format&fit=crop', basePrice: 350000, comparePrice: 0, discountPercentage: 0, averageRating: 4.9, reviewCount: 32, totalSold: 90, inStock: true }
    ];
  }

  private getFilteredMockProducts(params?: any): ProductSummary[] {
    let products = this.getMockProducts();
    const keyword = String(params?.keyword || '').trim().toLowerCase();

    if (keyword) {
      products = products.filter(product =>
        product.name.toLowerCase().includes(keyword) ||
        product.slug.toLowerCase().includes(keyword)
      );
    }

    if (params?.categoryId) {
      const categoryKeywords: Record<string, string[]> = {
        '1': ['bóng', 'adidas'],
        '2': ['giày', 'nike', 'pegasus'],
        '3': ['gym', 'tạ', 'under armour']
      };
      const keywords = categoryKeywords[String(params.categoryId)] || [];
      if (keywords.length > 0) {
        products = products.filter(product => {
          const haystack = `${product.name} ${product.slug}`.toLowerCase();
          return keywords.some(term => haystack.includes(term));
        });
      }
    }

    return products;
  }

  private getMockProductDetail(id: string): ProductDetail {
    const products = this.getMockProducts();
    const product = products.find(p => p.id === id) || products[0];
    
    return {
      product: {
        ...product,
        sku: 'SKU-' + id,
        category: { id: '1', name: 'Thể thao' },
        brand: { id: '1', name: 'Delta' },
        shortDescription: 'Sản phẩm chất lượng cao cho vận động viên chuyên nghiệp.',
        description: 'Đây là mô tả chi tiết của sản phẩm. Được thiết kế để mang lại hiệu suất tối đa và sự thoải mái cho người dùng trong mọi điều kiện tập luyện.',
        images: [product.primaryImage, 'https://images.unsplash.com/photo-1608231387042-66d1773070a5?q=80&w=1000&auto=format&fit=crop'],
        featured: true
      },
      variants: [
        { id: 'v1', name: 'Size 40', availableQuantity: 10, finalPrice: product.basePrice, inStock: true },
        { id: 'v2', name: 'Size 41', availableQuantity: 5, finalPrice: product.basePrice, inStock: true },
        { id: 'v3', name: 'Size 42', availableQuantity: 0, finalPrice: product.basePrice, inStock: false }
      ],
      galleryImages: [product.primaryImage, 'https://images.unsplash.com/photo-1608231387042-66d1773070a5?q=80&w=1000&auto=format&fit=crop'],
      relatedProducts: products.filter(p => p.id !== id)
    };
  }
}
