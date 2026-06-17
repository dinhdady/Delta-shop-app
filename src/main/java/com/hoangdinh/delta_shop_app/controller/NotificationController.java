package com.hoangdinh.delta_shop_app.controller;

import com.hoangdinh.delta_shop_app.dto.response.PageResponse;
import com.hoangdinh.delta_shop_app.dto.response.notification.NotificationResponse;
import com.hoangdinh.delta_shop_app.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "APIs for user notifications")
@SecurityRequirement(name = "Bearer Authentication")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "Get current user notifications")
    public ResponseEntity<PageResponse<NotificationResponse>> getNotifications(
            @RequestAttribute("userId") UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(notificationService.getUserNotifications(userId, page, size));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Get unread notification count")
    public ResponseEntity<Map<String, Integer>> getUnreadCount(@RequestAttribute("userId") UUID userId) {
        return ResponseEntity.ok(Map.of("count", notificationService.getUnreadCount(userId)));
    }

    @GetMapping("/{notificationId}")
    @Operation(summary = "Get notification detail")
    public ResponseEntity<NotificationResponse> getNotification(
            @RequestAttribute("userId") UUID userId,
            @PathVariable UUID notificationId) {
        return ResponseEntity.ok(notificationService.getNotificationById(notificationId, userId));
    }

    @PatchMapping("/{notificationId}/read")
    @Operation(summary = "Mark notification as read")
    public ResponseEntity<Void> markAsRead(
            @RequestAttribute("userId") UUID userId,
            @PathVariable UUID notificationId) {
        notificationService.markAsRead(notificationId, userId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/read-all")
    @Operation(summary = "Mark all notifications as read")
    public ResponseEntity<Void> markAllAsRead(@RequestAttribute("userId") UUID userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{notificationId}")
    @Operation(summary = "Delete notification")
    public ResponseEntity<Void> deleteNotification(
            @RequestAttribute("userId") UUID userId,
            @PathVariable UUID notificationId) {
        notificationService.deleteNotification(notificationId, userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/read")
    @Operation(summary = "Delete all read notifications")
    public ResponseEntity<Void> deleteReadNotifications(@RequestAttribute("userId") UUID userId) {
        notificationService.deleteAllReadNotifications(userId);
        return ResponseEntity.noContent().build();
    }
}
