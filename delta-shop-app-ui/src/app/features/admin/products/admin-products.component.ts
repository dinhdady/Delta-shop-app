import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminProductService } from './admin-product.service';
import { AdminCategoryService } from '../categories/admin-category.service';
import { AdminBrandService } from '../brands/admin-brand.service';
import { ProductFormComponent } from './product-form.component';

@Component({
  selector: 'app-admin-products',
  standalone: true,
  imports: [CommonModule, FormsModule, ProductFormComponent],
  templateUrl: './admin-products.component.html',
  styleUrls: ['./admin-products.component.scss']
})
export class AdminProductsComponent implements OnInit {
  private productService = inject(AdminProductService);
  private categoryService = inject(AdminCategoryService);
  private brandService = inject(AdminBrandService);

  products = signal<any[]>([]);
  totalElements = signal(0);
  totalPages = signal(0);
  currentPage = signal(0);
  pageSize = 10;

  categories = signal<any[]>([]);
  brands = signal<any[]>([]);

  searchKeyword = '';
  selectedCategory = '';
  selectedStatus = '';

  showForm = signal(false);
  editingProduct = signal<any>(null);

  // Modal state
  showModal = signal(false);
  modalImageUrl = signal('');
  modalImageAlt = signal('');

  ngOnInit() {
    this.loadProducts();
    this.loadMetadata();
  }

  loadProducts() {
    this.productService.getProducts(this.currentPage(), this.pageSize, this.searchKeyword, this.selectedStatus)
      .subscribe(res => {
        console.log('Products data:', res);
        this.products.set(res.content);
        this.totalElements.set(res.totalElements);
        this.totalPages.set(res.totalPages);
      });
  }

  loadMetadata() {
    this.categoryService.getCategories().subscribe((res: any) => this.categories.set(res));
    this.brandService.getAllBrands().subscribe((res: any) => this.brands.set(res));
  }

  onSearch() {
    this.currentPage.set(0);
    this.loadProducts();
  }

  onPageChange(page: number) {
    this.currentPage.set(page);
    this.loadProducts();
  }

  toggleFeatured(product: any) {
    this.productService.updateFeatured(product.id, !product.featured).subscribe(() => {
      this.loadProducts();
    });
  }

  toggleStatus(product: any) {
    const newStatus = product.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE';
    this.productService.updateStatus(product.id, newStatus).subscribe(() => {
      this.loadProducts();
    });
  }

  deleteProduct(id: string) {
    if (confirm('Bạn có chắc chắn muốn xóa sản phẩm này?')) {
      this.productService.deleteProduct(id).subscribe(() => {
        this.loadProducts();
      });
    }
  }

  addProduct() {
    this.editingProduct.set(null);
    this.showForm.set(true);
  }

  editProduct(product: any) {
    this.productService.getProductById(product.id).subscribe((res: any) => {
      const detailProduct = res?.product || product;
      this.editingProduct.set({
        ...detailProduct,
        categoryId: detailProduct.category?.id,
        brandId: detailProduct.brand?.id,
        primaryImage: detailProduct.primaryImage || detailProduct.imageUrl || '',
        images: detailProduct.images || (detailProduct.primaryImage ? [{ url: detailProduct.primaryImage, primary: true }] : [])
      });
      this.showForm.set(true);
    });
  }

  onSave(formData: any) {
    // Log để debug
    console.log('Raw formData:', formData);

    // Xử lý tags - kiểm tra type an toàn
    let processedTags: string[] = [];
    if (formData.tags) {
        if (typeof formData.tags === 'string') {
            processedTags = formData.tags.split(',').map((t: string) => t.trim()).filter((t: string) => t);
        } else if (Array.isArray(formData.tags)) {
            processedTags = formData.tags;
        }
    }

    // Xử lý sportTypes - kiểm tra type an toàn
    let processedSportTypes: string[] = [];
    if (formData.sportTypes) {
        if (typeof formData.sportTypes === 'string') {
            processedSportTypes = formData.sportTypes.split(',').map((t: string) => t.trim()).filter((t: string) => t);
        } else if (Array.isArray(formData.sportTypes)) {
            processedSportTypes = formData.sportTypes;
        }
    }

    // Xử lý sizeGuides - lọc bỏ những size trống
    let processedSizeGuides: any[] = [];
    if (formData.sizeGuides && Array.isArray(formData.sizeGuides)) {
        processedSizeGuides = formData.sizeGuides
            .filter((guide: any) => guide.size && guide.size.trim() !== '')
            .map((guide: any) => ({
                size: guide.size,
                label: guide.label || '',
                description: guide.description || '',
                measurement: guide.measurement || '',
                stockQuantity: guide.stockQuantity ? Number(guide.stockQuantity) : 0,
                priceModifier: guide.priceModifier ? Number(guide.priceModifier) : 0,
                sku: guide.sku || ''
            }));
    }

    // Xử lý specifications - chuyển từ array sang object
    let processedSpecifications: Record<string, any> = {};
    if (formData.specifications) {
        if (Array.isArray(formData.specifications)) {
            // Nếu là array từ form
            formData.specifications.forEach((spec: any) => {
                if (spec.key && spec.key.trim() && spec.value && spec.value.trim()) {
                    processedSpecifications[spec.key.trim()] = spec.value.trim();
                }
            });
        } else if (typeof formData.specifications === 'object') {
            // Nếu đã là object
            processedSpecifications = formData.specifications;
        }
    }

    // Xử lý images
    let processedImages: any[] = [];
    if (formData.images && Array.isArray(formData.images)) {
        processedImages = formData.images.map((img: any, index: number) => ({
            url: img.url,
            publicId: img.publicId || '',
            altText: img.altText || '',
            sortOrder: img.sortOrder !== undefined ? img.sortOrder : index,
            primary: img.primary === true || (index === 0 && !formData.images.some((i: any) => i.primary === true))
        }));
    }

    // Tìm ảnh chính
    const primaryImage = processedImages.find((img: any) => img.primary)?.url ||
                         (processedImages[0]?.url) ||
                         formData.primaryImage || '';

    // Tạo payload
    const payload = {
        name: formData.name,
        categoryId: formData.categoryId,
        brandId: formData.brandId || null,
        sku: formData.sku || null,
        shortDescription: formData.shortDescription || '',
        description: formData.description || '',
        basePrice: Number(formData.basePrice) || 0,
        comparePrice: formData.comparePrice ? Number(formData.comparePrice) : null,
        costPrice: formData.costPrice ? Number(formData.costPrice) : null,
        stockQuantity: Number(formData.stockQuantity) || 0,
        status: formData.status || 'ACTIVE',
        weight: formData.weight ? Number(formData.weight) : null,
        length: formData.length ? Number(formData.length) : null,
        width: formData.width ? Number(formData.width) : null,
        height: formData.height ? Number(formData.height) : null,
        featured: formData.featured === true || formData.featured === 'true',
        newArrival: formData.newArrival === true || formData.newArrival === 'true',
        bestSeller: formData.bestSeller === true || formData.bestSeller === 'true',
        tags: processedTags,
        sportTypes: processedSportTypes,
        sizeGuides: processedSizeGuides,
        specifications: processedSpecifications,
        images: processedImages,
        primaryImage: primaryImage
    };

    // Log payload trước khi gửi
    console.log('Final payload:', JSON.stringify(payload, null, 2));

    // Gọi API
    if (this.editingProduct()) {
        this.productService.updateProduct(this.editingProduct().id, payload).subscribe({
            next: (response) => {
                console.log('Product updated successfully:', response);
                this.closeForm();
                this.loadProducts();
                // Hiển thị thông báo thành công
                // this.toastService.success('Cập nhật sản phẩm thành công');
            },
            error: (err) => {
                console.error('Error updating product:', err);
                // Hiển thị thông báo lỗi
                const errorMsg = err.error?.message || err.message || 'Cập nhật sản phẩm thất bại';
                alert(errorMsg);
                // this.toastService.error(errorMsg);
            }
        });
    } else {
        this.productService.createProduct(payload).subscribe({
            next: (response) => {
                console.log('Product created successfully:', response);
                this.closeForm();
                this.loadProducts();
                // Hiển thị thông báo thành công
                // this.toastService.success('Thêm sản phẩm thành công');
            },
            error: (err) => {
                console.error('Error creating product:', err);
                // Hiển thị thông báo lỗi
                const errorMsg = err.error?.message || err.message || 'Thêm sản phẩm thất bại';
                alert(errorMsg);
                // this.toastService.error(errorMsg);
            }
        });
    }
}

  closeForm() {
    this.showForm.set(false);
    this.editingProduct.set(null);
  }

  // Helper methods for image handling
  getProductImage(product: any): string {
    if (!product) return '';

    // Ưu tiên primaryImage
    if (product.primaryImage && product.primaryImage !== 'null') {
      return product.primaryImage;
    }

    // Nếu có images array
    if (product.images && Array.isArray(product.images) && product.images.length > 0) {
      const firstImage = product.images[0];
      if (typeof firstImage === 'string') return firstImage;
      if (firstImage.url) return firstImage.url;
    }

    // Nếu có imageUrl
    if (product.imageUrl) return product.imageUrl;

    // Nếu có mainImage
    if (product.mainImage) return product.mainImage;

    return '';
  }

  // Open image modal for zoom
  openImageModal(product: any, event?: Event) {
    if (event) {
      event.stopPropagation();
    }
    const imageUrl = this.getProductImage(product);
    if (imageUrl) {
      this.modalImageUrl.set(imageUrl);
      this.modalImageAlt.set(product.name || 'Product image');
      this.showModal.set(true);
    }
  }

  // Close image modal
  closeModal() {
    this.showModal.set(false);
    this.modalImageUrl.set('');
    this.modalImageAlt.set('');
  }

  // Close modal on ESC key
  onModalKeydown(event: KeyboardEvent) {
    if (event.key === 'Escape') {
      this.closeModal();
    }
  }

  getStatusText(status: string): string {
    switch (status?.toUpperCase()) {
      case 'ACTIVE':
        return 'Đang bán';
      case 'INACTIVE':
      case 'DRAFT':
        return 'Ngừng bán';
      case 'OUT_OF_STOCK':
        return 'Hết hàng';
      case 'DISCONTINUED':
        return 'Ngừng SX';
      default:
        return 'N/A';
    }
  }

  getStatusClass(status: string): string {
    switch (status?.toUpperCase()) {
      case 'ACTIVE':
        return 'active';
      case 'INACTIVE':
      case 'DRAFT':
        return 'draft';
      case 'OUT_OF_STOCK':
        return 'out-of-stock';
      case 'DISCONTINUED':
        return 'discontinued';
      default:
        return 'draft';
    }
  }
}
