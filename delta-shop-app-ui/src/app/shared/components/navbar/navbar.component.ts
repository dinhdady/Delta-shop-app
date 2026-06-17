// navbar.component.ts - Fixed version
import { Component, inject, HostListener, ElementRef, signal, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { LucideAngularModule, Search, ShoppingCart, User } from 'lucide-angular';
import { CartService } from '../../../core/services/cart.service';
import { AuthService } from '../../../core/services/auth.service';
import { WishlistService } from '../../../core/services/wishlist.service';
import { NotificationService } from '../../../core/services/notification.service';
import { Subject, of } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap, takeUntil, catchError } from 'rxjs/operators';
import { SearchService, AutoCompleteSuggestion } from '../../../core/services/search.service';


@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterModule,
    LucideAngularModule
  ],
  template: `
    <header class="navbar">
      <div class="container navbar-container">
        <!-- Logo with Image -->
        <a routerLink="/" class="logo">
          <img src="https://res.cloudinary.com/dp9ltogc9/image/upload/v1777330102/delta-sports/Logo/ChatGPT_Image_05_43_51_28_thg_4_2026_hvdmhh.png" alt="Delta Sports" class="logo-img" (error)="logoError = true">
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
          <button class="icon-btn search-trigger-btn" type="button" aria-label="Search" (click)="toggleSearch($event)" [class.active]="searchOpen">
            <lucide-icon [name]="searchOpen ? 'x' : 'search'"></lucide-icon>
          </button>

          <a routerLink="/cart" class="icon-btn cart-btn" aria-label="Cart">
            <lucide-icon name="shopping-cart"></lucide-icon>
            @if (cartCount() > 0) {
              <span class="badge">{{ cartCount() }}</span>
            }
          </a>

          @if (isAuthenticated()) {
            <div class="notification-menu">
              <button class="icon-btn cart-btn" aria-label="Notifications" (click)="toggleNotifications($event)" [class.active]="notificationsOpen">
                <lucide-icon name="bell"></lucide-icon>
                @if (notificationCount() > 0) {
                  <span class="badge">{{ notificationCount() }}</span>
                }
              </button>
              
              <!-- Floating Notification Panel -->
              <div class="notification-dropdown" [class.show]="notificationsOpen" (click)="$event.stopPropagation()">
                <div class="dropdown-header">
                  <h3>Thông báo</h3>
                  @if (notifications().length > 0) {
                    <div class="header-actions">
                      <button type="button" (click)="markAllNotificationsAsRead()">Đánh dấu đã đọc tất cả</button>
                    </div>
                  }
                </div>
                
                <div class="dropdown-content" #notificationContent
                  (scroll)="onNotificationScroll(notificationContent)">
                  @if (loadingNotifications && notifications().length === 0) {
                    <div class="dropdown-state">Đang tải thông báo...</div>
                  } @else if (notifications().length === 0) {
                    <div class="dropdown-empty">
                      <lucide-icon name="bell" class="empty-icon"></lucide-icon>
                      <p>Không có thông báo mới</p>
                    </div>
                  } @else {
                    <div class="dropdown-list">
                      @for (notif of notifications(); track notif.id) {
                        <div class="dropdown-item" [class.unread]="!notif.read" (click)="markNotificationAsRead(notif)">
                          <div class="item-icon-wrapper" [class]="notif.type.toLowerCase()">
                            <lucide-icon [name]="getNotificationIcon(notif.type)"></lucide-icon>
                          </div>
                          <div class="item-body">
                            <div class="item-title">{{ notif.title }}</div>
                            <div class="item-desc">{{ notif.body }}</div>
                            <div class="item-time">{{ getRelativeTime(notif.createdAt) }}</div>
                          </div>
                          @if (!notif.read) {
                            <span class="unread-dot"></span>
                          }
                        </div>
                      }
                    </div>
                    @if (loadingMoreNotifications) {
                      <div class="notifications-loading-more">Đang tải thêm thông báo...</div>
                    } @else if (!hasMoreNotifications) {
                      <div class="notifications-end">Bạn đã xem tất cả thông báo</div>
                    }
                  }
                </div>
              </div>
            </div>
          }

          @if (isAuthenticated() && currentUser()) {
            <div class="user-menu">
              <button class="user-trigger" (click)="toggleDropdown($event)">
                <img *ngIf="currentUser()?.avatarUrl; else navbarUserIcon"
                  [src]="currentUser()?.avatarUrl" alt="" class="navbar-avatar"
                  (error)="handleAvatarError($event)">
                <ng-template #navbarUserIcon>
                  <lucide-icon name="user"></lucide-icon>
                </ng-template>
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
                <a routerLink="/wishlist" class="dropdown-link-with-badge" (click)="closeDropdown()">
                  <span>Yêu thích</span>
                  @if (wishlistCount() > 0) {
                    <span class="dropdown-badge">{{ wishlistCount() }}</span>
                  }
                </a>
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

      <!-- Dropdown Search Bar -->
      <div class="navbar-search-bar" [class.open]="searchOpen" (click)="$event.stopPropagation()">
        <div class="container search-bar-container">
          <form class="search-form-full" (ngSubmit)="submitSearch()">
            <div class="search-input-wrapper">
              <lucide-icon name="search" class="search-bar-icon"></lucide-icon>
              <input
                #searchInput
                type="text"
                name="searchTerm"
                [(ngModel)]="searchTerm"
                (ngModelChange)="onSearchTermChange($event)"
                placeholder="Bạn đang tìm kiếm sản phẩm gì?..."
                aria-label="Tìm sản phẩm"
                autocomplete="off"
                (keydown.escape)="closeSearch()">
              @if (searchTerm) {
                <button class="search-clear-btn" type="button" (click)="clearSearch(searchInput)">
                  <lucide-icon name="x"></lucide-icon>
                </button>
              }
            </div>
          </form>

          <!-- Suggestions Dropdown -->
          @if (searchSuggestions.length > 0 && searchTerm.trim().length >= 2) {
            <div class="search-suggestions-dropdown">
              <div class="suggestions-header">Sản phẩm gợi ý</div>
              <div class="suggestions-list">
                @for (item of searchSuggestions; track item.id) {
                  <a [routerLink]="['/products/' + (item.slug || item.id)]" class="suggestion-item" (click)="closeSearch()">
                    <img [src]="item.primaryImage || 'assets/images/placeholder.jpg'" alt="" class="suggestion-img">
                    <div class="suggestion-info">
                      <div class="suggestion-name">{{ item.name }}</div>
                      <div class="suggestion-category" *ngIf="item.categoryName">{{ item.categoryName }}</div>
                      <div class="suggestion-price">{{ item.basePrice | currency:'VND':'symbol':'1.0-0' }}</div>
                    </div>
                  </a>
                }
              </div>
            </div>
          } @else if (searchTerm.trim().length >= 2 && loadingSuggestions) {
            <div class="search-suggestions-dropdown">
              <div class="suggestions-state">Đang tìm kiếm gợi ý...</div>
            </div>
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
      display: grid;
      grid-template-columns: minmax(190px, 1fr) auto minmax(260px, 1fr);
      align-items: center;
      height: 70px;
      max-width: 1280px;
      margin: 0 auto;
      padding: 0 20px;
      gap: 1rem;
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
      justify-self: start;
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
      justify-content: center;
      align-items: center;
      white-space: nowrap;
      justify-self: center;
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
      justify-content: flex-end;
      gap: clamp(0.5rem, 1vw, 0.9rem);
      justify-self: end;
      flex-shrink: 0;
      min-width: 0;
      height: 100%;
      align-self: stretch;
    }

    .search-trigger-btn {
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

    .search-trigger-btn:hover,
    .search-trigger-btn.active {
      color: #cd4631;
      background: rgba(205, 70, 49, 0.1);
    }

    /* Dropdown Search Bar below Navbar - Dynamic Island Style (Light Mode) */
    .navbar-search-bar {
      position: absolute;
      top: calc(100% + 12px);
      left: 50%;
      transform: translateX(-50%) translateY(-15px) scale(0.9);
      width: min(650px, 92vw);
      background: #ffffff;
      border: 1px solid rgba(0, 0, 0, 0.08);
      border-radius: 30px;
      padding: 8px 10px;
      z-index: 999;
      box-shadow: 0 20px 40px rgba(0, 0, 0, 0.12), 
                  0 0 0 1px rgba(0, 0, 0, 0.04),
                  0 4px 15px rgba(205, 70, 49, 0.1); /* Subtle premium orange glow */
      
      /* Dynamic Island spring-like cubic-bezier transition */
      opacity: 0;
      visibility: hidden;
      transition: opacity 0.3s ease,
                  visibility 0.3s ease,
                  transform 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
    }

    .navbar-search-bar.open {
      opacity: 1;
      visibility: visible;
      transform: translateX(-50%) translateY(0) scale(1);
    }

    .search-bar-container {
      width: 100%;
      padding: 0 5px;
    }

    .search-form-full {
      display: flex;
      align-items: center;
      gap: 10px;
      width: 100%;
    }

    .search-input-wrapper {
      position: relative;
      flex-grow: 1;
      display: flex;
      align-items: center;
      background: #f5f5f7;
      border: 1px solid rgba(0, 0, 0, 0.03);
      border-radius: 22px;
      padding: 4px 16px;
      transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
    }

    .search-input-wrapper:focus-within {
      background: #ffffff;
      border-color: #cd4631;
      box-shadow: 0 0 12px rgba(205, 70, 49, 0.15);
    }

    .search-bar-icon {
      color: #888888;
      margin-right: 12px;
      flex-shrink: 0;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: color 0.25s ease;
    }

    .search-input-wrapper:focus-within .search-bar-icon {
      color: #cd4631;
    }

    .search-form-full input {
      flex-grow: 1;
      background: transparent;
      border: none;
      outline: none;
      color: #1d1d1f;
      font-size: 0.95rem;
      padding: 8px 0;
      font-family: inherit;
      width: 100%;
      letter-spacing: 0.5px;
    }

    .search-form-full input::placeholder {
      color: rgba(0, 0, 0, 0.4);
    }

    .search-clear-btn {
      background: none;
      border: none;
      color: #888888;
      cursor: pointer;
      padding: 6px;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 50%;
      transition: all 0.2s ease;
      margin-left: 8px;
    }

    .search-clear-btn:hover {
      color: #cd4631;
      background: rgba(0, 0, 0, 0.05);
    }

    /* Search Suggestions Dropdown (Light Mode) */
    .search-suggestions-dropdown {
      position: absolute;
      top: 100%;
      left: 0;
      right: 0;
      background: #ffffff;
      border: 1px solid rgba(0, 0, 0, 0.08);
      border-radius: 16px;
      margin-top: 8px;
      max-height: 400px;
      overflow-y: auto;
      z-index: 1005;
      box-shadow: 0 10px 30px rgba(0, 0, 0, 0.08);
    }
    .suggestions-header {
      padding: 12px 16px 8px;
      font-size: 0.8rem;
      text-transform: uppercase;
      letter-spacing: 0.5px;
      color: rgba(0, 0, 0, 0.45);
      border-bottom: 1px solid rgba(0, 0, 0, 0.05);
      font-weight: 600;
    }
    .suggestions-list {
      display: flex;
      flex-direction: column;
    }
    .suggestion-item {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 10px 16px;
      text-decoration: none;
      color: #1d1d1f;
      transition: background 0.2s ease;
      border-bottom: 1px solid rgba(0, 0, 0, 0.04);
    }
    .suggestion-item:last-child {
      border-bottom: none;
    }
    .suggestion-item:hover {
      background: #fff5f2; /* Light primary background on hover */
    }
    .suggestion-img {
      width: 44px;
      height: 44px;
      object-fit: cover;
      border-radius: 8px;
      background: #f5f5f7;
      flex-shrink: 0;
      border: 1px solid rgba(0, 0, 0, 0.05);
    }
    .suggestion-info {
      flex-grow: 1;
      min-width: 0;
      display: flex;
      flex-direction: column;
      gap: 2px;
    }
    .suggestion-name {
      font-size: 0.9rem;
      font-weight: 600;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      color: #1d1d1f;
    }
    .suggestion-category {
      font-size: 0.75rem;
      color: rgba(0, 0, 0, 0.45);
    }
    .suggestion-price {
      font-size: 0.85rem;
      color: #cd4631;
      font-weight: 700;
    }
    .suggestions-state {
      padding: 24px;
      text-align: center;
      color: rgba(0, 0, 0, 0.45);
      font-size: 0.9rem;
    }
    /* Notifications Menu Dropdown - Facebook Style */
    .notification-menu {
      position: relative;
      height: 100%;
      display: flex;
      align-items: center;
    }

    .notification-dropdown {
      position: absolute;
      top: 100%;
      right: 0;
      width: 360px;
      max-height: 480px;
      background-color: #ffffff;
      border-radius: 16px;
      overflow: hidden;
      z-index: 1001;
      opacity: 0;
      visibility: hidden;
      transform: translateY(10px);
      transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
      box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
      border: 1px solid #eaeaea;
      display: flex;
      flex-direction: column;
    }

    .notification-dropdown.show {
      opacity: 1;
      visibility: visible;
      transform: translateY(4px);
    }

    .notification-dropdown::before {
      content: '';
      position: absolute;
      top: -6px;
      right: 16px;
      width: 12px;
      height: 12px;
      background-color: #ffffff;
      transform: rotate(45deg);
      border-left: 1px solid #eaeaea;
      border-top: 1px solid #eaeaea;
      z-index: 1002;
    }

    .dropdown-header {
      padding: 16px;
      border-bottom: 1px solid #eee;
      display: flex;
      justify-content: space-between;
      align-items: center;
      background: #fafafa;
      z-index: 1003;
    }

    .dropdown-header h3 {
      font-size: 1rem;
      font-weight: 700;
      color: #1a1a1a;
      margin: 0;
    }

    .header-actions button {
      background: none;
      border: none;
      color: #cd4631;
      font-size: 0.8rem;
      font-weight: 600;
      cursor: pointer;
      padding: 0;
      transition: color 0.2s ease;
    }

    .header-actions button:hover {
      color: #b83a26;
      text-decoration: underline;
    }

    .dropdown-content {
      overflow-y: auto;
      max-height: 400px;
      display: flex;
      flex-direction: column;
    }

    .dropdown-state {
      padding: 30px;
      text-align: center;
      color: #666;
      font-size: 0.875rem;
    }

    .dropdown-empty {
      padding: 40px 20px;
      text-align: center;
      color: #888;
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 12px;
    }

    .empty-icon {
      color: #ccc;
      width: 48px;
      height: 48px;
    }

    .dropdown-empty p {
      font-size: 0.875rem;
      margin: 0;
    }

    .dropdown-list {
      display: flex;
      flex-direction: column;
    }

    .dropdown-item {
      display: flex;
      gap: 12px;
      padding: 14px 16px;
      border-bottom: 1px solid #f9f9f9;
      cursor: pointer;
      transition: background-color 0.2s ease;
      position: relative;
      align-items: flex-start;
    }

    .dropdown-item:hover {
      background-color: #f5f5f5;
    }

    .dropdown-item.unread {
      background-color: #fff8f6;
    }

    .dropdown-item.unread:hover {
      background-color: #fff2ee;
    }

    .item-icon-wrapper {
      width: 40px;
      height: 40px;
      border-radius: 50%;
      background-color: #f0f0f0;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #666;
      flex-shrink: 0;
    }

    .item-icon-wrapper lucide-icon {
      width: 18px;
      height: 18px;
    }

    /* Icon color customization based on notification type */
    .item-icon-wrapper.order_placed,
    .item-icon-wrapper.order_confirmed {
      background-color: #e8f5e9;
      color: #2e7d32;
    }

    .item-icon-wrapper.order_shipped {
      background-color: #e3f2fd;
      color: #1565c0;
    }

    .item-icon-wrapper.order_delivered,
    .item-icon-wrapper.payment_success {
      background-color: #e8f5e9;
      color: #2e7d32;
    }

    .item-icon-wrapper.order_cancelled,
    .item-icon-wrapper.payment_failed {
      background-color: #ffebee;
      color: #c62828;
    }

    .item-icon-wrapper.promotion {
      background-color: #fff3e0;
      color: #ef6c00;
    }

    .item-body {
      flex-grow: 1;
      display: flex;
      flex-direction: column;
      gap: 4px;
      min-width: 0;
    }

    .item-title {
      font-size: 0.85rem;
      font-weight: 600;
      color: #1a1a1a;
      line-height: 1.3;
    }

    .dropdown-item.unread .item-title {
      color: #000000;
    }

    .item-desc {
      font-size: 0.8rem;
      color: #666;
      line-height: 1.4;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }

    .item-time {
      font-size: 0.7rem;
      color: #999;
      margin-top: 2px;
    }

    .unread-dot {
      width: 8px;
      height: 8px;
      border-radius: 50%;
      background-color: #cd4631;
      align-self: center;
      flex-shrink: 0;
      margin-left: auto;
    }
    .notifications-loading-more, .notifications-end {
      padding: 12px 16px;
      text-align: center;
      color: #999;
      font-size: 0.75rem;
      border-top: 1px solid #f1f1f1;
    }
    .notifications-loading-more {
      color: #cd4631;
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
      height: 100%;
      display: flex;
      align-items: center;
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
    .navbar-avatar {
      width: 30px;
      height: 30px;
      border-radius: 50%;
      object-fit: cover;
      border: 1px solid rgba(255, 255, 255, 0.35);
      flex-shrink: 0;
    }

    .dropdown {
      position: absolute;
      top: 100%;
      right: 0;
      background-color: white;
      min-width: 220px;
      border-radius: 16px;
      overflow: hidden;
      z-index: 1001;
      opacity: 0;
      visibility: hidden;
      transform: translateY(10px);
      transition: all 0.25s cubic-bezier(0.16, 1, 0.3, 1);
      box-shadow: 0 10px 30px rgba(0, 0, 0, 0.15);
      border: 1px solid #eaeaea;
    }

    .dropdown.show {
      opacity: 1;
      visibility: visible;
      transform: translateY(4px);
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
      border-left: 1px solid #eaeaea;
      border-top: 1px solid #eaeaea;
      z-index: 1002;
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

    .dropdown a.dropdown-link-with-badge {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 0.75rem;
    }

    .dropdown-badge {
      min-width: 20px;
      height: 20px;
      padding: 0 6px;
      border-radius: 999px;
      background: #cd4631;
      color: white;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      font-size: 0.75rem;
      font-weight: 700;
    }

    .dropdown a:hover,
    .dropdown button:hover {
      background-color: #f5f5f5;
      color: #cd4631;
    }

    /* Responsive */
    @media (max-width: 1100px) {
      .navbar-container {
        grid-template-columns: minmax(170px, 1fr) auto minmax(220px, 1fr);
        gap: 0.75rem;
      }

      .nav-links {
        gap: 1rem;
      }

      .nav-links a {
        font-size: 0.8rem;
      }
    }

    @media (max-width: 920px) {
      .navbar-container {
        display: flex;
        justify-content: space-between;
      }

      .nav-links {
        display: none;
      }
    }

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

      .navbar-search-bar {
        top: calc(100% + 8px);
        width: 92vw;
        padding: 6px 8px;
        border-radius: 24px;
      }
      
      .search-form-full {
        gap: 8px;
      }
      
      .search-input-wrapper {
        border-radius: 20px;
        padding: 2px 12px;
      }
      
      
    }

    @media (max-width: 480px) {
      .logo-text {
        display: none;
      }

      .notification-dropdown {
        width: 320px;
        right: -60px;
      }

      .notification-dropdown::before {
        right: 76px;
      }
    }
  `]
})
export class NavbarComponent implements OnInit, OnDestroy {
  private cartService = inject(CartService);
  private wishlistService = inject(WishlistService);
  private notificationService = inject(NotificationService);
  public authService = inject(AuthService);
  private router = inject(Router);
  private elementRef = inject(ElementRef);
  private searchService = inject(SearchService);

  logoError = false;
  dropdownOpen = false;
  searchOpen = false;
  searchTerm = '';

  searchSuggestions: AutoCompleteSuggestion[] = [];
  loadingSuggestions = false;
  private searchSubject = new Subject<string>();
  private destroy$ = new Subject<void>();

  // Facebook Style Notifications
  notifications = signal<any[]>([]);
  notificationsOpen = false;
  loadingNotifications = false;
  loadingMoreNotifications = false;
  notificationPage = 0;
  readonly notificationPageSize = 5;
  hasMoreNotifications = false;

  cartCount = this.cartService.cartCount;
  wishlistCount = this.wishlistService.wishlistCount;
  notificationCount = this.notificationService.unreadCount;
  isAuthenticated = this.authService.isAuthenticated;
  currentUser = this.authService.currentUser;

  constructor() {
    if (this.isAuthenticated()) {
      this.wishlistService.loadSummary();
      this.notificationService.loadUnreadCount();
    }
  }

  ngOnInit() {
    this.searchSubject.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      switchMap(term => {
        if (!term || term.trim().length < 2) {
          this.searchSuggestions = [];
          return of([]);
        }
        this.loadingSuggestions = true;
        return this.searchService.getAutoComplete(term).pipe(
          catchError(() => {
            this.loadingSuggestions = false;
            this.searchSuggestions = [];
            return of([]);
          })
        );
      }),
      takeUntil(this.destroy$)
    ).subscribe(res => {
      this.loadingSuggestions = false;
      this.searchSuggestions = res;
    });
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  onSearchTermChange(term: string) {
    this.searchSubject.next(term);
  }

  getUserFirstName(): string {
    const fullName = this.currentUser()?.fullName;
    if (!fullName) return '';
    return fullName.split(' ')[0];
  }

  handleAvatarError(event: Event): void {
    this.authService.updateCurrentUserAvatar(null);
  }

  toggleDropdown(event: Event) {
    event.stopPropagation();
    this.dropdownOpen = !this.dropdownOpen;
  }

  closeDropdown() {
    this.dropdownOpen = false;
  }

  toggleSearch(event: Event) {
    event.stopPropagation();
    this.searchOpen = !this.searchOpen;
    if (this.searchOpen) {
      setTimeout(() => {
        const inputEl = document.querySelector('.search-form-full input') as HTMLInputElement;
        if (inputEl) inputEl.focus();
      }, 100);
    }
  }

  closeSearch() {
    this.searchOpen = false;
  }

  @HostListener('document:click', ['$event'])
  onClickOutside(event: Event) {
    const target = event.target as HTMLElement;
    
    // Handle Search Bar click outside
    if (this.searchOpen) {
      const clickedInsideSearch = this.elementRef.nativeElement.querySelector('.navbar-search-bar')?.contains(target);
      const clickedSearchTrigger = this.elementRef.nativeElement.querySelector('.search-trigger-btn')?.contains(target);
      if (!clickedInsideSearch && !clickedSearchTrigger) {
        this.closeSearch();
      }
    }
    
    // Handle User Dropdown click outside
    if (this.dropdownOpen) {
      const clickedInsideDropdown = this.elementRef.nativeElement.querySelector('.user-menu')?.contains(target);
      if (!clickedInsideDropdown) {
        this.closeDropdown();
      }
    }

    // Handle Notifications click outside
    if (this.notificationsOpen) {
      const clickedInsideNotifications = this.elementRef.nativeElement.querySelector('.notification-menu')?.contains(target);
      if (!clickedInsideNotifications) {
        this.closeNotifications();
      }
    }
  }

  clearSearch(input: HTMLInputElement) {
    this.searchTerm = '';
    input.focus();
  }

  submitSearch() {
    const keyword = this.searchTerm.trim();
    this.router.navigate(['/products'], {
      queryParams: keyword ? { keyword } : {}
    });
    this.closeSearch();
  }

  logout() {
    this.closeDropdown();
    this.authService.logout();
    this.router.navigate(['/auth/login']);
  }

  // --- Popover Notifications ---
  toggleNotifications(event: Event) {
    event.stopPropagation();
    this.closeSearch();
    this.closeDropdown();
    this.notificationsOpen = !this.notificationsOpen;
    
    if (this.notificationsOpen) {
      this.loadRecentNotifications();
    }
  }

  closeNotifications() {
    this.notificationsOpen = false;
  }

  loadRecentNotifications() {
    this.loadingNotifications = true;
    this.notificationPage = 0;
    this.notificationService.getNotifications(0, this.notificationPageSize).subscribe({
      next: (res) => {
        this.notifications.set(res.content || []);
        this.hasMoreNotifications = !res.last;
        this.loadingNotifications = false;
      },
      error: () => {
        this.hasMoreNotifications = false;
        this.loadingNotifications = false;
      }
    });
  }

  onNotificationScroll(content: HTMLElement) {
    const distanceFromBottom = content.scrollHeight - content.scrollTop - content.clientHeight;
    if (distanceFromBottom <= 80) {
      this.loadMoreNotifications();
    }
  }

  loadMoreNotifications() {
    if (this.loadingMoreNotifications || !this.hasMoreNotifications) return;

    this.loadingMoreNotifications = true;
    const nextPage = this.notificationPage + 1;
    this.notificationService.getNotifications(nextPage, this.notificationPageSize).subscribe({
      next: res => {
        const existingIds = new Set(this.notifications().map(item => item.id));
        const newItems = (res.content || []).filter(item => !existingIds.has(item.id));
        this.notifications.update(items => [...items, ...newItems]);
        this.notificationPage = res.pageNumber;
        this.hasMoreNotifications = !res.last;
        this.loadingMoreNotifications = false;
      },
      error: () => {
        this.loadingMoreNotifications = false;
      }
    });
  }

  markNotificationAsRead(notification: any) {
    if (!notification.read) {
      this.notificationService.markAsRead(notification.id).subscribe({
        next: () => {
          this.notifications.update(items =>
            items.map(item => item.id === notification.id ? { ...item, read: true } : item)
          );
        }
      });
    }
    this.handleNotificationNavigation(notification);
  }

  handleNotificationNavigation(notification: any) {
    this.closeNotifications();
    
    let orderId: string | null = null;
    if (notification.data) {
      try {
        const parsedData = JSON.parse(notification.data);
        orderId = parsedData.orderId;
      } catch (e) {
        orderId = notification.data;
      }
    }
    
    if (orderId) {
      this.router.navigate(['/orders'], {
        queryParams: { orderId: orderId }
      });
    } else {
      this.router.navigate(['/orders']);
    }
  }

  markAllNotificationsAsRead() {
    this.notificationService.markAllAsRead().subscribe({
      next: () => {
        this.notifications.update(items =>
          items.map(item => ({ ...item, read: true }))
        );
      }
    });
  }

  getNotificationIcon(type: string): string {
    switch (type) {
      case 'ORDER_PLACED':
      case 'ORDER_CONFIRMED':
        return 'shopping-cart';
      case 'ORDER_SHIPPED':
        return 'truck';
      case 'ORDER_DELIVERED':
      case 'PAYMENT_SUCCESS':
        return 'check-circle';
      case 'ORDER_CANCELLED':
      case 'PAYMENT_FAILED':
        return 'alert-circle';
      case 'PROMOTION':
        return 'heart';
      default:
        return 'bell';
    }
  }

  getRelativeTime(dateStr: string): string {
    try {
      const now = new Date();
      const date = new Date(dateStr);
      const diffMs = now.getTime() - date.getTime();
      const diffMins = Math.floor(diffMs / 60000);
      
      if (diffMins < 1) return 'Vừa xong';
      if (diffMins < 60) return `${diffMins} phút trước`;
      
      const diffHours = Math.floor(diffMins / 60);
      if (diffHours < 24) return `${diffHours} giờ trước`;
      
      const diffDays = Math.floor(diffHours / 24);
      if (diffDays < 30) return `${diffDays} ngày trước`;
      
      return date.toLocaleDateString('vi-VN');
    } catch (e) {
      return '';
    }
  }
}
