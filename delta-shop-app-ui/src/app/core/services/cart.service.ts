import { Injectable, signal, computed, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of, tap } from 'rxjs';
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
  private mockCartKey = 'mockAdminCart';

  // Use Signal to manage cart state
  public cartItems = signal<CartItem[]>([]);
  public cartTotal = signal<number>(0);
  public cartCount = computed(() => this.cartItems().reduce((acc, item) => acc + item.quantity, 0));

  constructor() {
    this.loadCart();
  }

  private loadCart() {
    if (this.isMockSession()) {
      this.applyCart(this.readMockCart());
      return;
    }

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
    if (this.isMockSession()) {
      return of(this.readMockCart());
    }
    return this.http.get<CartResponse>(this.apiUrl);
  }

  addToCart(request: { variantId: string, quantity: number }): Observable<CartResponse> {
    if (this.isMockSession()) {
      const cart = this.addMockItem(request.variantId, request.quantity);
      this.saveAndApplyMockCart(cart);
      return of(cart);
    }

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
    if (this.isMockSession()) {
      const cart = this.readMockCart();
      const item = cart.items.find(cartItem => cartItem.id === cartItemId);
      if (item) {
        item.quantity = Math.max(1, quantity);
        item.subtotal = item.unitPrice * item.quantity;
      }
      const nextCart = this.buildCartResponse(cart.items);
      this.saveAndApplyMockCart(nextCart);
      return of(nextCart);
    }

    return this.http.put<CartResponse>(`${this.apiUrl}/items`, { cartItemId, quantity }).pipe(
      tap(cart => {
        this.cartItems.set(cart.items || []);
        this.cartTotal.set(cart.total);
      })
    );
  }

  removeFromCart(cartItemId: string): Observable<CartResponse> {
    if (this.isMockSession()) {
      const cart = this.readMockCart();
      const nextCart = this.buildCartResponse(cart.items.filter(item => item.id !== cartItemId));
      this.saveAndApplyMockCart(nextCart);
      return of(nextCart);
    }

    return this.http.delete<CartResponse>(`${this.apiUrl}/items/${cartItemId}`).pipe(
      tap(cart => {
        this.cartItems.set(cart.items || []);
        this.cartTotal.set(cart.total);
      })
    );
  }

  clearCart() {
    if (this.isMockSession()) {
      localStorage.removeItem(this.mockCartKey);
      this.applyCart(this.buildCartResponse([]));
      return;
    }

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

  private isMockSession(): boolean {
    return this.authService.getToken() === 'mock-admin-access-token';
  }

  private addMockItem(variantId: string, quantity: number): CartResponse {
    const cart = this.readMockCart();
    const product = this.getMockProductByVariant(variantId);
    const existing = cart.items.find(item => item.variantId === variantId);

    if (existing) {
      existing.quantity += quantity;
      existing.subtotal = existing.unitPrice * existing.quantity;
      return this.buildCartResponse(cart.items);
    }

    const item: CartItem = {
      id: `mock-cart-${Date.now()}`,
      variantId,
      productId: product.productId,
      productName: product.productName,
      variantName: product.variantName,
      unitPrice: product.unitPrice,
      quantity,
      subtotal: product.unitPrice * quantity,
      productImage: product.productImage,
      availableStock: 99,
      selectedSize: product.variantName,
      selectedSizeLabel: product.variantName
    };

    return this.buildCartResponse([...cart.items, item]);
  }

  private getMockProductByVariant(variantId: string): {
    productId: string;
    productName: string;
    variantName: string;
    unitPrice: number;
    productImage: string;
  } {
    const catalog: Record<string, any> = {
      'v1': {
        productId: '1',
        productName: 'Giày Nike Air Zoom Pegasus 40',
        variantName: 'Size 40',
        unitPrice: 2890000,
        productImage: 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?q=80&w=800&auto=format&fit=crop'
      },
      'v2': {
        productId: '1',
        productName: 'Giày Nike Air Zoom Pegasus 40',
        variantName: 'Size 41',
        unitPrice: 2890000,
        productImage: 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?q=80&w=800&auto=format&fit=crop'
      },
      'v3': {
        productId: '1',
        productName: 'Giày Nike Air Zoom Pegasus 40',
        variantName: 'Size 42',
        unitPrice: 2890000,
        productImage: 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?q=80&w=800&auto=format&fit=crop'
      }
    };

    if (catalog[variantId]) {
      return catalog[variantId];
    }

    const match = variantId.match(/^mock-variant-(.+)-([a-z0-9]+)$/i);
    const productId = match?.[1] || '1';
    const size = match?.[2]?.toUpperCase() || 'M';
    const mockProducts: Record<string, any> = {
      '1': {
        productName: 'Giày Nike Air Zoom Pegasus 40',
        unitPrice: 2890000,
        productImage: 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?q=80&w=800&auto=format&fit=crop'
      },
      '2': {
        productName: 'Bóng Đá Adidas World Cup 2022',
        unitPrice: 850000,
        productImage: 'https://images.unsplash.com/photo-1614632537190-23e4146777db?q=80&w=800&auto=format&fit=crop'
      },
      '3': {
        productName: 'Áo Tập Gym Under Armour',
        unitPrice: 550000,
        productImage: 'https://images.unsplash.com/photo-1581655353564-df123a1eb820?q=80&w=800&auto=format&fit=crop'
      },
      '4': {
        productName: 'Tạ Tay Chrome 5kg',
        unitPrice: 350000,
        productImage: 'https://images.unsplash.com/photo-1586401100295-7a8096fd231a?q=80&w=800&auto=format&fit=crop'
      }
    };
    const product = mockProducts[productId] || mockProducts['1'];

    return {
      productId,
      productName: product.productName,
      variantName: `Size ${size}`,
      unitPrice: product.unitPrice,
      productImage: product.productImage
    };
  }

  private readMockCart(): CartResponse {
    const savedCart = localStorage.getItem(this.mockCartKey);
    if (!savedCart) {
      return this.buildCartResponse([]);
    }

    try {
      const parsed = JSON.parse(savedCart);
      return this.buildCartResponse(parsed.items || []);
    } catch {
      return this.buildCartResponse([]);
    }
  }

  private saveAndApplyMockCart(cart: CartResponse): void {
    localStorage.setItem(this.mockCartKey, JSON.stringify(cart));
    this.applyCart(cart);
  }

  private applyCart(cart: CartResponse): void {
    this.cartItems.set(cart.items || []);
    this.cartTotal.set(cart.total);
  }

  private buildCartResponse(items: CartItem[]): CartResponse {
    const subtotal = items.reduce((sum, item) => sum + item.unitPrice * item.quantity, 0);
    return {
      items: items.map(item => ({
        ...item,
        subtotal: item.unitPrice * item.quantity
      })),
      totalItems: items.reduce((sum, item) => sum + item.quantity, 0),
      subtotal,
      discount: 0,
      total: subtotal
    };
  }
}
