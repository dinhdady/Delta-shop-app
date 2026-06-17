package com.hoangdinh.delta_shop_app.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Dataset {
    private String label;
    private List<Number> data;
    private String backgroundColor;
    private String borderColor;
    private int borderWidth;
    private String fill;
    private String tension;
}
