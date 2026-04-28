package com.hoangdinh.delta_shop_app.dto.request.review;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class BatchModerateRequest {
    private List<UUID> reviewIds;
    private String status;
}