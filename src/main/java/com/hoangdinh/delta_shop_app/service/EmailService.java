package com.hoangdinh.delta_shop_app.service;

import com.hoangdinh.delta_shop_app.entity.Order;
import com.hoangdinh.delta_shop_app.entity.User;

import java.util.Map;

public interface EmailService {

    // Verification emails
    void sendEmailVerification(String email, String name, String token);
    void sendPasswordReset(String email, String name, String token);
    void sendPhoneVerification(String phone, String otp);

    // Contact emails
    void sendContactConfirmation(String to, String name, String message);
    void sendContactReply(String to, String name, String subject, String reply);

    // Order emails
    void sendOrderConfirmation(Order order);
    void sendOrderShipped(Order order);
    void sendOrderDelivered(Order order);
    void sendOrderCancelled(Order order);

    // Promotional emails
    void sendWelcomeEmail(User user);
    void sendPromotionEmail(String email, String promotionCode, Map<String, Object> promotionData);

    // Base email methods
    void sendEmail(String to, String subject, String body);
    void sendHtmlEmail(String to, String subject, String htmlBody);
    void sendHtmlEmail(String to, String subject, String templateName, Map<String, Object> variables);
}