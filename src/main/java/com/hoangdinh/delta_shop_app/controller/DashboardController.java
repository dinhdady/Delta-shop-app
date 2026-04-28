package com.hoangdinh.delta_shop_app.controller;

import com.hoangdinh.delta_shop_app.dto.response.dashboard.*;
import com.hoangdinh.delta_shop_app.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "APIs for admin dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/admin")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get admin dashboard data")
    public ResponseEntity<AdminDashboardResponse> getAdminDashboard() {
        return ResponseEntity.ok(dashboardService.getAdminDashboard());
    }

    @GetMapping("/admin/sales")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get sales overview")
    public ResponseEntity<SalesOverviewResponse> getSalesOverview(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        if (startDate == null) startDate = LocalDate.now().minusDays(30);
        if (endDate == null) endDate = LocalDate.now();
        return ResponseEntity.ok(dashboardService.getSalesOverview(startDate, endDate));
    }

    @GetMapping("/admin/top-products")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get top selling products")
    public ResponseEntity<TopProductsResponse> getTopProducts(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "30") String period) {
        return ResponseEntity.ok(dashboardService.getTopProducts(limit, period));
    }

    @GetMapping("/admin/charts/sales")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get sales chart data")
    public ResponseEntity<ChartDataResponse> getSalesChartData(
            @RequestParam(defaultValue = "monthly") String period) {
        return ResponseEntity.ok(dashboardService.getSalesChartData(period));
    }

    @GetMapping("/admin/charts/order-status")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get order status chart data")
    public ResponseEntity<ChartDataResponse> getOrderStatusChartData() {
        return ResponseEntity.ok(dashboardService.getOrderStatusChartData());
    }

    @GetMapping("/admin/inventory")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get inventory dashboard")
    public ResponseEntity<InventoryDashboardResponse> getInventoryDashboard() {
        return ResponseEntity.ok(dashboardService.getInventoryDashboard());
    }
}
