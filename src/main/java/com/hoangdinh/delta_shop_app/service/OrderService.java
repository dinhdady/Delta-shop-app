package com.hoangdinh.delta_shop_app.service;

import com.hoangdinh.delta_shop_app.dto.request.order.CreateOrderRequest;
import com.hoangdinh.delta_shop_app.dto.request.order.OrderUpdateStatusRequest;
import com.hoangdinh.delta_shop_app.dto.response.PageResponse;
import com.hoangdinh.delta_shop_app.dto.response.order.*;
import com.hoangdinh.delta_shop_app.enums.OrderStatus;
import com.hoangdinh.delta_shop_app.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface OrderService {

    // ========== USER ORDER OPERATIONS ==========

    OrderDetailResponse createOrder(UUID userId, CreateOrderRequest request);

    PageResponse<OrderResponse> getUserOrders(UUID userId, int page, int size);

    OrderDetailResponse getOrderDetail(UUID orderId, UUID userId);

    OrderDetailResponse cancelOrder(UUID orderId, UUID userId, String reason);

    // ========== ADMIN ORDER OPERATIONS ==========

    PageResponse<OrderResponse> getAllOrders(int page, int size, String status);

    OrderDetailResponse getOrderById(UUID orderId);

    OrderDetailResponse updateStatus(UUID orderId, OrderUpdateStatusRequest request, UUID adminId);

    void bulkUpdateStatus(List<UUID> orderIds, OrderStatus status, UUID adminId);

    OrderDetailResponse updatePaymentStatus(UUID orderId, PaymentStatus paymentStatus, UUID adminId);

    OrderDetailResponse addTrackingNumber(UUID orderId, String trackingNumber, UUID adminId);

    OrderDetailResponse addAdminNote(UUID orderId, String note, UUID adminId);

    void deleteOrder(UUID orderId, UUID adminId);

    OrderDetailResponse restoreOrder(UUID orderId, UUID adminId);

    // ========== ORDER STATISTICS ==========

    OrderStatisticsResponse getOrderStatistics();

    DailyOrderStatisticsResponse getDailyStatistics(LocalDate date);

    MonthlyOrderStatisticsResponse getMonthlyStatistics(int year, int month);

    YearlyOrderStatisticsResponse getYearlyStatistics(int year);

    RevenueResponse getRevenueByDateRange(LocalDate startDate, LocalDate endDate);

    List<TopProductResponse> getTopSellingProducts(int limit, LocalDate startDate, LocalDate endDate);

    OrderStatusDistributionResponse getOrderStatusDistribution();

    // ========== EXPORT OPERATIONS ==========

    byte[] exportOrdersToExcel(LocalDate startDate, LocalDate endDate, String status);

    byte[] exportOrdersToPdf(LocalDate startDate, LocalDate endDate, String status);

    byte[] generateInvoice(UUID orderId);

    // ========== VALIDATION & HELPER ==========

    boolean isOrderCancellable(UUID orderId);

    List<OrderStatusHistoryResponse> getOrderStatusHistory(UUID orderId);

    long countOrdersByStatus(OrderStatus status);

    BigDecimal getTotalRevenue();

    BigDecimal getRevenueForPeriod(LocalDate startDate, LocalDate endDate);

    BigDecimal getAverageOrderValue();

    long getOrderCountByDateRange(LocalDate startDate, LocalDate endDate);
}