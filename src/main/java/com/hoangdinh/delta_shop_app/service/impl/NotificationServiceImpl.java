package com.hoangdinh.delta_shop_app.service.impl;

import com.hoangdinh.delta_shop_app.dto.request.notification.SendNotificationRequest;
import com.hoangdinh.delta_shop_app.dto.response.PageResponse;
import com.hoangdinh.delta_shop_app.dto.response.notification.NotificationResponse;
import com.hoangdinh.delta_shop_app.entity.Order;
import com.hoangdinh.delta_shop_app.entity.User;
import com.hoangdinh.delta_shop_app.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    @Override
    public void sendNotification(UUID userId, SendNotificationRequest request) {

    }

    @Override
    public void sendBulkNotification(List<UUID> userIds, SendNotificationRequest request) {

    }

    @Override
    public void sendToAllUsers(SendNotificationRequest request) {

    }

    @Override
    public void notifyOrderPlaced(Order order) {
        log.info("=== NOTIFICATION: ORDER PLACED ===");
        log.info("Order number: {}", order.getOrderNumber());
        log.info("Customer: {}", order.getUser() != null ? order.getUser().getEmail() : order.getGuestEmail());
        log.info("Total amount: {}", order.getTotalAmount());
        log.info("==================================");
        // TODO: Implement real notification (email, websocket, push notification)
    }

    @Override
    public void notifyOrderConfirmed(Order order) {
        log.info("=== NOTIFICATION: ORDER CONFIRMED ===");
        log.info("Order number: {}", order.getOrderNumber());
        log.info("Status: {}", order.getStatus());
        log.info("=====================================");
    }

    @Override
    public void notifyOrderShipped(Order order) {
        log.info("=== NOTIFICATION: ORDER SHIPPED ===");
        log.info("Order number: {}", order.getOrderNumber());
        log.info("Tracking number: {}", order.getTrackingNumber());
        log.info("===================================");
    }

    @Override
    public void notifyOrderDelivered(Order order) {
        log.info("=== NOTIFICATION: ORDER DELIVERED ===");
        log.info("Order number: {}", order.getOrderNumber());
        log.info("Delivered at: {}", order.getDeliveredAt());
        log.info("=====================================");
    }

    @Override
    public void notifyOrderCancelled(Order order) {
        log.info("=== NOTIFICATION: ORDER CANCELLED ===");
        log.info("Order number: {}", order.getOrderNumber());
        log.info("Cancel reason: {}", order.getCancelReason());
        log.info("=====================================");
    }

    @Override
    public void notifyPaymentSuccess(UUID userId, UUID orderId) {

    }

    @Override
    public void notifyPaymentFailed(UUID userId, UUID orderId, String reason) {

    }

    @Override
    public void notifyPromotionCreated(String promotionName) {

    }

    @Override
    public void notifySystemAlert(String title, String message) {

    }

    @Override
    public PageResponse<NotificationResponse> getUserNotifications(UUID userId, int page, int size) {
        return null;
    }

    @Override
    public NotificationResponse getNotificationById(UUID notificationId, UUID userId) {
        return null;
    }

    @Override
    public void markAsRead(UUID notificationId, UUID userId) {

    }

    @Override
    public void markAllAsRead(UUID userId) {

    }

    @Override
    public void deleteNotification(UUID notificationId, UUID userId) {

    }

    @Override
    public void deleteAllReadNotifications(UUID userId) {

    }

    @Override
    public int getUnreadCount(UUID userId) {
        return 0;
    }

    @Override
    public void sendRealTimeNotification(UUID userId, NotificationResponse notification) {

    }
}