# ĐẶC TẢ USE CASE CHÍNH - DELTA SHOP APP

---

## 1. ĐĂNG KÝ / ĐĂNG NHẬP (Authentication)

### 1.1. Đăng ký tài khoản
- **Tên**: Đăng ký tài khoản mới
- **Tác nhân**: Khách hàng (Guest)
- **Mô tả**: Người dùng tạo tài khoản mới với email, mật khẩu, thông tin cá nhân
- **Luồng chính**:
  1. Người dùng nhập email, mật khẩu, xác nhận mật khẩu
  2. Hệ thống kiểm tra email chưa tồn tại
  3. Hệ thống kiểm tra độ mạnh mật khẩu
  4. Tạo tài khoản với trạng thái "Chờ xác thực"
  5. Gửi email xác thực
  6. Thông báo đăng ký thành công

### 1.2. Đăng nhập
- **Tên**: Đăng nhập hệ thống
- **Tác nhân**: Khách hàng, Admin
- **Mô tả**: Người dùng đăng nhập bằng email và mật khẩu
- **Luồng chính**:
  1. Người dùng nhập email, mật khẩu
  2. Hệ thống xác thực thông tin
  3. Kiểm tra trạng thái tài khoản (ACTIVE)
  4. Tạo Access Token và Refresh Token
  5. Chuyển hướng theo vai trò (Customer/Admin)

### 1.3. Quên mật khẩu
- **Tên**: Khôi phục mật khẩu
- **Tác nhân**: Khách hàng
- **Mô tả**: Người dùng yêu cầu đặt lại mật khẩu qua email
- **Luồng chính**:
  1. Nhập email đăng ký
  2. Hệ thống gửi link đặt lại mật khẩu
  3. Người dùng click link, nhập mật khẩu mới
  4. Cập nhật mật khẩu thành công

---

## 2. TÌM KIẾM SẢN PHẨM (Product Search)

### 2.1. Tìm kiếm theo từ khóa
- **Tên**: Tìm kiếm sản phẩm
- **Tác nhân**: Khách hàng
- **Mô tả**: Tìm sản phẩm theo tên, mô tả, SKU
- **Luồng chính**:
  1. Người dùng nhập từ khóa vào ô tìm kiếm
  2. Hệ thống tìm kiếm full-text trong database
  3. Trả về danh sách sản phẩm phù hợp
  4. Hỗ trợ phân trang, sắp xếp

### 2.2. Gợi ý tìm kiếm
- **Tên**: Tự động hoàn thành tìm kiếm
- **Tác nhân**: Khách hàng
- **Mô tả**: Hiển thị gợi ý khi người dùng gõ
- **Luồng chính**:
  1. Người dùng nhập từ khóa (≥2 ký tự)
  2. Hệ thống trả về 5-10 gợi ý phù hợp
  3. Hiển thị tên sản phẩm, hình ảnh thumbnail

### 2.3. Lọc kết quả
- **Tên**: Lọc sản phẩm
- **Tác nhân**: Khách hàng
- **Mô tả**: Thu hẹp kết quả tìm kiếm theo tiêu chí
- **Luồng chính**:
  1. Chọn danh mục
  2. Chọn thương hiệu
  3. Chọn khoảng giá
  4. Chọn thuộc tính (size, màu sắc...)
  5. Sắp xếp (giá, mới nhất, bán chạy)

---

## 3. XEM CHI TIẾT SẢN PHẨM (Product Detail)

### 3.1. Xem thông tin sản phẩm
- **Tên**: Xem chi tiết sản phẩm
- **Tác nhân**: Khách hàng
- **Mô tả**: Hiển thị đầy đủ thông tin một sản phẩm
- **Luồng chính**:
  1. Người dùng click vào sản phẩm
  2. Hệ thống lấy thông tin theo slug/ID
  3. Hiển thị:
     - Tên, giá, mô tả, thông số
     - Hình ảnh gallery
     - Biến thể (size, màu)
     - Đánh giá, rating
     - Sản phẩm liên quan

### 3.2. Chọn biến thể
- **Tên**: Chọn biến thể sản phẩm
- **Tác nhân**: Khách hàng
- **Mô tả**: Chọn size, màu sắc, phiên bản
- **Luồng chính**:
  1. Hiển thị các tùy chọn biến thể
  2. Người dùng chọn thuộc tính
  3. Cập nhật giá, tồn kho theo biến thể
  4. Kiểm tra tồn kho trước khi thêm giỏ

---

## 4. THÊM / XÓA SẢN PHẨM TRONG GIỎ HÀNG (Shopping Cart)

### 4.1. Thêm vào giỏ hàng
- **Tên**: Thêm sản phẩm vào giỏ
- **Tác nhân**: Khách hàng
- **Mô tả**: Thêm sản phẩm với số lượng vào giỏ
- **Luồng chính**:
  1. Chọn sản phẩm, biến thể
  2. Chọn số lượng
  3. Kiểm tra tồn kho đủ
  4. Thêm vào giỏ hàng (DB hoặc Redis)
  5. Cập nhật số lượng giỏ hàng icon
  6. Thông báo thêm thành công

### 4.2. Xem giỏ hàng
- **Tên**: Xem chi tiết giỏ hàng
- **Tác nhân**: Khách hàng
- **Mô tả**: Hiển thị tất cả sản phẩm trong giỏ
- **Luồng chính**:
  1. Lấy danh sách sản phẩm trong giỏ
  2. Hiển thị: hình ảnh, tên, biến thể, giá, số lượng
  3. Tính tổng tiền, phí ship (ước tính)
  4. Hiển thị gợi ý sản phẩm liên quan

### 4.3. Cập nhật giỏ hàng
- **Tên**: Thay đổi số lượng / Xóa
- **Tác nhân**: Khách hàng
- **Mô tả**: Điều chỉnh số lượng hoặc xóa sản phẩm
- **Luồng chính**:
  1. Tăng/giảm số lượng
  2. Kiểm tra tồn kho
  3. Cập nhật giá tự động
  4. Hoặc xóa sản phẩm khỏi giỏ
  5. Nếu giỏ trống → hiển thị thông báo

### 4.4. Giỏ hàng khách (Guest Cart)
- **Tên**: Giỏ hàng không đăng nhập
- **Tác nhân**: Khách hàng
- **Mô tả**: Lưu giỏ hàng bằng session/localStorage
- **Luồng chính**:
  1. Lưu giỏ hàng vào localStorage
  2. Khi đăng nhập → gộp vào giỏ hàng tài khoản
  3. Đồng bộ với server

---

## 5. THANH TOÁN ĐƠN HÀNG (Order Payment)

### 5.1. Tạo đơn hàng
- **Tên**: Đặt hàng
- **Tác nhân**: Khách hàng
- **Mô tả**: Chuyển giỏ hàng thành đơn hàng
- **Luồng chính**:
  1. Chọn địa chỉ giao hàng
  2. Chọn phương thức vận chuyển
  3. Tính phí ship
  4. Nhập mã khuyến mãi (nếu có)
  5. Tính tổng thanh toán
  6. Chọn phương thức thanh toán
  7. Xác nhận đặt hàng

### 5.2. Thanh toán VNPay
- **Tên**: Thanh toán qua VNPay
- **Tác nhân**: Khách hàng
- **Mô tả**: Thanh toán online qua cổng VNPay
- **Luồng chính**:
  1. Tạo URL thanh toán VNPay
  2. Chuyển hướng đến VNPay
  3. Người dùng nhập thông tin thẻ
  4. VNPay xử lý và callback
  5. Xác minh chữ ký
  6. Cập nhật trạng thái đơn hàng
  7. Gửi email xác nhận

### 5.3. Thanh toán COD
- **Tên**: Thanh toán khi nhận hàng
- **Tác nhân**: Khách hàng
- **Mô tả**: Đặt hàng không cần thanh toán trước
- **Luồng chính**:
  1. Tạo đơn hàng với status "Chờ xác nhận"
  2. Trừ tồn kho tạm thời
  3. Thông báo đặt hàng thành công
  4. Admin xác nhận và giao hàng

### 5.4. Áp dụng khuyến mãi
- **Tên**: Sử dụng mã giảm giá
- **Tác nhân**: Khách hàng
- **Mô tả**: Nhập và áp dụng mã khuyến mãi
- **Luồng chính**:
  1. Nhập mã khuyến mãi
  2. Validate mã (còn hiệu lực, đủ điều kiện)
  3. Tính giảm giá
  4. Cập nhật tổng tiền
  5. Ghi nhận sử dụng

---

## 6. QUẢN LÝ ĐƠN HÀNG (Order Management)

### 6.1. Xem danh sách đơn hàng (Customer)
- **Tên**: Lịch sử đơn hàng
- **Tác nhân**: Khách hàng
- **Mô tả**: Xem tất cả đơn hàng của mình
- **Luồng chính**:
  1. Lấy danh sách đơn hàng theo user ID
  2. Hiển thị: Mã đơn, ngày đặt, tổng tiền, trạng thái
  3. Lọc theo trạng thái (Tất cả, Chờ xác nhận, Đang giao...)
  4. Phân trang

### 6.2. Xem chi tiết đơn hàng
- **Tên**: Chi tiết đơn hàng
- **Tác nhân**: Khách hàng
- **Mô tả**: Xem thông tin đầy đủ một đơn hàng
- **Luồng chính**:
  1. Thông tin đơn: Mã, ngày, trạng thái
  2. Danh sách sản phẩm đã mua
  3. Thông tin giao hàng
  4. Thông tin thanh toán
  5. Timeline trạng thái đơn
  6. Nút hành động (Hủy, Đánh giá...)

### 6.3. Hủy đơn hàng
- **Tên**: Hủy đơn hàng
- **Tác nhân**: Khách hàng
- **Mô tả**: Hủy đơn hàng chưa giao
- **Luồng chính**:
  1. Kiểm tra đơn có thể hủy (chưa xác nhận/giao)
  2. Nhập lý do hủy
  3. Cập nhật trạng thái "Đã hủy"
  4. Hoàn trả tồn kho
  5. Hoàn tiền nếu đã thanh toán
  6. Thông báo cho khách hàng

### 6.4. Quản lý đơn hàng (Admin)
- **Tên**: Xử lý đơn hàng
- **Tác nhân**: Admin
- **Mô tả**: Xem và cập nhật trạng thái tất cả đơn hàng
- **Luồng chính**:
  1. Xem danh sách tất cả đơn hàng
  2. Lọc theo trạng thái, ngày tháng
  3. Cập nhật trạng thái: Chờ xác nhận → Đã xác nhận → Đang giao → Hoàn thành
  4. Thêm mã vận chuyển
  5. Thêm ghi chú đơn hàng
  6. Export danh sách

---

## 7. ĐÁNH GIÁ SẢN PHẨM (Product Review)

### 7.1. Tạo đánh giá
- **Tên**: Viết đánh giá
- **Tác nhân**: Khách hàng
- **Mô tả**: Đánh giá sản phẩm đã mua
- **Luồng chính**:
  1. Kiểm tra user đã mua sản phẩm
  2. Chọn số sao (1-5)
  3. Viết nhận xét
  4. Upload hình ảnh (tùy chọn)
  5. Gửi đánh giá
  6. Chờ duyệt hoặc hiển thị ngay

### 7.2. Xem đánh giá
- **Tên**: Xem đánh giá sản phẩm
- **Tác nhân**: Khách hàng
- **Mô tả**: Xem đánh giá của người mua khác
- **Luồng chính**:
  1. Hiển thị điểm trung bình
  2. Hiển thị phân bố số sao
  3. Danh sách đánh giá (có phân trang)
  4. Lọc theo số sao
  5. Đánh dấu đánh giá hữu ích

### 7.3. Kiểm duyệt đánh giá (Admin)
- **Tên**: Duyệt đánh giá
- **Tác nhân**: Admin
- **Mô tả**: Phê duyệt hoặc từ chối đánh giá
- **Luồng chính**:
  1. Xem danh sách đánh giá chờ duyệt
  2. Xem nội dung đánh giá
  3. Phê duyệt → hiển thị công khai
  4. Hoặc Từ chối → nhập lý do
  5. Thông báo kết quả cho người đánh giá

---

## 8. QUẢN TRỊ SẢN PHẨM / DANH MỤC / ĐƠN HÀNG (Admin Management)

### 8.1. Quản lý sản phẩm
- **Tên**: CRUD Sản phẩm
- **Tác nhân**: Admin
- **Luồng chính**:
  - **Tạo**: Nhập tên, mô tả, giá, tồn kho → Upload ảnh → Thêm biến thể → Lưu
  - **Sửa**: Cập nhật thông tin, giá, tồn kho
  - **Xóa**: Xóa mềm (ẩn khỏi hiển thị)
  - **Tìm kiếm**: Theo tên, SKU, danh mục

### 8.2. Quản lý danh mục
- **Tên**: CRUD Danh mục
- **Tác nhân**: Admin
- **Luồng chính**:
  - Tạo danh mục cha/con
  - Sắp xếp thứ tự hiển thị
  - Upload ảnh danh mục
  - Bật/tắt trạng thái

### 8.3. Quản lý đơn hàng
- **Tên**: Xử lý đơn hàng
- **Tác nhân**: Admin
- **Luồng chính**:
  - Xem đơn hàng mới
  - Xác nhận đơn hàng
  - Cập nhật trạng thái giao hàng
  - Xử lý đơn hủy/hoàn trả
  - Export báo cáo

---

## 9. QUẢN LÝ NGƯỜI DÙNG (User Management)

### 9.1. Xem danh sách người dùng
- **Tên**: Danh sách khách hàng
- **Tác nhân**: Admin
- **Mô tả**: Xem tất cả tài khoản người dùng
- **Luồng chính**:
  1. Hiển thị danh sách với thông tin: Tên, Email, Ngày đăng ký
  2. Lọc theo trạng thái (Active/Pending/Blocked)
  3. Tìm kiếm theo tên/email
  4. Phân trang

### 9.2. Cập nhật thông tin người dùng
- **Tên**: Chỉnh sửa thông tin khách hàng
- **Tác nhân**: Admin
- **Luồng chính**:
  - Cập nhật thông tin cá nhân
  - Thay đổi vai trò (Customer → Admin)
  - Reset mật khẩu

### 9.3. Khóa / Mở khóa tài khoản
- **Tên**: Quản lý trạng thái tài khoản
- **Tác nhân**: Admin
- **Luồng chính**:
  1. Chọn tài khoản
  2. Khóa: Chuyển trạng thái BLOCKED, nhập lý do
  3. Mở khóa: Chuyển trạng thái ACTIVE
  4. Gửi email thông báo

---

## 10. XEM BÁO CÁO DASHBOARD (Dashboard & Reports)

### 10.1. Dashboard tổng quan
- **Tên**: Bảng điều khiển Admin
- **Tác nhân**: Admin
- **Mô tả**: Xem tổng quan hoạt động hệ thống
- **Luồng chính**:
  1. Hiển thị các chỉ số:
     - Tổng đơn hàng hôm nay
     - Doanh thu hôm nay
     - Đơn hàng chờ xử lý
     - Người dùng mới
     - Sản phẩm sắp hết hàng
  2. Biểu đồ doanh thu 7 ngày gần nhất
  3. Danh sách đơn hàng mới

### 10.2. Báo cáo doanh số
- **Tên**: Thống kê bán hàng
- **Tác nhân**: Admin
- **Luồng chính**:
  - Chọn khoảng thời gian (Hôm nay/Tuần/Tháng/Năm)
  - Hiển thị:
    - Tổng doanh thu
    - Số đơn hàng
    - Giá trị đơn trung bình
    - Sản phẩm bán chạy top 10
    - Doanh thu theo danh mục
  - Export Excel/PDF

### 10.3. Báo cáo tồn kho
- **Tên**: Thống kê hàng tồn
- **Tác nhân**: Admin
- **Luồng chính**:
  - Sản phẩm sắp hết hàng (dưới ngưỡng)
  - Sản phẩm hết hàng
  - Giá trị tồn kho tổng
  - Xuất nhập tồn

### 10.4. Báo cáo người dùng
- **Tên**: Thống kê khách hàng
- **Tác nhân**: Admin
- **Luồng chính**:
  - Tổng người dùng
  - Người dùng mới theo thời gian
  - Khách hàng thân thiết (top mua hàng)
  - Người dùng hoạt động gần đây

---

## TỔNG KẾT CÁC TÁC NHÂN

| Tác nhân | Mô tả | Use case liên quan |
|----------|-------|-------------------|
| **Khách vãng lai (Guest)** | Chưa đăng nhập | Tìm kiếm, Xem sản phẩm, Giỏ hàng (tạm) |
| **Khách hàng (Customer)** | Đã đăng nhập | Tất cả use case mua hàng |
| **Quản trị viên (Admin)** | Quản lý hệ thống | Quản trị sản phẩm, đơn hàng, người dùng, báo cáo |

---

## TỔNG KẾT 10 USE CASE CHÍNH

| STT | Use Case Chính | Mức độ quan trọng |
|-----|----------------|------------------|
| 1 | Đăng ký / Đăng nhập | Cao |
| 2 | Tìm kiếm sản phẩm | Cao |
| 3 | Xem chi tiết sản phẩm | Cao |
| 4 | Thêm/Xóa giỏ hàng | Cao |
| 5 | Thanh toán đơn hàng | Cao |
| 6 | Quản lý đơn hàng | Cao |
| 7 | Đánh giá sản phẩm | Trung bình |
| 8 | Quản trị sản phẩm/danh mục/đơn hàng | Cao |
| 9 | Quản lý người dùng | Cao |
| 10 | Xem báo cáo Dashboard | Trung bình |

