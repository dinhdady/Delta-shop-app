# PHÂN CẤP USE CASE - DELTA SHOP APP

## I. HỆ THỐNG XÁC THỰC VÀ QUẢN LÝ NGƯỜI DÙNG (Authentication & User Management)

### I.1. Xác thực (Authentication)
- **UC-001**: Đăng ký tài khoản (Register)
- **UC-002**: Đăng nhập (Login)
- **UC-003**: Đăng xuất (Logout)
- **UC-004**: Làm mới token (Refresh Token)
- **UC-005**: Xác thực email (Verify Email)
- **UC-006**: Gửi lại email xác thực (Resend Verification)
- **UC-007**: Yêu cầu đặt lại mật khẩu (Forgot Password)
- **UC-008**: Đặt lại mật khẩu (Reset Password)
- **UC-009**: Đổi mật khẩu (Change Password)
- **UC-010**: Kiểm tra token hợp lệ (Verify Token)
- **UC-011**: Thu hồi tất cả phiên đăng nhập (Revoke Sessions)

### I.2. Quản lý hồ sơ cá nhân (User Profile)
- **UC-012**: Xem thông tin cá nhân (View Profile)
- **UC-013**: Cập nhật thông tin cá nhân (Update Profile)
- **UC-014**: Upload ảnh đại diện (Upload Avatar)
- **UC-015**: Xóa ảnh đại diện (Delete Avatar)

### I.3. Quản lý địa chỉ (Address Management)
- **UC-016**: Thêm địa chỉ mới (Add Address)
- **UC-017**: Cập nhật địa chỉ (Update Address)
- **UC-018**: Xóa địa chỉ (Delete Address)
- **UC-019**: Đặt địa chỉ mặc định (Set Default Address)
- **UC-020**: Xem danh sách địa chỉ (Get User Addresses)
- **UC-021**: Xem địa chỉ mặc định (Get Default Address)

### I.4. Quản lý người dùng (Admin)
- **UC-022**: Xem danh sách tất cả người dùng (Get All Users)
- **UC-023**: Xem chi tiết người dùng (Get User By ID)
- **UC-024**: Cập nhật thông tin người dùng (Update User)
- **UC-025**: Cập nhật trạng thái người dùng (Update User Status)
- **UC-026**: Gán vai trò người dùng (Assign User Role)
- **UC-027**: Xóa người dùng (Delete User)
- **UC-028**: Xem thống kê người dùng (User Statistics)

---

## II. HỆ THỐNG SẢN PHẨM (Product Management)

### II.1. Xem sản phẩm (Product Browsing)
- **UC-029**: Xem chi tiết sản phẩm theo slug (Get By Slug)
- **UC-030**: Xem chi tiết sản phẩm theo ID (Get By ID)
- **UC-031**: Tìm kiếm sản phẩm với bộ lọc (Search with Filter)
- **UC-032**: Xem sản phẩm nổi bật (Get Featured)
- **UC-033**: Xem sản phẩm mới (Get New Arrivals)
- **UC-034**: Xem sản phẩm bán chạy (Get Best Sellers)
- **UC-035**: Xem sản phẩm liên quan (Get Related)
- **UC-036**: Xem sản phẩm theo danh mục (By Category)
- **UC-037**: Xem sản phẩm theo thương hiệu (By Brand)
- **UC-038**: Xem sản phẩm đang giảm giá (On Sale)

### II.2. Quản lý sản phẩm (Admin - Product CRUD)
- **UC-039**: Tạo sản phẩm mới (Create Product)
- **UC-040**: Cập nhật sản phẩm (Update Product)
- **UC-041**: Xóa mềm sản phẩm (Soft Delete)
- **UC-042**: Xóa vĩnh viễn sản phẩm (Hard Delete)
- **UC-043**: Khôi phục sản phẩm (Restore)
- **UC-044**: Cập nhật trạng thái sản phẩm (Update Status)
- **UC-045**: Cập nhật trạng thái hàng loạt (Bulk Update Status)
- **UC-046**: Xóa sản phẩm hàng loạt (Bulk Delete)
- **UC-047**: Cập nhật hàng loạt (Bulk Update)

### II.3. Quản lý biến thể (Variant Management)
- **UC-048**: Thêm biến thể sản phẩm (Add Variant)
- **UC-049**: Cập nhật biến thể (Update Variant)
- **UC-050**: Xóa biến thể (Delete Variant)
- **UC-051**: Cập nhật tồn kho biến thể (Update Variant Stock)

### II.4. Quản lý hình ảnh (Image Management)
- **UC-052**: Thêm hình ảnh sản phẩm (Add Image)
- **UC-053**: Xóa hình ảnh (Delete Image)
- **UC-054**: Đặt ảnh chính (Set Primary Image)
- **UC-055**: Sắp xếp lại hình ảnh (Reorder Images)

### II.5. Quản lý thuộc tính (Attribute Management)
- **UC-056**: Gán thuộc tính cho biến thể (Assign Attribute)
- **UC-057**: Xóa thuộc tính khỏi biến thể (Remove Attribute)
- **UC-058**: Xóa tất cả thuộc tính (Remove All Attributes)
- **UC-059**: Xem thuộc tính theo biến thể (Get by Variant)
- **UC-060**: Gán thuộc tính hàng loạt (Bulk Assign)

### II.6. Theo dõi tồn kho (Inventory Tracking)
- **UC-061**: Xem sản phẩm hết hàng (Out of Stock)
- **UC-062**: Xem sản phẩm sắp hết hàng (Low Stock)
- **UC-063**: Xem thống kê sản phẩm (Product Statistics)

---

## III. HỆ THỐNG DANH MỤC (Category Management)

- **UC-064**: Xem tất cả danh mục (Get All)
- **UC-065**: Xem danh mục đang hoạt động (Get Active)
- **UC-066**: Xem cây danh mục (Get Tree)
- **UC-067**: Xem danh mục theo ID (Get By ID)
- **UC-068**: Xem danh mục theo slug (Get By Slug)
- **UC-069**: Xem danh mục con (Get Subcategories)
- **UC-070**: Tạo danh mục mới (Create)
- **UC-071**: Cập nhật danh mục (Update)
- **UC-072**: Upload ảnh danh mục (Upload Image)
- **UC-073**: Xóa ảnh danh mục (Delete Image)
- **UC-074**: Xóa danh mục (Delete)
- **UC-075**: Bật/tắt trạng thái danh mục (Toggle Status)
- **UC-076**: Sắp xếp lại danh mục (Reorder)

---

## IV. HỆ THỐNG THƯƠNG HIỆU (Brand Management)

- **UC-077**: Xem tất cả thương hiệu (Get All)
- **UC-078**: Xem thương hiệu đang hoạt động (Get Active)
- **UC-079**: Xem thương hiệu nổi bật (Get Featured)
- **UC-080**: Xem thương hiệu theo ID (Get By ID)
- **UC-081**: Xem thương hiệu theo slug (Get By Slug)
- **UC-082**: Tạo thương hiệu mới (Create)
- **UC-083**: Cập nhật thương hiệu (Update)
- **UC-084**: Xóa thương hiệu (Delete)
- **UC-085**: Bật/tắt trạng thái thương hiệu (Toggle Status)
- **UC-086**: Đánh dấu thương hiệu nổi bật (Toggle Featured)

---

## V. HỆ THỐNG GIỎ HÀNG (Shopping Cart)

### V.1. Giỏ hàng người dùng (User Cart)
- **UC-087**: Xem giỏ hàng (Get Cart)
- **UC-088**: Thêm vào giỏ hàng (Add To Cart)
- **UC-089**: Cập nhật số lượng (Update Cart Item)
- **UC-090**: Xóa khỏi giỏ hàng (Remove Cart Item)
- **UC-091**: Xóa toàn bộ giỏ hàng (Clear Cart)
- **UC-092**: Xem tóm tắt giỏ hàng (Get Cart Summary)

### V.2. Giỏ hàng khách (Guest Cart)
- **UC-093**: Xem giỏ hàng khách (Get Guest Cart)
- **UC-094**: Thêm vào giỏ khách (Add To Guest Cart)
- **UC-095**: Gộp giỏ khách vào tài khoản (Merge Guest Cart)

### V.3. Validation & Checkout
- **UC-096**: Validate các mục trong giỏ (Validate Cart Items)
- **UC-097**: Validate tồn kho (Validate Stock)
- **UC-098**: Chuẩn bị giỏ để thanh toán (Prepare For Checkout)
- **UC-099**: Khóa giỏ khi thanh toán (Lock Cart)

---

## VI. HỆ THỐNG ĐƠN HÀNG (Order Management)

### VI.1. Đơn hàng người dùng (User Orders)
- **UC-100**: Tạo đơn hàng mới (Create Order)
- **UC-101**: Xem danh sách đơn hàng (Get User Orders)
- **UC-102**: Xem chi tiết đơn hàng (Get Order Detail)
- **UC-103**: Hủy đơn hàng (Cancel Order)

### VI.2. Quản lý đơn hàng (Admin)
- **UC-104**: Xem tất cả đơn hàng (Get All Orders)
- **UC-105**: Xem đơn hàng theo ID (Get By ID)
- **UC-106**: Cập nhật trạng thái đơn (Update Status)
- **UC-107**: Cập nhật trạng thái hàng loạt (Bulk Update)
- **UC-108**: Cập nhật trạng thái thanh toán (Update Payment Status)
- **UC-109**: Thêm mã vận chuyển (Add Tracking Number)
- **UC-110**: Thêm ghi chú admin (Add Admin Note)
- **UC-111**: Xóa đơn hàng (Delete Order)
- **UC-112**: Khôi phục đơn hàng (Restore Order)

### VI.3. Thống kê đơn hàng (Order Statistics)
- **UC-113**: Xem thống kê tổng quan (Get Statistics)
- **UC-114**: Thống kê theo ngày (Daily Statistics)
- **UC-115**: Thống kê theo tháng (Monthly Statistics)
- **UC-116**: Thống kê theo năm (Yearly Statistics)
- **UC-117**: Doanh thu theo khoảng thời gian (Revenue By Date Range)
- **UC-118**: Sản phẩm bán chạy nhất (Top Selling Products)
- **UC-119**: Phân bố trạng thái đơn (Status Distribution)

### VI.4. Xuất báo cáo (Export)
- **UC-120**: Export đơn hàng sang Excel
- **UC-121**: Export đơn hàng sang PDF
- **UC-122**: Tạo hóa đơn (Generate Invoice)

### VI.5. Lịch sử đơn hàng
- **UC-123**: Xem lịch sử trạng thái đơn (Get Status History)
- **UC-124**: Kiểm tra đơn có thể hủy (Is Cancellable)

---

## VII. HỆ THỐNG THANH TOÁN (Payment System)

### VII.1. Tạo và xử lý thanh toán
- **UC-125**: Tạo thanh toán (Create Payment)
- **UC-126**: Xử lý thanh toán (Process Payment)
- **UC-127**: Xem thanh toán theo ID (Get By ID)
- **UC-128**: Xem thanh toán theo mã giao dịch (Get By Transaction)
- **UC-129**: Xem danh sách thanh toán của đơn (Get Order Payments)

### VII.2. Thanh toán VNPay
- **UC-130**: Tạo thanh toán VNPay (Create VNPay)
- **UC-131**: Xử lý callback VNPay (Handle Callback)

### VII.3. Thanh toán MoMo
- **UC-132**: Tạo thanh toán MoMo (Create MoMo)
- **UC-133**: Xử lý callback MoMo (Handle Callback)

### VII.4. Chuyển khoản ngân hàng
- **UC-134**: Tạo thanh toán chuyển khoản (Create Bank Transfer)

### VII.5. Hoàn tiền (Refund)
- **UC-135**: Hoàn tiền đầy đủ (Full Refund)
- **UC-136**: Hoàn tiền một phần (Partial Refund)

### VII.6. Validation & Thống kê
- **UC-137**: Xác minh chữ ký thanh toán (Verify Signature)
- **UC-138**: Validate trạng thái thanh toán (Validate Status)
- **UC-139**: Doanh thu theo khoảng thời gian (Revenue Between)
- **UC-140**: Thống kê thanh toán (Payment Statistics)

---

## VIII. HỆ THỐNG KHUYẾN MÃI (Promotion System)

### VIII.1. Quản lý khuyến mãi (Admin)
- **UC-141**: Tạo khuyến mãi mới (Create)
- **UC-142**: Cập nhật khuyến mãi (Update)
- **UC-143**: Xóa khuyến mãi (Delete)
- **UC-144**: Kích hoạt khuyến mãi (Activate)
- **UC-145**: Vô hiệu hóa khuyến mãi (Deactivate)
- **UC-146**: Nhân bản khuyến mãi (Duplicate)
- **UC-147**: Xem tất cả khuyến mãi (Get All)

### VIII.2. Tra cứu khuyến mãi
- **UC-148**: Xem khuyến mãi theo ID (Get By ID)
- **UC-149**: Xem khuyến mãi theo mã (Get By Code)
- **UC-150**: Xem khuyến mãi đang hoạt động (Get Active)
- **UC-151**: Xem khuyến mãi áp dụng cho đơn (Applicable To Order)

### VIII.3. Validation & Tính toán
- **UC-152**: Validate khuyến mãi (Validate)
- **UC-153**: Tính giảm giá (Calculate Discount)
- **UC-154**: Tính giảm giá tốt nhất (Calculate Best Discount)

### VIII.4. Theo dõi sử dụng
- **UC-155**: Ghi nhận sử dụng khuyến mãi (Record Usage)
- **UC-156**: Đếm số lần user đã dùng (Get Usage Count)
- **UC-157**: Tự động vô hiệu hóa hết hạn (Deactivate Expired)
- **UC-158**: Gửi thông báo sắp hết hạn (Send Expiration Notifications)

---

## IX. HỆ THỐNG ĐÁNH GIÁ (Review System)

### IX.1. Đánh giá sản phẩm (User)
- **UC-159**: Tạo đánh giá (Create Review)
- **UC-160**: Cập nhật đánh giá (Update Review)
- **UC-161**: Xóa đánh giá (Delete Review)
- **UC-162**: Đánh dấu đánh giá hữu ích (Vote Helpful)

### IX.2. Xem đánh giá
- **UC-163**: Xem đánh giá sản phẩm (Get Product Reviews)
- **UC-164**: Xem đánh giá của user (Get User Reviews)
- **UC-165**: Xem chi tiết đánh giá (Get By ID)
- **UC-166**: Thống kê đánh giá sản phẩm (Get Product Stats)

### IX.3. Kiểm duyệt (Admin)
- **UC-167**: Xem đánh giá chờ duyệt (Get Pending)
- **UC-168**: Kiểm duyệt đánh giá (Moderate)
- **UC-169**: Kiểm duyệt hàng loạt (Batch Moderate)

### IX.4. Kiểm tra quyền
- **UC-170**: Kiểm tra user có thể đánh giá (Can Review)
- **UC-171**: Cập nhật điểm rating sản phẩm (Update Rating)

---

## X. HỆ THỐNG TỒN KHO (Inventory Management)

### X.1. Quản lý tồn kho
- **UC-172**: Xem tồn kho biến thể (Get Variant Inventory)
- **UC-173**: Xem tồn kho sản phẩm (Get Product Inventory)
- **UC-174**: Điều chỉnh tồn kho (Adjust Stock)
- **UC-175**: Đặt trước hàng (Reserve Stock)
- **UC-176**: Hủy đặt trước (Release Reserved)
- **UC-177**: Xác nhận trừ tồn kho (Confirm Deduction)

### X.2. Lịch sử xuất nhập
- **UC-178**: Xem lịch sử biến thể (Get Stock Movements)
- **UC-179**: Xem tất cả lịch sử (Get All Movements)

### X.3. Cảnh báo tồn kho
- **UC-180**: Xem cảnh báo sắp hết hàng (Low Stock Alerts)
- **UC-181**: Xem cảnh báo hết hàng (Out of Stock Alerts)
- **UC-182**: Gửi thông báo hết hàng (Send Notifications)

### X.4. Thao tác hàng loạt
- **UC-183**: Cập nhật tồn kho hàng loạt (Bulk Update)
- **UC-184**: Import tồn kho từ file (Bulk Import)

### X.5. Báo cáo
- **UC-185**: Tạo báo cáo tồn kho (Generate Report)
- **UC-186**: Tạo báo cáo xuất nhập (Stock Movement Report)

---

## XI. HỆ THỐNG VẬN CHUYỂN (Shipping Management)

### XI.1. Tính phí vận chuyển
- **UC-187**: Tính phí vận chuyển (Calculate Fee)
- **UC-188**: Tính phí theo đơn hàng (Calculate Order Fee)
- **UC-189**: Xem các phương thức vận chuyển (Get Methods)

### XI.2. Quản lý vùng giao hàng (Admin)
- **UC-190**: Tạo vùng giao hàng (Create Zone)
- **UC-191**: Cập nhật vùng (Update Zone)
- **UC-192**: Xóa vùng (Delete Zone)
- **UC-193**: Xem tất cả vùng (Get All Zones)

### XI.3. Theo dõi vận chuyển
- **UC-194**: Theo dõi đơn hàng (Track Order)
- **UC-195**: Cập nhật thông tin tracking (Update Tracking)
- **UC-196**: Cập nhật trạng thái giao (Update Delivery Status)

### XI.4. Tích hợp đơn vị vận chuyển
- **UC-197**: Tạo vận đơn (Create Shipment)
- **UC-198**: In nhãn vận chuyển (Print Label)
- **UC-199**: Hủy vận đơn (Cancel Shipment)

### XI.5. Thao tác hàng loạt
- **UC-200**: Xử lý vận chuyển hàng loạt (Process Bulk)
- **UC-201**: Tạo manifest vận chuyển (Generate Manifest)

---

## XII. HỆ THỐNG ĐIỂM THƯỞNG (Loyalty Program)

### XII.1. Quản lý điểm
- **UC-202**: Thêm điểm cho user (Add Points)
- **UC-203**: Trừ điểm (Deduct Points)
- **UC-204**: Tính điểm từ đơn hàng (Calculate From Order)
- **UC-205**: Xem điểm user (Get User Points)
- **UC-206**: Xem lịch sử giao dịch điểm (Get Transactions)
- **UC-207**: Xem điểm trong khoảng thời gian (Points In Range)

### XII.2. Đổi điểm
- **UC-208**: Kiểm tra đủ điểm đổi (Can Redeem)
- **UC-209**: Đổi điểm (Redeem Points)
- **UC-210**: Xem giá trị điểm (Get Points Value)

### XII.3. Hạng thành viên
- **UC-211**: Xem hạng user (Get User Tier)
- **UC-212**: Xem điểm cần lên hạng (Points For Next Tier)
- **UC-213**: Cập nhật hạng user (Update User Tier)
- **UC-214**: Cập nhật hạng tất cả user (Update All Tiers)

### XII.4. Xử lý hết hạn
- **UC-215**: Hết hạn điểm cũ (Expire Old Points)
- **UC-216**: Thông báo điểm sắp hết hạn (Notify Expiration)

### XII.5. Thống kê
- **UC-217**: Tổng điểm đã cấp (Total Points Earned)
- **UC-218**: Tổng điểm đã đổi (Total Points Redeemed)

---

## XIII. HỆ THỐNG THÔNG BÁO (Notification System)

### XIII.1. Gửi thông báo
- **UC-219**: Gửi thông báo đến user (Send To User)
- **UC-220**: Gửi thông báo hàng loạt (Send Bulk)
- **UC-221**: Gửi cho tất cả user (Send To All)
- **UC-222**: Gửi real-time (WebSocket)

### XIII.2. Thông báo đơn hàng
- **UC-223**: Thông báo đặt hàng thành công (Order Placed)
- **UC-224**: Thông báo xác nhận đơn (Order Confirmed)
- **UC-225**: Thông báo đã giao hàng (Order Shipped)
- **UC-226**: Thông báo giao thành công (Order Delivered)
- **UC-227**: Thông báo hủy đơn (Order Cancelled)

### XIII.3. Thông báo thanh toán
- **UC-228**: Thông báo thanh toán thành công (Payment Success)
- **UC-229**: Thông báo thanh toán thất bại (Payment Failed)

### XIII.4. Thông báo hệ thống
- **UC-230**: Thông báo khuyến mãi mới (Promotion Created)
- **UC-231**: Thông báo cảnh báo hệ thống (System Alert)

### XIII.5. Quản lý thông báo user
- **UC-232**: Xem thông báo (Get Notifications)
- **UC-233**: Xem chi tiết thông báo (Get By ID)
- **UC-234**: Đánh dấu đã đọc (Mark As Read)
- **UC-235**: Đánh dấu tất cả đã đọc (Mark All Read)
- **UC-236**: Xóa thông báo (Delete)
- **UC-237**: Xóa tất cả đã đọc (Delete All Read)
- **UC-238**: Xem số thông báo chưa đọc (Get Unread Count)

---

## XIV. HỆ THỐNG TÌM KIẾM (Search System)

### XIV.1. Tìm kiếm sản phẩm
- **UC-239**: Tìm kiếm theo từ khóa (Search Products)
- **UC-240**: Tìm kiếm nâng cao (Advanced Search)

### XIV.2. Gợi ý và tự động hoàn thành
- **UC-241**: Gợi ý tự động hoàn thành (Auto-complete)
- **UC-242**: Gợi ý tìm kiếm (Search Suggestions)
- **UC-243**: Từ khóa tìm kiếm phổ biến (Popular Keywords)

### XIV.3. Bộ lọc và phân loại
- **UC-244**: Lấy tùy chọn lọc (Get Filter Options)
- **UC-245**: Lấy facets tìm kiếm (Get Search Facets)

### XIV.4. Quản lý index
- **UC-246**: Đánh index sản phẩm (Index Product)
- **UC-247**: Đánh index lại tất cả (Reindex All)
- **UC-248**: Xóa khỏi index (Remove From Index)

### XIV.5. Phân tích tìm kiếm
- **UC-249**: Ghi log tìm kiếm (Log Search)
- **UC-250**: Xu hướng tìm kiếm (Trending Searches)
- **UC-251**: Thống kê tìm kiếm (Search Analytics)

---

## XV. HỆ THỐNG YÊU THÍCH (Wishlist)

- **UC-252**: Thêm vào yêu thích (Add To Wishlist)
- **UC-253**: Xóa khỏi yêu thích (Remove From Wishlist)
- **UC-254**: Xóa toàn bộ yêu thích (Clear Wishlist)
- **UC-255**: Xem danh sách yêu thích (Get User Wishlist)
- **UC-256**: Xem tóm tắt yêu thích (Get Summary)
- **UC-257**: Kiểm tra sản phẩm yêu thích (Is In Wishlist)
- **UC-258**: Đếm số lượng yêu thích (Get Count)
- **UC-259**: Thêm nhiều vào yêu thích (Add Multiple)
- **UC-260**: Xóa nhiều khỏi yêu thích (Remove Multiple)
- **UC-261**: Chuyển sang giỏ hàng (Move To Cart)
- **UC-262**: Chuyển tất cả sang giỏ (Move All To Cart)

---

## XVI. HỆ THỐNG LIÊN HỆ (Contact System)

- **UC-263**: Gửi liên hệ (User đăng nhập)
- **UC-264**: Gửi liên hệ (Khách)
- **UC-265**: Trả lời liên hệ (Admin)
- **UC-266**: Xem tất cả liên hệ (Admin)
- **UC-267**: Xem liên hệ theo trạng thái (By Status)
- **UC-268**: Xem chi tiết liên hệ (Get Detail)
- **UC-269**: Cập nhật trạng thái liên hệ (Update Status)
- **UC-270**: Xóa liên hệ (Delete)
- **UC-271**: Xem số liên hệ chờ xử lý (Get Pending Count)
- **UC-272**: Xem tổng số liên hệ (Get Total Count)
- **UC-273**: Thống kê liên hệ (Get Stats)

---

## XVII. HỆ THỐNG BÁO CÁO & DASHBOARD (Dashboard & Reports)

### XVII.1. Dashboard Admin
- **UC-274**: Xem dashboard tổng quan (Admin Dashboard)
- **UC-275**: Tổng quan doanh số (Sales Overview)
- **UC-276**: Sản phẩm bán chạy (Top Products)
- **UC-277**: Danh mục bán chạy (Top Categories)

### XVII.2. Phân tích bán hàng
- **UC-278**: Doanh số theo ngày (Daily Sales)
- **UC-279**: Doanh số theo tháng (Monthly Sales)
- **UC-280**: Doanh số theo năm (Yearly Sales)

### XVII.3. Dữ liệu biểu đồ
- **UC-281**: Dữ liệu biểu đồ doanh số (Sales Chart)
- **UC-282**: Dữ liệu trạng thái đơn (Order Status Chart)
- **UC-283**: Doanh thu theo danh mục (Revenue By Category)

### XVII.4. Phân tích khách hàng
- **UC-284**: Phân tích khách hàng (Customer Analytics)
- **UC-285**: Hoạt động gần đây (Recent Activities)

### XVII.5. Dashboard tồn kho
- **UC-286**: Dashboard tồn kho (Inventory Dashboard)

### XVII.6. Export báo cáo
- **UC-287**: Export báo cáo doanh số (Export Sales)
- **UC-288**: Export báo cáo tồn kho (Export Inventory)

---

## XVIII. HỆ THỐNG FILE & EMAIL (File & Email System)

### XVIII.1. Quản lý file
- **UC-289**: Upload file (Upload File)
- **UC-290**: Upload ảnh (Upload Image)
- **UC-291**: Upload với tùy chọn (Upload With Options)
- **UC-292**: Upload nhiều file (Upload Multiple)
- **UC-293**: Upload nhiều ảnh (Upload Multiple Images)
- **UC-294**: Xóa file (Delete File)
- **UC-295**: Xóa nhiều file (Delete Multiple)
- **UC-296**: Xem thông tin file (Get File Info)
- **UC-297**: Lấy URL file (Get File URL)
- **UC-298**: Resize ảnh (Resize Image)
- **UC-299**: Thêm watermark (Apply Watermark)

### XVIII.2. Upload theo ngữ cảnh
- **UC-300**: Upload ảnh sản phẩm (Upload Product Image)
- **UC-301**: Xóa ảnh sản phẩm (Delete Product Image)
- **UC-302**: Sắp xếp ảnh sản phẩm (Reorder Product Images)
- **UC-303**: Upload avatar (Upload User Avatar)
- **UC-304**: Xóa avatar (Delete User Avatar)
- **UC-305**: Upload ảnh đánh giá (Upload Review Image)
- **UC-306**: Xóa ảnh đánh giá (Delete Review Image)

### XVIII.3. Gửi email
- **UC-307**: Gửi email xác thực (Send Email Verification)
- **UC-308**: Gửi email đặt lại mật khẩu (Send Password Reset)
- **UC-309**: Gửi OTP xác thực điện thoại (Send Phone Verification)
- **UC-310**: Gửi xác nhận liên hệ (Send Contact Confirmation)
- **UC-311**: Gửi trả lời liên hệ (Send Contact Reply)
- **UC-312**: Gửi xác nhận đơn hàng (Send Order Confirmation)
- **UC-313**: Gửi thông báo giao hàng (Send Order Shipped)
- **UC-314**: Gửi thông báo đã nhận hàng (Send Order Delivered)
- **UC-315**: Gửi thông báo hủy đơn (Send Order Cancelled)
- **UC-316**: Gửi email chào mừng (Send Welcome)
- **UC-317**: Gửi email khuyến mãi (Send Promotion)
- **UC-318**: Gửi email cơ bản (Send Email)
- **UC-319**: Gửi email HTML (Send Html Email)
- **UC-320**: Gửi email từ template (Send Template Email)

---

## XIX. HỆ THỐNG TOKEN XÁC THỰC (Verification Token)

- **UC-321**: Tạo token xác thực (Create Token)
- **UC-322**: Validate token (Validate Token)
- **UC-323**: Lấy user từ token (Get User By Token)
- **UC-324**: Đánh dấu token đã dùng (Mark As Used)
- **UC-325**: Xóa token hết hạn (Delete Expired)
- **UC-326**: Xem thông tin token (Get Token Info)
- **UC-327**: Vô hiệu hóa token của user (Invalidate User Tokens)

---

## TỔNG KẾT

| Hệ thống | Số Use Case |
|----------|-------------|
| Authentication & User Management | 28 |
| Product Management | 35 |
| Category Management | 13 |
| Brand Management | 10 |
| Shopping Cart | 13 |
| Order Management | 25 |
| Payment System | 16 |
| Promotion System | 18 |
| Review System | 13 |
| Inventory Management | 15 |
| Shipping Management | 15 |
| Loyalty Program | 17 |
| Notification System | 20 |
| Search System | 13 |
| Wishlist | 11 |
| Contact System | 11 |
| Dashboard & Reports | 15 |
| File & Email System | 32 |
| Verification Token | 7 |
| **TỔNG CỘNG** | **327** |

---

*File này được tạo tự động từ mã nguồn Delta Shop App*
