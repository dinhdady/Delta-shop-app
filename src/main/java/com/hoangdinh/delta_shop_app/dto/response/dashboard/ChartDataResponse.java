package com.hoangdinh.delta_shop_app.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChartDataResponse {
    private List<String> labels;
    private List<Dataset> datasets;
    private String period;
    private String title;
    private String xAxisLabel;
    private String yAxisLabel;
}

