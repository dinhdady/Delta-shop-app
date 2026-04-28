package com.hoangdinh.delta_shop_app.service;

import com.hoangdinh.delta_shop_app.dto.response.dashboard.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface DashboardService {

    // Admin dashboard
    AdminDashboardResponse getAdminDashboard();
    SalesOverviewResponse getSalesOverview(LocalDate startDate, LocalDate endDate);
    TopProductsResponse getTopProducts(int limit, String period);
    TopCategoriesResponse getTopCategories(int limit);

    // Sales analytics
    DailySalesResponse getDailySales(LocalDate date);
    MonthlySalesResponse getMonthlySales(int year, int month);
    YearlySalesResponse getYearlySales(int year);

    // Charts data
    ChartDataResponse getSalesChartData(String period); // daily, weekly, monthly, yearly
    ChartDataResponse getOrderStatusChartData();
    ChartDataResponse getRevenueByCategoryChartData();

    // Customer analytics
    CustomerAnalyticsResponse getCustomerAnalytics();
    List<UserActivityResponse> getRecentUserActivities(int limit);

    // Inventory dashboard
    InventoryDashboardResponse getInventoryDashboard();

    // Export reports
    byte[] exportSalesReport(LocalDate startDate, LocalDate endDate, String format);
    byte[] exportInventoryReport(String format);
}