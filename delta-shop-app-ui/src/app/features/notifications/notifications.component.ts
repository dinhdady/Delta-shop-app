import { CommonModule, DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterModule } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { NotificationItem, NotificationService } from '../../core/services/notification.service';

@Component({
  selector: 'app-notifications',
  standalone: true,
  imports: [CommonModule, RouterModule, DatePipe],
  template: `
    <div class="notifications-page">
      <div class="container">
        <div class="page-header">
          <div>
            <p class="eyebrow">Tài khoản</p>
            <h1>Thông báo</h1>
          </div>
          <div class="actions">
            <button class="btn btn-outline" (click)="markAllAsRead()" [disabled]="notifications().length === 0">Đánh dấu đã đọc</button>
            <button class="btn btn-outline danger" (click)="deleteRead()" [disabled]="notifications().length === 0">Xóa đã đọc</button>
          </div>
        </div>

        @if (loading()) {
          <div class="state">Đang tải thông báo...</div>
        } @else if (notifications().length === 0) {
          <div class="empty">
            <h2>Chưa có thông báo</h2>
            <p>Các cập nhật về đơn hàng, thanh toán và hệ thống sẽ hiển thị tại đây.</p>
            <a routerLink="/products" class="btn btn-primary">Tiếp tục mua sắm</a>
          </div>
        } @else {
          <div class="notification-list">
            @for (notification of notifications(); track notification.id) {
              <article class="notification-card" [class.unread]="!notification.read">
                <div>
                  <div class="card-top">
                    <span class="type">{{ getTypeLabel(notification.type) }}</span>
                    <span class="date">{{ notification.createdAt | date:'dd/MM/yyyy HH:mm' }}</span>
                  </div>
                  <h3>{{ notification.title }}</h3>
                  <p>{{ notification.body }}</p>
                </div>
                <div class="card-actions">
                  @if (!notification.read) {
                    <button class="link-btn" (click)="markAsRead(notification)">Đã đọc</button>
                  }
                  <button class="link-btn danger" (click)="deleteNotification(notification)">Xóa</button>
                </div>
              </article>
            }
          </div>
        }
      </div>
    </div>
  `,
  styles: [`
    .notifications-page {
      min-height: 70vh;
      padding: 3rem 0;
      background: #f7f7f7;
    }

    .page-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-end;
      gap: 1rem;
      margin-bottom: 1.5rem;
    }

    .eyebrow {
      color: var(--color-primary);
      text-transform: uppercase;
      font-weight: 700;
      margin: 0 0 0.25rem;
      letter-spacing: 0.08em;
      font-size: 0.8rem;
    }

    h1 {
      margin: 0;
      font-size: clamp(2rem, 4vw, 3rem);
    }

    .actions {
      display: flex;
      gap: 0.75rem;
      flex-wrap: wrap;
    }

    .state,
    .empty,
    .notification-card {
      background: #fff;
      border: 1px solid var(--color-border);
      border-radius: 16px;
      padding: 1.5rem;
    }

    .empty {
      text-align: center;
      padding: 4rem 1.5rem;
    }

    .notification-list {
      display: grid;
      gap: 1rem;
    }

    .notification-card {
      display: grid;
      grid-template-columns: 1fr auto;
      gap: 1rem;
      border-left: 4px solid transparent;
    }

    .notification-card.unread {
      border-left-color: var(--color-primary);
      background: #fff8f6;
    }

    .card-top {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      margin-bottom: 0.5rem;
      color: var(--color-gray);
      font-size: 0.85rem;
    }

    .type {
      color: var(--color-primary);
      font-weight: 700;
    }

    h3 {
      margin: 0 0 0.5rem;
      font-size: 1.1rem;
    }

    p {
      margin: 0;
      color: var(--color-gray);
      line-height: 1.6;
    }

    .card-actions {
      display: flex;
      align-items: flex-start;
      gap: 0.75rem;
    }

    .link-btn {
      border: 0;
      background: none;
      color: var(--color-primary);
      cursor: pointer;
      font-weight: 700;
      padding: 0;
      white-space: nowrap;
    }

    .danger {
      color: #b42318;
    }

    @media (max-width: 768px) {
      .page-header,
      .notification-card {
        grid-template-columns: 1fr;
      }

      .page-header {
        display: grid;
      }
    }
  `]
})
export class NotificationsComponent implements OnInit {
  private notificationService = inject(NotificationService);
  private toastr = inject(ToastrService);

  notifications = signal<NotificationItem[]>([]);
  loading = signal(false);

  ngOnInit(): void {
    this.loadNotifications();
  }

  loadNotifications(): void {
    this.loading.set(true);
    this.notificationService.getNotifications().subscribe({
      next: response => {
        this.notifications.set(response.content || []);
        this.loading.set(false);
        this.notificationService.loadUnreadCount();
      },
      error: () => {
        this.loading.set(false);
        this.toastr.error('Không thể tải thông báo');
      }
    });
  }

  markAsRead(notification: NotificationItem): void {
    this.notificationService.markAsRead(notification.id).subscribe({
      next: () => {
        this.notifications.update(items =>
          items.map(item => item.id === notification.id ? { ...item, read: true } : item)
        );
      },
      error: () => this.toastr.error('Không thể cập nhật thông báo')
    });
  }

  markAllAsRead(): void {
    this.notificationService.markAllAsRead().subscribe({
      next: () => this.notifications.update(items => items.map(item => ({ ...item, read: true }))),
      error: () => this.toastr.error('Không thể cập nhật thông báo')
    });
  }

  deleteNotification(notification: NotificationItem): void {
    this.notificationService.delete(notification.id).subscribe({
      next: () => {
        this.notifications.update(items => items.filter(item => item.id !== notification.id));
        if (!notification.read) {
          this.notificationService.loadUnreadCount();
        }
      },
      error: () => this.toastr.error('Không thể xóa thông báo')
    });
  }

  deleteRead(): void {
    this.notificationService.deleteRead().subscribe({
      next: () => this.notifications.update(items => items.filter(item => !item.read)),
      error: () => this.toastr.error('Không thể xóa thông báo đã đọc')
    });
  }

  getTypeLabel(type: string): string {
    const labels: Record<string, string> = {
      ORDER_PLACED: 'Đơn hàng',
      ORDER_CONFIRMED: 'Đơn hàng',
      ORDER_SHIPPED: 'Vận chuyển',
      ORDER_DELIVERED: 'Hoàn tất',
      ORDER_CANCELLED: 'Đơn hàng',
      PAYMENT_SUCCESS: 'Thanh toán',
      PAYMENT_FAILED: 'Thanh toán',
      PROMOTION: 'Khuyến mãi',
      SYSTEM: 'Hệ thống'
    };
    return labels[type] || 'Thông báo';
  }
}
