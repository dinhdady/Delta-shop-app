import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { CartService } from '../../core/services/cart.service';
import { OrderService } from '../../core/services/order.service';
import { PaymentService } from '../../core/services/payment.service';
import { UserService, UserAddress } from '../../core/services/user.service';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-checkout',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div class="page-header">
      <div class="container">
        <h1>THANH TOÁN</h1>
      </div>
    </div>

    <section class="section">
      <div class="container">
        <form [formGroup]="checkoutForm" (ngSubmit)="onSubmit()" class="checkout-layout">
          <!-- Form Info -->
          <div class="checkout-form">
            <div class="form-section">
              <h2>1. THÔNG TIN GIAO HÀNG</h2>

              <!-- Address Selection -->
              @if (!isLoadingAddresses && addresses.length > 0) {
                <div class="address-selection">
                  <h3>Chọn địa chỉ giao hàng</h3>
                  <div class="saved-addresses">
                    @for (addr of addresses; track addr.id) {
                      <div class="address-card"
                           [class.selected]="selectedAddressId === addr.id"
                           (click)="selectAddress(addr)">
                        <div class="address-radio">
                          <input type="radio"
                                 [checked]="selectedAddressId === addr.id"
                                 name="addressSelection">
                        </div>
                        <div class="address-details">
                          <strong>{{ addr.recipientName }}</strong> - {{ addr.phone }}
                          <p>{{ addr.fullAddress }}</p>
                          @if (addr.isDefault) {
                            <span class="badge-default">Mặc định</span>
                          }
                        </div>
                      </div>
                    }

                    <button type="button" class="btn btn-outline btn-sm" (click)="toggleAddressForm()">
                      {{ showAddressForm ? '← Chọn địa chỉ đã lưu' : '+ Nhập địa chỉ mới' }}
                    </button>
                  </div>
                </div>
              }

              <!-- Address Form - show when no saved addresses or user chooses to enter new -->
              @if (showAddressForm) {
                <div class="form-group">
                  <label>Họ và tên *</label>
                  <input type="text" formControlName="fullName" [class.is-invalid]="isInvalid('fullName')">
                  @if (isInvalid('fullName')) {
                    <div class="error-message">Vui lòng nhập họ tên</div>
                  }
                </div>

                <div class="form-grid">
                  <div class="form-group">
                    <label>Số điện thoại *</label>
                    <input type="tel" formControlName="phone" [class.is-invalid]="isInvalid('phone')">
                    @if (isInvalid('phone')) {
                      <div class="error-message">Vui lòng nhập số điện thoại hợp lệ</div>
                    }
                  </div>
                  <div class="form-group">
                    <label>Email *</label>
                    <input type="email" formControlName="email" [class.is-invalid]="isInvalid('email')">
                    @if (isInvalid('email')) {
                      <div class="error-message">
                        @if (checkoutForm.get('email')?.errors?.['required']) {
                          Vui lòng nhập email
                        }
                        @if (checkoutForm.get('email')?.errors?.['email']) {
                          Email không hợp lệ
                        }
                      </div>
                    }
                  </div>
                </div>

                <div class="form-grid">
                  <div class="form-group">
                    <label>Tỉnh/Thành phố *</label>
                    <input type="text" formControlName="province" [class.is-invalid]="isInvalid('province')">
                    @if (isInvalid('province')) {
                      <div class="error-message">Vui lòng nhập tỉnh/thành phố</div>
                    }
                  </div>
                  <div class="form-group">
                    <label>Quận/Huyện *</label>
                    <input type="text" formControlName="district" [class.is-invalid]="isInvalid('district')">
                    @if (isInvalid('district')) {
                      <div class="error-message">Vui lòng nhập quận/huyện</div>
                    }
                  </div>
                </div>

                <div class="form-group">
                  <label>Phường/Xã *</label>
                  <input type="text" formControlName="ward" [class.is-invalid]="isInvalid('ward')">
                  @if (isInvalid('ward')) {
                    <div class="error-message">Vui lòng nhập phường/xã</div>
                  }
                </div>

                <div class="form-group">
                  <label>Địa chỉ giao hàng *</label>
                  <textarea formControlName="address" rows="3" [class.is-invalid]="isInvalid('address')"></textarea>
                  @if (isInvalid('address')) {
                    <div class="error-message">Vui lòng nhập địa chỉ giao hàng</div>
                  }
                </div>

                <div class="form-group">
                  <label>Ghi chú đơn hàng (Tùy chọn)</label>
                  <textarea formControlName="note" rows="2"></textarea>
                </div>
              }
            </div>

            <div class="form-section">
              <h2>2. PHƯƠNG THỨC THANH TOÁN</h2>
              <div class="payment-methods">
                <label class="payment-method">
                  <input type="radio" formControlName="paymentMethod" value="COD">
                  <span class="method-box">
                    <span class="method-name">Thanh toán khi nhận hàng (COD)</span>
                  </span>
                </label>
                <label class="payment-method">
                  <input type="radio" formControlName="paymentMethod" value="VNPAY">
                  <span class="method-box">
                    <span class="method-name">Thanh toán qua VNPay</span>
                  </span>
                </label>
              </div>
            </div>
          </div>

          <!-- Order Summary -->
          <div class="order-summary">
            <h2>ĐƠN HÀNG CỦA BẠN</h2>

            <div class="order-items">
              @for (item of cartItems(); track item.id) {
                <div class="order-item">
                  <div class="item-info">
                    <span class="item-name">{{ item.productName }}</span>
                    <span class="item-qty">x{{ item.quantity }}</span>
                  </div>
                  <div class="item-price">
                    {{ item.subtotal | currency:'VND':'symbol':'1.0-0' }}
                  </div>
                </div>
              }
            </div>

            <div class="summary-totals">
              <div class="summary-row">
                <span>Tạm tính</span>
                <span>{{ cartTotal() | currency:'VND':'symbol':'1.0-0' }}</span>
              </div>
              <div class="summary-row">
                <span>Phí vận chuyển</span>
                <span>Miễn phí</span>
              </div>
              <hr>
              <div class="summary-row total">
                <span>Tổng cộng</span>
                <span class="text-primary">{{ cartTotal() | currency:'VND':'symbol':'1.0-0' }}</span>
              </div>
            </div>

            <button
              type="submit"
              class="btn btn-primary btn-full submit-btn"
              [disabled]="isSubmitting || (!showAddressForm && !selectedAddressId) || (showAddressForm && checkoutForm.invalid)">
              {{ isSubmitting ? 'ĐANG XỬ LÝ...' : (checkoutForm.value.paymentMethod === 'VNPAY' ? 'THANH TOÁN VNPAY' : 'ĐẶT HÀNG') }}
            </button>
          </div>
        </form>
      </div>
    </section>
  `,
  styles: [`
    .page-header {
      background-color: var(--color-dark);
      padding: 3rem 0;
      text-align: center;
      h1 {
        color: var(--color-light);
        font-size: 3rem;
        margin: 0;
      }
    }

    .checkout-layout {
      display: grid;
      grid-template-columns: 3fr 2fr;
      gap: 4rem;

      @media (max-width: 992px) {
        grid-template-columns: 1fr;
      }
    }

    .form-section {
      margin-bottom: 3rem;

      h2 {
        font-size: 1.5rem;
        margin-bottom: 1.5rem;
        padding-bottom: 0.5rem;
        border-bottom: 2px solid var(--color-dark);
      }
    }

    .form-grid {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 1.5rem;

      @media (max-width: 576px) {
        grid-template-columns: 1fr;
      }
    }

    /* Payment Methods */
    .payment-methods {
      display: flex;
      flex-direction: column;
      gap: 1rem;
    }

    .payment-method {
      cursor: pointer;

      input {
        display: none;
      }

      .method-box {
        display: block;
        padding: 1.25rem;
        border: 1px solid var(--color-border);
        border-radius: var(--radius);
        transition: all var(--transition);
      }

      .method-name {
        font-weight: 600;
        font-size: 1.125rem;
      }

      input:checked + .method-box {
        border-color: var(--color-primary);
        background-color: rgba(230, 57, 70, 0.05);
      }
    }

    /* Order Summary */
    .order-summary {
      background-color: var(--color-light);
      padding: 2rem;
      border: 1px solid var(--color-border);
      border-radius: var(--radius);
      height: fit-content;
      position: sticky;
      top: 100px;

      h2 {
        font-size: 1.5rem;
        margin-bottom: 1.5rem;
        padding-bottom: 0.5rem;
        border-bottom: 2px solid var(--color-dark);
      }
    }

    .order-item {
      display: flex;
      justify-content: space-between;
      margin-bottom: 1rem;
      padding-bottom: 1rem;
      border-bottom: 1px dashed var(--color-border);

      .item-info {
        flex: 1;
        padding-right: 1rem;
      }

      .item-name {
        font-weight: 500;
      }

      .item-qty {
        color: var(--color-gray);
        margin-left: 0.5rem;
      }

      .item-price {
        font-weight: 600;
        white-space: nowrap;
      }
    }

    .summary-totals {
      margin-top: 1.5rem;

      .summary-row {
        display: flex;
        justify-content: space-between;
        margin-bottom: 1rem;
        font-size: 1.125rem;

        &.total {
          font-family: var(--font-heading);
          font-size: 1.5rem;
          font-weight: 700;
          margin-top: 1rem;
        }
      }

      hr {
        border: none;
        border-top: 1px solid var(--color-border);
        margin: 1.5rem 0;
      }
    }

    .submit-btn {
      margin-top: 2rem;
      height: 3.5rem;
    }

    /* Address Selection Styles */
    .address-selection {
      margin-bottom: 2rem;

      h3 {
        font-size: 1.125rem;
        margin-bottom: 1rem;
        color: var(--color-dark);
      }
    }

    .saved-addresses {
      display: flex;
      flex-direction: column;
      gap: 0.75rem;
    }

    .address-card {
      display: flex;
      align-items: flex-start;
      gap: 1rem;
      padding: 1rem;
      border: 1px solid var(--color-border);
      border-radius: var(--radius);
      cursor: pointer;
      transition: all var(--transition);

      &:hover {
        border-color: var(--color-primary);
        background-color: rgba(230, 57, 70, 0.02);
      }

      &.selected {
        border-color: var(--color-primary);
        background-color: rgba(230, 57, 70, 0.05);
      }

      .address-radio {
        padding-top: 0.25rem;

        input {
          width: 18px;
          height: 18px;
          accent-color: var(--color-primary);
        }
      }

      .address-details {
        flex: 1;

        strong {
          display: block;
          margin-bottom: 0.25rem;
        }

        p {
          margin: 0.5rem 0;
          color: var(--color-gray);
          font-size: 0.875rem;
        }

        .badge-default {
          display: inline-block;
          padding: 0.25rem 0.5rem;
          background-color: var(--color-primary);
          color: white;
          font-size: 0.75rem;
          border-radius: 4px;
        }
      }
    }

    .btn-sm {
      padding: 0.5rem 1rem;
      font-size: 0.875rem;
    }
  `]
})
export class CheckoutComponent implements OnInit {
  private fb = inject(FormBuilder);
  private cartService = inject(CartService);
  private orderService = inject(OrderService);
  private paymentService = inject(PaymentService);
  private userService = inject(UserService);
  private router = inject(Router);
  private toastr = inject(ToastrService);

  cartItems = this.cartService.cartItems;
  cartTotal = this.cartService.cartTotal;
  isSubmitting = false;

  // Address selection properties
  addresses: UserAddress[] = [];
  selectedAddressId: string | null = null;
  showAddressForm = false;
  isLoadingAddresses = false;

  // Trong component, sửa lại form group
  checkoutForm: FormGroup = this.fb.group({
    fullName: ['', Validators.required],
    phone: ['', [Validators.required, Validators.pattern(/^[0-9]{10,11}$/)]],
    email: ['', Validators.email], // Bỏ required, chỉ validate email nếu có value
    province: ['', Validators.required],
    district: ['', Validators.required],
    ward: ['', Validators.required],
    address: ['', Validators.required],
    note: [''],
    paymentMethod: ['COD', Validators.required]
  });

  ngOnInit() {
    this.loadAddresses();

    // Không set required cho email ở đây
    this.checkoutForm.get('email')?.setValidators(Validators.email);
    this.checkoutForm.get('email')?.updateValueAndValidity();
  }

  loadAddresses() {
    this.isLoadingAddresses = true;
    this.userService.getAddresses().subscribe({
      next: (addresses) => {
        this.addresses = addresses;
        // Auto select default address
        const defaultAddr = addresses.find(a => a.isDefault);
        if (defaultAddr) {
          this.selectAddress(defaultAddr);
          // QUAN TRỌNG: Không set showAddressForm = true
          this.showAddressForm = false; // Đảm bảo là false
        } else if (addresses.length > 0) {
          // Có địa chỉ nhưng không có default, chọn địa chỉ đầu tiên
          this.selectAddress(addresses[0]);
          this.showAddressForm = false;
        } else {
          // Không có địa chỉ nào thì mới hiển thị form
          this.showAddressForm = true;
        }
        this.isLoadingAddresses = false;
      },
      error: () => {
        this.isLoadingAddresses = false;
        this.showAddressForm = true;
      }
    });
  }
  checkFormStatus() {
    console.log('Form valid:', this.checkoutForm.valid);
    console.log('Form errors:', this.checkoutForm.errors);
    console.log('Selected address:', this.selectedAddressId);
    console.log('Show address form:', this.showAddressForm);

    Object.keys(this.checkoutForm.controls).forEach(key => {
      const control = this.checkoutForm.get(key);
      if (control?.invalid) {
        console.log(`Field ${key} invalid:`, control.errors);
      }
    });
  }
  selectAddress(address: UserAddress) {
    this.selectedAddressId = address.id;
    this.checkoutForm.patchValue({
      fullName: address.recipientName,
      phone: address.phone,
      email: address.email || '', // Nếu address không có email, set empty
      province: address.province,
      district: address.district,
      ward: address.ward,
      address: address.streetAddress
    });

    // QUAN TRỌNG: Set showAddressForm = false
    this.showAddressForm = false;

    // Mark tất cả fields là touched
    Object.keys(this.checkoutForm.controls).forEach(key => {
      const control = this.checkoutForm.get(key);
      if (control?.value) {
        control.markAsTouched();
        control.updateValueAndValidity();
      }
    });
  }

  toggleAddressForm() {
    this.showAddressForm = !this.showAddressForm;
    if (this.showAddressForm) {
      // Chuyển sang form nhập mới
      this.selectedAddressId = null;
      this.checkoutForm.reset({ paymentMethod: 'COD' });
      // Set lại validators cho email là required khi nhập tay
      this.checkoutForm.get('email')?.setValidators([Validators.required, Validators.email]);
      this.checkoutForm.get('email')?.updateValueAndValidity();
    } else {
      // Quay lại chọn địa chỉ, bỏ required email
      this.checkoutForm.get('email')?.clearValidators();
      this.checkoutForm.get('email')?.setValidators(Validators.email);
      this.checkoutForm.get('email')?.updateValueAndValidity();

      // Nếu có địa chỉ đã chọn, load lại
      if (this.selectedAddressId) {
        const selectedAddr = this.addresses.find(a => a.id === this.selectedAddressId);
        if (selectedAddr) {
          this.selectAddress(selectedAddr);
        }
      }
    }
  }

  isInvalid(field: string): boolean {
    const control = this.checkoutForm.get(field);
    return !!(control && control.invalid && (control.dirty || control.touched));
  }

  onSubmit() {
  if (this.checkoutForm.invalid || this.cartItems().length === 0) {
    this.checkoutForm.markAllAsTouched();
    this.toastr.warning('Vui lòng điền đầy đủ thông tin');
    return;
  }

  this.isSubmitting = true;
  const formValue = this.checkoutForm.getRawValue();

  // Validate shipping address
  if (!this.showAddressForm && !this.selectedAddressId) {
    this.toastr.error('Vui lòng chọn địa chỉ giao hàng');
    this.isSubmitting = false;
    return;
  }

  const orderData = {
    items: this.cartItems().map(item => ({
      variantId: item.variantId,
      quantity: item.quantity,
      unitPrice: item.unitPrice,  // ✅ THÊM - gửi giá hiện tại
      totalPrice: item.subtotal
    })),
    shippingName: formValue.fullName,
    shippingPhone: formValue.phone,
    shippingEmail: formValue.email,
    shippingProvince: formValue.province,
    shippingDistrict: formValue.district,
    shippingWard: formValue.ward,
    shippingAddress: formValue.address,
    paymentMethod: formValue.paymentMethod,
    notes: formValue.note
  };

  this.orderService.createOrder(orderData).subscribe({
    next: (order) => {
      if (formValue.paymentMethod === 'VNPAY') {
        // Tạo payment URL từ VNPay
        this.paymentService.createVNPayPayment({
          orderId: order.id,
          paymentMethod: 'VNPAY'
        }).subscribe({
          next: (payment) => {
            // Clear cart trước khi redirect
            this.cartService.clearCart();

            // Lưu order info vào sessionStorage để phòng trường hợp callback fail
            sessionStorage.setItem('pendingOrder', JSON.stringify({
              orderId: order.id,
              orderNumber: order.orderNumber,
              timestamp: Date.now()
            }));

            // Redirect đến VNPay
            if (payment.paymentUrl) {
              window.location.href = payment.paymentUrl;
            } else {
              this.toastr.error('Không nhận được URL thanh toán');
              this.isSubmitting = false;
              this.router.navigate(['/checkout']);
            }
          },
          error: (err) => {
            console.error('VNPay error:', err);
            this.isSubmitting = false;
            this.toastr.error(err.error?.message || 'Không thể khởi tạo thanh toán VNPay');
            // Rollback: xóa order vừa tạo nếu thanh toán fail?
            this.orderService.cancelOrder(order.id, 'Payment cancelled').subscribe();
          }
        });
        return;
      }

      // COD flow
      this.cartService.clearCart();
      this.toastr.success('Đặt hàng thành công!');
      if (localStorage.getItem('accessToken') === 'mock-admin-access-token') {
        this.router.navigate(['/payment-result'], {
          queryParams: {
            status: 'SUCCESS',
            orderId: order.id,
            orderNumber: order.orderNumber,
            transactionNo: 'MOCK-COD'
          }
        });
      } else {
        this.router.navigate(['/order-success', order.id]);
      }
    },
    error: (err) => {
      this.isSubmitting = false;
      console.error('Order creation error:', err);
      this.toastr.error(err.error?.message || 'Có lỗi xảy ra, vui lòng thử lại');
    }
  });
}
}
