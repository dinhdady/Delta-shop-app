import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AdminPromotionService, DiscountType, Promotion } from './admin-promotion.service';

@Component({
  selector: 'app-admin-promotions',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="page">
      <div class="page-header">
        <div>
          <h1>Quản lý khuyến mãi</h1>
          <p>Tạo mã giảm giá, bật/tắt chương trình và theo dõi lượt dùng.</p>
        </div>
        <button class="btn primary" (click)="openCreate()">+ Tạo khuyến mãi</button>
      </div>

      <div class="card">
        <table>
          <thead>
            <tr>
              <th>Tên</th>
              <th>Mã</th>
              <th>Loại</th>
              <th>Giá trị</th>
              <th>Thời gian</th>
              <th>Lượt dùng</th>
              <th>Trạng thái</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let promo of promotions()">
              <td>
                <strong>{{ promo.name }}</strong>
                <small>{{ promo.description || 'Không có mô tả' }}</small>
              </td>
              <td><code>{{ promo.code || 'AUTO' }}</code></td>
              <td>{{ typeLabel(promo.type) }}</td>
              <td>{{ valueLabel(promo) }}</td>
              <td>
                <span>{{ formatDate(promo.startsAt) }}</span>
                <small>{{ promo.endsAt ? formatDate(promo.endsAt) : 'Không giới hạn' }}</small>
              </td>
              <td>{{ promo.usedCount || 0 }} / {{ promo.usageLimit || '∞' }}</td>
              <td>
                <span class="status" [class.active]="promo.active">{{ promo.active ? 'Đang chạy' : 'Tạm tắt' }}</span>
              </td>
              <td class="actions">
                <button class="btn small" (click)="openEdit(promo)">Sửa</button>
                <button class="btn small" (click)="toggleStatus(promo)">{{ promo.active ? 'Tắt' : 'Bật' }}</button>
                <button class="btn small danger" (click)="deletePromotion(promo)">Xóa</button>
              </td>
            </tr>
            <tr *ngIf="!loading() && promotions().length === 0">
              <td colspan="8" class="empty">Chưa có khuyến mãi nào.</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div class="modal-backdrop" *ngIf="showForm()">
      <div class="modal">
        <div class="modal-header">
          <h2>{{ editingPromotion ? 'Cập nhật khuyến mãi' : 'Tạo khuyến mãi' }}</h2>
          <button class="close" (click)="closeForm()">×</button>
        </div>

        <form (ngSubmit)="savePromotion()" class="form">
          <label>
            Tên khuyến mãi
            <input name="name" [(ngModel)]="form.name" required>
          </label>

          <label>
            Mã giảm giá
            <input name="code" [(ngModel)]="form.code" [disabled]="!!editingPromotion" placeholder="VD: SALE10">
          </label>

          <label class="full">
            Mô tả
            <textarea name="description" [(ngModel)]="form.description" rows="3"></textarea>
          </label>

          <label>
            Loại
            <select name="type" [(ngModel)]="form.type" [disabled]="!!editingPromotion" required>
              <option value="PERCENTAGE">Phần trăm</option>
              <option value="FIXED_AMOUNT">Số tiền cố định</option>
              <option value="FREE_SHIPPING">Miễn phí vận chuyển</option>
            </select>
          </label>

          <label>
            Giá trị
            <input name="value" type="number" min="0" [(ngModel)]="form.value" required>
          </label>

          <label>
            Đơn tối thiểu
            <input name="minOrderAmount" type="number" min="0" [(ngModel)]="form.minOrderAmount">
          </label>

          <label>
            Giảm tối đa
            <input name="maxDiscountAmount" type="number" min="0" [(ngModel)]="form.maxDiscountAmount">
          </label>

          <label>
            Giới hạn lượt dùng
            <input name="usageLimit" type="number" min="0" [(ngModel)]="form.usageLimit">
          </label>

          <label>
            Mỗi người dùng
            <input name="usagePerUser" type="number" min="1" [(ngModel)]="form.usagePerUser">
          </label>

          <label>
            Bắt đầu
            <input name="startsAt" type="datetime-local" [(ngModel)]="form.startsAt" required>
          </label>

          <label>
            Kết thúc
            <input name="endsAt" type="datetime-local" [(ngModel)]="form.endsAt">
          </label>

          <label class="checkbox">
            <input name="stackable" type="checkbox" [(ngModel)]="form.stackable">
            Cho phép cộng dồn
          </label>

          <div class="form-actions">
            <button type="button" class="btn" (click)="closeForm()">Hủy</button>
            <button type="submit" class="btn primary" [disabled]="saving()">
              {{ saving() ? 'Đang lưu...' : 'Lưu' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  `,
  styles: [`
    .page { display: flex; flex-direction: column; gap: 1.5rem; }
    .page-header { display: flex; justify-content: space-between; align-items: center; gap: 1rem; }
    h1 { margin: 0; font-size: 1.8rem; color: #111827; }
    p { margin: .35rem 0 0; color: #6b7280; }
    .card { background: #fff; border-radius: 16px; box-shadow: 0 10px 25px rgba(15, 23, 42, .08); overflow: hidden; }
    table { width: 100%; border-collapse: collapse; }
    th, td { padding: 1rem; border-bottom: 1px solid #edf0f5; text-align: left; vertical-align: top; }
    th { background: #f8fafc; color: #64748b; font-size: .8rem; text-transform: uppercase; }
    td strong, td small { display: block; }
    td small { color: #64748b; margin-top: .25rem; }
    code { background: #f1f5f9; padding: .25rem .5rem; border-radius: 6px; color: #be123c; }
    .status { display: inline-flex; padding: .25rem .65rem; border-radius: 999px; background: #fee2e2; color: #b91c1c; font-size: .8rem; }
    .status.active { background: #dcfce7; color: #15803d; }
    .actions { display: flex; gap: .5rem; justify-content: flex-end; }
    .btn { border: 1px solid #d1d5db; background: #fff; color: #111827; border-radius: 10px; padding: .65rem 1rem; cursor: pointer; }
    .btn.primary { background: #cd4631; border-color: #cd4631; color: #fff; }
    .btn.danger { color: #dc2626; border-color: #fecaca; }
    .btn.small { padding: .45rem .7rem; font-size: .82rem; }
    .empty { text-align: center; color: #64748b; padding: 2rem; }
    .modal-backdrop { position: fixed; inset: 0; background: rgba(15, 23, 42, .55); display: grid; place-items: center; z-index: 2000; padding: 1rem; }
    .modal { width: min(760px, 100%); max-height: 90vh; overflow: auto; background: #fff; border-radius: 18px; padding: 1.25rem; }
    .modal-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem; }
    .modal-header h2 { margin: 0; }
    .close { border: 0; background: transparent; font-size: 1.8rem; cursor: pointer; }
    .form { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 1rem; }
    label { display: flex; flex-direction: column; gap: .4rem; color: #374151; font-weight: 600; }
    input, select, textarea { border: 1px solid #d1d5db; border-radius: 10px; padding: .7rem .8rem; font: inherit; }
    textarea { resize: vertical; }
    .full, .checkbox, .form-actions { grid-column: 1 / -1; }
    .checkbox { flex-direction: row; align-items: center; }
    .form-actions { display: flex; justify-content: flex-end; gap: .75rem; margin-top: .5rem; }
    @media (max-width: 900px) { .card { overflow-x: auto; } .form { grid-template-columns: 1fr; } }
  `]
})
export class AdminPromotionsComponent implements OnInit {
  promotions = signal<Promotion[]>([]);
  loading = signal(false);
  saving = signal(false);
  showForm = signal(false);
  editingPromotion: Promotion | null = null;

  form = this.emptyForm();

  constructor(private promotionService: AdminPromotionService) {}

  ngOnInit(): void {
    this.loadPromotions();
  }

  loadPromotions(): void {
    this.loading.set(true);
    this.promotionService.getPromotions().subscribe({
      next: res => {
        this.promotions.set(res.content || []);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  openCreate(): void {
    this.editingPromotion = null;
    this.form = this.emptyForm();
    this.showForm.set(true);
  }

  openEdit(promotion: Promotion): void {
    this.editingPromotion = promotion;
    this.form = {
      name: promotion.name,
      code: promotion.code,
      description: promotion.description || '',
      type: promotion.type,
      value: promotion.value,
      minOrderAmount: promotion.minOrderAmount || 0,
      maxDiscountAmount: promotion.maxDiscountAmount || 0,
      usageLimit: promotion.usageLimit || 0,
      usagePerUser: promotion.usagePerUser || 1,
      appliesTo: promotion.appliesTo || 'ALL',
      startsAt: this.toDatetimeLocal(promotion.startsAt),
      endsAt: promotion.endsAt ? this.toDatetimeLocal(promotion.endsAt) : '',
      stackable: promotion.stackable
    };
    this.showForm.set(true);
  }

  closeForm(): void {
    this.showForm.set(false);
    this.saving.set(false);
  }

  savePromotion(): void {
    this.saving.set(true);
    const payload = this.buildPayload();
    const request$ = this.editingPromotion
      ? this.promotionService.updatePromotion(this.editingPromotion.id, payload)
      : this.promotionService.createPromotion(payload);

    request$.subscribe({
      next: () => {
        this.closeForm();
        this.loadPromotions();
      },
      error: () => this.saving.set(false)
    });
  }

  toggleStatus(promotion: Promotion): void {
    const request$ = promotion.active
      ? this.promotionService.deactivatePromotion(promotion.id)
      : this.promotionService.activatePromotion(promotion.id);
    request$.subscribe(() => this.loadPromotions());
  }

  deletePromotion(promotion: Promotion): void {
    if (!confirm(`Xóa khuyến mãi "${promotion.name}"?`)) return;
    this.promotionService.deletePromotion(promotion.id).subscribe(() => this.loadPromotions());
  }

  typeLabel(type: DiscountType): string {
    return {
      PERCENTAGE: 'Phần trăm',
      FIXED_AMOUNT: 'Số tiền',
      FREE_SHIPPING: 'Miễn phí ship',
      BUY_X_GET_Y: 'Mua X tặng Y'
    }[type];
  }

  valueLabel(promotion: Promotion): string {
    if (promotion.type === 'PERCENTAGE') return `${promotion.value}%`;
    if (promotion.type === 'FREE_SHIPPING') return 'Miễn phí ship';
    return this.formatCurrency(promotion.value);
  }

  formatCurrency(value: number): string {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value || 0);
  }

  formatDate(value?: string): string {
    if (!value) return '';
    return new Date(value).toLocaleString('vi-VN');
  }

  private buildPayload(): any {
    const payload: any = {
      name: this.form.name,
      description: this.form.description,
      value: Number(this.form.value),
      minOrderAmount: this.form.minOrderAmount ? Number(this.form.minOrderAmount) : null,
      maxDiscountAmount: this.form.maxDiscountAmount ? Number(this.form.maxDiscountAmount) : null,
      usageLimit: this.form.usageLimit ? Number(this.form.usageLimit) : null,
      startsAt: this.form.startsAt,
      endsAt: this.form.endsAt || null
    };

    if (!this.editingPromotion) {
      payload.code = this.form.code;
      payload.type = this.form.type;
      payload.usagePerUser = this.form.usagePerUser ? Number(this.form.usagePerUser) : 1;
      payload.appliesTo = this.form.appliesTo || 'ALL';
      payload.stackable = this.form.stackable;
    }

    return payload;
  }

  private emptyForm() {
    const now = new Date();
    now.setMinutes(now.getMinutes() - now.getTimezoneOffset());
    return {
      name: '',
      code: '',
      description: '',
      type: 'PERCENTAGE' as DiscountType,
      value: 10,
      minOrderAmount: 0,
      maxDiscountAmount: 0,
      usageLimit: 0,
      usagePerUser: 1,
      appliesTo: 'ALL',
      startsAt: now.toISOString().slice(0, 16),
      endsAt: '',
      stackable: false
    };
  }

  private toDatetimeLocal(value: string): string {
    const date = new Date(value);
    date.setMinutes(date.getMinutes() - date.getTimezoneOffset());
    return date.toISOString().slice(0, 16);
  }
}
