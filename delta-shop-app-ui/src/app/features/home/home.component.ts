// home.component.ts - Updated styles with animations
import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ProductService, ProductSummary } from '../../core/services/product.service';
import { ProductCardComponent } from '../../shared/components/product-card/product-card.component';
import { SkeletonLoaderComponent } from '../../shared/components/skeleton-loader/skeleton-loader.component';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterModule, ProductCardComponent, SkeletonLoaderComponent],
  template: `
    <!-- Hero Section -->
    <section class="hero">
      <div class="hero-bg"></div>
      <div class="container hero-content">
        <div class="hero-text">
          <h1 class="animate-text">VƯỢT QUA<br><span class="text-primary">GIỚI HẠN</span></h1>
          <p class="animate-text-delay">Khám phá bộ sưu tập dụng cụ thể thao chuyên nghiệp mới nhất. Sẵn sàng bứt phá mọi giới hạn.</p>
          <a routerLink="/products" class="btn btn-primary btn-large animate-btn">MUA SẮM NGAY →</a>
        </div>
      </div>
      <div class="scroll-indicator">
        <span>Cuộn xuống</span>
        <div class="mouse"></div>
      </div>
    </section>

    <!-- Categories Section -->
    <section class="section categories-section">
      <div class="container">
        <h2 class="section-title reveal">DANH MỤC NỔI BẬT</h2>
        <div class="categories-grid">
          <a routerLink="/products" class="category-card reveal">
            <div class="category-image">
              <img src="https://images.unsplash.com/photo-1579952363873-27f3bade9f55?q=80&w=800&auto=format&fit=crop" alt="Bóng đá">
            </div>
            <div class="overlay">
              <h3>BÓNG ĐÁ</h3>
              <span class="shop-now">Shop Now →</span>
            </div>
          </a>
          <a routerLink="/products" class="category-card reveal-delay-1">
            <div class="category-image">
              <img src="https://images.unsplash.com/photo-1530549387789-4c1017266635?q=80&w=800&auto=format&fit=crop" alt="Chạy bộ">
            </div>
            <div class="overlay">
              <h3>CHẠY BỘ</h3>
              <span class="shop-now">Shop Now →</span>
            </div>
          </a>
          <a routerLink="/products" class="category-card reveal-delay-2">
            <div class="category-image">
              <img src="https://images.unsplash.com/photo-1534438327276-14e5300c3a48?q=80&w=800&auto=format&fit=crop" alt="Gym">
            </div>
            <div class="overlay">
              <h3>GYM & FITNESS</h3>
              <span class="shop-now">Shop Now →</span>
            </div>
          </a>
        </div>
      </div>
    </section>

    <!-- Featured Products -->
    <section class="section">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title reveal">SẢN PHẨM NỔI BẬT</h2>
          <a routerLink="/products" class="view-all reveal">Xem tất cả →</a>
        </div>

        @if (loading) {
          <div class="products-grid">
            @for (item of [1,2,3,4]; track item) {
              <app-skeleton-loader type="card"></app-skeleton-loader>
            }
          </div>
        } @else {
          <div class="products-grid">
            @for (product of featuredProducts; track product.id; let i = $index) {
              <div class="product-item" [style.animation-delay]="(i * 0.1) + 's'">
                <app-product-card [product]="product"></app-product-card>
              </div>
            }
          </div>
        }
      </div>
    </section>

    <!-- Promo Banner -->
    <section class="promo-banner">
      <div class="container promo-content">
        <div class="promo-badge">🔥 GIỚI HẠN</div>
        <h2 class="reveal">SALE UP TO <span class="text-primary">50%</span></h2>
        <p class="reveal-delay-1">Ưu đãi lớn nhất trong năm cho các dòng giày chạy bộ.</p>
        <a routerLink="/products" class="btn btn-outline btn-outline-light reveal-delay-2">XEM ƯU ĐÃI →</a>
      </div>
    </section>
  `,
  styles: [`
    /* ========== ANIMATIONS ========== */
    @keyframes fadeInUp {
      from {
        opacity: 0;
        transform: translateY(30px);
      }
      to {
        opacity: 1;
        transform: translateY(0);
      }
    }

    @keyframes fadeInLeft {
      from {
        opacity: 0;
        transform: translateX(-50px);
      }
      to {
        opacity: 1;
        transform: translateX(0);
      }
    }

    @keyframes fadeInRight {
      from {
        opacity: 0;
        transform: translateX(50px);
      }
      to {
        opacity: 1;
        transform: translateX(0);
      }
    }

    @keyframes scaleIn {
      from {
        opacity: 0;
        transform: scale(0.9);
      }
      to {
        opacity: 1;
        transform: scale(1);
      }
    }

    @keyframes shimmer {
      0% {
        background-position: -1000px 0;
      }
      100% {
        background-position: 1000px 0;
      }
    }

    @keyframes pulse {
      0%, 100% {
        transform: scale(1);
      }
      50% {
        transform: scale(1.05);
      }
    }

    @keyframes float {
      0%, 100% {
        transform: translateY(0);
      }
      50% {
        transform: translateY(-10px);
      }
    }

    @keyframes gradientShift {
      0% {
        background-position: 0% 50%;
      }
      50% {
        background-position: 100% 50%;
      }
      100% {
        background-position: 0% 50%;
      }
    }

    /* Reveal animations */
    .reveal {
      opacity: 0;
      animation: fadeInUp 0.8s ease forwards;
    }

    .reveal-delay-1 {
      opacity: 0;
      animation: fadeInUp 0.8s ease 0.2s forwards;
    }

    .reveal-delay-2 {
      opacity: 0;
      animation: fadeInUp 0.8s ease 0.4s forwards;
    }

    /* Hero Section */
    .hero {
      height: 100vh;
      min-height: 700px;
      position: relative;
      display: flex;
      align-items: center;
      overflow: hidden;
    }

    .hero-bg {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background-image: url('https://images.unsplash.com/photo-1517836357463-d25dfeac3438?q=80&w=2000&auto=format&fit=crop');
      background-size: cover;
      background-position: center;
      transform: scale(1.1);
      animation: scaleIn 1.5s ease-out forwards;

      &::before {
        content: '';
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background: linear-gradient(135deg, rgba(0,0,0,0.8) 0%, rgba(0,0,0,0.4) 100%);
      }
    }

    .hero-content {
      position: relative;
      z-index: 1;
      color: var(--color-light);
      width: 100%;
    }

    .hero-text {
      max-width: 700px;
    }

    .animate-text {
      opacity: 0;
      animation: fadeInLeft 1s ease 0.3s forwards;
    }

    .animate-text-delay {
      opacity: 0;
      animation: fadeInLeft 1s ease 0.6s forwards;
    }

    .animate-btn {
      opacity: 0;
      animation: fadeInUp 0.8s ease 0.9s forwards;
    }

    .hero-text h1 {
      color: var(--color-light);
      font-size: 5.5rem;
      line-height: 1.1;
      margin-bottom: 1.5rem;

      span {
        color: var(--color-primary);
        position: relative;
        display: inline-block;

        &::after {
          content: '';
          position: absolute;
          bottom: 10px;
          left: 0;
          width: 100%;
          height: 4px;
          background: linear-gradient(90deg, var(--color-primary), transparent);
          animation: scaleIn 0.8s ease 1.2s forwards;
          transform: scaleX(0);
          transform-origin: left;
        }
      }

      @media (max-width: 768px) {
        font-size: 3.5rem;
      }
    }

    .hero-text p {
      font-size: 1.25rem;
      margin-bottom: 2rem;
      color: #e0e0e0;
      line-height: 1.6;
    }

    /* Scroll Indicator */
    .scroll-indicator {
      position: absolute;
      bottom: 30px;
      left: 50%;
      transform: translateX(-50%);
      text-align: center;
      z-index: 2;
      animation: float 2s ease infinite;

      span {
        display: block;
        font-size: 0.75rem;
        color: rgba(255,255,255,0.7);
        margin-bottom: 8px;
        letter-spacing: 2px;
      }

      .mouse {
        width: 26px;
        height: 40px;
        border: 2px solid rgba(255,255,255,0.5);
        border-radius: 20px;
        position: relative;
        margin: 0 auto;

        &::before {
          content: '';
          position: absolute;
          top: 8px;
          left: 50%;
          transform: translateX(-50%);
          width: 4px;
          height: 8px;
          background: white;
          border-radius: 2px;
          animation: pulse 1.5s ease infinite;
        }
      }
    }

    /* Section Global */
    .section {
      padding: 5rem 0;
    }

    .section-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-end;
      margin-bottom: 2.5rem;
      flex-wrap: wrap;
      gap: 1rem;
    }

    .section-title {
      font-size: 2.5rem;
      margin-bottom: 0;
      position: relative;
      display: inline-block;

      &::before {
        content: '';
        position: absolute;
        bottom: -10px;
        left: 0;
        width: 60px;
        height: 3px;
        background: linear-gradient(90deg, var(--color-primary), transparent);
        border-radius: 2px;
      }

      @media (max-width: 768px) {
        font-size: 1.75rem;
      }
    }

    .view-all {
      font-weight: 600;
      text-transform: uppercase;
      padding: 0.5rem 1rem;
      transition: all 0.3s ease;
      display: inline-flex;
      align-items: center;
      gap: 8px;

      &:hover {
        color: var(--color-primary);
        transform: translateX(5px);
      }
    }

    /* Categories Grid */
    .categories-section {
      background: linear-gradient(135deg, #faf9f8 0%, #ffffff 100%);
    }

    .categories-grid {
      display: grid;
      grid-template-columns: repeat(3, 1fr);
      gap: 2rem;
      margin-top: 2rem;

      @media (max-width: 768px) {
        grid-template-columns: 1fr;
        gap: 1.5rem;
      }
    }

    .category-card {
      position: relative;
      height: 350px;
      overflow: hidden;
      display: block;
      border-radius: 20px;
      box-shadow: 0 10px 30px rgba(0,0,0,0.1);
      transition: all 0.4s ease;

      &:hover {
        transform: translateY(-10px);
        box-shadow: 0 20px 40px rgba(0,0,0,0.2);

        .category-image img {
          transform: scale(1.15);
        }

        .overlay {
          background: linear-gradient(to top, rgba(0,0,0,0.9), transparent);
        }

        .shop-now {
          transform: translateX(0);
          opacity: 1;
        }
      }

      .category-image {
        width: 100%;
        height: 100%;
        overflow: hidden;

        img {
          width: 100%;
          height: 100%;
          object-fit: cover;
          transition: transform 0.6s cubic-bezier(0.33, 1, 0.68, 1);
        }
      }

      .overlay {
        position: absolute;
        bottom: 0;
        left: 0;
        right: 0;
        padding: 2rem;
        background: linear-gradient(to top, rgba(0,0,0,0.8), transparent);
        transition: all 0.3s ease;

        h3 {
          color: var(--color-light);
          margin: 0 0 0.5rem;
          font-size: 1.5rem;
          font-weight: 700;
        }

        .shop-now {
          display: inline-block;
          color: var(--color-primary);
          font-size: 0.875rem;
          font-weight: 600;
          transform: translateX(-20px);
          opacity: 0;
          transition: all 0.3s ease;
        }
      }
    }

    /* Products Grid */
    .products-grid {
      display: grid;
      grid-template-columns: repeat(4, 1fr);
      gap: 1.5rem;

      @media (max-width: 992px) {
        grid-template-columns: repeat(3, 1fr);
      }

      @media (max-width: 768px) {
        grid-template-columns: repeat(2, 1fr);
      }

      @media (max-width: 576px) {
        grid-template-columns: 1fr;
      }
    }

    .product-item {
      opacity: 0;
      animation: scaleIn 0.5s ease forwards;
    }

    /* Promo Banner */
    .promo-banner {
      background: linear-gradient(135deg, #0a0a0a 0%, #1a1a1a 100%);
      padding: 5rem 0;
      text-align: center;
      color: var(--color-light);
      position: relative;
      overflow: hidden;

      &::before {
        content: '';
        position: absolute;
        top: -50%;
        left: -50%;
        width: 200%;
        height: 200%;
        background: radial-gradient(circle, rgba(249,115,22,0.1) 0%, transparent 70%);
        animation: gradientShift 15s ease infinite;
      }
    }

    .promo-badge {
      display: inline-block;
      padding: 0.5rem 1rem;
      background: rgba(249,115,22,0.2);
      border: 1px solid rgba(249,115,22,0.5);
      border-radius: 50px;
      font-size: 0.75rem;
      font-weight: 600;
      margin-bottom: 1.5rem;
      letter-spacing: 2px;
    }

    .promo-content {
      position: relative;
      z-index: 1;

      h2 {
        font-size: 4rem;
        margin-bottom: 1rem;

        .text-primary {
          color: var(--color-primary);
          font-size: 4.5rem;
        }

        @media (max-width: 768px) {
          font-size: 2.5rem;

          .text-primary {
            font-size: 3rem;
          }
        }
      }

      p {
        font-size: 1.25rem;
        margin-bottom: 2rem;
        color: #d0d0d0;
      }
    }

    .btn-large {
      padding: 1rem 2rem;
      font-size: 1.125rem;
      transition: all 0.3s ease;

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 10px 25px rgba(230,57,70,0.3);
      }
    }

    .btn-outline-light {
      border: 2px solid var(--color-light);
      background: transparent;
      color: var(--color-light);
      padding: 0.875rem 2rem;
      border-radius: 50px;
      font-weight: 600;
      transition: all 0.3s ease;

      &:hover {
        background-color: var(--color-light);
        color: var(--color-dark);
        transform: translateY(-2px);
        box-shadow: 0 10px 25px rgba(255,255,255,0.2);
      }
    }

    /* Loading Animation */
    @keyframes skeletonPulse {
      0%, 100% {
        opacity: 0.5;
      }
      50% {
        opacity: 1;
      }
    }

    /* Responsive */
    @media (max-width: 768px) {
      .section {
        padding: 3rem 0;
      }

      .hero {
        min-height: 600px;
      }

      .hero-text h1 {
        font-size: 2.5rem;
      }

      .hero-text p {
        font-size: 1rem;
      }
    }

    /* Hover effect for buttons */
    .btn-primary {
      position: relative;
      overflow: hidden;

      &::before {
        content: '';
        position: absolute;
        top: 50%;
        left: 50%;
        width: 0;
        height: 0;
        border-radius: 50%;
        background: rgba(255,255,255,0.3);
        transform: translate(-50%, -50%);
        transition: width 0.6s, height 0.6s;
      }

      &:hover::before {
        width: 300px;
        height: 300px;
      }
    }
  `]
})
export class HomeComponent implements OnInit {
  private productService = inject(ProductService);

  featuredProducts: ProductSummary[] = [];
  loading = true;

  ngOnInit() {
    this.loadFeaturedProducts();
  }

  loadFeaturedProducts() {
    this.productService.getFeaturedProducts().subscribe({
      next: (res) => {
        this.featuredProducts = res.slice(0, 4);
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }
}
