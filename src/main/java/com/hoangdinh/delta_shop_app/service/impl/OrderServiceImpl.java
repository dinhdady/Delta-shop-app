package com.hoangdinh.delta_shop_app.service.impl;

import com.hoangdinh.delta_shop_app.dto.request.order.CreateOrderRequest;
import com.hoangdinh.delta_shop_app.dto.request.order.OrderItemRequest;
import com.hoangdinh.delta_shop_app.dto.request.order.OrderUpdateStatusRequest;
import com.hoangdinh.delta_shop_app.dto.response.PageResponse;
import com.hoangdinh.delta_shop_app.dto.response.order.*;
import com.hoangdinh.delta_shop_app.entity.*;
import com.hoangdinh.delta_shop_app.enums.OrderStatus;
import com.hoangdinh.delta_shop_app.enums.PaymentMethod;
import com.hoangdinh.delta_shop_app.enums.PaymentStatus;
import com.hoangdinh.delta_shop_app.exception.BusinessException;
import com.hoangdinh.delta_shop_app.exception.ResourceNotFoundException;
import com.hoangdinh.delta_shop_app.repository.*;
import com.hoangdinh.delta_shop_app.service.EmailService;
import com.hoangdinh.delta_shop_app.service.LoyaltyService;
import com.hoangdinh.delta_shop_app.service.NotificationService;
import com.hoangdinh.delta_shop_app.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductVariantRepository variantRepository;
    private final PromotionRepository promotionRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final NotificationService notificationService;
    private final LoyaltyService loyaltyService;

    @Transactional
    public OrderDetailResponse createOrder(UUID userId, CreateOrderRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (OrderItemRequest itemReq : request.getItems()) {
            ProductVariant variant = variantRepository.findByIdWithLock(itemReq.getVariantId())
                    .orElseThrow(() -> new ResourceNotFoundException("Variant", "id", itemReq.getVariantId()));

            int available = variant.getAvailableQuantity();
            if (available < itemReq.getQuantity()) {
                throw new BusinessException(
                        "Sản phẩm '" + variant.getProduct().getName() + "' không đủ số lượng. Còn lại: " + available);
            }

            variant.setReservedQuantity(variant.getReservedQuantity() + itemReq.getQuantity());
            variantRepository.save(variant);

            // ✅ SỬA: Dùng giá từ request nếu có, nếu không thì lấy từ variant
            BigDecimal unitPrice = itemReq.getUnitPrice() != null
                    ? itemReq.getUnitPrice()
                    : variant.getFinalPrice();

            BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            subtotal = subtotal.add(totalPrice);

            OrderItem item = OrderItem.builder()
                    .variant(variant)
                    .product(variant.getProduct())
                    .productName(variant.getProduct().getName())
                    .variantName(variant.getName())
                    .productSku(variant.getSku())
                    .productImage(variant.getImageUrl() != null ? variant.getImageUrl() :
                            variant.getProduct().getPrimaryImage() != null ? variant.getProduct().getPrimaryImage().getUrl() : null)
                    .quantity(itemReq.getQuantity())
                    .unitPrice(unitPrice)  // Lưu giá đúng vào order
                    .discountAmount(BigDecimal.ZERO)
                    .totalPrice(totalPrice)
                    .build();
            orderItems.add(item);
        }

        BigDecimal discountAmount = BigDecimal.ZERO;
        Promotion promotion = null;
        if (request.getPromotionCode() != null && !request.getPromotionCode().isEmpty()) {
            promotion = promotionRepository.findValidByCode(
                            request.getPromotionCode(), subtotal, LocalDateTime.now())
                    .orElseThrow(() -> new BusinessException("Mã giảm giá không hợp lệ hoặc đã hết hạn"));
            discountAmount = calculateDiscount(promotion, subtotal);
        }

        BigDecimal loyaltyDiscount = BigDecimal.ZERO;
        int loyaltyPointsUsed = 0;
        if (request.getLoyaltyPointsToUse() != null && request.getLoyaltyPointsToUse() > 0) {
            loyaltyPointsUsed = Math.min(request.getLoyaltyPointsToUse(), user.getLoyaltyPoints());
            loyaltyDiscount = BigDecimal.valueOf(loyaltyPointsUsed).multiply(BigDecimal.valueOf(100));
        }

        BigDecimal shippingFee = calculateShippingFee(subtotal, request.getShippingProvince());
        if (promotion != null && promotion.getType().name().equals("FREE_SHIPPING")) {
            shippingFee = BigDecimal.ZERO;
        }

        BigDecimal totalDiscount = discountAmount.add(loyaltyDiscount);
        BigDecimal total = subtotal.subtract(totalDiscount).add(shippingFee);
        if (total.compareTo(BigDecimal.ZERO) < 0) total = BigDecimal.ZERO;

        int pointsEarned = total.divide(BigDecimal.valueOf(1000), 0, java.math.RoundingMode.DOWN).intValue();

        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .user(user)
                .status(OrderStatus.PENDING)
                .paymentStatus(PaymentStatus.PENDING)
                .paymentMethod(PaymentMethod.valueOf(request.getPaymentMethod()))
                .shippingName(request.getShippingName())
                .shippingPhone(request.getShippingPhone())
                .shippingProvince(request.getShippingProvince())
                .shippingDistrict(request.getShippingDistrict())
                .shippingWard(request.getShippingWard())
                .shippingAddress(request.getShippingAddress())
                .subtotal(subtotal)
                .discountAmount(totalDiscount)
                .shippingFee(shippingFee)
                .totalAmount(total)
                .loyaltyPointsUsed(loyaltyPointsUsed)
                .loyaltyPointsEarned(pointsEarned)
                .promotion(promotion)
                .promotionCode(request.getPromotionCode())
                .notes(request.getNotes())
                .estimatedDelivery(LocalDate.now().plusDays(3))
                .build();

        orderItems.forEach(item -> {
            item.setOrder(order);
            order.getItems().add(item);
        });

        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(order)
                .toStatus(OrderStatus.PENDING)
                .note("Đơn hàng mới được tạo")
                .build();
        order.getStatusHistory().add(history);

        Order saved = orderRepository.save(order);

        if (loyaltyPointsUsed > 0) {
            loyaltyService.deductPoints(user, loyaltyPointsUsed, saved.getId(),
                    "Sử dụng điểm cho đơn hàng " + saved.getOrderNumber());
        }

        if (saved.getPaymentMethod() == PaymentMethod.COD) {
            confirmOrder(saved.getId());
        }

        cartRepository.findByUserId(userId).ifPresent(cart -> {
            cart.getItems().clear();
            cartRepository.save(cart);
        });

        emailService.sendOrderConfirmation(saved);

        log.info("Order created: {} for user {}", saved.getOrderNumber(), userId);
        return mapToDetailResponse(saved);
    }

    @Override
    @Transactional
    public OrderDetailResponse updateStatus(UUID orderId, OrderUpdateStatusRequest request, UUID adminId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        validateStatusTransition(order.getStatus(), request.getStatus());

        OrderStatus prevStatus = order.getStatus();
        order.setStatus(request.getStatus());

        switch (request.getStatus()) {
            case SHIPPED -> {
                order.setShippedAt(LocalDateTime.now());
                notificationService.notifyOrderShipped(order);
            }
            case DELIVERED -> {
                order.setDeliveredAt(LocalDateTime.now());
                order.setPaymentStatus(PaymentStatus.PAID);
                deductStock(order);
                if (order.getLoyaltyPointsEarned() > 0 && order.getUser() != null) {
                    loyaltyService.addPoints(order.getUser(), order.getLoyaltyPointsEarned(),
                            order.getId(), "Thưởng điểm đơn hàng " + order.getOrderNumber());
                }
                if (order.getUser() != null) {
                    userRepository.incrementTotalSpent(order.getUser().getId(), order.getTotalAmount());
                }
                notificationService.notifyOrderDelivered(order);
            }
            case CANCELLED -> {
                order.setCancelledAt(LocalDateTime.now());
                order.setCancelReason(request.getNote());
                releaseReservedStock(order);
                if (order.getLoyaltyPointsUsed() > 0 && order.getUser() != null) {
                    loyaltyService.addPoints(order.getUser(), order.getLoyaltyPointsUsed(),
                            order.getId(), "Hoàn điểm đơn hàng bị hủy " + order.getOrderNumber());
                }
                notificationService.notifyOrderCancelled(order);
            }
            default -> {}
        }

        User admin = adminId != null ? userRepository.findById(adminId).orElse(null) : null;
        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(order)
                .fromStatus(prevStatus)
                .toStatus(request.getStatus())
                .note(request.getNote())
                .createdBy(admin)
                .build();
        order.getStatusHistory().add(history);

        Order saved = orderRepository.save(order);
        log.info("Order {} status changed: {} -> {}", order.getOrderNumber(), prevStatus, request.getStatus());
        return mapToDetailResponse(saved);
    }

    @Override
    public void bulkUpdateStatus(List<UUID> orderIds, OrderStatus status, UUID adminId) {

    }

    @Override
    @Transactional
    public OrderDetailResponse updatePaymentStatus(UUID orderId, PaymentStatus paymentStatus, UUID adminId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        PaymentStatus previousStatus = order.getPaymentStatus();
        order.setPaymentStatus(paymentStatus);

        String note = "Cap nhat trang thai thanh toan: " + previousStatus + " -> " + paymentStatus;
        User admin = adminId != null ? userRepository.findById(adminId).orElse(null) : null;
        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(order)
                .fromStatus(order.getStatus())
                .toStatus(order.getStatus())
                .note(note)
                .createdBy(admin)
                .build();
        order.getStatusHistory().add(history);

        Order saved = orderRepository.save(order);
        return mapToDetailResponse(saved);
    }

    @Override
    @Transactional
    public OrderDetailResponse addTrackingNumber(UUID orderId, String trackingNumber, UUID adminId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        order.setTrackingNumber(trackingNumber);

        User admin = adminId != null ? userRepository.findById(adminId).orElse(null) : null;
        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(order)
                .fromStatus(order.getStatus())
                .toStatus(order.getStatus())
                .note("Cap nhat ma van don: " + trackingNumber)
                .createdBy(admin)
                .build();
        order.getStatusHistory().add(history);

        Order saved = orderRepository.save(order);
        return mapToDetailResponse(saved);
    }

    @Override
    public OrderDetailResponse addAdminNote(UUID orderId, String note, UUID adminId) {
        return null;
    }

    @Override
    @Transactional
    public void deleteOrder(UUID orderId, UUID adminId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        OrderStatus previousStatus = order.getStatus();

        if (order.getStatus() != OrderStatus.CANCELLED) {
            releaseReservedStock(order);
            order.setStatus(OrderStatus.CANCELLED);
            order.setCancelReason("Don hang bi huy boi quan tri vien");
            order.setCancelledAt(LocalDateTime.now());
        }

        User admin = adminId != null ? userRepository.findById(adminId).orElse(null) : null;
        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(order)
                .fromStatus(previousStatus)
                .toStatus(OrderStatus.CANCELLED)
                .note("Quan tri vien huy don hang")
                .createdBy(admin)
                .build();
        order.getStatusHistory().add(history);

        orderRepository.save(order);
    }

    @Override
    public OrderDetailResponse restoreOrder(UUID orderId, UUID adminId) {
        return null;
    }

    @Override
    public OrderStatisticsResponse getOrderStatistics() {
        return null;
    }

    @Override
    public DailyOrderStatisticsResponse getDailyStatistics(LocalDate date) {
        return null;
    }

    @Override
    public MonthlyOrderStatisticsResponse getMonthlyStatistics(int year, int month) {
        return null;
    }

    @Override
    public YearlyOrderStatisticsResponse getYearlyStatistics(int year) {
        return null;
    }

    @Override
    public RevenueResponse getRevenueByDateRange(LocalDate startDate, LocalDate endDate) {
        return null;
    }

    @Override
    public List<TopProductResponse> getTopSellingProducts(int limit, LocalDate startDate, LocalDate endDate) {
        return List.of();
    }

    @Override
    public OrderStatusDistributionResponse getOrderStatusDistribution() {
        return null;
    }

    @Override
    public byte[] exportOrdersToExcel(LocalDate startDate, LocalDate endDate, String status) {
        return new byte[0];
    }

    @Override
    public byte[] exportOrdersToPdf(LocalDate startDate, LocalDate endDate, String status) {
        return new byte[0];
    }

    @Override
    public byte[] generateInvoice(UUID orderId) {
        return new byte[0];
    }

    @Override
    public boolean isOrderCancellable(UUID orderId) {
        return false;
    }

    @Override
    public List<OrderStatusHistoryResponse> getOrderStatusHistory(UUID orderId) {
        return List.of();
    }

    @Override
    public long countOrdersByStatus(OrderStatus status) {
        return 0;
    }

    @Override
    public BigDecimal getTotalRevenue() {
        return null;
    }

    @Override
    public BigDecimal getRevenueForPeriod(LocalDate startDate, LocalDate endDate) {
        return null;
    }

    @Override
    public BigDecimal getAverageOrderValue() {
        return null;
    }

    @Override
    public long getOrderCountByDateRange(LocalDate startDate, LocalDate endDate) {
        return 0;
    }

    @Override
    @Transactional
    public OrderDetailResponse cancelOrder(UUID orderId, UUID userId, String reason) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (!order.isCancellable()) {
            throw new BusinessException("Đơn hàng không thể hủy ở trạng thái hiện tại: " + order.getStatus());
        }

        OrderUpdateStatusRequest request = new OrderUpdateStatusRequest();
        request.setStatus(OrderStatus.CANCELLED);
        request.setNote(reason);
        return updateStatus(orderId, request, null);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDetailResponse getOrderById(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        return mapToDetailResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getUserOrders(UUID userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Order> orders = orderRepository.findByUserId(userId, pageable);
        return PageResponse.of(orders.map(this::mapToResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDetailResponse getOrderDetail(UUID orderId, UUID userId) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        return mapToDetailResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getAllOrders(int page, int size, String status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Order> orders;
        if (status != null && !status.isEmpty()) {
            orders = orderRepository.findByStatus(OrderStatus.valueOf(status), pageable);
        } else {
            orders = orderRepository.findAll(pageable);
        }
        return PageResponse.of(orders.map(this::mapToResponse));
    }

    private void confirmOrder(UUID orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow();
        order.setStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);
    }

    private BigDecimal calculateDiscount(Promotion promotion, BigDecimal subtotal) {
        return switch (promotion.getType()) {
            case PERCENTAGE -> {
                BigDecimal discount = subtotal.multiply(promotion.getValue())
                        .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
                yield promotion.getMaxDiscountAmount() != null
                        ? discount.min(promotion.getMaxDiscountAmount()) : discount;
            }
            case FIXED_AMOUNT -> promotion.getValue().min(subtotal);
            default -> BigDecimal.ZERO;
        };
    }

    private BigDecimal calculateShippingFee(BigDecimal subtotal, String province) {
        if (subtotal.compareTo(BigDecimal.valueOf(500_000)) >= 0) return BigDecimal.ZERO;
        if (province != null && (province.contains("Hồ Chí Minh") || province.contains("Hà Nội"))) {
            return BigDecimal.valueOf(25_000);
        }
        return BigDecimal.valueOf(35_000);
    }

    private void deductStock(Order order) {
        order.getItems().forEach(item -> {
            if (item.getVariant() != null) {
                ProductVariant variant = item.getVariant();
                variant.setStockQuantity(variant.getStockQuantity() - item.getQuantity());
                variant.setReservedQuantity(Math.max(0, variant.getReservedQuantity() - item.getQuantity()));
                variantRepository.save(variant);
            }
        });
    }

    private void releaseReservedStock(Order order) {
        order.getItems().forEach(item -> {
            if (item.getVariant() != null) {
                ProductVariant variant = item.getVariant();
                variant.setReservedQuantity(Math.max(0, variant.getReservedQuantity() - item.getQuantity()));
                variantRepository.save(variant);
            }
        });
    }

    private void validateStatusTransition(OrderStatus from, OrderStatus to) {
        boolean valid = switch (from) {
            case PENDING -> to == OrderStatus.CONFIRMED || to == OrderStatus.CANCELLED;
            case CONFIRMED -> to == OrderStatus.PROCESSING || to == OrderStatus.SHIPPED || to == OrderStatus.DELIVERED || to == OrderStatus.CANCELLED;
            case PROCESSING -> to == OrderStatus.SHIPPED || to == OrderStatus.DELIVERED || to == OrderStatus.CANCELLED;
            case SHIPPED -> to == OrderStatus.DELIVERED || to == OrderStatus.CANCELLED;
            case DELIVERED -> to == OrderStatus.REFUNDED;
            default -> false;
        };
        if (!valid) {
            throw new BusinessException(String.format("Không thể chuyển trạng thái từ %s sang %s", from, to));
        }
    }

    private String generateOrderNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String orderNumber;
        do {
            int suffix = ThreadLocalRandom.current().nextInt(1000, 10000);
            orderNumber = String.format("DLT-%s-%04d", timestamp, suffix);
        } while (orderRepository.findByOrderNumber(orderNumber).isPresent());
        return orderNumber;
    }

    private OrderResponse mapToResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .createdAt(order.getCreatedAt())
                .status(order.getStatus())
                .paymentStatus(order.getPaymentStatus())
                .paymentMethod(order.getPaymentMethod())
                .totalAmount(order.getTotalAmount())
                .totalItems(order.getItems().size())
                .shippingName(order.getShippingName())
                .shippingPhone(order.getShippingPhone())
                .shippingAddress(order.getShippingAddress())
                .build();
    }

    private OrderDetailResponse mapToDetailResponse(Order order) {
        return OrderDetailResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .createdAt(order.getCreatedAt())
                .status(order.getStatus())
                .paymentStatus(order.getPaymentStatus())
                .paymentMethod(order.getPaymentMethod())
                .userId(order.getUser() != null ? order.getUser().getId() : null)
                .customerName(order.getUser() != null ? order.getUser().getFullName() : null)
                .customerEmail(order.getUser() != null ? order.getUser().getEmail() : order.getGuestEmail())
                .customerPhone(order.getUser() != null ? order.getUser().getPhone() : order.getShippingPhone())
                .shippingName(order.getShippingName())
                .shippingPhone(order.getShippingPhone())
                .shippingAddress(order.getShippingAddress())
                .shippingProvince(order.getShippingProvince())
                .shippingDistrict(order.getShippingDistrict())
                .shippingWard(order.getShippingWard())
                .subtotal(order.getSubtotal())
                .discountAmount(order.getDiscountAmount())
                .shippingFee(order.getShippingFee())
                .taxAmount(order.getTaxAmount())
                .totalAmount(order.getTotalAmount())
                .loyaltyPointsUsed(order.getLoyaltyPointsUsed())
                .loyaltyPointsEarned(order.getLoyaltyPointsEarned())
                .promotionCode(order.getPromotionCode())
                .trackingNumber(order.getTrackingNumber())
                .estimatedDelivery(order.getEstimatedDelivery())
                .shippedAt(order.getShippedAt())
                .deliveredAt(order.getDeliveredAt())
                .notes(order.getNotes())
                .cancelReason(order.getCancelReason())
                .items(order.getItems().stream().map(this::mapToOrderItemResponse).collect(Collectors.toList()))
                .statusHistory(order.getStatusHistory().stream().map(this::mapToHistoryResponse).collect(Collectors.toList()))
                .build();
    }

    private OrderItemResponse mapToOrderItemResponse(OrderItem item) {
        return OrderItemResponse.builder()
                .id(item.getId())
                .productName(item.getProductName())
                .variantName(item.getVariantName())
                .productImage(item.getProductImage())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .discountAmount(item.getDiscountAmount())
                .totalPrice(item.getTotalPrice())
                .reviewed(item.isReviewed())
                .build();
    }

    private OrderStatusHistoryResponse mapToHistoryResponse(OrderStatusHistory history) {
        return OrderStatusHistoryResponse.builder()
                .fromStatus(history.getFromStatus() != null ? history.getFromStatus().getDescription() : null)
                .toStatus(history.getToStatus().getDescription())
                .note(history.getNote())
                .createdAt(history.getCreatedAt())
                .build();
    }
}
