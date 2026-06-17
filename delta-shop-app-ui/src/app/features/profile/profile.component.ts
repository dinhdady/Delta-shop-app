// profile.component.ts - Thêm tính năng đổi mật khẩu
import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient, HttpClientModule, HttpHeaders } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { RouterModule, Router } from '@angular/router';
import { Subject, takeUntil } from 'rxjs';
import { ToastrService } from 'ngx-toastr';
import { AuthService } from '../../core/services/auth.service';

// Interfaces
interface UserProfileResponse {
  id: string;
  email: string;
  phone: string;
  firstName: string;
  lastName: string;
  fullName: string;
  avatarUrl: string | null;
  dateOfBirth: string;
  role: string;
  status: string;
  emailVerified: boolean;
  phoneVerified: boolean;
  loyaltyPoints: number;
  totalSpent: number;
  lastLoginAt: string;
  createdAt: string;
}

interface UserAddressResponse {
  id: string;
  type: string;
  recipientName: string;
  phone: string;
  province: string;
  district: string;
  ward: string;
  streetAddress: string;
  postalCode: string;
  isDefault: boolean;
  fullAddress: string;
}

interface UserStatisticsResponse {
  totalOrders: number;
  completedOrders: number;
  cancelledOrders: number;
  totalSpent: number;
  averageOrderValue: number;
  loyaltyPoints: number;
  totalReviews: number;
  helpfulVotesReceived: number;
  wishlistCount: number;
}

interface UpdateProfileRequest {
  firstName: string;
  lastName: string;
  phone: string;
  dateOfBirth: string;
}

interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
  confirmPassword: string;
}

interface AddAddressRequest {
  type: string;
  recipientName: string;
  phone: string;
  province: string;
  district: string;
  ward: string;
  streetAddress: string;
  postalCode: string;
  isDefault: boolean;
}

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, HttpClientModule],
  template: `
    <div class="profile-container">
      <div class="page-header">
        <div class="container">
          <h1>TÀI KHOẢN CỦA TÔI</h1>
          <p>Quản lý thông tin cá nhân và đơn hàng</p>
        </div>
      </div>

      <div class="container">
        <div class="profile-wrapper">
          <aside class="profile-sidebar">
            <div class="user-card">
              <div class="avatar-section">
                <div class="avatar-wrapper">
                  <img
                    *ngIf="avatarPreviewUrl || profile?.avatarUrl"
                    [src]="avatarPreviewUrl || profile?.avatarUrl"
                    [alt]="profile?.fullName || 'Avatar'"
                    class="avatar"
                    (error)="handleAvatarError()"
                  >
                  <div *ngIf="!avatarPreviewUrl && !profile?.avatarUrl" class="avatar-placeholder">
                    <i class="fas fa-user"></i>
                  </div>
                  <div class="avatar-uploading" *ngIf="uploadingAvatar">
                    <i class="fas fa-spinner fa-spin"></i>
                  </div>
                  <button type="button" class="avatar-edit-btn" title="Thay ảnh đại diện"
                    (click)="avatarInput.click()" [disabled]="uploadingAvatar">
                    <i class="fas fa-camera"></i>
                  </button>
                  <input
                    type="file"
                    #avatarInput
                    id="avatarInput"
                    accept="image/jpeg,image/png,image/webp,image/gif"
                    style="display: none"
                    (change)="uploadAvatar($event)"
                  >
                </div>
                <button type="button" class="avatar-delete-btn" *ngIf="profile?.avatarUrl"
                  (click)="deleteAvatar()" [disabled]="uploadingAvatar">
                  Xóa ảnh đại diện
                </button>
              </div>
              <h3 class="user-name">{{ profile?.fullName || 'Chưa cập nhật' }}</h3>
              <p class="user-email">{{ profile?.email }}</p>
              <div class="user-badge" [class]="profile?.status === 'ACTIVE' ? 'active' : 'inactive'">
                {{ profile?.status === 'ACTIVE' ? 'Đang hoạt động' : 'Tạm khóa' }}
              </div>
              <div class="loyalty-points" *ngIf="profile?.loyaltyPoints">
                <i class="fas fa-star"></i>
                <span>{{ profile?.loyaltyPoints }} điểm</span>
              </div>
            </div>

            <nav class="profile-nav">
              <button class="nav-item" [class.active]="activeTab === 'profile'" (click)="setActiveTab('profile')">
                <i class="fas fa-user"></i>
                <span>Thông tin cá nhân</span>
              </button>
              <button class="nav-item" [class.active]="activeTab === 'addresses'" (click)="setActiveTab('addresses')">
                <i class="fas fa-map-marker-alt"></i>
                <span>Địa chỉ giao hàng</span>
              </button>
              <button class="nav-item" [class.active]="activeTab === 'statistics'" (click)="setActiveTab('statistics')">
                <i class="fas fa-chart-line"></i>
                <span>Thống kê</span>
              </button>
              <button class="nav-item" [class.active]="activeTab === 'orders'" (click)="setActiveTab('orders')" routerLink="/orders">
                <i class="fas fa-shopping-bag"></i>
                <span>Đơn hàng</span>
              </button>
              <button class="nav-item" [class.active]="activeTab === 'change-password'" (click)="setActiveTab('change-password')">
                <i class="fas fa-key"></i>
                <span>Đổi mật khẩu</span>
              </button>
              <button class="nav-item" (click)="logout()">
                <i class="fas fa-sign-out-alt"></i>
                <span>Đăng xuất</span>
              </button>
            </nav>
          </aside>

          <main class="profile-content">
            <div *ngIf="loading" class="loading-wrapper">
              <div class="spinner"></div>
              <p>Đang tải...</p>
            </div>

            <!-- Profile Tab -->
            <div *ngIf="!loading && activeTab === 'profile'" class="tab-content">
              <div class="content-header">
                <h2>Thông tin cá nhân</h2>
                <button *ngIf="!isEditing" class="btn-edit" (click)="toggleEdit()">
                  <i class="fas fa-pen"></i> Chỉnh sửa
                </button>
              </div>

              <form *ngIf="!isEditing" class="info-view">
                <div class="info-row">
                  <div class="info-label">Họ:</div>
                  <div class="info-value">{{ profile?.firstName || 'Chưa cập nhật' }}</div>
                </div>
                <div class="info-row">
                  <div class="info-label">Tên:</div>
                  <div class="info-value">{{ profile?.lastName || 'Chưa cập nhật' }}</div>
                </div>
                <div class="info-row">
                  <div class="info-label">Email:</div>
                  <div class="info-value">{{ profile?.email }}</div>
                  <span class="verified-badge" *ngIf="profile?.emailVerified">
                    <i class="fas fa-check-circle"></i> Đã xác thực
                  </span>
                </div>
                <div class="info-row">
                  <div class="info-label">Số điện thoại:</div>
                  <div class="info-value">{{ profile?.phone || 'Chưa cập nhật' }}</div>
                  <span class="verified-badge" *ngIf="profile?.phoneVerified">
                    <i class="fas fa-check-circle"></i> Đã xác thực
                  </span>
                </div>
                <div class="info-row">
                  <div class="info-label">Ngày sinh:</div>
                  <div class="info-value">{{ formatDate(profile?.dateOfBirth || '') || 'Chưa cập nhật' }}</div>
                </div>
                <div class="info-row">
                  <div class="info-label">Tham gia từ:</div>
                  <div class="info-value">{{ formatDateTime(profile?.createdAt || '') }}</div>
                </div>
                <div class="info-row">
                  <div class="info-label">Đăng nhập lần cuối:</div>
                  <div class="info-value">{{ formatDateTime(profile?.lastLoginAt || '') || 'Chưa có' }}</div>
                </div>
              </form>

              <form *ngIf="isEditing" (ngSubmit)="updateProfile()" class="info-edit">
                <div class="form-group">
                  <label>Họ *</label>
                  <input type="text" [(ngModel)]="editForm.firstName" name="firstName" required class="form-control">
                </div>
                <div class="form-group">
                  <label>Tên *</label>
                  <input type="text" [(ngModel)]="editForm.lastName" name="lastName" required class="form-control">
                </div>
                <div class="form-group">
                  <label>Số điện thoại</label>
                  <input type="tel" [(ngModel)]="editForm.phone" name="phone" class="form-control">
                </div>
                <div class="form-group">
                  <label>Ngày sinh</label>
                  <input type="date" [(ngModel)]="editForm.dateOfBirth" name="dateOfBirth" class="form-control">
                </div>
                <div class="form-actions">
                  <button type="submit" class="btn-save" [disabled]="saving">
                    <i class="fas fa-save"></i> {{ saving ? 'Đang lưu...' : 'Lưu thay đổi' }}
                  </button>
                  <button type="button" class="btn-cancel" (click)="cancelEdit()">Hủy</button>
                </div>
              </form>
            </div>

            <!-- Addresses Tab -->
            <div *ngIf="!loading && activeTab === 'addresses'" class="tab-content">
              <div class="content-header">
                <h2>Địa chỉ giao hàng</h2>
                <button class="btn-add" (click)="openAddressModal()">
                  <i class="fas fa-plus"></i> Thêm địa chỉ mới
                </button>
              </div>

              <div class="addresses-grid">
                <div *ngFor="let address of addresses" class="address-card" [class.default]="address.isDefault">
                  <div class="address-badge" *ngIf="address.isDefault">Mặc định</div>
                  <div class="address-content">
                    <p class="recipient">{{ address.recipientName }}</p>
                    <p class="phone">{{ address.phone }}</p>
                    <p class="address">{{ address.fullAddress || getFullAddress(address) }}</p>
                  </div>
                  <div class="address-actions">
                    <button class="action-btn edit" (click)="editAddress(address)">
                      <i class="fas fa-pen"></i>
                    </button>
                    <button *ngIf="!address.isDefault" class="action-btn delete" (click)="deleteAddress(address.id)">
                      <i class="fas fa-trash"></i>
                    </button>
                    <button *ngIf="!address.isDefault" class="action-btn default" (click)="setDefaultAddress(address.id)">
                      <i class="fas fa-check-circle"></i> Đặt mặc định
                    </button>
                  </div>
                </div>
              </div>

              <!-- Address Modal -->
              <div class="modal" [class.show]="showAddressModal" (click)="closeAddressModalOnOverlay($event)">
                <div class="modal-content">
                  <div class="modal-header">
                    <h3>{{ isEditingAddress ? 'Chỉnh sửa địa chỉ' : 'Thêm địa chỉ mới' }}</h3>
                    <button class="close-btn" (click)="closeAddressModal()">&times;</button>
                  </div>
                  <form (ngSubmit)="saveAddress()">
                    <div class="form-group">
                      <label>Loại địa chỉ</label>
                      <select [(ngModel)]="addressForm.type" name="type" class="form-control">
                        <option value="HOME">Nhà riêng</option>
                        <option value="WORK">Văn phòng</option>
                        <option value="OTHER">Khác</option>
                      </select>
                    </div>
                    <div class="form-group">
                      <label>Tên người nhận *</label>
                      <input type="text" [(ngModel)]="addressForm.recipientName" name="recipientName" required class="form-control">
                    </div>
                    <div class="form-group">
                      <label>Số điện thoại *</label>
                      <input type="tel" [(ngModel)]="addressForm.phone" name="phone" required class="form-control">
                    </div>
                    <div class="form-group">
                      <label>Địa chỉ chi tiết *</label>
                      <input type="text" [(ngModel)]="addressForm.streetAddress" name="streetAddress" required class="form-control">
                    </div>
                    <div class="form-row">
                      <div class="form-group">
                        <label>Phường/Xã</label>
                        <input type="text" [(ngModel)]="addressForm.ward" name="ward" class="form-control">
                      </div>
                      <div class="form-group">
                        <label>Quận/Huyện</label>
                        <input type="text" [(ngModel)]="addressForm.district" name="district" class="form-control">
                      </div>
                    </div>
                    <div class="form-group">
                      <label>Tỉnh/Thành phố *</label>
                      <input type="text" [(ngModel)]="addressForm.province" name="province" required class="form-control">
                    </div>
                    <div class="form-group checkbox">
                      <label>
                        <input type="checkbox" [(ngModel)]="addressForm.isDefault" name="isDefault">
                        Đặt làm địa chỉ mặc định
                      </label>
                    </div>
                    <div class="form-actions">
                      <button type="submit" class="btn-save">{{ isEditingAddress ? 'Cập nhật' : 'Thêm mới' }}</button>
                      <button type="button" class="btn-cancel" (click)="closeAddressModal()">Hủy</button>
                    </div>
                  </form>
                </div>
              </div>
            </div>

            <!-- Statistics Tab -->
            <div *ngIf="!loading && activeTab === 'statistics'" class="tab-content">
              <div class="content-header">
                <h2>Thống kê tài khoản</h2>
              </div>

              <div class="stats-grid">
                <div class="stat-card">
                  <div class="stat-icon">
                    <i class="fas fa-shopping-cart"></i>
                  </div>
                  <div class="stat-info">
                    <div class="stat-value">{{ statistics?.totalOrders || 0 }}</div>
                    <div class="stat-label">Tổng đơn hàng</div>
                  </div>
                </div>
                <div class="stat-card">
                  <div class="stat-icon">
                    <i class="fas fa-dollar-sign"></i>
                  </div>
                  <div class="stat-info">
                    <div class="stat-value">{{ formatCurrency(statistics?.totalSpent) }}</div>
                    <div class="stat-label">Tổng chi tiêu</div>
                  </div>
                </div>
                <div class="stat-card">
                  <div class="stat-icon">
                    <i class="fas fa-star"></i>
                  </div>
                  <div class="stat-info">
                    <div class="stat-value">{{ statistics?.loyaltyPoints || 0 }}</div>
                    <div class="stat-label">Điểm tích lũy</div>
                  </div>
                </div>
                <div class="stat-card">
                  <div class="stat-icon">
                    <i class="fas fa-comment"></i>
                  </div>
                  <div class="stat-info">
                    <div class="stat-value">{{ statistics?.totalReviews || 0 }}</div>
                    <div class="stat-label">Đánh giá</div>
                  </div>
                </div>
              </div>
            </div>

            <!-- Change Password Tab -->
            <div *ngIf="!loading && activeTab === 'change-password'" class="tab-content">
              <div class="content-header">
                <h2>Đổi mật khẩu</h2>
              </div>

              <form (ngSubmit)="changePassword()" class="change-password-form">
                <div class="form-group">
                  <label>Mật khẩu hiện tại *</label>
                  <div class="password-input-wrapper">
                    <input
                      [type]="showCurrentPassword ? 'text' : 'password'"
                      [(ngModel)]="passwordForm.currentPassword"
                      name="currentPassword"
                      required
                      class="form-control"
                      placeholder="Nhập mật khẩu hiện tại"
                    >
                    <button type="button" class="toggle-password" (click)="toggleCurrentPasswordVisibility()">
                      <i [class]="showCurrentPassword ? 'fas fa-eye-slash' : 'fas fa-eye'"></i>
                    </button>
                  </div>
                </div>

                <div class="form-group">
                  <label>Mật khẩu mới *</label>
                  <div class="password-input-wrapper">
                    <input
                      [type]="showNewPassword ? 'text' : 'password'"
                      [(ngModel)]="passwordForm.newPassword"
                      name="newPassword"
                      required
                      class="form-control"
                      placeholder="Nhập mật khẩu mới (tối thiểu 6 ký tự)"
                      (input)="validatePasswordStrength()"
                    >
                    <button type="button" class="toggle-password" (click)="toggleNewPasswordVisibility()">
                      <i [class]="showNewPassword ? 'fas fa-eye-slash' : 'fas fa-eye'"></i>
                    </button>
                  </div>
                  <div class="password-strength" *ngIf="passwordForm.newPassword">
                    <div class="strength-bar">
                      <div class="strength-level" [class]="passwordStrength.class" [style.width]="passwordStrength.percentage"></div>
                    </div>
                    <span class="strength-text" [class]="passwordStrength.class">{{ passwordStrength.text }}</span>
                  </div>
                  <ul class="password-requirements">
                    <li [class.valid]="hasMinLength">✓ Ít nhất 6 ký tự</li>
                    <li [class.valid]="hasUpperCase">✓ Ít nhất 1 chữ hoa</li>
                    <li [class.valid]="hasLowerCase">✓ Ít nhất 1 chữ thường</li>
                    <li [class.valid]="hasNumber">✓ Ít nhất 1 số</li>
                  </ul>
                </div>

                <div class="form-group">
                  <label>Xác nhận mật khẩu mới *</label>
                  <div class="password-input-wrapper">
                    <input
                      [type]="showConfirmPassword ? 'text' : 'password'"
                      [(ngModel)]="passwordForm.confirmPassword"
                      name="confirmPassword"
                      required
                      class="form-control"
                      placeholder="Nhập lại mật khẩu mới"
                      (input)="checkPasswordMatch()"
                    >
                    <button type="button" class="toggle-password" (click)="toggleConfirmPasswordVisibility()">
                      <i [class]="showConfirmPassword ? 'fas fa-eye-slash' : 'fas fa-eye'"></i>
                    </button>
                  </div>
                  <div class="password-match" *ngIf="passwordForm.confirmPassword">
                    <span *ngIf="passwordsMatch" class="valid">✓ Mật khẩu xác nhận chính xác</span>
                    <span *ngIf="!passwordsMatch" class="invalid">✗ Mật khẩu xác nhận không khớp</span>
                  </div>
                </div>

                <div class="form-actions">
                  <button type="submit" class="btn-save" [disabled]="!isPasswordFormValid() || changingPassword">
                    <i class="fas fa-key"></i> {{ changingPassword ? 'Đang xử lý...' : 'Đổi mật khẩu' }}
                  </button>
                  <button type="button" class="btn-cancel" (click)="resetPasswordForm()">Hủy</button>
                </div>
              </form>
            </div>
          </main>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .profile-container {
      min-height: 100vh;
      background: #F5F5F5;
    }
    .container {
      max-width: 1200px;
      margin: 0 auto;
      padding: 0 20px;
    }
    .page-header {
      background: linear-gradient(135deg, #1A1A1A 0%, #2C2C2C 100%);
      padding: 60px 0;
      text-align: center;
      position: relative;
      overflow: hidden;
    }
    .page-header h1 {
      font-size: 48px;
      font-weight: 700;
      color: white;
      margin-bottom: 16px;
      position: relative;
      z-index: 1;
    }
    .page-header p {
      font-size: 18px;
      color: rgba(255,255,255,0.8);
      position: relative;
      z-index: 1;
    }
    .profile-wrapper {
      display: grid;
      grid-template-columns: 300px 1fr;
      gap: 30px;
      padding: 60px 0;
    }
    @media (max-width: 992px) {
      .profile-wrapper {
        grid-template-columns: 1fr;
      }
    }
    .profile-sidebar {
      background: white;
      border-radius: 12px;
      overflow: hidden;
      box-shadow: 0 4px 20px rgba(0,0,0,0.08);
      height: fit-content;
    }
    .user-card {
      padding: 30px;
      text-align: center;
      border-bottom: 1px solid #E0E0E0;
    }
    .avatar-section {
      position: relative;
      display: inline-block;
      margin-bottom: 20px;
    }
    .avatar-wrapper {
      position: relative;
    }
    .avatar, .avatar-placeholder {
      width: 100px;
      height: 100px;
      border-radius: 50%;
      object-fit: cover;
    }
    .avatar-placeholder {
      background: linear-gradient(135deg, #C41E3A, #8B0000);
      display: flex;
      align-items: center;
      justify-content: center;
    }
    .avatar-placeholder i {
      font-size: 48px;
      color: white;
    }
    .avatar-edit-btn {
      position: absolute;
      bottom: 0;
      right: 0;
      width: 32px;
      height: 32px;
      background: #C41E3A;
      border: none;
      border-radius: 50%;
      color: white;
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: all 0.3s ease;
    }
    .avatar-edit-btn:hover {
      background: #8B0000;
      transform: scale(1.1);
    }
    .avatar-edit-btn:disabled, .avatar-delete-btn:disabled {
      opacity: 0.6;
      cursor: not-allowed;
    }
    .avatar-uploading {
      position: absolute;
      inset: 0;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      background: rgba(0, 0, 0, 0.55);
      color: #fff;
      font-size: 24px;
    }
    .avatar-delete-btn {
      margin-top: 10px;
      border: none;
      background: transparent;
      color: #C41E3A;
      font-size: 12px;
      font-weight: 600;
      cursor: pointer;
    }
    .avatar-delete-btn:hover {
      text-decoration: underline;
    }
    .user-name {
      font-size: 18px;
      font-weight: 600;
      color: #333;
      margin-bottom: 5px;
    }
    .user-email {
      font-size: 13px;
      color: #666;
      margin-bottom: 10px;
    }
    .user-badge {
      display: inline-block;
      padding: 4px 12px;
      border-radius: 20px;
      font-size: 11px;
      font-weight: 600;
    }
    .user-badge.active {
      background: #E8F5E9;
      color: #2E7D32;
    }
    .user-badge.inactive {
      background: #FFEBEE;
      color: #C62828;
    }
    .loyalty-points {
      margin-top: 12px;
      padding: 8px 12px;
      background: #FFF8E1;
      border-radius: 20px;
      display: inline-flex;
      align-items: center;
      gap: 6px;
      font-size: 12px;
      font-weight: 600;
      color: #F57C00;
    }
    .profile-nav {
      padding: 10px 0;
    }
    .nav-item {
      width: 100%;
      padding: 14px 24px;
      display: flex;
      align-items: center;
      gap: 12px;
      background: none;
      border: none;
      cursor: pointer;
      font-size: 14px;
      color: #666;
      transition: all 0.3s ease;
      text-align: left;
    }
    .nav-item i {
      width: 20px;
    }
    .nav-item:hover {
      background: #F5F5F5;
      color: #C41E3A;
    }
    .nav-item.active {
      background: #F5F5F5;
      color: #C41E3A;
      border-right: 3px solid #C41E3A;
    }
    .profile-content {
      background: white;
      border-radius: 12px;
      padding: 30px;
      box-shadow: 0 4px 20px rgba(0,0,0,0.08);
    }
    .content-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 30px;
      padding-bottom: 20px;
      border-bottom: 2px solid #C41E3A;
    }
    .content-header h2 {
      font-size: 24px;
      font-weight: 600;
      color: #333;
    }
    .btn-edit, .btn-add {
      padding: 8px 20px;
      background: #C41E3A;
      color: white;
      border: none;
      border-radius: 30px;
      cursor: pointer;
      font-weight: 500;
      display: flex;
      align-items: center;
      gap: 8px;
      transition: all 0.3s ease;
    }
    .btn-edit:hover, .btn-add:hover {
      background: #8B0000;
      transform: translateY(-2px);
    }
    .info-row {
      display: flex;
      padding: 12px 0;
      border-bottom: 1px solid #F0F0F0;
      align-items: center;
      flex-wrap: wrap;
    }
    .info-label {
      width: 150px;
      font-weight: 600;
      color: #666;
    }
    .info-value {
      flex: 1;
      color: #333;
    }
    .verified-badge {
      margin-left: 10px;
      font-size: 11px;
      color: #4CAF50;
    }
    .info-edit, .change-password-form {
      display: flex;
      flex-direction: column;
      gap: 20px;
    }
    .form-group {
      display: flex;
      flex-direction: column;
      gap: 8px;
    }
    .form-group label {
      font-weight: 500;
      color: #666;
      font-size: 13px;
    }
    .form-control {
      padding: 10px 14px;
      border: 1px solid #E0E0E0;
      border-radius: 8px;
      font-size: 14px;
      transition: all 0.3s ease;
    }
    .form-control:focus {
      outline: none;
      border-color: #C41E3A;
      box-shadow: 0 0 0 2px rgba(196,30,58,0.1);
    }
    .form-row {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 20px;
    }
    .form-actions {
      display: flex;
      gap: 15px;
      margin-top: 10px;
    }
    .btn-save {
      padding: 10px 24px;
      background: #C41E3A;
      color: white;
      border: none;
      border-radius: 8px;
      cursor: pointer;
      font-weight: 500;
      display: flex;
      align-items: center;
      gap: 8px;
    }
    .btn-save:hover:not(:disabled) {
      background: #8B0000;
    }
    .btn-save:disabled {
      opacity: 0.6;
      cursor: not-allowed;
    }
    .btn-cancel {
      padding: 10px 24px;
      background: #F5F5F5;
      color: #666;
      border: 1px solid #E0E0E0;
      border-radius: 8px;
      cursor: pointer;
    }
    .password-input-wrapper {
      position: relative;
    }
    .password-input-wrapper input {
      width: 100%;
      padding-right: 40px;
    }
    .toggle-password {
      position: absolute;
      right: 10px;
      top: 50%;
      transform: translateY(-50%);
      background: none;
      border: none;
      cursor: pointer;
      color: #999;
    }
    .toggle-password:hover {
      color: #C41E3A;
    }
    .password-strength {
      margin-top: 5px;
    }
    .strength-bar {
      height: 4px;
      background-color: #E0E0E0;
      border-radius: 2px;
      overflow: hidden;
      margin-bottom: 5px;
    }
    .strength-level {
      height: 100%;
      width: 0%;
      transition: all 0.3s ease;
    }
    .strength-level.weak {
      background-color: #F44336;
      width: 25%;
    }
    .strength-level.medium {
      background-color: #FF9800;
      width: 50%;
    }
    .strength-level.strong {
      background-color: #4CAF50;
      width: 75%;
    }
    .strength-level.very-strong {
      background-color: #2196F3;
      width: 100%;
    }
    .strength-text {
      font-size: 11px;
    }
    .strength-text.weak {
      color: #F44336;
    }
    .strength-text.medium {
      color: #FF9800;
    }
    .strength-text.strong {
      color: #4CAF50;
    }
    .strength-text.very-strong {
      color: #2196F3;
    }
    .password-requirements {
      list-style: none;
      padding: 0;
      margin: 5px 0 0;
      font-size: 11px;
    }
    .password-requirements li {
      color: #999;
      margin-bottom: 3px;
    }
    .password-requirements li.valid {
      color: #4CAF50;
    }
    .password-match {
      margin-top: 5px;
      font-size: 12px;
    }
    .password-match .valid {
      color: #4CAF50;
    }
    .password-match .invalid {
      color: #F44336;
    }
    .addresses-grid {
      display: grid;
      gap: 20px;
    }
    .address-card {
      background: #F9F9F9;
      border-radius: 12px;
      padding: 20px;
      position: relative;
      border: 1px solid #E0E0E0;
      transition: all 0.3s ease;
    }
    .address-card.default {
      border-color: #C41E3A;
      background: #FFF5F5;
    }
    .address-badge {
      position: absolute;
      top: -10px;
      right: 20px;
      background: #C41E3A;
      color: white;
      padding: 4px 12px;
      border-radius: 20px;
      font-size: 11px;
      font-weight: 600;
    }
    .address-content p {
      margin: 5px 0;
    }
    .recipient {
      font-weight: 600;
      color: #333;
    }
    .phone, .address {
      font-size: 13px;
      color: #666;
    }
    .address-actions {
      display: flex;
      gap: 10px;
      margin-top: 15px;
      padding-top: 15px;
      border-top: 1px solid #E0E0E0;
    }
    .action-btn {
      padding: 6px 12px;
      border: none;
      border-radius: 6px;
      cursor: pointer;
      font-size: 12px;
      display: flex;
      align-items: center;
      gap: 6px;
      transition: all 0.3s ease;
    }
    .action-btn.edit {
      background: #2196F3;
      color: white;
    }
    .action-btn.delete {
      background: #F44336;
      color: white;
    }
    .action-btn.default {
      background: #4CAF50;
      color: white;
    }
    .action-btn:hover {
      transform: translateY(-2px);
    }
    .modal {
      display: none;
      position: fixed;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(0,0,0,0.5);
      z-index: 1000;
      align-items: center;
      justify-content: center;
    }
    .modal.show {
      display: flex;
    }
    .modal-content {
      background: white;
      border-radius: 12px;
      width: 90%;
      max-width: 500px;
      max-height: 90vh;
      overflow-y: auto;
    }
    .modal-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 20px;
      border-bottom: 1px solid #E0E0E0;
    }
    .modal-header h3 {
      margin: 0;
      font-size: 20px;
    }
    .close-btn {
      background: none;
      border: none;
      font-size: 28px;
      cursor: pointer;
      color: #999;
    }
    .modal form {
      padding: 20px;
    }
    .stats-grid {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 20px;
    }
    @media (max-width: 768px) {
      .stats-grid {
        grid-template-columns: 1fr;
      }
    }
    .stat-card {
      background: #F9F9F9;
      border-radius: 12px;
      padding: 25px;
      display: flex;
      align-items: center;
      gap: 20px;
      transition: all 0.3s ease;
    }
    .stat-card:hover {
      transform: translateY(-4px);
      box-shadow: 0 4px 20px rgba(0,0,0,0.1);
    }
    .stat-icon {
      width: 60px;
      height: 60px;
      background: linear-gradient(135deg, #C41E3A, #8B0000);
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
    }
    .stat-icon i {
      font-size: 28px;
      color: white;
    }
    .stat-info {
      flex: 1;
    }
    .stat-value {
      font-size: 28px;
      font-weight: 700;
      color: #C41E3A;
    }
    .stat-label {
      font-size: 13px;
      color: #666;
      margin-top: 5px;
    }
    .loading-wrapper {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      min-height: 400px;
    }
    .spinner {
      width: 50px;
      height: 50px;
      border: 3px solid #E0E0E0;
      border-top-color: #C41E3A;
      border-radius: 50%;
      animation: spin 1s linear infinite;
    }
    @keyframes spin {
      to { transform: rotate(360deg); }
    }
    .checkbox {
      flex-direction: row;
      align-items: center;
    }
    .checkbox label {
      display: flex;
      align-items: center;
      gap: 8px;
      cursor: pointer;
    }
    @media (max-width: 768px) {
      .page-header h1 { font-size: 32px; }
      .profile-content { padding: 20px; }
      .info-row { flex-direction: column; gap: 5px; }
      .info-label { width: 100%; }
      .form-row { grid-template-columns: 1fr; }
    }
  `]
})
export class ProfileComponent implements OnInit, OnDestroy {
  private http = inject(HttpClient);
  private toastr = inject(ToastrService);
  private router = inject(Router);
  private authService = inject(AuthService);
  private destroy$ = new Subject<void>();
  private apiUrl = 'http://localhost:8080/api';

  activeTab = 'profile';
  loading = true;
  saving = false;
  isEditing = false;
  changingPassword = false;
  uploadingAvatar = false;
  avatarPreviewUrl: string | null = null;

  profile: UserProfileResponse | null = null;
  addresses: UserAddressResponse[] = [];
  statistics: UserStatisticsResponse | null = null;

  editForm: UpdateProfileRequest = {
    firstName: '',
    lastName: '',
    phone: '',
    dateOfBirth: ''
  };

  passwordForm: ChangePasswordRequest = {
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  };

  // Password visibility toggles
  showCurrentPassword = false;
  showNewPassword = false;
  showConfirmPassword = false;

  // Password validation
  passwordStrength = { class: '', text: '', percentage: '0%' };
  passwordsMatch = false;
  hasMinLength = false;
  hasUpperCase = false;
  hasLowerCase = false;
  hasNumber = false;

  addressForm: AddAddressRequest = {
    type: 'HOME',
    recipientName: '',
    phone: '',
    province: '',
    district: '',
    ward: '',
    streetAddress: '',
    postalCode: '',
    isDefault: false
  };

  showAddressModal = false;
  isEditingAddress = false;
  editingAddressId: string | null = null;

  private getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('accessToken') || localStorage.getItem('token');
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
  }

  ngOnInit(): void {
    this.loadProfile();
    this.loadAddresses();
    this.loadStatistics();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadProfile(): void {
    this.http.get<UserProfileResponse>(`${this.apiUrl}/users/me`, {
      headers: this.getAuthHeaders()
    }).pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (res) => {
          this.profile = res;
          this.authService.updateCurrentUserAvatar(res.avatarUrl);
          this.loading = false;
          this.editForm = {
            firstName: res.firstName || '',
            lastName: res.lastName || '',
            phone: res.phone || '',
            dateOfBirth: res.dateOfBirth || ''
          };
        },
        error: (error) => {
          console.error('Error loading profile:', error);
          this.loading = false;
          if (error.status === 401) {
            this.toastr.error('Vui lòng đăng nhập lại');
            this.logout();
          } else {
            this.toastr.error('Không thể tải thông tin profile');
          }
        }
      });
  }

  loadAddresses(): void {
    this.http.get<UserAddressResponse[]>(`${this.apiUrl}/users/me/addresses`, {
      headers: this.getAuthHeaders()
    }).pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (res) => {
          this.addresses = res;
        },
        error: (error) => {
          console.error('Error loading addresses:', error);
        }
      });
  }

  loadStatistics(): void {
    this.http.get<UserStatisticsResponse>(`${this.apiUrl}/users/me/statistics`, {
      headers: this.getAuthHeaders()
    }).pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (res) => {
          this.statistics = res;
        },
        error: (error) => {
          console.error('Error loading statistics:', error);
        }
      });
  }

  setActiveTab(tab: string): void {
    this.activeTab = tab;
    if (tab === 'addresses') {
      this.loadAddresses();
    } else if (tab === 'statistics') {
      this.loadStatistics();
    } else if (tab === 'change-password') {
      this.resetPasswordForm();
    }
  }

  toggleEdit(): void {
    this.isEditing = true;
  }

  cancelEdit(): void {
    this.isEditing = false;
    if (this.profile) {
      this.editForm = {
        firstName: this.profile.firstName || '',
        lastName: this.profile.lastName || '',
        phone: this.profile.phone || '',
        dateOfBirth: this.profile.dateOfBirth || ''
      };
    }
  }

  updateProfile(): void {
    this.saving = true;
    this.http.put<UserProfileResponse>(`${this.apiUrl}/users/me`, this.editForm, {
      headers: this.getAuthHeaders()
    }).pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (res) => {
          this.profile = res;
          this.isEditing = false;
          this.saving = false;
          this.toastr.success('Cập nhật thông tin thành công');
          this.loadStatistics();
        },
        error: (error) => {
          console.error('Error updating profile:', error);
          this.saving = false;
          if (error.status === 401 || error.status === 403) {
            this.toastr.error('Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại');
            this.logout();
          } else {
            this.toastr.error(error.error?.message || 'Cập nhật thất bại, vui lòng thử lại');
          }
        }
      });
  }

  // ========== CHANGE PASSWORD METHODS ==========

  changePassword(): void {
  if (!this.isPasswordFormValid()) {
    this.toastr.warning('Vui lòng kiểm tra lại thông tin mật khẩu');
    return;
  }

  this.changingPassword = true;

  console.log('Change password request:', this.passwordForm);

  this.http.post(`${this.apiUrl}/users/me/change-password`, this.passwordForm, {
    headers: this.getAuthHeaders()
  }).pipe(takeUntil(this.destroy$))
    .subscribe({
      next: (response: any) => {
        this.toastr.success(response.message || 'Đổi mật khẩu thành công! Vui lòng đăng nhập lại.');
        this.changingPassword = false;
        // Logout after password change
        setTimeout(() => {
          this.logout();
        }, 2000);
      },
      error: (error) => {
        console.error('Error changing password:', error);
        this.changingPassword = false;

        // Hiển thị lỗi chi tiết từ backend
        if (error.error) {
          // Nếu là string
          if (typeof error.error === 'string') {
            this.toastr.error(error.error);
          }
          // Nếu có message property
          else if (error.error.message) {
            this.toastr.error(error.error.message);
          }
          // Nếu là validation errors object
          else if (typeof error.error === 'object') {
            const errors = Object.values(error.error).join(', ');
            this.toastr.error(errors);
          }
          else {
            this.toastr.error('Đổi mật khẩu thất bại, vui lòng thử lại');
          }
        } else {
          this.toastr.error('Đổi mật khẩu thất bại, vui lòng thử lại');
        }
      }
    });
}
  validatePasswordStrength(): void {
    const password = this.passwordForm.newPassword;

    this.hasMinLength = password.length >= 6;
    this.hasUpperCase = /[A-Z]/.test(password);
    this.hasLowerCase = /[a-z]/.test(password);
    this.hasNumber = /[0-9]/.test(password);
console.log('Password validation:', {
    hasMinLength: this.hasMinLength,
    hasUpperCase: this.hasUpperCase,
    hasLowerCase: this.hasLowerCase,
    hasNumber: this.hasNumber
  });
    let strength = 0;
    if (this.hasMinLength) strength++;
    if (this.hasUpperCase) strength++;
    if (this.hasLowerCase) strength++;
    if (this.hasNumber) strength++;

    switch (strength) {
      case 1:
        this.passwordStrength = { class: 'weak', text: 'Yếu', percentage: '25%' };
        break;
      case 2:
        this.passwordStrength = { class: 'medium', text: 'Trung bình', percentage: '50%' };
        break;
      case 3:
        this.passwordStrength = { class: 'strong', text: 'Mạnh', percentage: '75%' };
        break;
      case 4:
        this.passwordStrength = { class: 'very-strong', text: 'Rất mạnh', percentage: '100%' };
        break;
      default:
        this.passwordStrength = { class: 'weak', text: 'Yếu', percentage: '0%' };
    }

    this.checkPasswordMatch();
  }

  checkPasswordMatch(): void {
    this.passwordsMatch = this.passwordForm.newPassword === this.passwordForm.confirmPassword;
  }

  isPasswordFormValid(): boolean {
  const isValid = this.passwordForm.currentPassword.length > 0 &&
             this.hasMinLength &&
             this.hasUpperCase &&
             this.hasLowerCase &&
             this.hasNumber &&
             this.passwordsMatch;

  console.log('Form valid:', isValid); // Debug
  return isValid;
}
  resetPasswordForm(): void {
    this.passwordForm = {
      currentPassword: '',
      newPassword: '',
      confirmPassword: ''
    };
    this.showCurrentPassword = false;
    this.showNewPassword = false;
    this.showConfirmPassword = false;
    this.passwordStrength = { class: '', text: '', percentage: '0%' };
    this.passwordsMatch = false;
    this.hasMinLength = false;
    this.hasUpperCase = false;
    this.hasLowerCase = false;
    this.hasNumber = false;
  }

  toggleCurrentPasswordVisibility(): void {
    this.showCurrentPassword = !this.showCurrentPassword;
  }

  toggleNewPasswordVisibility(): void {
    this.showNewPassword = !this.showNewPassword;
  }

  toggleConfirmPasswordVisibility(): void {
    this.showConfirmPassword = !this.showConfirmPassword;
  }

  // ========== AVATAR METHODS ==========

  uploadAvatar(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files || input.files.length === 0) return;

    const file = input.files[0];
    input.value = '';

    const allowedTypes = ['image/jpeg', 'image/png', 'image/webp', 'image/gif'];
    if (!allowedTypes.includes(file.type)) {
      this.toastr.error('Chỉ hỗ trợ ảnh JPEG, PNG, WEBP hoặc GIF');
      return;
    }

    if (file.size > 5 * 1024 * 1024) {
      this.toastr.error('File ảnh không được vượt quá 5MB');
      return;
    }

    const reader = new FileReader();
    reader.onload = () => this.avatarPreviewUrl = reader.result as string;
    reader.readAsDataURL(file);
    this.uploadingAvatar = true;

    const formData = new FormData();
    formData.append('file', file);

    this.http.post<UserProfileResponse>(`${this.apiUrl}/users/me/avatar`, formData, {
      headers: this.getAuthHeaders()
    }).pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (profile) => {
          this.profile = profile;
          this.avatarPreviewUrl = null;
          this.uploadingAvatar = false;
          this.authService.updateCurrentUserAvatar(profile.avatarUrl);
          this.toastr.success('Cập nhật ảnh đại diện thành công');
        },
        error: (error) => {
          this.avatarPreviewUrl = null;
          this.uploadingAvatar = false;
          console.error('Error uploading avatar:', error);
          this.toastr.error(error.error?.message || 'Upload ảnh thất bại');
        }
      });
  }

  deleteAvatar(): void {
    if (!this.profile?.avatarUrl || this.uploadingAvatar || !confirm('Bạn có chắc muốn xóa ảnh đại diện?')) {
      return;
    }

    this.uploadingAvatar = true;
    this.http.delete<void>(`${this.apiUrl}/users/me/avatar`, {
      headers: this.getAuthHeaders()
    }).pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          if (this.profile) this.profile.avatarUrl = null;
          this.avatarPreviewUrl = null;
          this.uploadingAvatar = false;
          this.authService.updateCurrentUserAvatar(null);
          this.toastr.success('Đã xóa ảnh đại diện');
        },
        error: error => {
          this.uploadingAvatar = false;
          this.toastr.error(error.error?.message || 'Không thể xóa ảnh đại diện');
        }
      });
  }

  handleAvatarError(): void {
    this.avatarPreviewUrl = null;
    if (this.profile) this.profile.avatarUrl = null;
  }

  // ========== ADDRESS METHODS ==========

  openAddressModal(address?: UserAddressResponse): void {
    if (address) {
      this.isEditingAddress = true;
      this.editingAddressId = address.id;
      this.addressForm = {
        type: address.type || 'HOME',
        recipientName: address.recipientName,
        phone: address.phone,
        province: address.province || '',
        district: address.district || '',
        ward: address.ward || '',
        streetAddress: address.streetAddress,
        postalCode: address.postalCode || '',
        isDefault: address.isDefault
      };
    } else {
      this.isEditingAddress = false;
      this.editingAddressId = null;
      this.addressForm = {
        type: 'HOME',
        recipientName: '',
        phone: '',
        province: '',
        district: '',
        ward: '',
        streetAddress: '',
        postalCode: '',
        isDefault: false
      };
    }
    this.showAddressModal = true;
  }

  closeAddressModal(): void {
    this.showAddressModal = false;
  }

  closeAddressModalOnOverlay(event: MouseEvent): void {
    if ((event.target as HTMLElement).classList.contains('modal')) {
      this.closeAddressModal();
    }
  }

  saveAddress(): void {
    if (!this.addressForm.recipientName || !this.addressForm.phone || !this.addressForm.streetAddress || !this.addressForm.province) {
      this.toastr.warning('Vui lòng điền đầy đủ thông tin bắt buộc');
      return;
    }

    const url = this.isEditingAddress
      ? `${this.apiUrl}/users/me/addresses/${this.editingAddressId}`
      : `${this.apiUrl}/users/me/addresses`;

    const request = this.isEditingAddress
      ? this.http.put<UserAddressResponse>(url, this.addressForm, { headers: this.getAuthHeaders() })
      : this.http.post<UserAddressResponse>(url, this.addressForm, { headers: this.getAuthHeaders() });

    request.pipe(takeUntil(this.destroy$)).subscribe({
      next: () => {
        this.toastr.success(this.isEditingAddress ? 'Cập nhật địa chỉ thành công' : 'Thêm địa chỉ thành công');
        this.closeAddressModal();
        this.loadAddresses();
      },
      error: (error) => {
        console.error('Error saving address:', error);
        this.toastr.error(error.error?.message || 'Thao tác thất bại, vui lòng thử lại');
      }
    });
  }

  deleteAddress(addressId: string): void {
    if (confirm('Bạn có chắc muốn xóa địa chỉ này?')) {
      this.http.delete(`${this.apiUrl}/users/me/addresses/${addressId}`, {
        headers: this.getAuthHeaders()
      }).pipe(takeUntil(this.destroy$))
        .subscribe({
          next: () => {
            this.toastr.success('Xóa địa chỉ thành công');
            this.loadAddresses();
          },
          error: (error) => {
            console.error('Error deleting address:', error);
            this.toastr.error(error.error?.message || 'Xóa địa chỉ thất bại');
          }
        });
    }
  }

  setDefaultAddress(addressId: string): void {
    this.http.patch(`${this.apiUrl}/users/me/addresses/${addressId}/default`, {}, {
      headers: this.getAuthHeaders()
    }).pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.toastr.success('Đặt địa chỉ mặc định thành công');
          this.loadAddresses();
        },
        error: (error) => {
          console.error('Error setting default address:', error);
          this.toastr.error(error.error?.message || 'Thao tác thất bại');
        }
      });
  }

  editAddress(address: UserAddressResponse): void {
    this.openAddressModal(address);
  }

  // ========== UTILITY METHODS ==========

  formatDate(dateStr: string): string {
    if (!dateStr) return '';
    const date = new Date(dateStr);
    return date.toLocaleDateString('vi-VN');
  }

  formatDateTime(dateStr: string): string {
    if (!dateStr) return '';
    const date = new Date(dateStr);
    return date.toLocaleDateString('vi-VN') + ' ' + date.toLocaleTimeString('vi-VN');
  }

  formatCurrency(amount: number | undefined | null): string {
    if (!amount && amount !== 0) return '0₫';
    return amount.toLocaleString('vi-VN') + '₫';
  }

  getFullAddress(address: UserAddressResponse): string {
    const parts = [address.streetAddress, address.ward, address.district, address.province];
    return parts.filter(p => p).join(', ');
  }

  logout(): void {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    this.toastr.success('Đăng xuất thành công');
    this.router.navigate(['/login']);
  }
}
