# ĐẶC TẢ USE CASE NGẮN GỌN - DELTA SHOP APP

---

## 1. ĐĂNG KÝ / ĐĂNG NHẬP
| Thuộc tính | Mô tả |
|------------|-------|
| **Use case** | Đăng ký, Đăng nhập, Quên mật khẩu |
| **Tác nhân** | Khách hàng |
| **Mô tả** | Người dùng tạo tài khoản/đăng nhập để sử dụng hệ thống |
| **Luồng** | Nhập thông tin → Validate → Tạo token → Vào hệ thống |

---

## 2. TÌM KIẾM SẢN PHẨM
| Thuộc tính | Mô tả |
|------------|-------|
| **Use case** | Tìm kiếm, Gợi ý, Lọc sản phẩm |
| **Tác nhân** | Khách hàng |
| **Mô tả** | Tìm sản phẩm theo từ khóa, áp dụng bộ lọc |
| **Luồng** | Nhập từ khóa → Tìm trong DB → Trả về kết quả |

---

## 3. XEM CHI TIẾT SẢN PHẨM
| Thuộc tính | Mô tả |
|------------|-------|
| **Use case** | Xem thông tin sản phẩm, Chọn biến thể |
| **Tác nhân** | Khách hàng |
| **Mô tả** | Hiển thị chi tiết: giá, mô tả, hình ảnh, đánh giá, biến thể |
| **Luồng** | Click sản phẩm → Lấy thông tin → Hiển thị chi tiết |

---

## 4. THÊM/XÓA GIỎ HÀNG
| Thuộc tính | Mô tả |
|------------|-------|
| **Use case** | Thêm/Xóa/Sửa số lượng sản phẩm trong giỏ |
| **Tác nhân** | Khách hàng |
| **Mô tả** | Quản lý giỏ hàng: thêm, cập nhật số lượng, xóa |
| **Luồng** | Chọn sản phẩm → Kiểm tra tồn kho → Thêm/Cập nhật giỏ |

---

## 5. THANH TOÁN ĐƠN HÀNG
| Thuộc tính | Mô tả |
|------------|-------|
| **Use case** | Tạo đơn, Thanh toán VNPay/MoMo/COD, Áp dụng khuyến mãi |
| **Tác nhân** | Khách hàng |
| **Mô tả** | Đặt hàng và thanh toán qua cổng thanh toán |
| **Luồng** | Chọn địa chỉ → Chọn phương thức → Xác nhận → Thanh toán |

---

## 6. QUẢN LÝ ĐƠN HÀNG
| Thuộc tính | Mô tả |
|------------|-------|
| **Use case** | Xem đơn hàng, Hủy đơn (Customer); Xử lý đơn (Admin) |
| **Tác nhân** | Khách hàng, Admin |
| **Mô tả** | Xem lịch sử đơn, cập nhật trạng thái, hủy đơn |
| **Luồng** | Xem danh sách → Xem chi tiết → Hủy/Cập nhật trạng thái |

---

## 7. ĐÁNH GIÁ SẢN PHẨM
| Thuộc tính | Mô tả |
|------------|-------|
| **Use case** | Tạo đánh giá, Xem đánh giá, Kiểm duyệt (Admin) |
| **Tác nhân** | Khách hàng, Admin |
| **Mô tả** | Đánh giá sản phẩm đã mua, xem đánh giá của người khác |
| **Luồng** | Kiểm tra đã mua → Chọn sao → Viết nhận xét → Gửi/Duyệt |

---

## 8. QUẢN TRỊ SẢN PHẨM/DANH MỤC/ĐƠN HÀNG
| Thuộc tính | Mô tả |
|------------|-------|
| **Use case** | CRUD Sản phẩm, Danh mục, Quản lý đơn hàng |
| **Tác nhân** | Admin |
| **Mô tả** | Quản lý toàn bộ sản phẩm, danh mục, xử lý đơn hàng |
| **Luồng** | Thêm/Sửa/Xóa thông tin → Cập nhật DB |

---

## 9. QUẢN LÝ NGƯỜI DÙNG
| Thuộc tính | Mô tả |
|------------|-------|
| **Use case** | Xem danh sách, Khóa/Mở khóa, Phân quyền |
| **Tác nhân** | Admin |
| **Mô tả** | Quản lý tài khoản người dùng, phân quyền |
| **Luồng** | Xem danh sách → Chọn tài khoản → Khóa/Mở/Phân quyền |

---

## 10. XEM BÁO CÁO DASHBOARD
| Thuộc tính | Mô tả |
|------------|-------|
| **Use case** | Dashboard tổng quan, Doanh số, Tồn kho, Người dùng |
| **Tác nhân** | Admin |
| **Mô tả** | Xem thống kê hoạt động hệ thống, doanh thu, báo cáo |
| **Luồng** | Chọn loại báo cáo → Chọn thời gian → Hiển thị số liệu |

---

## TÓM TẮT

| STT | Use Case | Tác nhân | Luồng chính |
|-----|----------|----------|-------------|
| 1 | Đăng ký/Đăng nhập | Khách hàng | Nhập thông tin → Validate → Vào hệ thống |
| 2 | Tìm kiếm sản phẩm | Khách hàng | Nhập từ khóa → Lọc → Xem kết quả |
| 3 | Xem chi tiết SP | Khách hàng | Click SP → Xem thông tin → Chọn biến thể |
| 4 | Giỏ hàng | Khách hàng | Thêm/Xóa/Sửa số lượng sản phẩm |
| 5 | Thanh toán | Khách hàng | Tạo đơn → Chọn PT thanh toán → Thanh toán |
| 6 | Quản lý đơn | Customer/Admin | Xem đơn → Xem chi tiết → Hủy/Cập nhật |
| 7 | Đánh giá SP | Customer/Admin | Viết đánh giá → Gửi → Duyệt |
| 8 | Quản trị | Admin | CRUD Sản phẩm/Danh mục/Đơn hàng |
| 9 | Quản lý user | Admin | Xem → Khóa/Mở/Phân quyền |
| 10 | Dashboard | Admin | Xem báo cáo → Export dữ liệu |
