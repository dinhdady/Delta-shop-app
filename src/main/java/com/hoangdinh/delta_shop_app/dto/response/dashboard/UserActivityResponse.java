package com.hoangdinh.delta_shop_app.dto.response.dashboard;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class UserActivityResponse {
    private UUID userId;
    private String userName;
    private String email;
    private String activity;
    private LocalDateTime occurredAt;
}
