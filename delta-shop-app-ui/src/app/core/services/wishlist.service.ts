import { Injectable, signal } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { PageResponse, ProductSummary } from './product.service';
import { CartResponse, CartService } from './cart.service';

export interface WishlistSummary {
  userId: string;
  totalItems: number;
  hasItems: boolean;
  productIds: string[];
}

@Injectable({ providedIn: 'root' })
export class WishlistService {
  private apiUrl = '/api/wishlist';
  public wishlistCount = signal(0);
  public productIds = signal<Set<string>>(new Set());

  constructor(private http: HttpClient, private cartService: CartService) {}

  loadSummary(): void {
    this.getSummary().subscribe({
      next: summary => this.applySummary(summary),
      error: () => {
        this.wishlistCount.set(0);
        this.productIds.set(new Set());
      }
    });
  }

  getWishlist(page = 0, size = 20): Observable<PageResponse<ProductSummary>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<PageResponse<ProductSummary>>(this.apiUrl, { params });
  }

  getSummary(): Observable<WishlistSummary> {
    return this.http.get<WishlistSummary>(`${this.apiUrl}/summary`);
  }

  add(productId: string): Observable<WishlistSummary> {
    return this.http.post<WishlistSummary>(`${this.apiUrl}/${productId}`, {}).pipe(
      tap(summary => this.applySummary(summary))
    );
  }

  remove(productId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${productId}`).pipe(
      tap(() => {
        const nextIds = new Set(this.productIds());
        nextIds.delete(productId);
        this.productIds.set(nextIds);
        this.wishlistCount.set(Math.max(0, this.wishlistCount() - 1));
      })
    );
  }

  clear(): Observable<void> {
    return this.http.delete<void>(this.apiUrl).pipe(
      tap(() => {
        this.productIds.set(new Set());
        this.wishlistCount.set(0);
      })
    );
  }

  moveToCart(productId: string): Observable<CartResponse> {
    return this.http.post<CartResponse>(`${this.apiUrl}/${productId}/move-to-cart`, {}).pipe(
      tap(cart => this.cartService.syncCart(cart))
    );
  }

  moveAllToCart(): Observable<CartResponse> {
    return this.http.post<CartResponse>(`${this.apiUrl}/move-all-to-cart`, {}).pipe(
      tap(cart => {
        this.cartService.syncCart(cart);
      })
    );
  }

  isInWishlist(productId: string): boolean {
    return this.productIds().has(productId);
  }

  private applySummary(summary: WishlistSummary): void {
    this.wishlistCount.set(summary.totalItems || 0);
    this.productIds.set(new Set(summary.productIds || []));
  }
}
