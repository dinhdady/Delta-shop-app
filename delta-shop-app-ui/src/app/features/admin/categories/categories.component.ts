import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AdminCategoryService } from './admin-category.service';
import { CategoryFormComponent } from './category-form.component';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-categories',
  standalone: true,
  imports: [CommonModule, CategoryFormComponent],
  templateUrl: './categories.component.html',
  styleUrls: ['./categories.component.scss']
})
export class CategoriesComponent implements OnInit {
  private categoryService = inject(AdminCategoryService);
  private toastr = inject(ToastrService);

  categories = signal<any[]>([]);
  showForm = signal(false);
  editingCategory = signal<any>(null);

  ngOnInit() {
    this.loadCategories();
  }

  loadCategories() {
    this.categoryService.getCategories().subscribe({
      next: (res) => {
        this.categories.set(res);
      },
      error: (err) => {
        console.error('Error loading categories:', err);
        this.toastr.error('Không thể tải danh mục');
      }
    });
  }

  addCategory() {
    this.editingCategory.set(null);
    this.showForm.set(true);
  }

  editCategory(category: any) {
    this.editingCategory.set(category);
    this.showForm.set(true);
  }

  deleteCategory(id: string) {
    if (confirm('Bạn có chắc muốn xóa danh mục này?')) {
      this.categoryService.deleteCategory(id).subscribe({
        next: () => {
          this.toastr.success('Xóa danh mục thành công');
          this.loadCategories();
        },
        error: (err) => {
          this.toastr.error('Không thể xóa danh mục');
        }
      });
    }
  }

  toggleStatus(id: string) {
    this.categoryService.toggleStatus(id).subscribe({
      next: () => {
        this.toastr.success('Cập nhật trạng thái thành công');
        this.loadCategories();
      },
      error: (err) => {
        this.toastr.error('Không thể cập nhật trạng thái');
      }
    });
  }

  onSave(event: any) {
  const formData = event.data;
  const imageFile = event.image;

  if (event.removeImage) {
    // Nếu xóa ảnh, gọi API xóa ảnh
    if (this.editingCategory()?.id) {
      this.categoryService.deleteCategoryImage(this.editingCategory().id).subscribe();
    }
    formData.imageUrl = null;
  }

  const saveObservable = this.editingCategory()
    ? this.categoryService.updateCategory(this.editingCategory().id, formData)
    : this.categoryService.createCategory(formData);

  saveObservable.subscribe({
    next: (response: any) => {
      const categoryId = response.id;

      if (imageFile && categoryId) {
        this.categoryService.uploadCategoryImage(categoryId, imageFile).subscribe({
          next: () => {
            this.toastr.success('Lưu danh mục thành công');
            this.closeForm();
            this.loadCategories();
          },
          error: (err) => {
            console.error('Upload error:', err);
            this.toastr.warning('Danh mục đã lưu nhưng upload ảnh thất bại');
            this.closeForm();
            this.loadCategories();
          }
        });
      } else {
        this.toastr.success('Lưu danh mục thành công');
        this.closeForm();
        this.loadCategories();
      }
    },
    error: (err) => {
      console.error('Save error:', err);
      this.toastr.error('Không thể lưu danh mục');
    }
  });
}
  closeForm() {
    this.showForm.set(false);
    this.editingCategory.set(null);
  }
}
