// navbar.component.ts - Fixed version
import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { LucideAngularModule, Search, ShoppingCart, User, X } from 'lucide-angular';
import { CartService } from '../../../core/services/cart.service';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    FormsModule,
    LucideAngularModule
  ],
  template: `
    <header class="navbar">
      <div class="container navbar-container">
        <!-- Logo with Image -->
        <a routerLink="/" class="logo">
          <img src="assets/images/logo.png" alt="Delta Sports" class="logo-img" (error)="logoError = true">
          <span class="logo-text">DELTA<span>SPORTS</span></span>
        </a>

        <!-- Navigation Links -->
        <nav class="nav-links">
          <a routerLink="/" routerLinkActive="active" [routerLinkActiveOptions]="{exact: true}">Trang chủ</a>
          <a routerLink="/products" routerLinkActive="active">Sản phẩm</a>
          <a routerLink="/categories" routerLinkActive="active">Danh mục</a>
          <a routerLink="/about" routerLinkActive="active">Giới thiệu</a>
          <a routerLink="/contact" routerLinkActive="active">Liên hệ</a>
        </nav>

        <!-- Actions -->
        <div class="nav-actions">
          <form class="search-form" [class.open]="searchOpen" (ngSubmit)="submitSearch()">
            @if (searchOpen) {
              <input
                type="search"
                name="keyword"
                [(ngModel)]="searchKeyword"
                placeholder="Tìm sản phẩm..."
                aria-label="Tìm sản phẩm"
                autocomplete="off"
                (keydown.escape)="closeSearch()">
            }
            @if (!searchOpen) {
              <button type="button" class="icon-btn" aria-label="Mở tìm kiếm" (click)="toggleSearch()">
                <lucide-icon name="search"></lucide-icon>
              </button>
            } @else {
              <button type="submit" class="icon-btn" aria-label="Tìm kiếm">
                <lucide-icon name="search"></lucide-icon>
              </button>
              <button type="button" class="icon-btn" aria-label="Đóng tìm kiếm" (click)="closeSearch()">
                <lucide-icon name="x"></lucide-icon>
              </button>
            }
          </form>

          <a routerLink="/cart" class="icon-btn cart-btn" aria-label="Cart">
            <lucide-icon name="shopping-cart"></lucide-icon>
            @if (cartCount() > 0) {
              <span class="badge">{{ cartCount() }}</span>
            }
          </a>

          @if (isAuthenticated() && currentUser()) {
            <div class="user-menu">
              <button class="user-trigger" (click)="toggleDropdown($event)">
                <lucide-icon name="user"></lucide-icon>
                <span class="user-name">{{ getUserFirstName() }}</span>
              </button>
              <div class="dropdown" [class.show]="dropdownOpen">
                <div class="user-info">
                  <strong>{{ currentUser()?.fullName }}</strong>
                  <span>{{ currentUser()?.email }}</span>
                </div>
                <hr>
                <a routerLink="/profile" (click)="closeDropdown()">Tài khoản</a>
                <a routerLink="/orders" (click)="closeDropdown()">Đơn hàng</a>
                @if (authService.isAdmin()) {
                  <a routerLink="/admin/dashboard" (click)="closeDropdown()">Quản trị</a>
                }
                <button (click)="logout()">Đăng xuất</button>
              </div>
            </div>
          } @else {
            <a routerLink="/auth/login" class="btn btn-primary login-btn">Đăng nhập</a>
          }
        </div>
      </div>
    </header>
  `,
  styles: [`
    .navbar {
      background-color: #000000;
      color: #ffffff;
      position: sticky;
      top: 0;
      z-index: 1000;
      border-bottom: 1px solid rgba(205, 70, 49, 0.3);
    }

    .navbar-container {
      display: flex;
      align-items: center;
      justify-content: space-between;
      height: 70px;
      max-width: 1200px;
      margin: 0 auto;
      padding: 0 20px;
      gap: 2rem;
    }

    /* Logo Styles */
    .logo {
      display: flex;
      align-items: center;
      gap: 10px;
      text-decoration: none;
      font-family: var(--font-heading);
      font-size: 1.5rem;
      font-weight: 800;
      letter-spacing: 1px;
      flex-shrink: 0;
    }

    .logo-img {
      width: 40px;
      height: 40px;
      object-fit: contain;
      display: block;
    }

    .logo-text {
      color: #ffffff;
      font-size: 1.25rem;
      font-weight: 700;
    }

    .logo-text span {
      color: #cd4631;
    }

    /* Navigation Links */
    .nav-links {
      display: flex;
      gap: clamp(0.75rem, 1.8vw, 2rem);
      flex: 1;
      justify-content: center;
    }

    .nav-links a {
      color: #ffffff;
      text-decoration: none;
      font-weight: 600;
      font-size: 0.9rem;
      text-transform: uppercase;
      position: relative;
      padding: 0.5rem 0;
      transition: color 0.2s ease;
    }

    .nav-links a::after {
      content: '';
      position: absolute;
      bottom: 0;
      left: 0;
      width: 0;
      height: 2px;
      background-color: #cd4631;
      transition: width 0.2s ease;
    }

    .nav-links a:hover::after,
    .nav-links a.active::after {
      width: 100%;
    }

    .nav-links a:hover,
    .nav-links a.active {
      color: #cd4631;
    }

    /* Actions */
    .nav-actions {
      display: flex;
      align-items: center;
      gap: clamp(0.75rem, 1.5vw, 1.5rem);
      flex-shrink: 0;
    }

    .search-form {
      display: flex;
      align-items: center;
      gap: 0.25rem;
    }

    .search-form.open {
      background: rgba(255, 255, 255, 0.08);
      border: 1px solid rgba(255, 255, 255, 0.14);
      border-radius: 8px;
      padding: 2px 4px 2px 10px;
    }

    .search-form input {
      width: clamp(160px, 20vw, 260px);
      border: 0;
      outline: 0;
      background: transparent;
      color: #ffffff;
      font-size: 0.9rem;
    }

    .search-form input::placeholder {
      color: #b8b8b8;
    }

    .icon-btn {
      background: none;
      border: none;
      color: #ffffff;
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: all 0.2s ease;
      padding: 8px;
      border-radius: 8px;
    }

    .icon-btn:hover {
      color: #cd4631;
      background: rgba(205, 70, 49, 0.1);
    }

    .cart-btn {
      position: relative;
    }

    .badge {
      position: absolute;
      top: -4px;
      right: -4px;
      background-color: #cd4631;
      color: white;
      font-size: 0.65rem;
      min-width: 18px;
      height: 18px;
      border-radius: 9px;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 0 4px;
      font-weight: 600;
    }

    .login-btn {
      padding: 0.5rem 1.25rem;
      font-size: 0.85rem;
      background: #cd4631;
      color: white;
      border-radius: 30px;
      text-decoration: none;
      transition: all 0.2s ease;
    }

    .login-btn:hover {
      background: #b83a26;
      transform: translateY(-1px);
    }

    /* User Menu - Fixed hover */
    .user-menu {
      position: relative;
    }

    .user-trigger {
      display: flex;
      align-items: center;
      gap: 8px;
      background: transparent;
      border: none;
      color: #ffffff;
      cursor: pointer;
      padding: 8px 12px;
      border-radius: 30px;
      transition: all 0.2s ease;
      font-size: 0.85rem;
    }

    .user-trigger:hover {
      background: rgba(205, 70, 49, 0.15);
      color: #cd4631;
    }

    .user-name {
      font-weight: 500;
    }

    .dropdown {
      position: absolute;
      top: calc(100% + 8px);
      right: 0;
      background-color: white;
      min-width: 220px;
      border-radius: 12px;
      overflow: hidden;
      z-index: 1001;
      opacity: 0;
      visibility: hidden;
      transform: translateY(-10px);
      transition: all 0.2s ease;
      box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
    }

    .dropdown.show {
      opacity: 1;
      visibility: visible;
      transform: translateY(0);
    }

    .dropdown::before {
      content: '';
      position: absolute;
      top: -6px;
      right: 16px;
      width: 12px;
      height: 12px;
      background-color: white;
      transform: rotate(45deg);
    }

    .user-info {
      padding: 12px 16px;
      display: flex;
      flex-direction: column;
      gap: 4px;
    }

    .user-info strong {
      font-size: 0.875rem;
      color: #1a1a1a;
    }

    .user-info span {
      font-size: 0.75rem;
      color: #666;
    }

    .dropdown hr {
      margin: 0;
      border: none;
      border-top: 1px solid #eee;
    }

    .dropdown a,
    .dropdown button {
      display: block;
      width: 100%;
      text-align: left;
      padding: 10px 16px;
      color: #333;
      background: none;
      border: none;
      font-size: 0.875rem;
      cursor: pointer;
      text-decoration: none;
      transition: all 0.2s ease;
    }

    .dropdown a:hover,
    .dropdown button:hover {
      background-color: #f5f5f5;
      color: #cd4631;
    }

    /* Responsive */
    @media (max-width: 768px) {
      .navbar-container {
        height: 60px;
        padding: 0 16px;
        gap: 1rem;
      }

      .logo-text {
        font-size: 1rem;
      }

      .logo-img {
        width: 32px;
        height: 32px;
      }

      .nav-links {
        display: none;
      }

      .search-form.open {
        position: absolute;
        left: 16px;
        right: 16px;
        top: 10px;
        background: #111111;
        border-color: rgba(205, 70, 49, 0.35);
        z-index: 2;
      }

      .search-form input {
        width: 100%;
      }

      .login-btn {
        padding: 0.4rem 1rem;
        font-size: 0.75rem;
      }

      .user-name {
        display: none;
      }

      .user-trigger {
        padding: 8px;
      }
    }

    @media (max-width: 480px) {
      .logo-text {
        display: none;
      }
    }
  `]
})
export class NavbarComponent {
  private cartService = inject(CartService);
  private router = inject(Router);
  public authService = inject(AuthService);

  logoError = false;
  dropdownOpen = false;
  searchOpen = false;
  searchKeyword = '';

  cartCount = this.cartService.cartCount;
  isAuthenticated = this.authService.isAuthenticated;
  currentUser = this.authService.currentUser;

  getUserFirstName(): string {
    const fullName = this.currentUser()?.fullName;
    if (!fullName) return '';
    return fullName.split(' ')[0];
  }

  toggleDropdown(event: Event) {
    event.stopPropagation();
    this.dropdownOpen = !this.dropdownOpen;
  }

  closeDropdown() {
    this.dropdownOpen = false;
  }

  toggleSearch() {
    this.searchOpen = true;
    this.closeDropdown();
  }

  closeSearch() {
    this.searchOpen = false;
    this.searchKeyword = '';
  }

  submitSearch() {
    const keyword = this.searchKeyword.trim();
    if (!keyword) {
      this.closeSearch();
      this.router.navigate(['/products'], {
        queryParams: { keyword: null },
        queryParamsHandling: 'merge'
      });
      return;
    }

    this.searchOpen = false;
    this.router.navigate(['/products'], { queryParams: { keyword } });
  }

  logout() {
    this.closeDropdown();
    this.authService.logout();
  }
}
