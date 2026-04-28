package com.hoangdinh.delta_shop_app.service;

import com.hoangdinh.delta_shop_app.dto.request.notification.SendNotificationRequest;
import com.hoangdinh.delta_shop_app.dto.response.PageResponse;
import com.hoangdinh.delta_shop_app.dto.response.notification.NotificationResponse;
import com.hoangdinh.delta_shop_app.entity.Order;

import java.util.List;
import java.util.UUID;

public interface NotificationService {

    // User notifications
    void sendNotification(UUID userId, SendNotificationRequest request);
    void sendBulkNotification(List<UUID> userIds, SendNotificationRequest request);
    void sendToAllUsers(SendNotificationRequest request);

    // Order notifications
    void notifyOrderPlaced(Order order);
    void notifyOrderConfirmed(Order order);
    void notifyOrderShipped(Order order);
    void notifyOrderDelivered(Order order);
    void notifyOrderCancelled(Order order);

    // Payment notifications
    void notifyPaymentSuccess(UUID userId, UUID orderId);
    void notifyPaymentFailed(UUID userId, UUID orderId, String reason);

    // System notifications
    void notifyPromotionCreated(String promotionName);
    void notifySystemAlert(String title, String message);

    // User operations
    PageResponse<NotificationResponse> getUserNotifications(UUID userId, int page, int size);
    NotificationResponse getNotificationById(UUID notificationId, UUID userId);
    void markAsRead(UUID notificationId, UUID userId);
    void markAllAsRead(UUID userId);
    void deleteNotification(UUID notificationId, UUID userId);
    void deleteAllReadNotifications(UUID userId);

    // Counts
    int getUnreadCount(UUID userId);

    // Real-time (WebSocket)
    void sendRealTimeNotification(UUID userId, NotificationResponse notification);
}