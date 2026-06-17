package com.hoangdinh.delta_shop_app.service.impl;

import com.hoangdinh.delta_shop_app.dto.response.dashboard.*;
import com.hoangdinh.delta_shop_app.enums.OrderStatus;
import com.hoangdinh.delta_shop_app.enums.PaymentStatus;
import com.hoangdinh.delta_shop_app.enums.UserStatus;
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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
        LocalDateTime yearStart = now.toLocalDate().withDayOfYear(1).atStartOfDay();

        return AdminDashboardResponse.builder()
                .totalOrders(orderRepository.count())
                .pendingOrders(orderRepository.countByStatus(OrderStatus.PENDING))
                .processingOrders(orderRepository.countByStatus(OrderStatus.PROCESSING))
                .shippedOrders(orderRepository.countByStatus(OrderStatus.SHIPPED))
                .deliveredOrders(orderRepository.countByStatus(OrderStatus.DELIVERED))
                .cancelledOrders(orderRepository.countByStatus(OrderStatus.CANCELLED))
                .refundedOrders(orderRepository.countByStatus(OrderStatus.REFUNDED))
                .totalRevenue(getTotalRevenue())
                .todayRevenue(getRevenueForPeriod(todayStart, now))
                .thisWeekRevenue(getRevenueForPeriod(weekStart, now))
                .thisMonthRevenue(getRevenueForPeriod(monthStart, now))
                .thisYearRevenue(getRevenueForPeriod(yearStart, now))
                .totalUsers(userRepository.count())
                .newUsersToday(userRepository.countByCreatedAtBetween(todayStart, now))
                .newUsersThisWeek(userRepository.countByCreatedAtBetween(weekStart, now))
                .newUsersThisMonth(userRepository.countByCreatedAtBetween(monthStart, now))
                .totalProducts(productRepository.count())
                .activeProducts(productRepository.countActiveProducts())
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
        LocalDateTime start = calculateStartDate(period);
        List<com.hoangdinh.delta_shop_app.entity.OrderItem> deliveredItems = orderItemRepository.findAll().stream()
                .filter(item -> item.getOrder() != null && isRecognizedSale(item.getOrder()))
                .toList();
        List<com.hoangdinh.delta_shop_app.entity.OrderItem> items = deliveredItems.stream()
                .filter(item -> start == null || !item.getOrder().getCreatedAt().isBefore(start))
                .toList();
        boolean usingAllTimeFallback = items.isEmpty() && !deliveredItems.isEmpty() && start != null;
        if (usingAllTimeFallback) {
            items = deliveredItems;
        }
        BigDecimal totalRevenue = items.stream().map(com.hoangdinh.delta_shop_app.entity.OrderItem::getTotalPrice)
                .filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
        List<TopProductItem> products = items.stream()
                .collect(Collectors.groupingBy(item -> item.getProduct() != null ? item.getProduct().getId() : null))
                .entrySet().stream().filter(entry -> entry.getKey() != null)
                .map(entry -> {
                    var first = entry.getValue().get(0);
                    int sold = entry.getValue().stream().mapToInt(com.hoangdinh.delta_shop_app.entity.OrderItem::getQuantity).sum();
                    BigDecimal revenue = entry.getValue().stream().map(com.hoangdinh.delta_shop_app.entity.OrderItem::getTotalPrice)
                            .filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
                    return TopProductItem.builder().productId(entry.getKey()).productName(first.getProductName())
                            .productSlug(first.getProduct().getSlug()).productImage(first.getProductImage())
                            .categoryName(first.getProduct().getCategory() != null ? first.getProduct().getCategory().getName() : null)
                            .brandName(first.getProduct().getBrand() != null ? first.getProduct().getBrand().getName() : null)
                            .totalSold(sold).totalRevenue(revenue)
                            .averagePrice(sold > 0 ? revenue.divide(BigDecimal.valueOf(sold), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO)
                            .percentageOfTotalSales(totalRevenue.signum() == 0 ? 0 : revenue.multiply(BigDecimal.valueOf(100)).divide(totalRevenue, 2, RoundingMode.HALF_UP).doubleValue())
                            .build();
                }).sorted(java.util.Comparator.comparingInt(TopProductItem::getTotalSold).reversed()).limit(limit).toList();
        return TopProductsResponse.builder()
                .products(products)
                .period(usingAllTimeFallback ? "Tất cả thời gian" : getPeriodName(period))
                .limit(limit).totalItems(products.size())
                .totalRevenue(totalRevenue).totalQuantitySold(items.stream().mapToInt(com.hoangdinh.delta_shop_app.entity.OrderItem::getQuantity).sum())
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
        Map<String, BigDecimal> revenue = getCategoryRevenue();
        List<Map<String, Object>> categories = revenue.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .limit(Math.max(1, limit))
                .map(entry -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("categoryName", entry.getKey());
                    item.put("totalRevenue", entry.getValue());
                    return item;
                }).toList();
        return TopCategoriesResponse.builder()
                .categories(categories)
                .totalRevenue(revenue.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add))
                .build();
    }

    @Override
    public DailySalesResponse getDailySales(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();
        return DailySalesResponse.builder()
                .date(date)
                .totalOrders(orderRepository.countByCreatedAtBetween(start, end))
                .deliveredOrders(orderRepository.countByStatusAndCreatedAtBetween(OrderStatus.DELIVERED, start, end))
                .cancelledOrders(orderRepository.countByStatusAndCreatedAtBetween(OrderStatus.CANCELLED, start, end))
                .totalRevenue(getRevenueForPeriod(start, end))
                .averageOrderValue(getAverageOrderValueForPeriod(start, end))
                .build();
    }

    @Override
    public MonthlySalesResponse getMonthlySales(int year, int month) {
        LocalDate first = LocalDate.of(year, month, 1);
        List<DailySalesResponse> days = first.datesUntil(first.plusMonths(1)).map(this::getDailySales).toList();
        return MonthlySalesResponse.builder()
                .year(year).month(month)
                .totalOrders(days.stream().mapToLong(DailySalesResponse::getTotalOrders).sum())
                .totalRevenue(days.stream().map(DailySalesResponse::getTotalRevenue).reduce(BigDecimal.ZERO, BigDecimal::add))
                .averageOrderValue(getAverageOrderValueForPeriod(first.atStartOfDay(), first.plusMonths(1).atStartOfDay()))
                .dailySales(days).build();
    }

    @Override
    public YearlySalesResponse getYearlySales(int year) {
        List<MonthlySalesResponse> months = java.util.stream.IntStream.rangeClosed(1, 12)
                .mapToObj(month -> getMonthlySales(year, month)).toList();
        return YearlySalesResponse.builder().year(year)
                .totalOrders(months.stream().mapToLong(MonthlySalesResponse::getTotalOrders).sum())
                .totalRevenue(months.stream().map(MonthlySalesResponse::getTotalRevenue).reduce(BigDecimal.ZERO, BigDecimal::add))
                .averageOrderValue(getAverageOrderValueForPeriod(LocalDate.of(year, 1, 1).atStartOfDay(),
                        LocalDate.of(year + 1, 1, 1).atStartOfDay()))
                .monthlySales(months).build();
    }

    @Override
    public ChartDataResponse getSalesChartData(String period) {
        LocalDate today = LocalDate.now();
        String normalizedPeriod = Objects.requireNonNullElse(period, "monthly").toLowerCase();

        if ("monthly".equals(normalizedPeriod)) {
            List<LocalDate> months = java.util.stream.IntStream.rangeClosed(0, 11)
                    .mapToObj(offset -> today.withDayOfMonth(1).minusMonths(11L - offset))
                    .toList();
            return ChartDataResponse.builder()
                    .labels(months.stream().map(month -> month.toString().substring(0, 7)).toList())
                    .datasets(List.of(Dataset.builder().label("Doanh thu")
                            .data(months.stream().map(month -> (Number) getRevenueForPeriod(
                                    month.atStartOfDay(), month.plusMonths(1).atStartOfDay())).toList())
                            .borderColor("#cd4631").backgroundColor("rgba(205,70,49,.15)")
                            .borderWidth(2).fill("true").tension("0.3").build()))
                    .period("monthly")
                    .title("Doanh số 12 tháng gần nhất")
                    .xAxisLabel("Tháng")
                    .yAxisLabel("Doanh thu")
                    .build();
        }

        int numberOfDays = "weekly".equals(normalizedPeriod) ? 7 : 30;
        List<LocalDate> dates = java.util.stream.IntStream.rangeClosed(0, numberOfDays - 1)
                .mapToObj(days -> today.minusDays(numberOfDays - 1L - days)).toList();
        return ChartDataResponse.builder()
                .labels(dates.stream().map(LocalDate::toString).toList())
                .datasets(List.of(Dataset.builder().label("Doanh thu")
                        .data(dates.stream().map(date -> (Number) getRevenueForPeriod(date.atStartOfDay(), date.plusDays(1).atStartOfDay())).toList())
                        .borderColor("#cd4631").backgroundColor("rgba(205,70,49,.15)").borderWidth(2).fill("true").tension("0.3").build()))
                .period(normalizedPeriod)
                .title(numberOfDays == 7 ? "Doanh số 7 ngày gần nhất" : "Doanh số 30 ngày gần nhất")
                .xAxisLabel("Ngày")
                .yAxisLabel("Doanh thu")
                .build();
    }

    @Override
    public ChartDataResponse getOrderStatusChartData() {
        List<OrderStatus> statuses = List.of(OrderStatus.PENDING, OrderStatus.CONFIRMED, OrderStatus.PROCESSING,
                OrderStatus.SHIPPED, OrderStatus.DELIVERED, OrderStatus.CANCELLED);
        return ChartDataResponse.builder()
                .labels(statuses.stream().map(OrderStatus::name).toList())
                .datasets(List.of(Dataset.builder().label("Đơn hàng")
                        .data(statuses.stream().map(status -> (Number) orderRepository.countByStatus(status)).toList())
                        .backgroundColor("#cd4631").borderWidth(1).build()))
                .build();
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ChartDataResponse getRevenueByCategoryChartData() {
        Map<String, BigDecimal> revenue = getCategoryRevenue();
        return ChartDataResponse.builder()
                .labels(new ArrayList<>(revenue.keySet()))
                .datasets(List.of(Dataset.builder().label("Doanh thu")
                        .data(revenue.values().stream().map(value -> (Number) value).toList())
                        .backgroundColor("#cd4631").borderWidth(1).build()))
                .build();
    }

    @Override
    public CustomerAnalyticsResponse getCustomerAnalytics() {
        LocalDateTime now = LocalDateTime.now();
        List<com.hoangdinh.delta_shop_app.entity.User> users = userRepository.findAll();
        BigDecimal totalSpent = users.stream().map(com.hoangdinh.delta_shop_app.entity.User::getTotalSpent)
                .filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
        return CustomerAnalyticsResponse.builder()
                .totalCustomers(users.size())
                .activeCustomers(userRepository.countByStatus(UserStatus.ACTIVE))
                .newCustomersToday(userRepository.countByCreatedAtBetween(now.toLocalDate().atStartOfDay(), now))
                .newCustomersThisMonth(userRepository.countByCreatedAtBetween(now.toLocalDate().withDayOfMonth(1).atStartOfDay(), now))
                .averageCustomerSpend(users.isEmpty() ? BigDecimal.ZERO
                        : totalSpent.divide(BigDecimal.valueOf(users.size()), 2, RoundingMode.HALF_UP))
                .build();
    }

    @Override
    public List<UserActivityResponse> getRecentUserActivities(int limit) {
        return userRepository.findAll().stream()
                .filter(user -> user.getCreatedAt() != null)
                .sorted(java.util.Comparator.comparing(com.hoangdinh.delta_shop_app.entity.User::getCreatedAt).reversed())
                .limit(Math.max(1, limit))
                .map(user -> UserActivityResponse.builder().userId(user.getId()).userName(user.getFullName())
                        .email(user.getEmail()).activity("Đăng ký tài khoản").occurredAt(user.getCreatedAt()).build())
                .toList();
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
        StringBuilder csv = new StringBuilder("date,orders,revenue\n");
        startDate.datesUntil(endDate.plusDays(1)).forEach(date -> {
            DailySalesResponse day = getDailySales(date);
            csv.append(date).append(',').append(day.getTotalOrders()).append(',').append(day.getTotalRevenue()).append('\n');
        });
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public byte[] exportInventoryReport(String format) {
        StringBuilder csv = new StringBuilder("id,name,status,totalSold,inStock\n");
        productRepository.findAll().forEach(product -> csv.append(product.getId()).append(',')
                .append(csvCell(product.getName())).append(',').append(product.getStatus()).append(',')
                .append(product.getTotalSold()).append(',').append(product.isInStock()).append('\n'));
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private Map<String, BigDecimal> getCategoryRevenue() {
        return orderItemRepository.findAll().stream()
                .filter(item -> item.getOrder() != null && isRecognizedSale(item.getOrder()))
                .filter(item -> item.getProduct() != null && item.getProduct().getCategory() != null)
                .collect(Collectors.groupingBy(item -> item.getProduct().getCategory().getName(),
                        LinkedHashMap::new,
                        Collectors.reducing(BigDecimal.ZERO, item -> Objects.requireNonNullElse(item.getTotalPrice(), BigDecimal.ZERO), BigDecimal::add)));
    }

    private boolean isRecognizedSale(com.hoangdinh.delta_shop_app.entity.Order order) {
        if (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.REFUNDED) {
            return false;
        }
        return order.getPaymentStatus() == PaymentStatus.PAID;
    }

    private String csvCell(String value) {
        return "\"" + Objects.requireNonNullElse(value, "").replace("\"", "\"\"") + "\"";
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
