import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { ToastrService } from 'ngx-toastr';
import { RouterModule } from '@angular/router';
import {
  LucideAngularModule,
  Mail,
  MailOpen,
  CheckCircle,
  XCircle,
  Clock,
  ChevronLeft,
  ChevronRight,
  Reply,
  RefreshCw,
  Check,
  AlertCircle
} from 'lucide-angular';

interface Contact {
  id: string;
  name: string;
  email: string;
  phone: string;
  subject: string;
  message: string;
  status: 'PENDING' | 'PROCESSING' | 'REPLIED' | 'RESOLVED' | 'CLOSED';
  adminNote?: string;
  repliedAt?: string;
  createdAt: string;
}

interface ReplyRequest {
  reply: string;
  status: string;
}

@Component({
  selector: 'app-admin-contact',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, LucideAngularModule],
  template: `
    <div class="admin-container">
      <div class="admin-header">
        <h1>QUẢN LÝ LIÊN HỆ</h1>
        <p>Quản lý và phản hồi các liên hệ từ khách hàng</p>
      </div>

      <!-- Stats Cards -->
      <div class="stats-grid">
        <div class="stat-card" (click)="filterByStatus('PENDING')">
          <div class="stat-icon pending">
            <lucide-icon name="clock" [size]="24"></lucide-icon>
          </div>
          <div class="stat-info">
            <h3>{{ stats.pending }}</h3>
            <p>Chờ xử lý</p>
          </div>
        </div>
        <div class="stat-card" (click)="filterByStatus('PROCESSING')">
          <div class="stat-icon processing">
            <lucide-icon name="refresh-cw" [size]="24"></lucide-icon>
          </div>
          <div class="stat-info">
            <h3>{{ stats.processing }}</h3>
            <p>Đang xử lý</p>
          </div>
        </div>
        <div class="stat-card" (click)="filterByStatus('REPLIED')">
          <div class="stat-icon replied">
            <lucide-icon name="mail-open" [size]="24"></lucide-icon>
          </div>
          <div class="stat-info">
            <h3>{{ stats.replied }}</h3>
            <p>Đã phản hồi</p>
          </div>
        </div>
        <div class="stat-card" (click)="filterByStatus('RESOLVED')">
          <div class="stat-icon resolved">
            <lucide-icon name="check-circle" [size]="24"></lucide-icon>
          </div>
          <div class="stat-info">
            <h3>{{ stats.resolved }}</h3>
            <p>Đã giải quyết</p>
          </div>
        </div>
        <div class="stat-card" (click)="filterByStatus('CLOSED')">
          <div class="stat-icon closed">
            <lucide-icon name="x-circle" [size]="24"></lucide-icon>
          </div>
          <div class="stat-info">
            <h3>{{ stats.closed }}</h3>
            <p>Đã đóng</p>
          </div>
        </div>
        <div class="stat-card" (click)="filterByStatus('ALL')">
          <div class="stat-icon total">
            <lucide-icon name="check-circle" [size]="24"></lucide-icon>
          </div>
          <div class="stat-info">
            <h3>{{ stats.total }}</h3>
            <p>Tổng số</p>
          </div>
        </div>
      </div>

      <!-- Filter Tabs -->
      <div class="filter-tabs">
        <button
          *ngFor="let tab of tabs"
          class="tab-btn"
          [class.active]="selectedTab === tab.value"
          (click)="filterByStatus(tab.value)">
          {{ tab.label }}
          <span class="count">{{ getStatusCount(tab.value) }}</span>
        </button>
      </div>

      <!-- Search Bar -->
      <div class="search-bar">
        <input
          type="text"
          [(ngModel)]="searchTerm"
          (ngModelChange)="onSearch()"
          placeholder="Tìm kiếm theo tên, email, số điện thoại hoặc tiêu đề..."
          class="search-input"
        >
      </div>

      <!-- Contacts Table -->
      <div class="table-container">
        <table class="contacts-table">
          <thead>
            <tr>
              <th>Ngày gửi</th>
              <th>Tên khách hàng</th>
              <th>Email/SĐT</th>
              <th>Tiêu đề</th>
              <th>Trạng thái</th>
              <th>Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let contact of filteredContacts">
              <td>{{ contact.createdAt | date:'dd/MM/yyyy HH:mm' }}</td>
              <td>
                <div class="customer-name">{{ contact.name }}</div>
                <div class="customer-subject" [title]="contact.subject">{{ contact.subject | slice:0:30 }}...</div>
              </td>
              <td>
                <div class="customer-email">{{ contact.email }}</div>
                <div class="customer-phone" *ngIf="contact.phone">{{ contact.phone }}</div>
              </td>
              <td>
                <div class="message-preview" [title]="contact.message">{{ contact.message | slice:0:50 }}...</div>
              </td>
              <td>
                <select
                  class="status-select"
                  [class]="contact.status.toLowerCase()"
                  [(ngModel)]="contact.status"
                  (change)="updateStatus(contact.id, $event)"
                >
                  <option value="PENDING">Chờ xử lý</option>
                  <option value="PROCESSING">Đang xử lý</option>
                  <option value="REPLIED">Đã phản hồi</option>
                  <option value="RESOLVED">Đã giải quyết</option>
                  <option value="CLOSED">Đã đóng</option>
                </select>
              </td>
              <td>
                <div class="action-buttons">
                  <button class="btn-view" (click)="viewDetail(contact)">Xem</button>
                  <button class="btn-reply" (click)="openReplyModal(contact)" *ngIf="contact.status !== 'REPLIED' && contact.status !== 'RESOLVED' && contact.status !== 'CLOSED'">
                    <lucide-icon name="reply" [size]="16"></lucide-icon>
                    Phản hồi
                  </button>
                </div>
              </td>
            </tr>
            <tr *ngIf="filteredContacts.length === 0">
              <td colspan="6" class="no-data">
                <p>Không có dữ liệu liên hệ</p>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Pagination -->
      <div class="pagination" *ngIf="totalPages > 1">
        <button (click)="previousPage()" [disabled]="currentPage === 0">
          <lucide-icon name="chevron-left" [size]="20"></lucide-icon>
        </button>
        <span class="page-info">Trang {{ currentPage + 1 }} / {{ totalPages }}</span>
        <button (click)="nextPage()" [disabled]="currentPage === totalPages - 1">
          <lucide-icon name="chevron-right" [size]="20"></lucide-icon>
        </button>
      </div>
    </div>

    <!-- Detail Modal -->
    <div class="modal" [class.show]="showDetailModal" (click)="closeModals()">
      <div class="modal-content" (click)="$event.stopPropagation()">
        <div class="modal-header">
          <h2>Chi tiết liên hệ</h2>
          <button class="close-btn" (click)="closeModals()">&times;</button>
        </div>
        <div class="modal-body" *ngIf="selectedContact">
          <div class="detail-group">
            <label>Thông tin khách hàng</label>
            <div class="detail-info">
              <p><strong>Họ tên:</strong> {{ selectedContact.name }}</p>
              <p><strong>Email:</strong> {{ selectedContact.email }}</p>
              <p><strong>Số điện thoại:</strong> {{ selectedContact.phone || 'Không có' }}</p>
              <p><strong>Ngày gửi:</strong> {{ selectedContact.createdAt | date:'dd/MM/yyyy HH:mm:ss' }}</p>
              <p><strong>Trạng thái:</strong>
                <select
                  class="status-select-inline"
                  [class]="selectedContact.status.toLowerCase()"
                  [(ngModel)]="selectedContact.status"
                  (change)="updateStatus(selectedContact.id, $event)"
                >
                  <option value="PENDING">Chờ xử lý</option>
                  <option value="PROCESSING">Đang xử lý</option>
                  <option value="REPLIED">Đã phản hồi</option>
                  <option value="RESOLVED">Đã giải quyết</option>
                  <option value="CLOSED">Đã đóng</option>
                </select>
              </p>
            </div>
          </div>

          <div class="detail-group">
            <label>Tiêu đề</label>
            <div class="detail-info">
              <p>{{ selectedContact.subject }}</p>
            </div>
          </div>

          <div class="detail-group">
            <label>Nội dung tin nhắn</label>
            <div class="detail-info message-content">
              <p>{{ selectedContact.message }}</p>
            </div>
          </div>

          <div class="detail-group" *ngIf="selectedContact.adminNote">
            <label>Phản hồi của admin</label>
            <div class="detail-info reply-content">
              <p>{{ selectedContact.adminNote }}</p>
              <p class="reply-date" *ngIf="selectedContact.repliedAt">
                <em>Phản hồi lúc: {{ selectedContact.repliedAt | date:'dd/MM/yyyy HH:mm:ss' }}</em>
              </p>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn-secondary" (click)="closeModals()">Đóng</button>
          <button class="btn-primary" (click)="openReplyModal(selectedContact!)" *ngIf="selectedContact?.status !== 'REPLIED' && selectedContact?.status !== 'RESOLVED' && selectedContact?.status !== 'CLOSED'">
            Phản hồi
          </button>
        </div>
      </div>
    </div>

    <!-- Reply Modal -->
    <div class="modal" [class.show]="showReplyModal" (click)="closeModals()">
      <div class="modal-content modal-lg" (click)="$event.stopPropagation()">
        <div class="modal-header">
          <h2>Phản hồi liên hệ</h2>
          <button class="close-btn" (click)="closeModals()">&times;</button>
        </div>
        <div class="modal-body" *ngIf="selectedContact">
          <div class="reply-info">
            <h3>Thông tin khách hàng</h3>
            <p><strong>Họ tên:</strong> {{ selectedContact.name }}</p>
            <p><strong>Email:</strong> {{ selectedContact.email }}</p>
            <p><strong>Tiêu đề:</strong> {{ selectedContact.subject }}</p>
            <div class="original-message">
              <strong>Nội dung gốc:</strong>
              <p>{{ selectedContact.message }}</p>
            </div>
          </div>

          <div class="reply-form">
            <label>Nội dung phản hồi *</label>
            <textarea
              [(ngModel)]="replyContent"
              rows="6"
              placeholder="Nhập nội dung phản hồi của bạn..."
              class="reply-textarea"
            ></textarea>

            <div class="reply-actions">
              <button class="btn-secondary" (click)="closeModals()">Hủy</button>
              <button class="btn-primary" (click)="submitReply()" [disabled]="!replyContent.trim()">
                Gửi phản hồi
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .admin-container {
      max-width: 1400px;
      margin: 0 auto;
      padding: 2rem;
    }
.status-select {
      padding: 0.4rem 0.75rem;
      border-radius: 20px;
      border: 1px solid #e0e0e0;
      font-size: 0.75rem;
      font-weight: 600;
      cursor: pointer;
      background: white;

      &.pending { border-color: #ff9800; color: #ff9800; }
      &.processing { border-color: #2196f3; color: #2196f3; }
      &.replied { border-color: #4caf50; color: #4caf50; }
      &.resolved { border-color: #0284c7; color: #0284c7; }
      &.closed { border-color: #6b7280; color: #6b7280; }

      &:focus {
        outline: none;
      }
    }

    .status-select-inline {
      margin-left: 0.5rem;
      padding: 0.25rem 0.5rem;
      border-radius: 6px;
      border: 1px solid #e0e0e0;
      font-size: 0.75rem;
      cursor: pointer;
    }

    .stat-card {
      cursor: pointer;
      transition: transform 0.2s ease, box-shadow 0.2s ease;

      &:hover {
        transform: translateY(-4px);
        box-shadow: 0 8px 20px rgba(0,0,0,0.12);
      }
    }

    .stat-icon.closed {
      background: #f3f4f6;
      color: #6b7280;
    }
    .admin-header {
      margin-bottom: 2rem;

      h1 {
        font-size: 2rem;
        color: var(--color-dark);
        margin-bottom: 0.5rem;
      }

      p {
        color: #666;
      }
    }

    /* Stats Grid */
    .stats-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
      gap: 1.5rem;
      margin-bottom: 2rem;
    }

    .stat-card {
      background: white;
      padding: 1.5rem;
      border-radius: 12px;
      display: flex;
      align-items: center;
      gap: 1rem;
      box-shadow: 0 2px 8px rgba(0,0,0,0.08);
      border: 1px solid #e0e0e0;
      transition: transform 0.2s ease;

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(0,0,0,0.12);
      }

      .stat-icon {
        width: 50px;
        height: 50px;
        border-radius: 10px;
        display: flex;
        align-items: center;
        justify-content: center;

        &.pending { background: #fff3e0; color: #ff9800; }
        &.processing { background: #e3f2fd; color: #2196f3; }
        &.replied { background: #e8f5e9; color: #4caf50; }
        &.resolved { background: #e0f2fe; color: #0284c7; }
        &.total { background: #f3e8ff; color: #9333ea; }
      }

      .stat-info {
        h3 {
          font-size: 1.8rem;
          font-weight: 700;
          margin: 0;
          color: var(--color-dark);
        }

        p {
          margin: 0;
          font-size: 0.875rem;
          color: #666;
        }
      }
    }

    /* Filter Tabs */
    .filter-tabs {
      display: flex;
      gap: 0.5rem;
      margin-bottom: 1.5rem;
      flex-wrap: wrap;

      .tab-btn {
        padding: 0.5rem 1.25rem;
        border: 1px solid #e0e0e0;
        background: white;
        border-radius: 20px;
        cursor: pointer;
        font-weight: 500;
        transition: all 0.2s ease;

        .count {
          margin-left: 0.5rem;
          color: #666;
          font-size: 0.875rem;
        }

        &:hover {
          border-color: var(--color-primary);
          color: var(--color-primary);
        }

        &.active {
          background: var(--color-primary);
          border-color: var(--color-primary);
          color: white;

          .count {
            color: white;
          }
        }
      }
    }

    /* Search Bar */
    .search-bar {
      margin-bottom: 1.5rem;

      .search-input {
        width: 100%;
        max-width: 300px;
        padding: 0.75rem 1rem;
        border: 1px solid #e0e0e0;
        border-radius: 8px;
        font-size: 0.875rem;

        &:focus {
          outline: none;
          border-color: var(--color-primary);
          box-shadow: 0 0 0 2px rgba(255,68,0,0.1);
        }
      }
    }

    /* Table */
    .table-container {
      overflow-x: auto;
      border-radius: 12px;
      border: 1px solid #e0e0e0;
      background: white;
    }

    .contacts-table {
      width: 100%;
      border-collapse: collapse;

      th, td {
        padding: 1rem;
        text-align: left;
        border-bottom: 1px solid #e0e0e0;
      }

      th {
        background: #f8f9fa;
        font-weight: 600;
        color: var(--color-dark);
      }

      tbody tr:hover {
        background: #f9f9f9;
      }

      .customer-name {
        font-weight: 600;
        margin-bottom: 0.25rem;
      }

      .customer-subject {
        font-size: 0.875rem;
        color: #666;
      }

      .customer-email {
        font-size: 0.875rem;
        margin-bottom: 0.25rem;
      }

      .customer-phone {
        font-size: 0.75rem;
        color: #666;
      }

      .message-preview {
        font-size: 0.875rem;
        color: #666;
        max-width: 250px;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }
    }

    .status-badge {
      display: inline-block;
      padding: 0.25rem 0.75rem;
      border-radius: 20px;
      font-size: 0.75rem;
      font-weight: 600;

      &.pending {
        background: #fff3e0;
        color: #ff9800;
      }

      &.processing {
        background: #e3f2fd;
        color: #2196f3;
      }

      &.replied {
        background: #e8f5e9;
        color: #4caf50;
      }

      &.resolved {
        background: #e0f2fe;
        color: #0284c7;
      }

      &.closed {
        background: #f3f4f6;
        color: #6b7280;
      }
    }

    .action-buttons {
      display: flex;
      gap: 0.5rem;

      button {
        padding: 0.5rem 0.75rem;
        border: none;
        border-radius: 6px;
        cursor: pointer;
        font-size: 0.75rem;
        display: inline-flex;
        align-items: center;
        gap: 0.25rem;
        transition: all 0.2s ease;

        &.btn-view {
          background: #e3f2fd;
          color: #1976d2;

          &:hover {
            background: #bbdef5;
          }
        }

        &.btn-reply {
          background: #e8f5e9;
          color: #4caf50;

          &:hover {
            background: #c8e6c9;
          }
        }
      }
    }

    .no-data {
      text-align: center;
      padding: 3rem;
      color: #999;
    }

    /* Pagination */
    .pagination {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 1rem;
      margin-top: 2rem;

      button {
        padding: 0.5rem 1rem;
        border: 1px solid #e0e0e0;
        background: white;
        border-radius: 6px;
        cursor: pointer;
        transition: all 0.2s ease;

        &:hover:not(:disabled) {
          border-color: var(--color-primary);
          color: var(--color-primary);
        }

        &:disabled {
          opacity: 0.5;
          cursor: not-allowed;
        }
      }

      .page-info {
        font-size: 0.875rem;
        color: #666;
      }
    }

    /* Modal */
    .modal {
      display: none;
      position: fixed;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      background: rgba(0,0,0,0.5);
      z-index: 1000;
      align-items: center;
      justify-content: center;

      &.show {
        display: flex;
      }
    }

    .modal-content {
      background: white;
      border-radius: 12px;
      width: 90%;
      max-width: 600px;
      max-height: 90vh;
      overflow-y: auto;

      &.modal-lg {
        max-width: 800px;
      }
    }

    .modal-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 1.5rem;
      border-bottom: 1px solid #e0e0e0;

      h2 {
        margin: 0;
        font-size: 1.5rem;
      }

      .close-btn {
        background: none;
        border: none;
        font-size: 1.5rem;
        cursor: pointer;
        color: #999;

        &:hover {
          color: var(--color-dark);
        }
      }
    }

    .modal-body {
      padding: 1.5rem;
    }

    .modal-footer {
      padding: 1.5rem;
      border-top: 1px solid #e0e0e0;
      display: flex;
      justify-content: flex-end;
      gap: 1rem;
    }

    .detail-group {
      margin-bottom: 1.5rem;

      label {
        display: block;
        font-weight: 600;
        margin-bottom: 0.5rem;
        color: var(--color-dark);
      }
    }

    .detail-info {
      background: #f9f9f9;
      padding: 1rem;
      border-radius: 8px;

      p {
        margin: 0 0 0.5rem 0;

        &:last-child {
          margin-bottom: 0;
        }
      }
    }

    .message-content, .reply-content {
      background: #f9f9f9;
      padding: 1rem;
      border-radius: 8px;
      line-height: 1.6;
    }

    .reply-date {
      margin-top: 0.5rem;
      font-size: 0.75rem;
      color: #999;
    }

    .reply-info {
      margin-bottom: 1.5rem;

      h3 {
        font-size: 1rem;
        margin-bottom: 1rem;
        color: var(--color-dark);
      }

      p {
        margin: 0 0 0.5rem 0;
      }
    }

    .original-message {
      margin-top: 1rem;
      padding: 1rem;
      background: #f9f9f9;
      border-radius: 8px;

      strong {
        display: block;
        margin-bottom: 0.5rem;
      }

      p {
        margin: 0;
        line-height: 1.5;
      }
    }

    .reply-form {
      label {
        display: block;
        font-weight: 600;
        margin-bottom: 0.5rem;
        color: var(--color-dark);
      }
    }

    .reply-textarea {
      width: 100%;
      padding: 0.75rem;
      border: 1px solid #e0e0e0;
      border-radius: 8px;
      font-family: inherit;
      font-size: 0.875rem;
      resize: vertical;

      &:focus {
        outline: none;
        border-color: var(--color-primary);
        box-shadow: 0 0 0 2px rgba(255,68,0,0.1);
      }
    }

    .reply-actions {
      display: flex;
      justify-content: flex-end;
      gap: 1rem;
      margin-top: 1.5rem;
    }

    .btn-primary, .btn-secondary {
      padding: 0.75rem 1.5rem;
      border: none;
      border-radius: 8px;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.2s ease;
    }

    .btn-primary {
      background: var(--color-primary);
      color: white;

      &:hover:not(:disabled) {
        background: #e65c00;
      }

      &:disabled {
        opacity: 0.5;
        cursor: not-allowed;
      }
    }

    .btn-secondary {
      background: #f0f0f0;
      color: #666;

      &:hover {
        background: #e0e0e0;
      }
    }

    @media (max-width: 768px) {
      .admin-container {
        padding: 1rem;
      }

      .stats-grid {
        grid-template-columns: repeat(2, 1fr);
      }

      .contacts-table th,
      .contacts-table td {
        padding: 0.75rem 0.5rem;
      }
    }
  `]
})
export class AdminContactComponent implements OnInit {
  private http = inject(HttpClient);
  private toastr = inject(ToastrService);

  contacts: Contact[] = [];
  filteredContacts: Contact[] = [];
  selectedContact: Contact | null = null;

  stats = {
    pending: 0,
    processing: 0,
    replied: 0,
    resolved: 0,
    closed: 0,
    total: 0
  };

  tabs = [
    { label: 'Tất cả', value: 'ALL' },
    { label: 'Chờ xử lý', value: 'PENDING' },
    { label: 'Đang xử lý', value: 'PROCESSING' },
    { label: 'Đã phản hồi', value: 'REPLIED' },
    { label: 'Đã giải quyết', value: 'RESOLVED' },
    { label: 'Đã đóng', value: 'CLOSED' }
  ];

  selectedTab = 'ALL';
  searchTerm = '';
  currentPage = 0;
  pageSize = 10;
  totalPages = 1;

  showDetailModal = false;
  showReplyModal = false;
  replyContent = '';

  ngOnInit(): void {
    this.loadContacts();
    this.loadStats();
  }

  loadContacts(): void {
    let url = 'http://localhost:8080/api/contacts/admin';
    if (this.selectedTab !== 'ALL') {
      url = `http://localhost:8080/api/contacts/admin/status/${this.selectedTab}`;
    }

    this.http.get<any>(url, { params: { page: this.currentPage, size: this.pageSize } })
      .subscribe({
        next: (response) => {
          this.contacts = response.content;
          this.filteredContacts = [...response.content];
          this.totalPages = response.totalPages;
        },
        error: (error) => {
          console.error('Error loading contacts:', error);
          this.toastr.error('Không thể tải danh sách liên hệ');
        }
      });
  }

  loadStats(): void {
    this.http.get<any>('http://localhost:8080/api/contacts/admin/stats')
      .subscribe({
        next: (stats) => {
          this.stats = stats;
        },
        error: (error) => {
          console.error('Error loading stats:', error);
        }
      });
  }

  // Cập nhật trạng thái
  updateStatus(contactId: string, event: Event): void {
    const select = event.target as HTMLSelectElement;
    const newStatus = select.value;

    this.http.put(`http://localhost:8080/api/contacts/admin/${contactId}/status?status=${newStatus}`, {})
      .subscribe({
        next: () => {
          this.toastr.success(`Đã cập nhật trạng thái thành công`);
          this.loadStats();
          this.loadContacts();
        },
        error: (error) => {
          console.error('Error updating status:', error);
          this.toastr.error('Không thể cập nhật trạng thái');
          // Reload lại dữ liệu để khôi phục trạng thái cũ
          this.loadContacts();
        }
      });
  }

  filterByStatus(status: string): void {
    this.selectedTab = status;
    this.currentPage = 0;
    if (status === 'ALL') {
      this.loadContacts();
    } else {
      this.loadContacts();
    }
  }

  onSearch(): void {
    if (!this.searchTerm.trim()) {
      this.filteredContacts = this.contacts;
    } else {
      const term = this.searchTerm.toLowerCase();
      this.filteredContacts = this.contacts.filter(contact =>
        contact.name.toLowerCase().includes(term) ||
        contact.email.toLowerCase().includes(term) ||
        (contact.phone && contact.phone.includes(term)) ||
        contact.subject.toLowerCase().includes(term) ||
        contact.message.toLowerCase().includes(term)
      );
    }
  }

  getStatusCount(status: string): number {
    if (status === 'ALL') return this.stats.total;
    if (status === 'PENDING') return this.stats.pending;
    if (status === 'PROCESSING') return this.stats.processing;
    if (status === 'REPLIED') return this.stats.replied;
    if (status === 'RESOLVED') return this.stats.resolved;
    if (status === 'CLOSED') return this.stats.closed;
    return 0;
  }

  getStatusLabel(status: string): string {
    const labels: Record<string, string> = {
      'PENDING': 'Chờ xử lý',
      'PROCESSING': 'Đang xử lý',
      'REPLIED': 'Đã phản hồi',
      'RESOLVED': 'Đã giải quyết',
      'CLOSED': 'Đã đóng'
    };
    return labels[status] || status;
  }

  viewDetail(contact: Contact): void {
    this.selectedContact = contact;
    this.showDetailModal = true;
  }

  openReplyModal(contact: Contact): void {
    this.selectedContact = contact;
    this.replyContent = '';
    this.showReplyModal = true;
    this.showDetailModal = false;
  }

  submitReply(): void {
    if (!this.selectedContact || !this.replyContent.trim()) return;

    const replyData: ReplyRequest = {
      reply: this.replyContent,
      status: 'REPLIED'
    };

    this.http.post(`http://localhost:8080/api/contacts/admin/${this.selectedContact.id}/reply`, replyData)
      .subscribe({
        next: () => {
          this.toastr.success('Đã gửi phản hồi thành công');
          this.closeModals();
          this.loadContacts();
          this.loadStats();
        },
        error: (error) => {
          console.error('Error sending reply:', error);
          this.toastr.error('Không thể gửi phản hồi');
        }
      });
  }

  previousPage(): void {
    if (this.currentPage > 0) {
      this.currentPage--;
      this.loadContacts();
    }
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages - 1) {
      this.currentPage++;
      this.loadContacts();
    }
  }

  closeModals(): void {
    this.showDetailModal = false;
    this.showReplyModal = false;
    this.selectedContact = null;
    this.replyContent = '';
  }
}
