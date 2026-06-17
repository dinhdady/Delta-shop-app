import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminUserService } from './admin-user.service';

@Component({
  selector: 'app-users',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './users.component.html',
  styleUrls: ['./users.component.scss']
})
export class UsersComponent implements OnInit {
  private userService = inject(AdminUserService);

  users = signal<any[]>([]);
  totalElements = signal(0);
  currentPage = signal(0);
  deletingUserId = signal<string | null>(null);
  userPendingDelete = signal<any | null>(null);
  selectedUser = signal<any | null>(null);
  loadingUserDetail = signal(false);
  errorMessage = signal('');
  pageSize = 10;
  
  searchKeyword = '';

  ngOnInit() {
    this.loadUsers();
  }

  loadUsers() {
    this.userService.getUsers(this.currentPage(), this.pageSize, undefined, undefined, this.searchKeyword)
      .subscribe({
        next: (res) => {
          this.users.set(res.content);
          this.totalElements.set(res.totalElements);
          this.errorMessage.set('');
        },
        error: (err) => {
          console.error('Error loading users:', err);
          this.errorMessage.set('Không thể tải danh sách người dùng.');
        }
      });
  }

  onSearch() {
    this.currentPage.set(0);
    this.loadUsers();
  }

  getInitials(name: string): string {
    if (!name) return 'U';
    const parts = name.split(' ');
    if (parts.length >= 2) {
      return parts[0].charAt(0) + parts[parts.length - 1].charAt(0);
    }
    return name.charAt(0);
  }

  toggleUserStatus(user: any) {
    const newStatus = user.status === 'ACTIVE' ? 'BANNED' : 'ACTIVE';
    this.userService.updateStatus(user.id, newStatus).subscribe(() => {
      this.loadUsers();
    });
  }

  changeRole(user: any, event: any) {
    const newRole = event.target.value;
    this.userService.updateUserRole(user.id, newRole).subscribe(() => {
      this.loadUsers();
    });
  }

  openUserDetail(user: any) {
    this.loadingUserDetail.set(true);
    this.errorMessage.set('');
    this.userService.getUserById(user.id).subscribe({
      next: detail => {
        this.selectedUser.set(detail);
        this.loadingUserDetail.set(false);
      },
      error: err => {
        console.error('Error loading user detail:', err);
        this.errorMessage.set('Không thể tải chi tiết người dùng.');
        this.loadingUserDetail.set(false);
      }
    });
  }

  closeUserDetail() {
    this.selectedUser.set(null);
  }

  openDeleteConfirm(user: any) {
    this.errorMessage.set('');
    this.userPendingDelete.set(user);
  }

  closeDeleteConfirm() {
    if (this.deletingUserId()) {
      return;
    }

    this.userPendingDelete.set(null);
  }

  confirmDeleteUser() {
    const user = this.userPendingDelete();
    if (!user) {
      return;
    }

    this.deletingUserId.set(user.id);
    this.errorMessage.set('');

    this.userService.deleteUser(user.id).subscribe({
      next: () => {
        this.users.update(users => users.filter(item => item.id !== user.id));
        this.totalElements.update(total => Math.max(0, total - 1));
        this.deletingUserId.set(null);
        this.userPendingDelete.set(null);
      },
      error: (err) => {
        console.error('Error deleting user:', err);
        this.errorMessage.set(err.error?.message || 'Không thể xóa tài khoản này.');
        this.deletingUserId.set(null);
      }
    });
  }
}
