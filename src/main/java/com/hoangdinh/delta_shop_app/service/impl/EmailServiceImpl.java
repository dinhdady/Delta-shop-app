package com.hoangdinh.delta_shop_app.service.impl;

import com.hoangdinh.delta_shop_app.entity.Contact;
import com.hoangdinh.delta_shop_app.entity.Order;
import com.hoangdinh.delta_shop_app.entity.OrderItem;
import com.hoangdinh.delta_shop_app.entity.User;
import com.hoangdinh.delta_shop_app.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import java.io.UnsupportedEncodingException;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;

    @Override
    public void sendEmailVerification(String email, String name, String token) {
        String subject = "Xác thực email - Delta Sports";
        String verificationUrl = frontendUrl + "/verify-email?token=" + token;

        String htmlContent = buildVerificationEmail(name, verificationUrl);
        sendHtmlEmail(email, subject, htmlContent);

        log.info("Verification email sent to: {}", email);
    }

    @Override
    public void sendPasswordReset(String email, String name, String token) {
        String subject = "Đặt lại mật khẩu - Delta Sports";
        String resetUrl = frontendUrl + "/reset-password?token=" + token;

        String htmlContent = buildPasswordResetEmail(name, resetUrl);
        sendHtmlEmail(email, subject, htmlContent);

        log.info("Password reset email sent to: {}", email);
    }

    @Override
    public void sendPhoneVerification(String phone, String otp) {
        // SMS implementation - có thể dùng SMS service như Twilio
        // Tạm thời log ra console
        log.info("SMS verification for {}: OTP={}", phone, otp);
    }

    @Override
    public void sendOrderConfirmation(Order order) {
        String subject = "Xác nhận đơn hàng #" + order.getOrderNumber();
        String htmlContent = buildOrderConfirmationEmail(order);
        sendHtmlEmail(order.getUser().getEmail(), subject, htmlContent);

        log.info("Order confirmation email sent for order: {}", order.getOrderNumber());
    }

    @Override
    public void sendOrderShipped(Order order) {
        String subject = "Đơn hàng #" + order.getOrderNumber() + " đã được giao";
        String htmlContent = buildOrderShippedEmail(order);
        sendHtmlEmail(order.getUser().getEmail(), subject, htmlContent);

        log.info("Order shipped email sent for order: {}", order.getOrderNumber());
    }

    @Override
    public void sendOrderDelivered(Order order) {
        String subject = "Đơn hàng #" + order.getOrderNumber() + " đã được giao thành công";
        String htmlContent = buildOrderDeliveredEmail(order);
        sendHtmlEmail(order.getUser().getEmail(), subject, htmlContent);

        log.info("Order delivered email sent for order: {}", order.getOrderNumber());
    }

    @Override
    public void sendOrderCancelled(Order order) {
        String subject = "Đơn hàng #" + order.getOrderNumber() + " đã bị hủy";
        String htmlContent = buildOrderCancelledEmail(order);
        sendHtmlEmail(order.getUser().getEmail(), subject, htmlContent);

        log.info("Order cancelled email sent for order: {}", order.getOrderNumber());
    }

    @Override
    public void sendWelcomeEmail(User user) {
        String subject = "Chào mừng bạn đến với Delta Sports";
        String htmlContent = buildWelcomeEmail(user);
        sendHtmlEmail(user.getEmail(), subject, htmlContent);

        log.info("Welcome email sent to: {}", user.getEmail());
    }

    @Override
    public void sendPromotionEmail(String email, String promotionCode, Map<String, Object> promotionData) {
        String subject = "Ưu đãi đặc biệt từ Delta Sports";
        String htmlContent = buildPromotionEmail(promotionCode, promotionData);
        sendEmail(email, subject, htmlContent);
    }
    private String buildPromotionEmail(String promotionCode, Map<String, Object> promotionData) {
        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <style>
                body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                .header { background: linear-gradient(135deg, #1a1a1a 0%, #333 100%); color: white; padding: 30px; text-align: center; }
                .header h1 { margin: 0; font-size: 28px; }
                .header span { color: #ff4400; }
                .content { padding: 30px; background: #f9f9f9; }
                .promo-code { font-size: 24px; font-weight: bold; color: #ff4400; text-align: center; margin: 20px 0; }
                .footer { text-align: center; padding: 20px; font-size: 12px; color: #666; }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header">
                    <h1>DELTA <span>SPORTS</span></h1>
                </div>
                <div class="content">
                    <h2>Ưu đãi đặc biệt dành cho bạn!</h2>
                    <p>Sử dụng mã khuyến mãi để nhận ưu đãi:</p>
                    <div class="promo-code">%s</div>
                    <p>Chương trình có hạn, nhanh tay đặt hàng ngay!</p>
                </div>
                <div class="footer">
                    <p>© 2024 Delta Sports. All rights reserved.</p>
                </div>
            </div>
        </body>
        </html>
        """.formatted(promotionCode);
    }

    @Override
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Email sent to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    private String buildPasswordResetEmail(String name, String resetUrl) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #0D8F81; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; background-color: #f9f9f9; }
                    .button { display: inline-block; padding: 12px 24px; background-color: #0D8F81; color: white; text-decoration: none; border-radius: 4px; margin: 20px 0; }
                    .footer { text-align: center; padding: 20px; font-size: 12px; color: #666; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Delta Sports</h1>
                    </div>
                    <div class="content">
                        <h2>Xin chào %s,</h2>
                        <p>Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn.</p>
                        <p>Nhấp vào nút bên dưới để đặt lại mật khẩu:</p>
                        <div style="text-align: center;">
                            <a href="%s" class="button">Đặt lại mật khẩu</a>
                        </div>
                        <p>Hoặc copy đường dẫn sau vào trình duyệt:</p>
                        <p style="word-break: break-all;">%s</p>
                        <p>Link này sẽ hết hạn sau 2 giờ.</p>
                        <p>Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.</p>
                    </div>
                    <div class="footer">
                        <p>© 2024 Delta Sports. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(name, resetUrl, resetUrl);
    }

    private String buildOrderConfirmationEmail(Order order) {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        StringBuilder itemsHtml = new StringBuilder();

        for (OrderItem item : order.getItems()) {
            itemsHtml.append("""
                <tr>
                    <td style="padding: 8px; border-bottom: 1px solid #ddd;">%s</td>
                    <td style="padding: 8px; text-align: center; border-bottom: 1px solid #ddd;">%d</td>
                    <td style="padding: 8px; text-align: right; border-bottom: 1px solid #ddd;">%s</td>
                </tr>
                """.formatted(
                    item.getProductName(),
                    item.getQuantity(),
                    currencyFormat.format(item.getUnitPrice())
            ));
        }

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #0D8F81; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; background-color: #f9f9f9; }
                    table { width: 100%%; border-collapse: collapse; margin: 20px 0; }
                    th { background-color: #0D8F81; color: white; padding: 10px; text-align: left; }
                    .total { font-size: 18px; font-weight: bold; text-align: right; margin-top: 20px; }
                    .footer { text-align: center; padding: 20px; font-size: 12px; color: #666; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Delta Sports</h1>
                    </div>
                    <div class="content">
                        <h2>Xác nhận đơn hàng #%s</h2>
                        <p>Cảm ơn bạn đã mua sắm tại Delta Sports!</p>
                        <h3>Thông tin đơn hàng:</h3>
                        <table>
                            <thead>
                                <tr>
                                    <th>Sản phẩm</th>
                                    <th>Số lượng</th>
                                    <th>Đơn giá</th>
                                </tr>
                            </thead>
                            <tbody>
                                %s
                            </tbody>
                        </table>
                        <div class="total">
                            <p>Tổng tiền: %s</p>
                        </div>
                        <h3>Thông tin giao hàng:</h3>
                        <p><strong>Người nhận:</strong> %s</p>
                        <p><strong>Số điện thoại:</strong> %s</p>
                        <p><strong>Địa chỉ:</strong> %s</p>
                        <p><strong>Ngày đặt hàng:</strong> %s</p>
                        <p>Chúng tôi sẽ thông báo khi đơn hàng được giao.</p>
                    </div>
                    <div class="footer">
                        <p>© 2024 Delta Sports. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                order.getOrderNumber(),
                itemsHtml.toString(),
                currencyFormat.format(order.getTotalAmount()),
                order.getShippingName(),
                order.getShippingPhone(),
                order.getShippingAddress(),
                order.getCreatedAt().toLocalDate().toString()
        );
    }

    private String buildOrderShippedEmail(Order order) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #0D8F81; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; background-color: #f9f9f9; }
                    .footer { text-align: center; padding: 20px; font-size: 12px; color: #666; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Delta Sports</h1>
                    </div>
                    <div class="content">
                        <h2>Đơn hàng #%s đã được giao</h2>
                        <p>Đơn hàng của bạn đã được giao cho đơn vị vận chuyển.</p>
                        <p><strong>Mã vận đơn:</strong> %s</p>
                        <p>Bạn có thể theo dõi đơn hàng tại đây.</p>
                        <p>Cảm ơn bạn đã tin tưởng Delta Sports!</p>
                    </div>
                    <div class="footer">
                        <p>© 2024 Delta Sports. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(order.getOrderNumber(), order.getTrackingNumber() != null ? order.getTrackingNumber() : "Chưa cập nhật");
    }

    private String buildOrderDeliveredEmail(Order order) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #0D8F81; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; background-color: #f9f9f9; }
                    .button { display: inline-block; padding: 12px 24px; background-color: #0D8F81; color: white; text-decoration: none; border-radius: 4px; margin: 20px 0; }
                    .footer { text-align: center; padding: 20px; font-size: 12px; color: #666; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Delta Sports</h1>
                    </div>
                    <div class="content">
                        <h2>Đơn hàng #%s đã giao thành công</h2>
                        <p>Đơn hàng của bạn đã được giao thành công.</p>
                        <p>Nếu bạn hài lòng với sản phẩm, hãy để lại đánh giá nhé!</p>
                        <div style="text-align: center;">
                            <a href="%s/products/%s/review" class="button">Đánh giá sản phẩm</a>
                        </div>
                        <p>Cảm ơn bạn đã mua sắm tại Delta Sports!</p>
                    </div>
                    <div class="footer">
                        <p>© 2024 Delta Sports. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(order.getOrderNumber(), frontendUrl, order.getItems().get(0).getProduct().getId());
    }

    private String buildOrderCancelledEmail(Order order) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #0D8F81; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; background-color: #f9f9f9; }
                    .footer { text-align: center; padding: 20px; font-size: 12px; color: #666; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Delta Sports</h1>
                    </div>
                    <div class="content">
                        <h2>Đơn hàng #%s đã bị hủy</h2>
                        <p>Đơn hàng của bạn đã bị hủy.</p>
                        <p><strong>Lý do:</strong> %s</p>
                        <p>Nếu có thắc mắc, vui lòng liên hệ với chúng tôi.</p>
                        <p>Rất mong được phục vụ bạn trong những đơn hàng tiếp theo!</p>
                    </div>
                    <div class="footer">
                        <p>© 2024 Delta Sports. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(order.getOrderNumber(), order.getCancelReason() != null ? order.getCancelReason() : "Không có lý do");
    }

    private String buildWelcomeEmail(User user) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #0D8F81; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; background-color: #f9f9f9; }
                    .button { display: inline-block; padding: 12px 24px; background-color: #0D8F81; color: white; text-decoration: none; border-radius: 4px; margin: 20px 0; }
                    .footer { text-align: center; padding: 20px; font-size: 12px; color: #666; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Delta Sports</h1>
                    </div>
                    <div class="content">
                        <h2>Chào mừng %s đến với Delta Sports!</h2>
                        <p>Cảm ơn bạn đã đăng ký tài khoản tại Delta Sports.</p>
                        <p>Chúng tôi cung cấp các sản phẩm thể thao chất lượng cao với giá cả cạnh tranh.</p>
                        <div style="text-align: center;">
                            <a href="%s/shop" class="button">Mua sắm ngay</a>
                        </div>
                        <p>Hãy khám phá những sản phẩm mới nhất của chúng tôi!</p>
                    </div>
                    <div class="footer">
                        <p>© 2024 Delta Sports. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(user.getFirstName(), frontendUrl);
    }
    @Override
    public void sendEmail(String to, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);
            helper.setFrom(fromEmail, "Delta Sports");

            mailSender.send(message);
            log.info("Email sent successfully to: {}", to);
        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("Failed to send email to: {}", to, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    @Override
    public void sendContactConfirmation(String to, String name, String message) {
        String subject = "Xác nhận liên hệ - Delta Sports";
        String htmlContent = String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #1a1a1a 0%, #333 100%); color: white; padding: 30px; text-align: center; }
                    .header h1 { margin: 0; font-size: 28px; }
                    .header span { color: #ff4400; }
                    .content { padding: 30px; background: #f9f9f9; }
                    .message-box { background: white; padding: 20px; border-radius: 8px; border-left: 4px solid #ff4400; margin: 20px 0; }
                    .footer { text-align: center; padding: 20px; font-size: 12px; color: #666; }
                    .btn { display: inline-block; padding: 10px 20px; background: #ff4400; color: white; text-decoration: none; border-radius: 5px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>DELTA <span>SPORTS</span></h1>
                        <p>Xác nhận liên hệ</p>
                    </div>
                    <div class="content">
                        <h2>Xin chào %s,</h2>
                        <p>Cảm ơn bạn đã liên hệ với Delta Sports. Chúng tôi đã nhận được tin nhắn của bạn và sẽ phản hồi trong thời gian sớm nhất.</p>
                        <div class="message-box">
                            <strong>Nội dung tin nhắn của bạn:</strong>
                            <p><em>"%s"</em></p>
                        </div>
                        <p>Chúng tôi sẽ liên hệ lại với bạn qua email hoặc số điện thoại trong vòng 24 giờ làm việc.</p>
                        <br/>
                        <p>Trân trọng,<br/><strong>Delta Sports Team</strong></p>
                    </div>
                    <div class="footer">
                        <p>&copy; 2024 Delta Sports. All rights reserved.</p>
                        <p>123 Đường Thể Thao, Quận 1, TP. Hồ Chí Minh</p>
                    </div>
                </div>
            </body>
            </html>
            """, name, message);

        sendEmail(to, subject, htmlContent);
    }
    private String buildVerificationEmail(String name, String verificationUrl) {
        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <style>
                body { font-family: 'Segoe UI', Arial, sans-serif; line-height: 1.6; color: #333; }
                .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                .header { background: linear-gradient(135deg, #C41E3A 0%, #8B0000 100%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                .header h1 { margin: 0; font-size: 28px; letter-spacing: 2px; }
                .header span { font-weight: normal; opacity: 0.9; }
                .content { padding: 30px; background: #ffffff; border: 1px solid #e0e0e0; border-top: none; border-radius: 0 0 10px 10px; }
                .button { display: inline-block; padding: 14px 32px; background: #C41E3A; color: white; text-decoration: none; border-radius: 30px; margin: 20px 0; font-weight: bold; transition: all 0.3s ease; }
                .button:hover { background: #8B0000; transform: translateY(-2px); box-shadow: 0 4px 12px rgba(196,30,58,0.3); }
                .footer { text-align: center; padding: 20px; font-size: 12px; color: #999; border-top: 1px solid #e0e0e0; margin-top: 20px; }
                .warning { background: #fff3cd; padding: 12px; border-radius: 8px; border-left: 4px solid #ffc107; margin: 20px 0; font-size: 13px; }
                .logo { font-size: 24px; font-weight: bold; margin-bottom: 10px; }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header">
                    <div class="logo">⚡ DELTA SPORTS</div>
                    <h1>XÁC THỰC <span>EMAIL</span></h1>
                </div>
                <div class="content">
                    <h2>Xin chào <strong>%s</strong>!</h2>
                    <p>Cảm ơn bạn đã đăng ký tài khoản tại <strong>Delta Sports</strong>.</p>
                    <p>Để hoàn tất quá trình đăng ký và kích hoạt tài khoản, vui lòng nhấp vào nút bên dưới:</p>
                    
                    <div style="text-align: center;">
                        <a href="%s" class="button">✓ XÁC THỰC TÀI KHOẢN</a>
                    </div>
                    
                    <div class="warning">
                        <strong>⚠️ Lưu ý:</strong>
                        <ul style="margin: 10px 0 0 20px; padding: 0;">
                            <li>Link xác thực có hiệu lực trong vòng <strong>24 giờ</strong></li>
                            <li>Sau khi xác thực, bạn có thể đăng nhập và mua sắm tại Delta Sports</li>
                            <li>Nếu không xác thực, tài khoản sẽ không thể đăng nhập</li>
                        </ul>
                    </div>
                    
                    <p>Nếu nút không hoạt động, bạn có thể copy đường dẫn sau vào trình duyệt:</p>
                    <p style="word-break: break-all; background: #f5f5f5; padding: 10px; border-radius: 5px; font-size: 12px;">%s</p>
                    
                    <p>Nếu bạn không đăng ký tài khoản, vui lòng bỏ qua email này.</p>
                    
                    <hr style="margin: 30px 0 20px; border: none; border-top: 1px solid #e0e0e0;">
                    <p style="font-size: 13px; color: #666;">Mọi thắc mắc xin vui lòng liên hệ: <strong>care@delta-sports.vn</strong> hoặc Hotline: <strong>1900 1009</strong></p>
                </div>
                <div class="footer">
                    <p>&copy; 2024 Delta Sports. All rights reserved.</p>
                    <p>Đường dẫn đến thể thao đỉnh cao</p>
                </div>
            </div>
        </body>
        </html>
        """.formatted(name, verificationUrl, verificationUrl);
    }
    @Override
    public void sendHtmlEmail(String to, String subject, String templateName, Map<String, Object> variables) {
        // Implement if needed, hoặc có thể dùng Thymeleaf
        throw new UnsupportedOperationException("Not implemented yet");
    }
    @Override
    public void sendContactReply(String to, String name, String subject, String reply) {
        String emailSubject = "Phản hồi từ Delta Sports - " + subject;
        String htmlContent = String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #1a1a1a 0%, #333 100%); color: white; padding: 30px; text-align: center; }
                    .header h1 { margin: 0; font-size: 28px; }
                    .header span { color: #ff4400; }
                    .content { padding: 30px; background: #f9f9f9; }
                    .reply-box { background: white; padding: 20px; border-radius: 8px; border-left: 4px solid #ff4400; margin: 20px 0; }
                    .footer { text-align: center; padding: 20px; font-size: 12px; color: #666; }
                    .hotline { color: #ff4400; font-weight: bold; font-size: 18px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>DELTA <span>SPORTS</span></h1>
                        <p>Phản hồi liên hệ</p>
                    </div>
                    <div class="content">
                        <h2>Xin chào %s,</h2>
                        <p>Cảm ơn bạn đã quan tâm và liên hệ với Delta Sports.</p>
                        <div class="reply-box">
                            <strong>Phản hồi từ chúng tôi:</strong>
                            <p><em>"%s"</em></p>
                        </div>
                        <p>Nếu bạn cần hỗ trợ thêm, vui lòng liên hệ:</p>
                        <p>📞 Hotline: <span class="hotline">1900 1009</span></p>
                        <p>📧 Email: <a href="mailto:care@delta-sports.vn">care@delta-sports.vn</a></p>
                        <br/>
                        <p>Trân trọng,<br/><strong>Delta Sports Team</strong></p>
                    </div>
                    <div class="footer">
                        <p>&copy; 2024 Delta Sports. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """, name, reply);

        sendEmail(to, emailSubject, htmlContent);
    }
}