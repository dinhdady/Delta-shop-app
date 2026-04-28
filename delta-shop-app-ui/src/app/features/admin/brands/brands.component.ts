import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AdminBrandService } from './admin-brand.service';
import { BrandFormComponent } from './brand-form.component';

@Component({
  selector: 'app-brands',
  standalone: true,
  imports: [CommonModule, BrandFormComponent],
  templateUrl: './brands.component.html',
  styleUrls: ['./brands.component.scss']
})
export class BrandsComponent implements OnInit {
  private brandService = inject(AdminBrandService);

  brands = signal<any[]>([]);
  showForm = signal(false);
  editingBrand = signal<any>(null);

  ngOnInit() {
    this.loadBrands();
  }

  loadBrands() {
    this.brandService.getAllBrands().subscribe({
      next: (res) => {
        this.brands.set(res);
      },
      error: (err) => {
        console.error('Error loading brands:', err);
      }
    });
  }

  addBrand() {
    this.editingBrand.set(null);
    this.showForm.set(true);
  }

  editBrand(brand: any) {
    this.editingBrand.set(brand);
    this.showForm.set(true);
  }

  deleteBrand(id: string) {
    if (confirm('Bạn có chắc muốn xóa thương hiệu này?')) {
      this.brandService.deleteBrand(id).subscribe({
        next: () => {
          this.loadBrands();
        },
        error: (err) => {
          console.error('Error deleting brand:', err);
          alert('Không thể xóa thương hiệu này vì có sản phẩm liên quan');
        }
      });
    }
  }

  toggleStatus(id: string) {
    this.brandService.toggleStatus(id).subscribe(() => {
      this.loadBrands();
    });
  }

  toggleFeatured(id: string) {
    this.brandService.toggleFeatured(id).subscribe(() => {
      this.loadBrands();
    });
  }

  onSave(formData: any) {
    if (this.editingBrand()) {
      this.brandService.updateBrand(this.editingBrand().id, formData).subscribe({
        next: () => {
          this.closeForm();
          this.loadBrands();
        },
        error: (err) => {
          console.error('Error updating brand:', err);
          alert('Cập nhật thất bại');
        }
      });
    } else {
      this.brandService.createBrand(formData).subscribe({
        next: () => {
          this.closeForm();
          this.loadBrands();
        },
        error: (err) => {
          console.error('Error creating brand:', err);
          alert('Tạo thương hiệu thất bại');
        }
      });
    }
  }

  closeForm() {
    this.showForm.set(false);
    this.editingBrand.set(null);
  }

  onImageError(event: Event) {
    const img = event.target as HTMLImageElement;
    img.src = 'assets/images/placeholder-brand.png';
  }
}
