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
      this.getCart().subscribe(cart => {
        this.cartItems.set(cart.items || []);
        this.cartTotal.set(cart.total);
      });
    } else {
      const savedCart = localStorage.getItem('cart');
      if (savedCart) {
        this.cartItems.set(JSON.parse(savedCart));
      }
    }
  }

  getCart(): Observable<CartResponse> {
    return this.http.get<CartResponse>(this.apiUrl);
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
      // Logic for guest cart can be added here
      return new Observable(observer => {
        // Mock logic for guest
        observer.error('Please login to add to cart');
      });
    }
  }

  updateQuantity(cartItemId: string, quantity: number): Observable<CartResponse> {
    return this.http.put<CartResponse>(`${this.apiUrl}/items`, { cartItemId, quantity }).pipe(
      tap(cart => {
        this.cartItems.set(cart.items || []);
        this.cartTotal.set(cart.total);
      })
    );
  }

  removeFromCart(cartItemId: string): Observable<CartResponse> {
    return this.http.delete<CartResponse>(`${this.apiUrl}/items/${cartItemId}`).pipe(
      tap(cart => {
        this.cartItems.set(cart.items || []);
        this.cartTotal.set(cart.total);
      })
    );
  }

  clearCart() {
    if (this.authService.isAuthenticated()) {
      this.http.delete(`${this.apiUrl}/clear`).subscribe(() => {
        this.cartItems.set([]);
        this.cartTotal.set(0);
      });
    } else {
      this.cartItems.set([]);
      this.cartTotal.set(0);
      localStorage.removeItem('cart');
    }
  }
}
