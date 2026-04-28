import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { LucideAngularModule, Facebook, Instagram, Twitter, MapPin, Phone, Mail } from 'lucide-angular';

@Component({
  selector: 'app-footer',
  standalone: true,
  imports: [
    CommonModule, 
    RouterModule, 
    LucideAngularModule
  ],
  template: `
    <footer class="footer">
      <div class="container">
        <div class="footer-grid">
          <div class="footer-col">
            <h3 class="logo">DELTA<span>SPORTS</span></h3>
            <p class="desc">
              Hệ thống cung cấp dụng cụ thể thao chuyên nghiệp hàng đầu Việt Nam. 
              Chúng tôi cam kết mang đến những sản phẩm chất lượng nhất cho đam mê của bạn.
            </p>
            <div class="social-links">
              <a href="#" aria-label="Facebook"><lucide-icon name="facebook"></lucide-icon></a>
              <a href="#" aria-label="Instagram"><lucide-icon name="instagram"></lucide-icon></a>
              <a href="#" aria-label="Twitter"><lucide-icon name="twitter"></lucide-icon></a>
            </div>
          </div>

          <div class="footer-col">
            <h4>Danh mục</h4>
            <ul>
              <li><a routerLink="/products" [queryParams]="{category: 'football'}">Bóng đá</a></li>
              <li><a routerLink="/products" [queryParams]="{category: 'gym'}">Gym & Fitness</a></li>
              <li><a routerLink="/products" [queryParams]="{category: 'running'}">Chạy bộ</a></li>
              <li><a routerLink="/products" [queryParams]="{category: 'swimming'}">Bơi lội</a></li>
              <li><a routerLink="/products" [queryParams]="{category: 'basketball'}">Bóng rổ</a></li>
            </ul>
          </div>

          <div class="footer-col">
            <h4>Hỗ trợ khách hàng</h4>
            <ul>
              <li><a routerLink="/policy/shipping">Chính sách vận chuyển</a></li>
              <li><a routerLink="/policy/return">Chính sách đổi trả</a></li>
              <li><a routerLink="/policy/privacy">Bảo mật thông tin</a></li>
              <li><a routerLink="/faq">Câu hỏi thường gặp (FAQ)</a></li>
              <li><a routerLink="/contact">Liên hệ</a></li>
            </ul>
          </div>

          <div class="footer-col">
            <h4>Thông tin liên hệ</h4>
            <ul class="contact-info">
              <li>
                <lucide-icon name="map-pin"></lucide-icon>
                <span>123 Đường Thể Thao, Quận 1, TP. HCM</span>
              </li>
              <li>
                <lucide-icon name="phone"></lucide-icon>
                <span>1900 1234</span>
              </li>
              <li>
                <lucide-icon name="mail"></lucide-icon>
                <span>support&#64;delta-sports.vn</span>
              </li>
            </ul>
          </div>
        </div>
      </div>
      <div class="footer-bottom">
        <div class="container">
          <p>&copy; 2024 Delta Sports. All rights reserved.</p>
        </div>
      </div>
    </footer>
  `,
  styles: [`
    .footer {
      background-color: var(--color-dark);
      color: var(--color-gray);
      padding-top: 4rem;
      
      h4 {
        color: var(--color-light);
        margin-bottom: 1.5rem;
        font-size: 1.25rem;
      }
    }

    .footer-grid {
      display: grid;
      grid-template-columns: 2fr 1fr 1fr 1fr;
      gap: 3rem;
      margin-bottom: 3rem;

      @media (max-width: 992px) {
        grid-template-columns: 1fr 1fr;
      }

      @media (max-width: 576px) {
        grid-template-columns: 1fr;
      }
    }

    .logo {
      font-size: 1.75rem;
      margin-bottom: 1rem;
      color: var(--color-light);
      
      span {
        color: var(--color-primary);
      }
    }

    .desc {
      margin-bottom: 1.5rem;
      line-height: 1.6;
    }

    .social-links {
      display: flex;
      gap: 1rem;

      a {
        display: flex;
        align-items: center;
        justify-content: center;
        width: 40px;
        height: 40px;
        background-color: var(--color-dark-alt);
        color: var(--color-light);
        border-radius: var(--radius);
        
        &:hover {
          background-color: var(--color-primary);
        }
      }
    }

    ul {
      li {
        margin-bottom: 0.75rem;
        
        a {
          &:hover {
            color: var(--color-primary);
            padding-left: 5px;
          }
        }
      }
    }

    .contact-info {
      li {
        display: flex;
        align-items: flex-start;
        gap: 1rem;
        
        lucide-icon {
          color: var(--color-primary);
          width: 20px;
          height: 20px;
          flex-shrink: 0;
        }
      }
    }

    .footer-bottom {
      background-color: var(--color-dark-alt);
      padding: 1.5rem 0;
      text-align: center;
      font-size: 0.875rem;
    }
  `]
})
export class FooterComponent {
}
