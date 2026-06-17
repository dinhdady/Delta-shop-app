import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http'; // Thêm import này
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-admin-layout',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './admin-layout.component.html',
  styleUrls: ['./admin-layout.component.scss']
})
export class AdminLayoutComponent implements OnInit {
  private authService = inject(AuthService);
  private router = inject(Router);
  private http = inject(HttpClient); // Thêm inject HttpClient

  currentUser = this.authService.currentUser;
  currentRoute = '';
  pendingCount = 0;
  sidebarCollapsed = false;

  constructor() {
    this.router.events.subscribe(() => {
      const url = this.router.url;
      if (url.includes('products')) this.currentRoute = 'Sản phẩm';
      else if (url.includes('categories')) this.currentRoute = 'Danh mục';
      else if (url.includes('brands')) this.currentRoute = 'Thương hiệu';
      else if (url.includes('promotions')) this.currentRoute = 'Khuyến mãi';
      else if (url.includes('orders')) this.currentRoute = 'Đơn hàng';
      else if (url.includes('users')) this.currentRoute = 'Người dùng';
      else if (url.includes('contacts')) this.currentRoute = 'Liên hệ';
      else if (url.includes('dashboard')) this.currentRoute = 'Dashboard';
      else this.currentRoute = '';
    });
  }

  ngOnInit() {
    this.loadPendingCount();
  }

  loadPendingCount() {
    this.http.get<any>('http://localhost:8080/api/contacts/admin/stats')
      .subscribe({
        next: (stats: any) => {
          this.pendingCount = stats.pending || 0;
        },
        error: (error: any) => {
          console.error('Error loading pending count:', error);
          this.pendingCount = 0;
        }
      });
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/auth/login']);
  }

  toggleSidebar() {
    this.sidebarCollapsed = !this.sidebarCollapsed;
  }
}
