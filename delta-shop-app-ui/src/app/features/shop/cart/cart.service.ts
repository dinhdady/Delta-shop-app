import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

export interface AddToCartRequest {
  variantId: string;
  quantity: number;
  selectedSize?: string;
  selectedSizeLabel?: string;
  selectedSizeMeasurement?: string;
}

export interface CartItem {
  id: string;
  variantId: string;
  productId?: string;
  productName: string;
  variantName?: string;
  unitPrice: number;
  quantity: number;
  subtotal: number;
  productImage?: string;
  availableStock?: number;
  selectedSize?: string;
  selectedSizeLabel?: string;
  selectedSizeMeasurement?: string;
}

@Injectable({ providedIn: 'root' })
export class CartService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8080/api/cart';

  cartItems = signal<CartItem[]>([]);
  cartTotal = signal<number>(0);

  addToCart(request: AddToCartRequest): Observable<any> {
    return this.http.post(`${this.apiUrl}/items`, request).pipe(
      tap(() => this.loadCart())
    );
  }

  loadCart(): void {
    this.http.get<any>(this.apiUrl).subscribe({
      next: (response) => {
        this.cartItems.set(response.items || []);
        this.cartTotal.set(response.total || 0);
      },
      error: (err) => console.error('Error loading cart:', err)
    });
  }

  updateQuantity(cartItemId: string, quantity: number): Observable<any> {
    return this.http.put(`${this.apiUrl}/items`, { cartItemId, quantity }).pipe(
      tap(() => this.loadCart())
    );
  }

  removeFromCart(cartItemId: string): Observable<any> {
    return this.http.delete(`${this.apiUrl}/items/${cartItemId}`).pipe(
      tap(() => this.loadCart())
    );
  }

  clearCart(): Observable<any> {
    return this.http.delete(`${this.apiUrl}/clear`).pipe(
      tap(() => this.loadCart())
    );
  }
}
