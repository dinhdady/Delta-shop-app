// contact.component.ts
import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { LucideAngularModule, MapPin, Phone, Mail, Clock, Facebook, Instagram, Twitter, Send } from 'lucide-angular';
import { ToastrService } from 'ngx-toastr';

interface ContactForm {
  name: string;
  email: string;
  phone: string;
  subject: string;
  message: string;
}

@Component({
  selector: 'app-contact',
  standalone: true,
  imports: [CommonModule, FormsModule, LucideAngularModule],
  template: `
    <div class="contact-container">
      <div class="page-header">
        <div class="container">
          <h1>LIÊN HỆ</h1>
          <p>Chúng tôi luôn sẵn sàng lắng nghe bạn</p>
        </div>
      </div>

      <section class="section">
        <div class="container">
          <!-- Contact Info Cards -->
          <div class="contact-cards">
            <div class="contact-card animate-fadeInUp">
              <div class="icon-wrapper">
                <lucide-icon name="map-pin" [size]="32"></lucide-icon>
              </div>
              <h3>Địa chỉ</h3>
              <p>123 Đường Thể Thao, Quận 1</p>
              <p>TP. Hồ Chí Minh, Việt Nam</p>
            </div>

            <div class="contact-card animate-fadeInUp" style="animation-delay: 0.1s">
              <div class="icon-wrapper">
                <lucide-icon name="phone" [size]="32"></lucide-icon>
              </div>
              <h3>Điện thoại</h3>
              <p>Hotline: <a href="tel:19001009">1900 1009</a></p>
              <p>CSKH: <a href="tel:02812345678">(028) 1234 5678</a></p>
            </div>

            <div class="contact-card animate-fadeInUp" style="animation-delay: 0.2s">
              <div class="icon-wrapper">
                <lucide-icon name="mail" [size]="32"></lucide-icon>
              </div>
              <h3>Email</h3>
              <p><a href="mailto:info&#64;delta-sports.vn">info&#64;delta-sports.vn</a></p>
              <p><a href="mailto:care&#64;delta-sports.vn">care&#64;delta-sports.vn</a></p>
            </div>

            <div class="contact-card animate-fadeInUp" style="animation-delay: 0.3s">
              <div class="icon-wrapper">
                <lucide-icon name="clock" [size]="32"></lucide-icon>
              </div>
              <h3>Giờ làm việc</h3>
              <p>Thứ 2 - Thứ 7: 8:00 - 21:00</p>
              <p>Chủ Nhật: 9:00 - 18:00</p>
            </div>
          </div>

          <!-- Contact Form & Map -->
          <div class="contact-wrapper">
            <div class="contact-form-container animate-slideInLeft">
              <h2>GỬI TIN NHẮN CHO CHÚNG TÔI</h2>
              <form (ngSubmit)="onSubmit()" #contactForm="ngForm" class="contact-form">
                <div class="form-row">
                  <div class="form-group">
                    <label>Họ và tên *</label>
                    <input
                      type="text"
                      [(ngModel)]="formData.name"
                      name="name"
                      required
                      placeholder="Nhập họ tên của bạn"
                      class="form-control"
                    >
                  </div>
                  <div class="form-group">
                    <label>Email *</label>
                    <input
                      type="email"
                      [(ngModel)]="formData.email"
                      name="email"
                      required
                      email
                      placeholder="example&#64;email.com"
                      class="form-control"
                    >
                  </div>
                </div>

                <div class="form-row">
                  <div class="form-group">
                    <label>Số điện thoại</label>
                    <input
                      type="tel"
                      [(ngModel)]="formData.phone"
                      name="phone"
                      placeholder="Nhập số điện thoại"
                      class="form-control"
                    >
                  </div>
                  <div class="form-group">
                    <label>Tiêu đề *</label>
                    <input
                      type="text"
                      [(ngModel)]="formData.subject"
                      name="subject"
                      required
                      placeholder="Tiêu đề tin nhắn"
                      class="form-control"
                    >
                  </div>
                </div>

                <div class="form-group full-width">
                  <label>Nội dung *</label>
                  <textarea
                    [(ngModel)]="formData.message"
                    name="message"
                    required
                    rows="6"
                    placeholder="Nhập nội dung tin nhắn của bạn..."
                    class="form-control"
                  ></textarea>
                </div>

                <button type="submit" class="btn btn-primary btn-submit" [disabled]="contactForm.invalid">
                  <lucide-icon name="send" [size]="18"></lucide-icon>
                  GỬI TIN NHẮN
                </button>
              </form>
            </div>

            <div class="map-container animate-slideInRight">
              <iframe
                src="https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d3919.123456789012!2d106.70000000000001!3d10.776889999999999!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x3175291234567890%3A0x1234567890abcdef!2zSOG7kyBDaMOtIE1pbmgsIFZp4buHdCBOYW0!5e0!3m2!1svi!2s!4v1234567890123!5m2!1svi!2s"
                width="100%"
                height="100%"
                style="border:0;"
                allowfullscreen=""
                loading="lazy"
                referrerpolicy="no-referrer-when-downgrade">
              </iframe>
            </div>
          </div>

          <!-- Social Links -->
          <div class="social-section animate-fadeInUp">
            <h3>KẾT NỐI VỚI CHÚNG TÔI</h3>
            <div class="social-links">
              <a href="#" class="social-link">
                <lucide-icon name="facebook" [size]="24"></lucide-icon>
                <span>Facebook</span>
              </a>
              <a href="#" class="social-link">
                <lucide-icon name="instagram" [size]="24"></lucide-icon>
                <span>Instagram</span>
              </a>
              <a href="#" class="social-link">
                <lucide-icon name="twitter" [size]="24"></lucide-icon>
                <span>Twitter</span>
              </a>
            </div>
          </div>
        </div>
      </section>
    </div>
  `,
 // contact.component.ts - Updated styles
 styles: [`
   .contact-container {
     min-height: 100vh;
     background: #f8f8f8;
   }

   .container {
     max-width: 1200px;
     margin: 0 auto;
     padding: 0 20px;
   }

   /* Header - Nền đen */
   .page-header {
     background: #000000;
     padding: 60px 0;
     text-align: center;
     position: relative;
     border-bottom: 1px solid rgba(205, 70, 49, 0.3);
   }

   .page-header::before {
     content: '';
     position: absolute;
     top: 0;
     left: 0;
     right: 0;
     bottom: 0;
     background: radial-gradient(circle at 50% 0%, rgba(205, 70, 49, 0.08) 0%, transparent 70%);
     pointer-events: none;
   }

   .page-header h1 {
     font-size: 48px;
     font-weight: 800;
     color: #ffffff;
     margin-bottom: 16px;
     letter-spacing: -0.5px;
   }

   .page-header p {
     font-size: 18px;
     color: #a0a0a0;
     max-width: 600px;
     margin: 0 auto;
   }

   /* Contact Cards */
   .contact-cards {
     display: grid;
     grid-template-columns: repeat(4, 1fr);
     gap: 1.5rem;
     margin-bottom: 4rem;
     margin-top: -50px;
     position: relative;
     z-index: 10;
   }

   @media (max-width: 992px) {
     .contact-cards {
       grid-template-columns: repeat(2, 1fr);
     }
   }

   @media (max-width: 576px) {
     .contact-cards {
       grid-template-columns: 1fr;
     }
   }

   .contact-card {
     background: #ffffff;
     padding: 2rem;
     text-align: center;
     border-radius: 12px;
     box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
     transition: all 0.3s ease;
     border: 1px solid #eaeaea;
     opacity: 0;
     animation: fadeInUp 0.5s ease forwards;
   }

   .contact-card:nth-child(1) { animation-delay: 0.05s; }
   .contact-card:nth-child(2) { animation-delay: 0.1s; }
   .contact-card:nth-child(3) { animation-delay: 0.15s; }
   .contact-card:nth-child(4) { animation-delay: 0.2s; }

   .contact-card:hover {
     transform: translateY(-4px);
     box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
     border-color: #cd4631;
   }

   .contact-card .icon-wrapper {
     width: 65px;
     height: 65px;
     background: #cd4631;
     border-radius: 50%;
     display: flex;
     align-items: center;
     justify-content: center;
     margin: 0 auto 1.5rem;
     transition: all 0.3s ease;
   }

   .contact-card:hover .icon-wrapper {
     transform: scale(1.05);
     background: #b83a26;
   }

   .contact-card .icon-wrapper lucide-icon {
     color: white;
   }

   .contact-card h3 {
     font-size: 1.2rem;
     margin-bottom: 1rem;
     color: #1a1a1a;
     font-weight: 700;
   }

   .contact-card p {
     color: #777;
     margin-bottom: 0.5rem;
     font-size: 14px;
   }

   .contact-card p a {
     color: #777;
     text-decoration: none;
     transition: color 0.2s ease;
   }

   .contact-card p a:hover {
     color: #cd4631;
   }

   /* Contact Wrapper */
   .contact-wrapper {
     display: grid;
     grid-template-columns: 1fr 1fr;
     gap: 3rem;
     margin-bottom: 4rem;
   }

   @media (max-width: 768px) {
     .contact-wrapper {
       grid-template-columns: 1fr;
     }
   }

   .contact-form-container {
     background: #ffffff;
     padding: 2.5rem;
     border-radius: 12px;
     box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
     border: 1px solid #eaeaea;
     opacity: 0;
     animation: slideInLeft 0.6s ease forwards;
   }

   .contact-form-container h2 {
     font-size: 1.6rem;
     margin-bottom: 2rem;
     color: #1a1a1a;
     position: relative;
     padding-bottom: 0.75rem;
     font-weight: 700;
   }

   .contact-form-container h2::after {
     content: '';
     position: absolute;
     bottom: 0;
     left: 0;
     width: 60px;
     height: 3px;
     background: #cd4631;
     border-radius: 2px;
   }

   .contact-form .form-row {
     display: grid;
     grid-template-columns: 1fr 1fr;
     gap: 1.5rem;
     margin-bottom: 1.5rem;
   }

   @media (max-width: 576px) {
     .contact-form .form-row {
       grid-template-columns: 1fr;
     }
   }

   .contact-form .form-group {
     text-align: left;
   }

   .contact-form .form-group label {
     display: block;
     font-weight: 600;
     margin-bottom: 0.5rem;
     color: #333;
     font-size: 0.85rem;
   }

   .contact-form .form-group.full-width {
     width: 100%;
   }

   .contact-form .form-control {
     width: 100%;
     padding: 0.75rem 1rem;
     border: 1px solid #e0e0e0;
     border-radius: 8px;
     font-size: 14px;
     transition: all 0.2s ease;
     font-family: inherit;
     background: #ffffff;
   }

   .contact-form .form-control:focus {
     outline: none;
     border-color: #cd4631;
     box-shadow: 0 0 0 3px rgba(205, 70, 49, 0.1);
   }

   .contact-form .form-control.ng-invalid.ng-touched {
     border-color: #dc3545;
   }

   .contact-form textarea.form-control {
     resize: vertical;
     min-height: 120px;
   }

   .btn-submit {
     display: inline-flex;
     align-items: center;
     gap: 0.75rem;
     padding: 0.75rem 2rem;
     font-size: 14px;
     margin-top: 1rem;
     background: #cd4631;
     color: white;
     border: none;
     border-radius: 40px;
     font-weight: 600;
     cursor: pointer;
     transition: all 0.3s ease;
   }

   .btn-submit:hover:not(:disabled) {
     background: #b83a26;
     transform: translateY(-2px);
     box-shadow: 0 5px 15px rgba(205, 70, 49, 0.3);
   }

   .btn-submit:disabled {
     opacity: 0.6;
     cursor: not-allowed;
   }

   .btn-submit lucide-icon {
     transition: transform 0.2s ease;
   }

   .btn-submit:hover lucide-icon {
     transform: translateX(4px);
   }

   /* Map Container */
   .map-container {
     background: #ffffff;
     border-radius: 12px;
     overflow: hidden;
     box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
     border: 1px solid #eaeaea;
     min-height: 450px;
     transition: all 0.3s ease;
     opacity: 0;
     animation: slideInRight 0.6s ease forwards;
   }

   .map-container:hover {
     transform: translateY(-4px);
     box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
     border-color: #cd4631;
   }

   .map-container iframe {
     width: 100%;
     height: 100%;
     min-height: 450px;
   }

   /* Social Section */
   .social-section {
     text-align: center;
     padding: 3rem 0 2rem;
     border-top: 1px solid #eaeaea;
     opacity: 0;
     animation: fadeInUp 0.6s ease 0.3s forwards;
   }

   .social-section h3 {
     font-size: 1.4rem;
     margin-bottom: 2rem;
     color: #1a1a1a;
     font-weight: 700;
   }

   .social-links {
     display: flex;
     justify-content: center;
     gap: 2rem;
     flex-wrap: wrap;
   }

   .social-links .social-link {
     display: inline-flex;
     align-items: center;
     gap: 0.75rem;
     padding: 0.7rem 1.5rem;
     background: #ffffff;
     border: 1px solid #e0e0e0;
     border-radius: 40px;
     text-decoration: none;
     color: #555;
     font-weight: 600;
     font-size: 14px;
     transition: all 0.3s ease;
   }

   .social-links .social-link lucide-icon {
     transition: all 0.3s ease;
     color: #cd4631;
   }

   .social-links .social-link:hover {
     background: #cd4631;
     border-color: #cd4631;
     color: white;
     transform: translateY(-3px);
     box-shadow: 0 5px 15px rgba(205, 70, 49, 0.3);
   }

   .social-links .social-link:hover lucide-icon {
     transform: scale(1.15);
     color: white;
   }

   /* Animations */
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

   @keyframes slideInLeft {
     from {
       opacity: 0;
       transform: translateX(-40px);
     }
     to {
       opacity: 1;
       transform: translateX(0);
     }
   }

   @keyframes slideInRight {
     from {
       opacity: 0;
       transform: translateX(40px);
     }
     to {
       opacity: 1;
       transform: translateX(0);
     }
   }

   @keyframes pulse {
     0%, 100% { transform: scale(1); opacity: 0.5; }
     50% { transform: scale(1.05); opacity: 0.8; }
   }

   /* Responsive */
   @media (max-width: 768px) {
     .page-header h1 {
       font-size: 32px;
     }
     .page-header p {
       font-size: 14px;
     }
     .contact-form-container {
       padding: 1.5rem;
     }
     .contact-card {
       padding: 1.5rem;
     }
     .contact-cards {
       margin-top: -30px;
     }
     .contact-form-container h2 {
       font-size: 1.3rem;
     }
   }

   @media (max-width: 480px) {
     .page-header h1 {
       font-size: 28px;
     }
     .social-links .social-link span {
       display: none;
     }
     .social-links .social-link {
       padding: 0.75rem;
     }
     .btn-submit {
       width: 100%;
       justify-content: center;
     }
     .contact-card .icon-wrapper {
       width: 55px;
       height: 55px;
     }
   }
 `]
})
export class ContactComponent {
  private http = inject(HttpClient);
  private toastr = inject(ToastrService);

  formData: ContactForm = {
    name: '',
    email: '',
    phone: '',
    subject: '',
    message: ''
  };

  onSubmit(): void {
    console.log('Form submitted:', this.formData);

    // Gọi API gửi contact
    this.http.post('http://localhost:8080/api/contacts', this.formData)
      .subscribe({
        next: (response: any) => {
          console.log('Contact submitted successfully:', response);
          this.toastr.success('Cảm ơn bạn đã liên hệ! Chúng tôi sẽ phản hồi sớm nhất có thể.');
          this.resetForm();
        },
        error: (error: any) => {
          console.error('Error submitting contact:', error);
          this.toastr.error('Có lỗi xảy ra, vui lòng thử lại sau.');
        }
      });
  }

  resetForm(): void {
    this.formData = {
      name: '',
      email: '',
      phone: '',
      subject: '',
      message: ''
    };
  }
}
