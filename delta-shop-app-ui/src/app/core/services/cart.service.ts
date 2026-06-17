import { Injectable, signal, computed, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthService } from './auth.service';

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
  // THÊM CÁC TRƯỜNG SIZE
  selectedSize?: string;
  selectedSizeLabel?: string;
  selectedSizeMeasurement?: string;
}

export interface CartResponse {
  items: CartItem[];
  totalItems: number;
  subtotal: number;
  discount: number;
  total: number;
}

@Injectable({
  providedIn: 'root'
})
export class CartService {
  private http = inject(HttpClient);
  private authService = inject(AuthService);
  private apiUrl = `${environment.apiUrl}/cart`;

  // Use Signal to manage cart state
  public cartItems = signal<CartItem[]>([]);
  public cartTotal = signal<number>(0);
  public cartCount = computed(() => this.cartItems().reduce((acc, item) => acc + item.quantity, 0));

  constructor() {
    this.loadCart();
  }

  private loadCart() {
    if (this.authService.isAuthenticated()) {
      this.mergeGuestCart().subscribe(cart => {
        this.cartItems.set(cart.items || []);
        this.cartTotal.set(cart.total);
      });
    } else {
      this.getGuestCart().subscribe(cart => this.applyCart(cart));
    }
  }

  getCart(): Observable<CartResponse> {
    return this.http.get<CartResponse>(this.apiUrl).pipe(
      tap(cart => this.applyCart(cart))
    );
  }

  addToCart(request: { variantId: string, quantity: number }): Observable<CartResponse> {
    if (this.authService.isAuthenticated()) {
      return this.http.post<CartResponse>(`${this.apiUrl}/items`, request).pipe(
        tap(cart => {
          this.cartItems.set(cart.items || []);
          this.cartTotal.set(cart.total);
        })
      );
    } else {
      return this.http.post<CartResponse>(`${this.apiUrl}/guest/items`, request, { withCredentials: true }).pipe(
        tap(cart => this.applyCart(cart))
      );
    }
  }

  updateQuantity(cartItemId: string, quantity: number): Observable<CartResponse> {
    const url = this.authService.isAuthenticated() ? `${this.apiUrl}/items` : `${this.apiUrl}/guest/items`;
    return this.http.put<CartResponse>(url, { cartItemId, quantity }, { withCredentials: true }).pipe(
      tap(cart => this.applyCart(cart))
    );
  }

  removeFromCart(cartItemId: string): Observable<CartResponse> {
    const url = this.authService.isAuthenticated()
      ? `${this.apiUrl}/items/${cartItemId}`
      : `${this.apiUrl}/guest/items/${cartItemId}`;
    return this.http.delete<CartResponse>(url, { withCredentials: true }).pipe(
      tap(cart => this.applyCart(cart))
    );
  }

  clearCart() {
    if (this.authService.isAuthenticated()) {
      this.http.delete(`${this.apiUrl}/clear`).subscribe(() => {
        this.cartItems.set([]);
        this.cartTotal.set(0);
      });
    } else {
      this.http.delete(`${this.apiUrl}/guest/clear`, { withCredentials: true }).subscribe(() => {
        this.cartItems.set([]);
        this.cartTotal.set(0);
      });
    }
  }

  getGuestCart(): Observable<CartResponse> {
    return this.http.get<CartResponse>(`${this.apiUrl}/guest`, { withCredentials: true });
  }

  mergeGuestCart(): Observable<CartResponse> {
    return this.http.post<CartResponse>(`${this.apiUrl}/merge`, {}, { withCredentials: true }).pipe(
      tap(cart => this.applyCart(cart))
    );
  }

  syncCart(cart: CartResponse): void {
    this.applyCart(cart);
  }

  private applyCart(cart: CartResponse): void {
    this.cartItems.set(cart.items || []);
    this.cartTotal.set(cart.total || 0);
  }
}
