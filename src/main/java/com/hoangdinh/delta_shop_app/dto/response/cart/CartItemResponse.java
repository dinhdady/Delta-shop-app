package com.hoangdinh.delta_shop_app.dto.response.cart;

import com.hoangdinh.delta_shop_app.entity.CartItem;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@Getter
@Setter
public class CartItemResponse {
    private UUID id;
    private UUID variantId;
    private UUID productId;
    private String productName;
    private String variantName;
    private String productImage;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
    private Integer availableStock;
    // THÊM SIZE FIELDS
    private String selectedSize;
    private String selectedSizeLabel;
    private String selectedSizeMeasurement;
    public static CartItemResponse from(CartItem item) {
        if (item == null) return null;

        return CartItemResponse.builder()
                .id(item.getId())
                .variantId(item.getVariant() != null ? item.getVariant().getId() : null)
                .productName(item.getVariant() != null && item.getVariant().getProduct() != null ?
                        item.getVariant().getProduct().getName() : null)
                .variantName(item.getVariant() != null ? item.getVariant().getName() : null)
                .productImage(item.getVariant() != null ? item.getVariant().getImageUrl() : null)
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .subtotal(item.getSubtotal())
                .availableStock(item.getVariant() != null ? item.getVariant().getAvailableQuantity() : 0)
                .selectedSize(item.getSelectedSize())
                .selectedSizeLabel(item.getSelectedSizeLabel())
                .selectedSizeMeasurement(item.getSelectedSizeMeasurement())
                .build();
    }
}