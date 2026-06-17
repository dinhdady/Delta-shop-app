package com.hoangdinh.delta_shop_app.dto.response.search;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AutoCompleteResponse {
    private UUID id;
    private String name;
    private String slug;
    private String primaryImage;
    private BigDecimal basePrice;
    private String categoryName;
}
