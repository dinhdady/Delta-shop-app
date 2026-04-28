// about.component.ts - Updated with black theme and growth charts
import { Component, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-about',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="about-container">
      <!-- Hero Section -->
      <div class="page-header">
        <div class="container">
          <h1>Về Chúng Tôi</h1>
          <p>Khám phá câu chuyện của Delta Shop - Nơi chất lượng và đam mê gặp nhau</p>
        </div>
      </div>

      <!-- Story Section -->
      <div class="story-section">
        <div class="container">
          <div class="story-grid">
            <div class="story-content animate-slideInLeft">
              <div class="section-badge">Câu Chuyện Của Chúng Tôi</div>
              <h2 class="section-title">Hành trình <span class="highlight">10 năm</span> phát triển</h2>
              <div class="story-text">
                <p>
                  Delta Shop được thành lập vào năm 2014 với sứ mệnh mang đến những sản phẩm chất lượng cao
                  nhất đến tay người tiêu dùng Việt Nam. Từ một cửa hàng nhỏ, chúng tôi đã không ngừng phát triển
                  và trở thành một trong những thương hiệu uy tín hàng đầu trong lĩnh vực thương mại điện tử.
                </p>
                <p>
                  Với triết lý "Khách hàng là trung tâm", chúng tôi luôn nỗ lực cải thiện dịch vụ, đa dạng hóa sản phẩm
                  và tối ưu hóa trải nghiệm mua sắm trực tuyến. Mỗi sản phẩm được chúng tôi lựa chọn kỹ lưỡng,
                  đảm bảo nguồn gốc xuất xứ rõ ràng và chất lượng tốt nhất.
                </p>
              </div>
              <div class="story-stats">
                <div class="stat-item">
                  <div class="stat-number">10+</div>
                  <div class="stat-label">Năm kinh nghiệm</div>
                </div>
                <div class="stat-item">
                  <div class="stat-number">50K+</div>
                  <div class="stat-label">Khách hàng tin tưởng</div>
                </div>
                <div class="stat-item">
                  <div class="stat-number">1000+</div>
                  <div class="stat-label">Sản phẩm chất lượng</div>
                </div>
              </div>
            </div>
            <div class="story-image animate-slideInRight">
              <img src="https://images.unsplash.com/photo-1441986300917-64674bd600d8?w=600&h=400&fit=crop" alt="About us">
              <div class="image-badge">
                <i class="fas fa-heart"></i> Tận tâm phục vụ
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Growth Charts Section -->
      <div class="charts-section">
        <div class="container">
          <div class="section-header">
            <div class="section-badge">Tăng Trưởng Vượt Bậc</div>
            <h2 class="section-title">Hành trình <span class="highlight">phát triển</span> của chúng tôi</h2>
            <p class="section-subtitle">Những con số biết nói về sự tín nhiệm của khách hàng</p>
          </div>

          <div class="charts-grid">
            <!-- Revenue Chart -->
            <div class="chart-card">
              <div class="chart-header">
                <h3>Doanh thu tăng trưởng</h3>
                <p class="chart-subtitle">Tỷ đồng (VNĐ)</p>
              </div>
              <div class="chart-container">
                <div class="bar-chart">
                  <div class="bar-item" *ngFor="let item of revenueData; let i = index">
                    <div class="bar-label">{{ item.year }}</div>
                    <div class="bar-wrapper">
                      <div class="bar" [style.height.%]="item.percent" [style.animationDelay]="(i * 0.15) + 's'">
                        <span class="bar-value">{{ item.value }} tỷ</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
              <div class="chart-trend up">
                <span class="trend-icon">📈</span>
                Tăng trưởng {{ growthData.revenueGrowth }}% so với năm trước
              </div>
            </div>

            <!-- Customer Chart -->
            <div class="chart-card">
              <div class="chart-header">
                <h3>Khách hàng mới</h3>
                <p class="chart-subtitle">Số lượng khách hàng (nghìn người)</p>
              </div>
              <div class="chart-container">
                <div class="line-chart">
                  <svg viewBox="0 0 500 180" class="line-svg">
                    <defs>
                      <linearGradient id="lineGradient" x1="0%" y1="0%" x2="100%" y2="0%">
                        <stop offset="0%" stop-color="#cd4631"/>
                        <stop offset="100%" stop-color="#ff6b4a"/>
                      </linearGradient>
                    </defs>
                    <polyline
                      [attr.points]="customerPoints"
                      class="line-path"
                    />
                    <circle
                      *ngFor="let point of customerPointsArray; let i = index"
                      [attr.cx]="point.x"
                      [attr.cy]="point.y"
                      r="4"
                      class="dot"
                      [style.animation-delay]="(i * 0.2) + 's'"
                    />
                  </svg>
                  <div class="line-labels">
                    <span *ngFor="let item of customerData">{{ item.year }}</span>
                  </div>
                </div>
              </div>
              <div class="chart-trend up">
                <span class="trend-icon">👥</span>
                {{ growthData.customerGrowth }}% khách hàng mới trong năm nay
              </div>
            </div>

            <!-- Satisfaction Chart -->
            <div class="chart-card">
              <div class="chart-header">
                <h3>Độ hài lòng của khách hàng</h3>
                <p class="chart-subtitle">Đánh giá trung bình / 5 sao</p>
              </div>
              <div class="chart-container">
                <div class="radial-chart">
                  <svg viewBox="0 0 200 200" class="radial-svg">
                    <circle cx="100" cy="100" r="80" class="radial-bg"/>
                    <circle
                      cx="100" cy="100" r="80"
                      class="radial-progress"
                      [style.strokeDasharray]="satisfactionCircumference"
                      [style.strokeDashoffset]="satisfactionOffset"
                    />
                    <text x="100" y="95" text-anchor="middle" class="radial-value">{{ satisfactionScore }}</text>
                    <text x="100" y="120" text-anchor="middle" class="radial-label">/ 5 sao</text>
                  </svg>
                </div>
              </div>
              <div class="chart-trend up">
                <span class="trend-icon">⭐</span>
                Tăng {{ growthData.satisfactionGrowth }}% so với năm ngoái
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Mission & Vision -->
      <div class="mission-section">
        <div class="container">
          <div class="mission-grid">
            <div class="mission-card">
              <div class="card-icon">
                <i class="fas fa-bullseye"></i>
              </div>
              <h3>Sứ Mệnh</h3>
              <p>
                Mang đến những sản phẩm chất lượng cao với giá cả hợp lý, cùng dịch vụ khách hàng chuyên nghiệp,
                góp phần nâng cao chất lượng cuộc sống của người Việt.
              </p>
            </div>
            <div class="vision-card">
              <div class="card-icon">
                <i class="fas fa-eye"></i>
              </div>
              <h3>Tầm Nhìn</h3>
              <p>
                Trở thành nền tảng thương mại điện tử hàng đầu Đông Nam Á, nơi khách hàng luôn tin tưởng
                và lựa chọn đầu tiên khi mua sắm trực tuyến.
              </p>
            </div>
            <div class="value-card">
              <div class="card-icon">
                <i class="fas fa-gem"></i>
              </div>
              <h3>Giá Trị Cốt Lõi</h3>
              <p>
                Chất lượng - Uy tín - Sáng tạo - Tận tâm - Trách nhiệm với cộng đồng và môi trường.
              </p>
            </div>
          </div>
        </div>
      </div>

      <!-- Values Section -->
      <div class="values-section">
        <div class="container">
          <div class="section-header">
            <div class="section-badge">Giá Trị Cốt Lõi</div>
            <h2 class="section-title">Điều <span class="highlight">chúng tôi</span> theo đuổi</h2>
            <p class="section-subtitle">5 giá trị làm nên thương hiệu Delta Shop</p>
          </div>
          <div class="values-grid">
            <div class="value-item">
              <div class="value-icon">
                <i class="fas fa-star"></i>
              </div>
              <h4>Chất lượng hàng đầu</h4>
              <p>Cam kết sản phẩm chính hãng, nguồn gốc rõ ràng</p>
            </div>
            <div class="value-item">
              <div class="value-icon">
                <i class="fas fa-shield-alt"></i>
              </div>
              <h4>Uy tín tuyệt đối</h4>
              <p>Luôn giữ lời hứa với khách hàng</p>
            </div>
            <div class="value-item">
              <div class="value-icon">
                <i class="fas fa-lightbulb"></i>
              </div>
              <h4>Sáng tạo không ngừng</h4>
              <p>Đổi mới công nghệ và dịch vụ</p>
            </div>
            <div class="value-item">
              <div class="value-icon">
                <i class="fas fa-heart"></i>
              </div>
              <h4>Tận tâm phục vụ</h4>
              <p>Khách hàng là trung tâm</p>
            </div>
            <div class="value-item">
              <div class="value-icon">
                <i class="fas fa-leaf"></i>
              </div>
              <h4>Phát triển bền vững</h4>
              <p>Trách nhiệm với cộng đồng và môi trường</p>
            </div>
          </div>
        </div>
      </div>

      <!-- Team Section -->
      <div class="team-section">
        <div class="container">
          <div class="section-header">
            <div class="section-badge">Đội Ngũ Của Chúng Tôi</div>
            <h2 class="section-title">Gặp gỡ <span class="highlight">đội ngũ</span> Delta</h2>
            <p class="section-subtitle">Những con người tâm huyết làm nên sự khác biệt</p>
          </div>
          <div class="team-grid">
            <div class="team-card">
              <div class="team-image">
                <img src="https://images.unsplash.com/photo-1560250097-0b93528c311a?w=400&h=400&fit=crop" alt="CEO">
              </div>
              <h4>Ethan Carter</h4>
              <p class="team-role">CEO & Founder</p>
              <p class="team-desc">Với hơn 15 năm kinh nghiệm trong lĩnh vực thương mại điện tử</p>
              <div class="team-social">
                <a href="#"><i class="fab fa-facebook"></i></a>
                <a href="#"><i class="fab fa-linkedin"></i></a>
                <a href="#"><i class="fab fa-twitter"></i></a>
              </div>
            </div>
            <div class="team-card">
              <div class="team-image">
                <img src="https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?w=400&h=400&fit=crop" alt="Marketing Director">
              </div>
              <h4>Victoria Sterling</h4>
              <p class="team-role">Marketing Director</p>
              <p class="team-desc">Chuyên gia chiến lược thương hiệu và tiếp thị số</p>
              <div class="team-social">
                <a href="#"><i class="fab fa-facebook"></i></a>
                <a href="#"><i class="fab fa-linkedin"></i></a>
                <a href="#"><i class="fab fa-twitter"></i></a>
              </div>
            </div>
            <div class="team-card">
              <div class="team-image">
                <img src="https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=400&h=400&fit=crop" alt="Technical Director">
              </div>
              <h4>Kaelen Vance</h4>
              <p class="team-role">Technical Director</p>
              <p class="team-desc">Đam mê công nghệ và giải pháp đột phá</p>
              <div class="team-social">
                <a href="#"><i class="fab fa-facebook"></i></a>
                <a href="#"><i class="fab fa-linkedin"></i></a>
                <a href="#"><i class="fab fa-twitter"></i></a>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Contact CTA -->
      <div class="cta-section">
        <div class="container">
          <div class="cta-content">
            <h2>Bạn muốn hợp tác cùng chúng tôi?</h2>
            <p>Hãy liên hệ ngay để trở thành đối tác của Delta Shop</p>
            <div class="cta-buttons">
              <a routerLink="/contact" class="btn-primary">
                <i class="fas fa-envelope"></i> Liên hệ ngay
              </a>
              <a routerLink="/products" class="btn-secondary">
                <i class="fas fa-shopping-cart"></i> Mua sắm ngay
              </a>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .about-container {
      min-height: 100vh;
      background: #f8f8f8;
      overflow-x: hidden;
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

    /* Charts Section */
    .charts-section {
      padding: 80px 0;
      background: #ffffff;
    }

    .charts-grid {
      display: grid;
      grid-template-columns: repeat(3, 1fr);
      gap: 30px;
      margin-top: 50px;
    }

    .chart-card {
      background: #ffffff;
      border-radius: 12px;
      padding: 25px;
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
      transition: all 0.3s ease;
      border: 1px solid #eaeaea;
      opacity: 0;
      animation: fadeInUp 0.5s ease forwards;
    }

    .chart-card:nth-child(1) { animation-delay: 0.05s; }
    .chart-card:nth-child(2) { animation-delay: 0.1s; }
    .chart-card:nth-child(3) { animation-delay: 0.15s; }

    .chart-card:hover {
      transform: translateY(-4px);
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
      border-color: #cd4631;
    }

    .chart-header {
      text-align: center;
      margin-bottom: 25px;
    }

    .chart-header h3 {
      font-size: 18px;
      font-weight: 700;
      color: #1a1a1a;
      margin-bottom: 5px;
    }

    .chart-subtitle {
      font-size: 12px;
      color: #999;
    }

    /* Bar Chart */
    .bar-chart {
      display: flex;
      justify-content: space-around;
      align-items: flex-end;
      height: 250px;
      padding: 20px 0;
    }

    .bar-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 10px;
      flex: 1;
    }

    .bar-label {
      font-size: 12px;
      color: #666;
      font-weight: 600;
    }

    .bar-wrapper {
      width: 60px;
      height: 180px;
      background: #f0f0f0;
      border-radius: 10px;
      overflow: hidden;
      position: relative;
    }

    .bar {
      position: absolute;
      bottom: 0;
      width: 100%;
      background: linear-gradient(180deg, #cd4631, #e86b4a);
      border-radius: 10px;
      transition: height 1s ease-out;
      height: 0;
      animation: growBar 1s ease-out forwards;
      display: flex;
      align-items: flex-start;
      justify-content: center;
    }

    @keyframes growBar {
      from { height: 0; }
      to { height: var(--target-height); }
    }

    .bar-value {
      position: absolute;
      top: -25px;
      font-size: 11px;
      font-weight: 600;
      color: #cd4631;
    }

    /* Line Chart */
    .line-chart {
      padding: 20px 0;
    }

    .line-svg {
      width: 100%;
      height: 180px;
    }

    .line-path {
      fill: none;
      stroke: #cd4631;
      stroke-width: 3;
      stroke-dasharray: 1000;
      stroke-dashoffset: 1000;
      animation: drawLine 2s ease-out forwards;
    }

    @keyframes drawLine {
      from { stroke-dashoffset: 1000; }
      to { stroke-dashoffset: 0; }
    }

    .dot {
      fill: #cd4631;
      opacity: 0;
      animation: fadeInScale 0.5s ease-out forwards;
    }

    @keyframes fadeInScale {
      from {
        opacity: 0;
        transform: scale(0);
      }
      to {
        opacity: 1;
        transform: scale(1);
      }
    }

    .line-labels {
      display: flex;
      justify-content: space-around;
      margin-top: 10px;
    }

    .line-labels span {
      font-size: 12px;
      color: #666;
    }

    /* Radial Chart */
    .radial-chart {
      display: flex;
      justify-content: center;
      align-items: center;
      padding: 20px;
    }

    .radial-svg {
      width: 180px;
      height: 180px;
      transform: rotate(-90deg);
    }

    .radial-bg {
      fill: none;
      stroke: #f0f0f0;
      stroke-width: 12;
    }

    .radial-progress {
      fill: none;
      stroke: #cd4631;
      stroke-width: 12;
      stroke-linecap: round;
      stroke-dasharray: 502.4;
      stroke-dashoffset: 502.4;
      animation: fillRadial 1.5s ease-out forwards;
    }

    @keyframes fillRadial {
      from { stroke-dashoffset: 502.4; }
      to { stroke-dashoffset: var(--target-offset); }
    }

    .radial-value {
      fill: #1a1a1a;
      font-size: 32px;
      font-weight: 800;
      transform: rotate(90deg);
    }

    .radial-label {
      fill: #999;
      font-size: 12px;
      transform: rotate(90deg);
    }

    /* Chart Trend */
    .chart-trend {
      margin-top: 20px;
      padding: 12px;
      background: #f8f9fa;
      border-radius: 12px;
      text-align: center;
      font-size: 14px;
      font-weight: 600;
    }

    .chart-trend.up {
      background: #e8f5e9;
      color: #2e7d32;
    }

    .trend-icon {
      margin-right: 8px;
    }

    /* Section Common */
    .section-badge {
      display: inline-block;
      padding: 6px 16px;
      background: #cd4631;
      color: white;
      border-radius: 30px;
      font-size: 14px;
      font-weight: 600;
      margin-bottom: 20px;
    }

    .section-title {
      font-size: 40px;
      font-weight: 800;
      color: #1a1a1a;
      margin-bottom: 20px;
    }

    .section-title .highlight {
      color: #cd4631;
    }

    .section-subtitle {
      font-size: 18px;
      color: #666;
      max-width: 600px;
      margin: 0 auto;
    }

    .section-header {
      text-align: center;
      margin-bottom: 50px;
    }

    /* Story Section */
    .story-section {
      padding: 80px 0;
      background: #ffffff;
    }

    .story-grid {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 60px;
      align-items: center;
    }

    .story-content {
      opacity: 0;
      animation: slideInLeft 0.6s ease forwards;
    }

    .story-image {
      opacity: 0;
      animation: slideInRight 0.6s ease forwards;
    }

    .story-text p {
      font-size: 16px;
      line-height: 1.8;
      color: #555;
      margin-bottom: 20px;
    }

    .story-stats {
      display: grid;
      grid-template-columns: repeat(3, 1fr);
      gap: 30px;
      margin-top: 40px;
    }

    .stat-item {
      text-align: center;
      transition: all 0.3s ease;
    }

    .stat-item:hover {
      transform: translateY(-5px);
    }

    .stat-number {
      font-size: 36px;
      font-weight: 800;
      color: #cd4631;
      margin-bottom: 10px;
    }

    .stat-label {
      font-size: 14px;
      color: #777;
    }

    .story-image {
      position: relative;
      border-radius: 12px;
      overflow: hidden;
      box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
    }

    .story-image img {
      width: 100%;
      height: auto;
      display: block;
    }

    .image-badge {
      position: absolute;
      bottom: 20px;
      right: 20px;
      background: #cd4631;
      color: white;
      padding: 10px 20px;
      border-radius: 30px;
      font-weight: 600;
      font-size: 14px;
    }

    /* Mission Section */
    .mission-section {
      padding: 80px 0;
      background: #f8f8f8;
    }

    .mission-grid {
      display: grid;
      grid-template-columns: repeat(3, 1fr);
      gap: 30px;
    }

    .mission-card, .vision-card, .value-card {
      background: #ffffff;
      padding: 40px 30px;
      border-radius: 12px;
      text-align: center;
      transition: all 0.3s ease;
      border: 1px solid #eaeaea;
      opacity: 0;
      animation: fadeInUp 0.5s ease forwards;
    }

    .mission-card:nth-child(1) { animation-delay: 0.05s; }
    .vision-card:nth-child(2) { animation-delay: 0.1s; }
    .value-card:nth-child(3) { animation-delay: 0.15s; }

    .mission-card:hover, .vision-card:hover, .value-card:hover {
      transform: translateY(-4px);
      border-color: #cd4631;
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
    }

    .card-icon {
      width: 70px;
      height: 70px;
      background: #cd4631;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      margin: 0 auto 20px;
      transition: all 0.3s ease;
    }

    .mission-card:hover .card-icon,
    .vision-card:hover .card-icon,
    .value-card:hover .card-icon {
      transform: scale(1.05);
    }

    .card-icon i {
      font-size: 30px;
      color: white;
    }

    .mission-card h3, .vision-card h3, .value-card h3 {
      font-size: 1.3rem;
      font-weight: 700;
      color: #1a1a1a;
      margin-bottom: 15px;
    }

    .mission-card p, .vision-card p, .value-card p {
      font-size: 14px;
      color: #666;
      line-height: 1.6;
    }

    /* Values Section */
    .values-section {
      padding: 80px 0;
      background: #ffffff;
    }

    .values-grid {
      display: grid;
      grid-template-columns: repeat(5, 1fr);
      gap: 30px;
    }

    .value-item {
      text-align: center;
      padding: 30px 20px;
      background: #f8f8f8;
      border-radius: 12px;
      transition: all 0.3s ease;
      border: 1px solid #eaeaea;
      opacity: 0;
      animation: fadeInUp 0.5s ease forwards;
    }

    .value-item:nth-child(1) { animation-delay: 0.05s; }
    .value-item:nth-child(2) { animation-delay: 0.1s; }
    .value-item:nth-child(3) { animation-delay: 0.15s; }
    .value-item:nth-child(4) { animation-delay: 0.2s; }
    .value-item:nth-child(5) { animation-delay: 0.25s; }

    .value-item:hover {
      transform: translateY(-4px);
      background: #cd4631;
      border-color: #cd4631;
    }

    .value-item:hover .value-icon i,
    .value-item:hover h4,
    .value-item:hover p {
      color: white;
    }

    .value-icon {
      width: 60px;
      height: 60px;
      background: white;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      margin: 0 auto 20px;
      box-shadow: 0 5px 15px rgba(0, 0, 0, 0.05);
    }

    .value-icon i {
      font-size: 28px;
      color: #cd4631;
    }

    .value-item h4 {
      font-size: 1rem;
      font-weight: 700;
      color: #1a1a1a;
      margin-bottom: 10px;
    }

    .value-item p {
      font-size: 13px;
      color: #777;
    }

    /* Team Section */
    .team-section {
      padding: 80px 0;
      background: #f8f8f8;
    }

    .team-grid {
      display: grid;
      grid-template-columns: repeat(3, 1fr);
      gap: 40px;
    }

    .team-card {
      background: #ffffff;
      border-radius: 12px;
      padding: 30px;
      text-align: center;
      transition: all 0.3s ease;
      border: 1px solid #eaeaea;
      opacity: 0;
      animation: fadeInUp 0.5s ease forwards;
    }

    .team-card:nth-child(1) { animation-delay: 0.05s; }
    .team-card:nth-child(2) { animation-delay: 0.1s; }
    .team-card:nth-child(3) { animation-delay: 0.15s; }

    .team-card:hover {
      transform: translateY(-4px);
      border-color: #cd4631;
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
    }

    .team-image {
      width: 150px;
      height: 150px;
      border-radius: 50%;
      overflow: hidden;
      margin: 0 auto 20px;
      border: 3px solid #cd4631;
      transition: all 0.3s ease;
    }

    .team-card:hover .team-image {
      transform: scale(1.02);
    }

    .team-image img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .team-card h4 {
      font-size: 1.2rem;
      font-weight: 700;
      color: #1a1a1a;
      margin-bottom: 5px;
    }

    .team-role {
      color: #cd4631;
      font-weight: 600;
      font-size: 0.85rem;
      margin-bottom: 15px;
    }

    .team-desc {
      font-size: 13px;
      color: #666;
      margin-bottom: 20px;
    }

    .team-social {
      display: flex;
      justify-content: center;
      gap: 15px;
    }

    .team-social a {
      width: 35px;
      height: 35px;
      background: #f0f0f0;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #cd4631;
      transition: all 0.3s ease;
    }

    .team-social a:hover {
      background: #cd4631;
      color: white;
      transform: translateY(-3px);
    }

    /* CTA Section */
    .cta-section {
      padding: 80px 0;
      background: #000000;
      text-align: center;
      border-top: 1px solid rgba(205, 70, 49, 0.3);
    }

    .cta-content h2 {
      font-size: 36px;
      font-weight: 800;
      color: #ffffff;
      margin-bottom: 15px;
    }

    .cta-content p {
      font-size: 18px;
      color: #a0a0a0;
      margin-bottom: 30px;
    }

    .cta-buttons {
      display: flex;
      gap: 20px;
      justify-content: center;
    }

    .btn-primary, .btn-secondary {
      padding: 12px 32px;
      border-radius: 40px;
      font-weight: 600;
      transition: all 0.3s ease;
      display: inline-flex;
      align-items: center;
      gap: 10px;
      text-decoration: none;
    }

    .btn-primary {
      background: #cd4631;
      color: white;
    }

    .btn-primary:hover {
      background: #b83a26;
      transform: translateY(-2px);
      box-shadow: 0 5px 15px rgba(205, 70, 49, 0.4);
    }

    .btn-secondary {
      background: transparent;
      color: white;
      border: 2px solid #cd4631;
    }

    .btn-secondary:hover {
      background: #cd4631;
      transform: translateY(-2px);
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

    /* Responsive */
    @media (max-width: 1024px) {
      .charts-grid {
        grid-template-columns: repeat(2, 1fr);
      }
      .values-grid {
        grid-template-columns: repeat(3, 1fr);
      }
    }

    @media (max-width: 768px) {
      .page-header h1 {
        font-size: 32px;
      }
      .page-header p {
        font-size: 14px;
      }
      .charts-grid {
        grid-template-columns: 1fr;
      }
      .story-grid {
        grid-template-columns: 1fr;
      }
      .mission-grid {
        grid-template-columns: 1fr;
      }
      .values-grid {
        grid-template-columns: repeat(2, 1fr);
      }
      .team-grid {
        grid-template-columns: 1fr;
      }
      .section-title {
        font-size: 28px;
      }
      .cta-buttons {
        flex-direction: column;
        align-items: center;
      }
      .story-stats {
        gap: 20px;
      }
      .stat-number {
        font-size: 28px;
      }
    }

    @media (max-width: 480px) {
      .page-header h1 {
        font-size: 28px;
      }
      .values-grid {
        grid-template-columns: 1fr;
      }
      .cta-content h2 {
        font-size: 24px;
      }
    }
  `]
})
export class AboutComponent implements AfterViewInit {
  // Revenue data for bar chart
  revenueData = [
    { year: '2020', value: 25, percent: 25 },
    { year: '2021', value: 42, percent: 42 },
    { year: '2022', value: 68, percent: 68 },
    { year: '2023', value: 95, percent: 95 },
    { year: '2024', value: 145, percent: 100 }
  ];

  // Customer data for line chart
  customerData = [
    { year: '2020', value: 12, x: 0, y: 160 },
    { year: '2021', value: 18, x: 125, y: 140 },
    { year: '2022', value: 28, x: 250, y: 110 },
    { year: '2023', value: 42, x: 375, y: 70 },
    { year: '2024', value: 58, x: 500, y: 35 }
  ];

  // Growth data
  growthData = {
    revenueGrowth: 52.6,
    customerGrowth: 38.2,
    satisfactionGrowth: 12.5
  };

  // Satisfaction score
  satisfactionScore = 4.8;
  satisfactionCircumference = 502.4;
  satisfactionOffset = 502.4 - (4.8 / 5) * 502.4;

  get customerPoints(): string {
    return this.customerData.map(d => `${d.x},${d.y}`).join(' ');
  }

  get customerPointsArray(): { x: number; y: number }[] {
    return this.customerData;
  }

  ngAfterViewInit() {
    // Set CSS variables for bar heights
    setTimeout(() => {
      const bars = document.querySelectorAll('.bar');
      bars.forEach((bar, index) => {
        const percent = this.revenueData[index]?.percent || 0;
        (bar as HTMLElement).style.setProperty('--target-height', `${percent}%`);
      });

      // Set radial chart offset
      const radialProgress = document.querySelector('.radial-progress');
      if (radialProgress) {
        (radialProgress as HTMLElement).style.setProperty('--target-offset', `${this.satisfactionOffset}px`);
      }
    }, 100);
  }
}
