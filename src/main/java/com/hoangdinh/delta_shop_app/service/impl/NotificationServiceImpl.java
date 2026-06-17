package com.hoangdinh.delta_shop_app.service.impl;

import com.hoangdinh.delta_shop_app.dto.request.notification.SendNotificationRequest;
import com.hoangdinh.delta_shop_app.dto.response.PageResponse;
import com.hoangdinh.delta_shop_app.dto.response.notification.NotificationResponse;
import com.hoangdinh.delta_shop_app.entity.Order;
import com.hoangdinh.delta_shop_app.entity.Notification;
import com.hoangdinh.delta_shop_app.entity.User;
import com.hoangdinh.delta_shop_app.enums.NotificationType;
import com.hoangdinh.delta_shop_app.exception.ResourceNotFoundException;
import com.hoangdinh.delta_shop_app.repository.NotificationRepository;
import com.hoangdinh.delta_shop_app.repository.UserRepository;
import com.hoangdinh.delta_shop_app.service.EmailService;
import com.hoangdinh.delta_shop_app.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final EmailService emailService;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void sendNotification(UUID userId, SendNotificationRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        Notification notification = Notification.builder()
                .user(user)
                .type(resolveType(request.getType()))
                .title(request.getTitle())
                .body(request.getBody())
                .data(request.getData())
                .build();
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void sendBulkNotification(List<UUID> userIds, SendNotificationRequest request) {
        userIds.forEach(userId -> sendNotification(userId, request));
    }

    @Override
    @Transactional
    public void sendToAllUsers(SendNotificationRequest request) {
        userRepository.findAll().forEach(user -> sendNotification(user.getId(), request));
    }

    @Override
    public void notifyOrderPlaced(Order order) {
        if (order.getUser() != null) {
            saveOrderNotification(order.getUser(), NotificationType.ORDER_PLACED,
                    "Đặt hàng thành công",
                    "Đơn hàng " + order.getOrderNumber() + " đã được ghi nhận.",
                    order.getId());
        }
        log.info("=== NOTIFICATION: ORDER PLACED ===");
        log.info("Order number: {}", order.getOrderNumber());
        log.info("Customer: {}", order.getUser() != null ? order.getUser().getEmail() : order.getGuestEmail());
        log.info("Total amount: {}", order.getTotalAmount());
        log.info("==================================");
        try {
            emailService.sendOrderConfirmation(order);
        } catch (Exception e) {
            log.error("Could not send order confirmation email for {}", order.getOrderNumber(), e);
        }
    }

    @Override
    public void notifyOrderConfirmed(Order order) {
        if (order.getUser() != null) {
            saveOrderNotification(order.getUser(), NotificationType.ORDER_CONFIRMED,
                    "Đơn hàng đã xác nhận",
                    "Đơn hàng " + order.getOrderNumber() + " đã được xác nhận.",
                    order.getId());
        }
        log.info("=== NOTIFICATION: ORDER CONFIRMED ===");
        log.info("Order number: {}", order.getOrderNumber());
        log.info("Status: {}", order.getStatus());
        log.info("=====================================");
    }

    @Override
    public void notifyOrderShipped(Order order) {
        if (order.getUser() != null) {
            saveOrderNotification(order.getUser(), NotificationType.ORDER_SHIPPED,
                    "Đơn hàng đang giao",
                    "Đơn hàng " + order.getOrderNumber() + " đang được giao.",
                    order.getId());
        }
        log.info("=== NOTIFICATION: ORDER SHIPPED ===");
        log.info("Order number: {}", order.getOrderNumber());
        log.info("Tracking number: {}", order.getTrackingNumber());
        log.info("===================================");
        try {
            emailService.sendOrderShipped(order);
        } catch (Exception e) {
            log.error("Could not send order shipped email for {}", order.getOrderNumber(), e);
        }
    }

    @Override
    public void notifyOrderDelivered(Order order) {
        if (order.getUser() != null) {
            saveOrderNotification(order.getUser(), NotificationType.ORDER_DELIVERED,
                    "Đơn hàng đã giao",
                    "Đơn hàng " + order.getOrderNumber() + " đã giao thành công.",
                    order.getId());
        }
        log.info("=== NOTIFICATION: ORDER DELIVERED ===");
        log.info("Order number: {}", order.getOrderNumber());
        log.info("Delivered at: {}", order.getDeliveredAt());
        log.info("=====================================");
        try {
            emailService.sendOrderDelivered(order);
        } catch (Exception e) {
            log.error("Could not send order delivered email for {}", order.getOrderNumber(), e);
        }
    }

    @Override
    public void notifyOrderCancelled(Order order) {
        if (order.getUser() != null) {
            saveOrderNotification(order.getUser(), NotificationType.ORDER_CANCELLED,
                    "Đơn hàng đã hủy",
                    "Đơn hàng " + order.getOrderNumber() + " đã bị hủy.",
                    order.getId());
        }
        log.info("=== NOTIFICATION: ORDER CANCELLED ===");
        log.info("Order number: {}", order.getOrderNumber());
        log.info("Cancel reason: {}", order.getCancelReason());
        log.info("=====================================");
        try {
            emailService.sendOrderCancelled(order);
        } catch (Exception e) {
            log.error("Could not send order cancelled email for {}", order.getOrderNumber(), e);
        }
    }

    @Override
    public void notifyPaymentSuccess(UUID userId, UUID orderId) {
        SendNotificationRequest request = new SendNotificationRequest();
        request.setTitle("Thanh toán thành công");
        request.setBody("Thanh toán cho đơn hàng đã được ghi nhận.");
        request.setType(NotificationType.PAYMENT_SUCCESS.name());
        request.setData("{\"orderId\":\"" + orderId + "\"}");
        sendNotification(userId, request);
    }

    @Override
    public void notifyPaymentFailed(UUID userId, UUID orderId, String reason) {
        SendNotificationRequest request = new SendNotificationRequest();
        request.setTitle("Thanh toán thất bại");
        request.setBody(reason == null || reason.isBlank() ? "Thanh toán chưa thành công." : reason);
        request.setType(NotificationType.PAYMENT_FAILED.name());
        request.setData("{\"orderId\":\"" + orderId + "\"}");
        sendNotification(userId, request);
    }

    @Override
    public void notifyPromotionCreated(String promotionName) {
        SendNotificationRequest request = new SendNotificationRequest();
        request.setTitle("Khuyến mãi mới");
        request.setBody("Chương trình " + promotionName + " đang khả dụng.");
        request.setType(NotificationType.PROMOTION.name());
        sendToAllUsers(request);
    }

    @Override
    public void notifySystemAlert(String title, String message) {
        SendNotificationRequest request = new SendNotificationRequest();
        request.setTitle(title);
        request.setBody(message);
        request.setType(NotificationType.SYSTEM.name());
        sendToAllUsers(request);
    }

    @Override
    public PageResponse<NotificationResponse> getUserNotifications(UUID userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return PageResponse.of(notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::toResponse));
    }

    @Override
    public NotificationResponse getNotificationById(UUID notificationId, UUID userId) {
        Notification notification = findOwnedNotification(notificationId, userId);
        return toResponse(notification);
    }

    @Override
    @Transactional
    public void markAsRead(UUID notificationId, UUID userId) {
        findOwnedNotification(notificationId, userId);
        notificationRepository.markAsRead(notificationId, userId);
    }

    @Override
    @Transactional
    public void markAllAsRead(UUID userId) {
        notificationRepository.markAllAsRead(userId);
    }

    @Override
    @Transactional
    public void deleteNotification(UUID notificationId, UUID userId) {
        Notification notification = findOwnedNotification(notificationId, userId);
        notificationRepository.delete(notification);
    }

    @Override
    @Transactional
    public void deleteAllReadNotifications(UUID userId) {
        notificationRepository.deleteByUserIdAndIsReadTrue(userId);
    }

    @Override
    public int getUnreadCount(UUID userId) {
        return Math.toIntExact(notificationRepository.countUnreadByUserId(userId));
    }

    @Override
    public void sendRealTimeNotification(UUID userId, NotificationResponse notification) {

    }

    private void saveOrderNotification(User user, NotificationType type, String title, String body, UUID orderId) {
        Notification notification = Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .body(body)
                .data("{\"orderId\":\"" + orderId + "\"}")
                .build();
        notificationRepository.save(notification);
    }

    private Notification findOwnedNotification(UUID notificationId, UUID userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", notificationId));
        if (!notification.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Notification", "id", notificationId);
        }
        return notification;
    }

    private NotificationType resolveType(String type) {
        if (type == null || type.isBlank()) {
            return NotificationType.SYSTEM;
        }
        try {
            return NotificationType.valueOf(type.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            return NotificationType.SYSTEM;
        }
    }

    private NotificationResponse toResponse(Notification notification) {
        ZoneId zoneId = ZoneId.systemDefault();
        return NotificationResponse.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .body(notification.getBody())
                .type(notification.getType().name())
                .data(notification.getData())
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt() == null ? null : notification.getCreatedAt().atZone(zoneId))
                .readAt(notification.getReadAt() == null ? null : notification.getReadAt().atZone(zoneId))
                .build();
    }
}
