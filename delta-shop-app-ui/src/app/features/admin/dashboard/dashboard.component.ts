import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AdminStatsService } from './admin-stats.service';
import { AdminOrderService } from '../orders/admin-order.service';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {
  private statsService = inject(AdminStatsService);
  private orderService = inject(AdminOrderService);

  stats = signal<any>(null);
  recentOrders = signal<any[]>([]);
  loading = signal(true);

  ngOnInit() {
    this.loadDashboardData();
  }

  loadDashboardData() {
    forkJoin({
      stats: this.statsService.getDashboardStats(),
      recentOrders: this.orderService.getOrders(0, 10) // Lấy 10 đơn hàng
    }).subscribe({
      next: (res) => {
        this.stats.set(res.stats);
        this.recentOrders.set(res.recentOrders?.content || []);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Dashboard error:', err);
        this.loading.set(false);
      }
    });
  }

  getStatusLabel(status: string): string {
    const statusMap: Record<string, string> = {
      'PENDING': 'Chờ xử lý',
      'CONFIRMED': 'Đã xác nhận',
      'PROCESSING': 'Đang xử lý',
      'SHIPPED': 'Đang giao',
      'DELIVERED': 'Đã giao',
      'CANCELLED': 'Đã hủy'
    };
    return statusMap[status] || status;
  }

  formatDate(date: string): string {
    if (!date) return '---';
    return new Date(date).toLocaleDateString('vi-VN');
  }
}
