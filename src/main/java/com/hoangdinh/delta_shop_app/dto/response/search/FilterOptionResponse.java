package com.hoangdinh.delta_shop_app.dto.response.search;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FilterOptionResponse {
    private String value;
    private String label;
    private Long count;
}
