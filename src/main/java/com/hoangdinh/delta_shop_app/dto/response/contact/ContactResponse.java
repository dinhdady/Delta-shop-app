package com.hoangdinh.delta_shop_app.dto.response.contact;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ContactResponse {
    private UUID id;
    private String name;
    private String email;
    private String phone;
    private String subject;
    private String message;
    private String status;
    private String adminNote;
    private LocalDateTime repliedAt;
    private LocalDateTime createdAt;
}