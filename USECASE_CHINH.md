# USE CASE CHÍNH - DELTA SHOP APP

## I. HỆ THỐNG NGƯỜI DÙNG (User System)

### 1. Xác thực (Authentication)
- Đăng ký tài khoản
- Đăng nhập / Đăng xuất
- Quên mật khẩu / Đặt lại mật khẩu
- Xác thực email

### 2. Quản lý tài khoản
- Xem / Cập nhật thông tin cá nhân
- Đổi mật khẩu
- Quản lý địa chỉ (Thêm, Sửa, Xóa, Đặt mặc định)
- Upload avatar

### 3. Quản lý người dùng (Admin)
- Xem danh sách người dùng
- Cập nhật trạng thái / vai trò người dùng
- Xóa người dùng

---

## II. HỆ THỐNG SẢN PHẨM (Product System)

### 1. Xem sản phẩm (Customer)
- Xem danh sách sản phẩm
- Xem chi tiết sản phẩm
- Tìm kiếm sản phẩm
- Lọc sản phẩm (theo danh mục, thương hiệu, giá)
- Xem sản phẩm nổi bật / mới / bán chạy / giảm giá

### 2. Quản lý sản phẩm (Admin)
- Thêm sản phẩm mới
- Cập nhật sản phẩm
- Xóa sản phẩm
- Quản lý biến thể (Thêm, Sửa, Xóa)
- Quản lý hình ảnh sản phẩm
- Quản lý tồn kho

### 3. Quản lý danh mục (Admin)
- Thêm / Cập nhật / Xóa danh mục
- Quản lý cây danh mục

### 4. Quản lý thương hiệu (Admin)
- Thêm / Cập nhật / Xóa thương hiệu

---

## III. HỆ THỐNG GIỎ HÀNG (Cart System)

### 1. Giỏ hàng
- Thêm sản phẩm vào giỏ hàng
- Cập nhật số lượng sản phẩm
- Xóa sản phẩm khỏi giỏ hàng
- Xem tóm tắt giỏ hàng

### 2. Giỏ hàng khách
- Thêm vào giỏ hàng (không đăng nhập)
- Gộp giỏ hàng khi đăng nhập

---

## IV. HỆ THỐNG ĐƠN HÀNG (Order System)

### 1. Đơn hàng (Customer)
- Tạo đơn hàng mới
- Xem danh sách đơn hàng
- Xem chi tiết đơn hàng
- Hủy đơn hàng

### 2. Quản lý đơn hàng (Admin)
- Xem tất cả đơn hàng
- Cập nhật trạng thái đơn hàng
- Cập nhật mã vận chuyển
- Xử lý hủy đơn hàng

### 3. Thống kê
- Doanh thu theo thời gian
- Đơn hàng theo trạng thái
- Export báo cáo (Excel, PDF)

---

## V. HỆ THỐNG THANH TOÁN (Payment System)

- Tạo thanh toán
- Xử lý callback thanh toán (VNPay, MoMo)
- Hoàn tiền (Full / Partial)
- Xem lịch sử thanh toán

---

## VI. HỆ THỐNG KHUYẾN MÃI (Promotion System)

- Tạo / Cập nhật / Xóa khuyến mãi
- Áp dụng mã khuyến mãi
- Validate khuyến mãi
- Tính giảm giá

---

## VII. HỆ THỐNG ĐÁNH GIÁ (Review System)

- Tạo đánh giá sản phẩm
- Cập nhật / Xóa đánh giá
- Xem đánh giá sản phẩm
- Kiểm duyệt đánh giá (Admin)

---

## VIII. HỆ THỐNG VẬN CHUYỂN (Shipping System)

- Tính phí vận chuyển
- Theo dõi đơn hàng
- Quản lý vùng giao hàng (Admin)
- Cập nhật trạng thái giao hàng

---

## IX. HỆ THỐNG ĐIỂM THƯỞNG (Loyalty System)

- Tích điểm từ đơn hàng
- Xem số điểm hiện có
- Đổi điểm lấy giảm giá
- Xem lịch sử điểm
- Quản lý hạng thành viên

---

## X. HỆ THỐNG THÔNG BÁO (Notification System)

- Gửi thông báo đến user
- Thông báo trạng thái đơn hàng
- Thông báo thanh toán
- Xem danh sách thông báo
- Đánh dấu đã đọc

---

## XI. HỆ THỐNG YÊU THÍCH (Wishlist)

- Thêm / Xóa sản phẩm yêu thích
- Xem danh sách yêu thích
- Chuyển từ yêu thích sang giỏ hàng

---

## XII. HỆ THỐNG LIÊN HỆ (Contact System)

- Gửi form liên hệ
- Trả lời liên hệ (Admin)
- Quản lý danh sách liên hệ

---

## XIII. HỆ THỐNG TÌM KIẾM (Search System)

- Tìm kiếm sản phẩm
- Gợi ý tìm kiếm tự động
- Lọc kết quả tìm kiếm

---

## XIV. HỆ THỐNG BÁO CÁO (Dashboard)

- Xem dashboard tổng quan (Admin)
- Thống kê doanh số
- Thống kê sản phẩm bán chạy
- Thống kê người dùng

---

## TỔNG KẾT

| STT | Hệ thống chính | Số use case chính |
|-----|----------------|-------------------|
| 1 | User System | 9 |
| 2 | Product System | 10 |
| 3 | Cart System | 4 |
| 4 | Order System | 8 |
| 5 | Payment System | 4 |
| 6 | Promotion System | 4 |
| 7 | Review System | 4 |
| 8 | Shipping System | 4 |
| 9 | Loyalty System | 5 |
| 10 | Notification System | 5 |
| 11 | Wishlist | 3 |
| 12 | Contact System | 3 |
| 13 | Search System | 3 |
| 14 | Dashboard | 4 |
| **TỔNG** | | **~70** |

