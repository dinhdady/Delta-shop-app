package com.hoangdinh.delta_shop_app.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryDashboardResponse {
    private long totalProducts;
    private long activeProducts;
    private long inactiveProducts;
    private long outOfStockProducts;
    private long lowStockProducts;
    private long discontinuedProducts;
    private BigDecimal totalInventoryValue;
    private BigDecimal averageProductPrice;
    private List<LowStockAlertItem> lowStockAlerts;
    private List<OutOfStockItem> outOfStockItems;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class LowStockAlertItem {
    private UUID productId;
    private String productName;
    private String variantName;
    private String sku;
    private int currentStock;
    private int minStockAlert;
    private int reorderQuantity;
    private String productImage;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class OutOfStockItem {
    private UUID productId;
    private String productName;
    private String variantName;
    private String sku;
    private String productImage;
    private int lastSoldDate;
}