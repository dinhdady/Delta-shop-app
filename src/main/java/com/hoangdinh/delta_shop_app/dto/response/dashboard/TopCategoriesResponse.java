package com.hoangdinh.delta_shop_app.dto.response.dashboard;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class TopCategoriesResponse {
    private List<Map<String, Object>> categories;
    private BigDecimal totalRevenue;
}
