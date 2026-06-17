import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AdminStatsService, TopProductItem } from './admin-stats.service';
import { AdminOrderService } from '../orders/admin-order.service';
import { catchError, forkJoin, of } from 'rxjs';

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
  salesChart = signal<any>(null);
  orderStatusChart = signal<any>(null);
  categoryChart = signal<any>(null);
  topProducts = signal<TopProductItem[]>([]);
  topProductsPeriod = signal('30 ngày qua');
  loading = signal(true);

  ngOnInit() {
    this.loadDashboardData();
  }

  loadDashboardData() {
    forkJoin({
      stats: this.statsService.getDashboardStats().pipe(catchError(() => of(null))),
      recentOrders: this.orderService.getOrders(0, 10).pipe(catchError(() => of({ content: [] }))),
      salesChart: this.statsService.getSalesChart().pipe(catchError(() => of(null))),
      orderStatusChart: this.statsService.getOrderStatusChart().pipe(catchError(() => of(null))),
      categoryChart: this.statsService.getRevenueByCategoryChart().pipe(catchError(() => of(null))),
      topProducts: this.statsService.getTopProducts(5, '30').pipe(catchError(() => of(null)))
    }).subscribe({
      next: (res) => {
        this.stats.set(res.stats);
        this.recentOrders.set(res.recentOrders?.content || []);
        this.salesChart.set(res.salesChart);
        this.orderStatusChart.set(res.orderStatusChart);
        this.categoryChart.set(res.categoryChart);
        this.topProducts.set(res.topProducts?.products || []);
        this.topProductsPeriod.set(res.topProducts?.period || '30 ngày qua');
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

  getChartItems(chart: any): { label: string; value: number }[] {
    const labels = chart?.labels || [];
    const values = chart?.datasets?.[0]?.data || [];
    return labels.map((label: string, index: number) => ({
      label,
      value: Number(values[index] || 0)
    }));
  }

  getSalesItems(): { label: string; value: number }[] {
    return this.getChartItems(this.salesChart());
  }

  getSalesChartTotal(): number {
    return this.getSalesItems().reduce((total, item) => total + item.value, 0);
  }

  getSalesChartTitle(): string {
    return this.salesChart()?.title || 'Doanh số';
  }

  getSalesChartDescription(): string {
    return this.salesChart()?.period === 'monthly'
      ? 'Doanh thu từ các đơn đã thanh toán theo từng tháng'
      : 'Doanh thu từ các đơn đã thanh toán theo từng ngày';
  }

  getBarHeight(value: number, chart: any): number {
    const values = (chart?.datasets?.[0]?.data || []).map(Number);
    const max = Math.max(...values, 1);
    return value === 0 ? 2 : Math.max((value / max) * 100, 6);
  }

  getBarWidth(value: number, chart: any): number {
    return this.getBarHeight(value, chart);
  }

  formatShortDate(date: string): string {
    if (/^\d{4}-\d{2}$/.test(date)) {
      const [year, month] = date.split('-');
      return `${month}/${year.slice(-2)}`;
    }
    const parsed = new Date(date);
    return Number.isNaN(parsed.getTime()) ? date : parsed.toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit' });
  }

  formatCompactCurrency(value: number): string {
    return new Intl.NumberFormat('vi-VN', {
      notation: 'compact',
      maximumFractionDigits: 1
    }).format(value) + 'đ';
  }
}
