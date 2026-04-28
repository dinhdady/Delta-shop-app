package com.hoangdinh.delta_shop_app.service.impl;

import com.hoangdinh.delta_shop_app.dto.response.dashboard.*;
import com.hoangdinh.delta_shop_app.enums.OrderStatus;
import com.hoangdinh.delta_shop_app.repository.OrderItemRepository;
import com.hoangdinh.delta_shop_app.repository.OrderRepository;
import com.hoangdinh.delta_shop_app.repository.ProductRepository;
import com.hoangdinh.delta_shop_app.repository.UserRepository;
import com.hoangdinh.delta_shop_app.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardServiceImpl implements DashboardService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;

    @Override
    public AdminDashboardResponse getAdminDashboard() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayStart = now.toLocalDate().atStartOfDay();
        LocalDateTime weekStart = now.minusDays(7);
        LocalDateTime monthStart = now.minusDays(30);

        return AdminDashboardResponse.builder()
                .totalOrders(orderRepository.count())
                .pendingOrders(orderRepository.countByStatus(OrderStatus.PENDING))
                .shippedOrders(orderRepository.countByStatus(OrderStatus.SHIPPED))
                .deliveredOrders(orderRepository.countByStatus(OrderStatus.DELIVERED))
                .cancelledOrders(orderRepository.countByStatus(OrderStatus.CANCELLED))
                .totalRevenue(getTotalRevenue())
                .todayRevenue(getRevenueForPeriod(todayStart, now))
                .thisWeekRevenue(getRevenueForPeriod(weekStart, now))
                .thisMonthRevenue(getRevenueForPeriod(monthStart, now))
                .totalUsers(userRepository.count())
                .newUsersThisMonth(userRepository.countByCreatedAtBetween(monthStart, now))
                .totalProducts(productRepository.count())
                .lowStockProducts(productRepository.countLowStock())
                .outOfStockProducts(productRepository.countOutOfStock())
                .revenueGrowth(0.0)
                .orderGrowth(0.0)
                .build();
    }

    @Override
    public SalesOverviewResponse getSalesOverview(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();

        return SalesOverviewResponse.builder()
                .totalRevenue(getRevenueForPeriod(start, end))
                .previousPeriodRevenue(BigDecimal.ZERO)
                .totalOrders(orderRepository.countByCreatedAtBetween(start, end))
                .previousPeriodOrders(0)
                .averageOrderValue(getAverageOrderValueForPeriod(start, end))
                .build();
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public TopProductsResponse getTopProducts(int limit, String period) {
        // TẠM THỜI trả về response rỗng
        log.warn("getTopProducts is temporarily disabled due to query issues");

        return TopProductsResponse.builder()
                .products(new ArrayList<>())
                .period(period)
                .limit(limit)
                .totalItems(0)
                .totalRevenue(BigDecimal.ZERO)
                .totalQuantitySold(0)
                .build();
    }
    private LocalDateTime calculateStartDate(String period) {
        LocalDateTime now = LocalDateTime.now();
        switch (period) {
            case "7":
                return now.minusDays(7);
            case "30":
                return now.minusDays(30);
            case "90":
                return now.minusDays(90);
            case "365":
                return now.minusDays(365);
            default:
                return null; // all time
        }
    }

    private String getPeriodName(String period) {
        switch (period) {
            case "7": return "7 ngày qua";
            case "30": return "30 ngày qua";
            case "90": return "90 ngày qua";
            case "365": return "1 năm qua";
            default: return "Tất cả thời gian";
        }
    }
    @Override
    public TopCategoriesResponse getTopCategories(int limit) {
        return null;
    }

    @Override
    public DailySalesResponse getDailySales(LocalDate date) {
        return null;
    }

    @Override
    public MonthlySalesResponse getMonthlySales(int year, int month) {
        return null;
    }

    @Override
    public YearlySalesResponse getYearlySales(int year) {
        return null;
    }

    @Override
    public ChartDataResponse getSalesChartData(String period) {
        return ChartDataResponse.builder()
                .labels(List.of())
                .datasets(new ArrayList<>())
                .period(period)
                .build();
    }

    @Override
    public ChartDataResponse getOrderStatusChartData() {
        return ChartDataResponse.builder()
                .labels(List.of())
                .datasets(new ArrayList<>())
                .build();
    }

    @Override
    public ChartDataResponse getRevenueByCategoryChartData() {
        return null;
    }

    @Override
    public CustomerAnalyticsResponse getCustomerAnalytics() {
        return null;
    }

    @Override
    public List<UserActivityResponse> getRecentUserActivities(int limit) {
        return List.of();
    }

    @Override
    public InventoryDashboardResponse getInventoryDashboard() {
        return InventoryDashboardResponse.builder()
                .totalProducts(productRepository.count())
                .activeProducts(productRepository.countActiveProducts())
                .inactiveProducts(productRepository.countInactiveProducts())
                .outOfStockProducts(productRepository.countOutOfStock())
                .lowStockProducts(productRepository.countLowStock())
                .lowStockAlerts(new ArrayList<>())
                .build();
    }

    @Override
    public byte[] exportSalesReport(LocalDate startDate, LocalDate endDate, String format) {
        return new byte[0];
    }

    @Override
    public byte[] exportInventoryReport(String format) {
        return new byte[0];
    }

    private BigDecimal getTotalRevenue() {
        try {
            return orderRepository.getTotalRevenue();
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private BigDecimal getRevenueForPeriod(LocalDateTime start, LocalDateTime end) {
        try {
            return orderRepository.getTotalRevenueForPeriod(start, end);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private BigDecimal getAverageOrderValueForPeriod(LocalDateTime start, LocalDateTime end) {
        try {
            return orderRepository.getAverageOrderValueForPeriod(start, end);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }
}
