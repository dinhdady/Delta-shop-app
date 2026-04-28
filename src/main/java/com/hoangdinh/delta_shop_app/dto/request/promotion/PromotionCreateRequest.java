package com.hoangdinh.delta_shop_app.dto.request.promotion;

import com.hoangdinh.delta_shop_app.enums.DiscountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class PromotionCreateRequest {
    @NotBlank(message = "Tên khuyến mãi không được để trống")
    private String name;

    private String code;

    private String description;

    @NotNull(message = "Loại khuyến mãi không được để trống")
    private DiscountType type;

    @NotNull(message = "Giá trị không được để trống")
    @Positive(message = "Giá trị phải lớn hơn 0")
    private BigDecimal value;

    private BigDecimal minOrderAmount;
    private BigDecimal maxDiscountAmount;
    private Integer usageLimit;
    private Integer usagePerUser;

    private String appliesTo;

    @NotNull(message = "Ngày bắt đầu không được để trống")
    private LocalDateTime startsAt;

    private LocalDateTime endsAt;

    private boolean isStackable;

    private List<PromotionItemRequest> applicableItems;
}

