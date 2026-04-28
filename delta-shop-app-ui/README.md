# Delta Sports App - Frontend (Angular)

Giao diện cửa hàng thương mại điện tử chuyên cung cấp dụng cụ thể thao chuyên nghiệp.

## Cài đặt và Khởi chạy

### Yêu cầu hệ thống
- Node.js (phiên bản khuyên dùng: 18.x hoặc 20.x)
- Angular CLI (phiên bản 19+)

### Cài đặt
1. Mở terminal tại thư mục `delta-shop-app-ui`.
2. Chạy lệnh cài đặt dependencies:
   ```bash
   npm install
   ```
   *(Lưu ý: Nếu gặp lỗi dependency conflict với `ngx-toastr` và `@angular/common`, vui lòng chạy `npm install --legacy-peer-deps`)*

### Chạy dự án
```bash
npm start
```
Dự án sẽ khởi chạy tại `http://localhost:4200/`.

---

## Cấu hình Backend (Spring Boot)

### Cấu hình API URL
Mở file `src/environments/environment.ts` và thiết lập `apiUrl` trỏ về backend Spring Boot của bạn.
Mặc định đang là:
```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api'
};
```

### Các API Endpoints đã được tích hợp (theo chuẩn)
- **Auth**: `/api/auth/login`, `/api/auth/register`
- **Products**: `/api/products`, `/api/products/{id}`, `/api/products/featured`, `/api/categories`
- **Cart**: `/api/cart`, `/api/cart/items`, `/api/cart/items/{id}`
- **Orders**: `/api/orders`, `/api/orders/{id}`, `/api/orders/my-orders`

### Authentication (JWT)
Hệ thống sử dụng JWT Bearer Token.
- Khi người dùng đăng nhập thành công, token được lưu vào `localStorage`.
- `authInterceptor` (`src/app/core/interceptors/auth.interceptor.ts`) tự động bắt mọi HTTP requests và thêm header: `Authorization: Bearer <token>`.
- Xử lý lỗi `401 Unauthorized`: tự động xóa token và chuyển hướng về trang `/login`.

---

## Kiến trúc Frontend
- **Framework**: Angular 19 Standalone Components.
- **Styling**: SCSS thuần, sử dụng CSS Variables định nghĩa tại `src/styles/_variables.scss`. Tuyệt đối không sử dụng gradient.
- **State Management**: Angular Signals thay vì NgRx (giúp tối ưu, gọn nhẹ, dễ bảo trì, được khuyến nghị mạnh mẽ từ Angular 17+).
- **Icons**: `lucide-angular`.
- **Notifications**: `ngx-toastr`.
