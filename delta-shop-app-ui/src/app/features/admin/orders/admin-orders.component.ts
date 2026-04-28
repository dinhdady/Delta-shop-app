import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ToastrService } from 'ngx-toastr';
import { AdminOrderService } from './admin-order.service';
import { OrderDetail, OrderSummary } from '../../../core/services/order.service';

@Component({
  selector: 'app-admin-orders',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-orders.component.html',
  styleUrls: ['./admin-orders.component.scss']
})
export class AdminOrdersComponent implements OnInit {
  private orderService = inject(AdminOrderService);
  private toastr = inject(ToastrService);

  orders = signal<OrderSummary[]>([]);
  totalElements = signal(0);
  totalPages = signal(0);
  currentPage = signal(0);
  pageSize = 10;
  loading = signal(false);

  selectedStatus = '';
  selectedOrder = signal<OrderDetail | null>(null);
  trackingNumber = '';

  ngOnInit(): void {
    this.loadOrders();
  }

  loadOrders(): void {
    this.loading.set(true);
    this.orderService.getOrders(this.currentPage(), this.pageSize, this.selectedStatus || undefined)
      .subscribe({
        next: res => {
          this.orders.set(res.content);
          this.totalElements.set(res.totalElements);
          this.totalPages.set(res.totalPages);
          this.loading.set(false);
        },
        error: () => {
          this.loading.set(false);
          this.toastr.error('Khong the tai danh sach don hang');
        }
      });
  }

  onFilter(): void {
    this.currentPage.set(0);
    this.loadOrders();
  }

  changePage(page: number): void {
    if (page < 0 || page >= this.totalPages()) {
      return;
    }
    this.currentPage.set(page);
    this.loadOrders();
  }

  updateStatus(order: OrderSummary, event: Event): void {
    const status = (event.target as HTMLSelectElement).value;
    this.orderService.updateStatus(order.id, status).subscribe({
      next: detail => {
        this.toastr.success('Da cap nhat trang thai don hang');
        this.updateOrderInList(detail);
        if (this.selectedOrder()?.id === detail.id) {
          this.selectedOrder.set(detail);
        }
      },
      error: () => this.toastr.error('Khong the cap nhat trang thai')
    });
  }

  updatePaymentStatus(order: OrderSummary, event: Event): void {
    const paymentStatus = (event.target as HTMLSelectElement).value;
    this.orderService.updatePaymentStatus(order.id, paymentStatus).subscribe({
      next: detail => {
        this.toastr.success('Da cap nhat trang thai thanh toan');
        this.updateOrderInList(detail);
        if (this.selectedOrder()?.id === detail.id) {
          this.selectedOrder.set(detail);
        }
      },
      error: () => this.toastr.error('Khong the cap nhat thanh toan')
    });
  }

  viewDetails(order: OrderSummary): void {
    this.orderService.getOrderById(order.id).subscribe({
      next: detail => {
        this.selectedOrder.set(detail);
        this.trackingNumber = detail.trackingNumber ?? '';
      },
      error: () => this.toastr.error('Khong the tai chi tiet don hang')
    });
  }

  saveTrackingNumber(): void {
    const order = this.selectedOrder();
    if (!order || !this.trackingNumber.trim()) {
      return;
    }

    this.orderService.addTrackingNumber(order.id, this.trackingNumber.trim()).subscribe({
      next: detail => {
        this.toastr.success('Da cap nhat ma van don');
        this.selectedOrder.set(detail);
        this.updateOrderInList(detail);
      },
      error: () => this.toastr.error('Khong the cap nhat ma van don')
    });
  }

  closeDetails(): void {
    this.selectedOrder.set(null);
    this.trackingNumber = '';
  }

  formatCurrency(value: number): string {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND', maximumFractionDigits: 0 }).format(value);
  }

  formatDate(value: string): string {
    return new Date(value).toLocaleString('vi-VN');
  }

  labelStatus(status: string): string {
    return ({
      PENDING: 'Cho xu ly',
      CONFIRMED: 'Da xac nhan',
      PROCESSING: 'Dang xu ly',
      SHIPPED: 'Dang giao',
      DELIVERED: 'Da giao',
      CANCELLED: 'Da huy',
      REFUNDED: 'Da hoan tien'
    } as Record<string, string>)[status] ?? status;
  }

  labelPayment(status: string): string {
    return ({
      PENDING: 'Cho thanh toan',
      PAID: 'Da thanh toan',
      FAILED: 'That bai',
      REFUNDED: 'Da hoan tien',
      PARTIALLY_REFUNDED: 'Hoan tien mot phan'
    } as Record<string, string>)[status] ?? status;
  }

  fullAddress(order: OrderDetail): string {
    return [order.shippingAddress, order.shippingWard, order.shippingDistrict, order.shippingProvince].filter(Boolean).join(', ');
  }

  private updateOrderInList(detail: OrderDetail): void {
    this.orders.update(orders => orders.map(order =>
      order.id === detail.id
        ? {
            ...order,
            status: detail.status,
            paymentStatus: detail.paymentStatus,
            totalAmount: detail.totalAmount,
            shippingName: detail.shippingName ?? order.shippingName,
            shippingPhone: detail.shippingPhone ?? order.shippingPhone,
            shippingAddress: detail.shippingAddress
          }
        : order
    ));
  }
}
