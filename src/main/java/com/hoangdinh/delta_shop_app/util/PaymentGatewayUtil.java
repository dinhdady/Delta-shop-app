package com.hoangdinh.delta_shop_app.util;

import com.hoangdinh.delta_shop_app.dto.response.payment.PaymentUrlResponse;
import com.hoangdinh.delta_shop_app.entity.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentGatewayUtil {

    @Value("${app.vnpay.tmn-code:}")
    private String vnpayTmnCode;

    @Value("${app.vnpay.hash-secret:}")
    private String vnpayHashSecret;

    @Value("${app.vnpay.url:https://sandbox.vnpayment.vn/paymentv2/vpcpay.html}")
    private String vnpayUrl;

    @Value("${app.vnpay.return-url:http://localhost:8080/api/vnpay/return}")
    private String defaultVnpayReturnUrl;

    @Value("${app.momo.partner-code:}")
    private String momoPartnerCode;

    @Value("${app.momo.access-key:}")
    private String momoAccessKey;

    @Value("${app.momo.secret-key:}")
    private String momoSecretKey;

    @Value("${app.momo.url:https://test-payment.momo.vn/v2/gateway/api/create}")
    private String momoUrl;

    public PaymentUrlResponse generateVNPayUrl(Payment payment, String returnUrl, String bankCode) {
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String vnp_TxnRef = payment.getTransactionNo();
        String vnp_IpAddr = "127.0.0.1";
        String vnp_TmnCode = vnpayTmnCode;
        String vnp_OrderInfo = "Thanh toan don hang " + payment.getOrder().getOrderNumber();
        String vnp_OrderType = "other";
        String vnp_Amount = String.valueOf(payment.getAmount().multiply(BigDecimal.valueOf(100)).longValue());
        String vnp_Locale = "vn";
        String vnp_ReturnUrl = (returnUrl != null && !returnUrl.isBlank()) ? returnUrl : defaultVnpayReturnUrl;
        String vnp_CreateDate = payment.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", vnp_Amount);
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", vnp_OrderInfo);
        vnp_Params.put("vnp_OrderType", vnp_OrderType);
        vnp_Params.put("vnp_Locale", vnp_Locale);
        vnp_Params.put("vnp_ReturnUrl", vnp_ReturnUrl);
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        if (bankCode != null && !bankCode.isEmpty()) {
            vnp_Params.put("vnp_BankCode", bankCode);
        }

        String hashData = buildVnpayHashData(vnp_Params);
        String query = buildVnpayQuery(vnp_Params);

        String vnp_SecureHash = hmacSHA512(vnpayHashSecret, hashData);
        String paymentUrl = vnpayUrl + "?" + query + "&vnp_SecureHashType=SHA512&vnp_SecureHash=" + vnp_SecureHash;

        return PaymentUrlResponse.builder()
                .paymentUrl(paymentUrl)
                .paymentId(payment.getId().toString())
                .orderId(payment.getOrder().getId())
                .orderNumber(payment.getOrder().getOrderNumber())
                .gateway("VNPAY")
                .amount(payment.getAmount())
                .transactionNo(payment.getTransactionNo())
                .build();
    }

    public PaymentUrlResponse generateMoMoUrl(Payment payment, String returnUrl, String cancelUrl) {
        String requestId = UUID.randomUUID().toString();
        String orderId = payment.getTransactionNo();
        String amount = String.valueOf(payment.getAmount().longValue());
        String orderInfo = "Thanh toan don hang " + payment.getOrder().getOrderNumber();
        String extraData = "";
        String requestType = "captureWallet";

        String rawHash = "partnerCode=" + momoPartnerCode +
                "&accessKey=" + momoAccessKey +
                "&requestId=" + requestId +
                "&amount=" + amount +
                "&orderId=" + orderId +
                "&orderInfo=" + orderInfo +
                "&returnUrl=" + returnUrl +
                "&notifyUrl=" + cancelUrl +
                "&extraData=" + extraData;

        String signature = hmacSHA256(momoSecretKey, rawHash);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("partnerCode", momoPartnerCode);
        requestBody.put("accessKey", momoAccessKey);
        requestBody.put("requestId", requestId);
        requestBody.put("amount", amount);
        requestBody.put("orderId", orderId);
        requestBody.put("orderInfo", orderInfo);
        requestBody.put("returnUrl", returnUrl);
        requestBody.put("notifyUrl", cancelUrl);
        requestBody.put("extraData", extraData);
        requestBody.put("requestType", requestType);
        requestBody.put("signature", signature);

        // In production, call MoMo API here
        String paymentUrl = momoUrl + "?orderId=" + orderId;

        return PaymentUrlResponse.builder()
                .paymentUrl(paymentUrl)
                .paymentId(payment.getId().toString())
                .orderId(payment.getOrder().getId())
                .orderNumber(payment.getOrder().getOrderNumber())
                .gateway("MOMO")
                .amount(payment.getAmount())
                .transactionNo(payment.getTransactionNo())
                .build();
    }

    public boolean verifySignature(Map<String, String> params, String gateway) {
        if ("VNPAY".equalsIgnoreCase(gateway)) {
            Map<String, String> signedParams = new HashMap<>(params);
            String vnp_SecureHash = signedParams.remove("vnp_SecureHash");
            signedParams.remove("vnp_SecureHashType");
            if (vnp_SecureHash == null || vnp_SecureHash.isBlank()) {
                return false;
            }

            String calculatedHash = hmacSHA512(vnpayHashSecret, buildVnpayHashData(signedParams));
            return calculatedHash.equalsIgnoreCase(vnp_SecureHash);
        }
        return true;
    }

    public boolean processRefund(Payment payment, BigDecimal amount, String reason) {
        // Implement gateway-specific refund logic
        log.info("Processing refund for payment: {}, amount: {}, reason: {}",
                payment.getTransactionNo(), amount, reason);
        return true;
    }

    private String hmacSHA512(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            mac.init(secretKeySpec);
            byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(rawHmac);
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate HMAC SHA-512", e);
        }
    }

    private String hmacSHA256(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(rawHmac);
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate HMAC SHA-256", e);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private String buildVnpayHashData(Map<String, String> params) {
        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);

        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (String fieldName : fieldNames) {
            String fieldValue = params.get(fieldName);
            if (fieldValue == null || fieldValue.isEmpty()) {
                continue;
            }
            if (!first) {
                builder.append('&');
            }
            builder.append(fieldName)
                    .append('=')
                    .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
            first = false;
        }
        return builder.toString();
    }

    private String buildVnpayQuery(Map<String, String> params) {
        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);

        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (String fieldName : fieldNames) {
            String fieldValue = params.get(fieldName);
            if (fieldValue == null || fieldValue.isEmpty()) {
                continue;
            }
            if (!first) {
                builder.append('&');
            }
            builder.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII))
                    .append('=')
                    .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
            first = false;
        }
        return builder.toString();
    }
}
