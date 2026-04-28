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
  pageSize = 10;
  
  searchKeyword = '';

  ngOnInit() {
    this.loadUsers();
  }

  loadUsers() {
    this.userService.getUsers(this.currentPage(), this.pageSize, undefined, undefined, this.searchKeyword)
      .subscribe(res => {
        this.users.set(res.content);
        this.totalElements.set(res.totalElements);
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
}
