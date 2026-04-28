package com.hoangdinh.delta_shop_app.service.impl;

import com.hoangdinh.delta_shop_app.dto.response.auth.VerificationTokenResponse;
import com.hoangdinh.delta_shop_app.entity.User;
import com.hoangdinh.delta_shop_app.entity.VerificationToken;
import com.hoangdinh.delta_shop_app.exception.BusinessException;
import com.hoangdinh.delta_shop_app.exception.ResourceNotFoundException;
import com.hoangdinh.delta_shop_app.repository.UserRepository;
import com.hoangdinh.delta_shop_app.repository.VerificationTokenRepository;
import com.hoangdinh.delta_shop_app.service.VerificationTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class VerificationTokenServiceImpl implements VerificationTokenService {

    private final VerificationTokenRepository verificationTokenRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public String createToken(User user, String type, long expirationHours) {
        // Invalidate old tokens of same type
        verificationTokenRepository.deleteByUserIdAndType(user.getId(), type);

        String token = UUID.randomUUID().toString();
        VerificationToken vt = VerificationToken.builder()
                .user(user)
                .token(token)
                .type(type)
                .expiryDate(LocalDateTime.now().plusHours(expirationHours))
                .used(false)
                .build();

        verificationTokenRepository.save(vt);
        log.info("Created {} token for user: {}", type, user.getEmail());
        return token;
    }

    @Override
    public boolean validateToken(String token, String type) {
        return verificationTokenRepository.existsValidToken(token, type);
    }

    @Override
    public User getUserByValidToken(String token, String type) {
        VerificationToken vt = verificationTokenRepository.findByTokenAndType(token, type)
                .orElseThrow(() -> new BusinessException("Token không hợp lệ"));

        // Kiểm tra token có hợp lệ không
        if (!vt.isValid()) {
            if (vt.isExpired()) {
                throw new BusinessException("Token đã hết hạn. Vui lòng yêu cầu gửi lại token mới.");
            }
            if (vt.isUsed()) {
                throw new BusinessException("Token đã được sử dụng. Token chỉ có hiệu lực một lần.");
            }
            throw new BusinessException("Token không hợp lệ");
        }

        return vt.getUser();
    }

    @Transactional
    public User consumeToken(String token, String type) {
        VerificationToken vt = verificationTokenRepository.findByTokenAndType(token, type)
                .orElseThrow(() -> new BusinessException("Token không hợp lệ"));

        if (!vt.isValid()) {
            if (vt.isExpired()) {
                throw new BusinessException("Token đã hết hạn. Vui lòng yêu cầu gửi lại token mới.");
            }
            if (vt.isUsed()) {
                throw new BusinessException("Token đã được sử dụng.");
            }
            throw new BusinessException("Token không hợp lệ");
        }

        // Đánh dấu token đã sử dụng
        vt.markAsUsed();
        verificationTokenRepository.save(vt);

        return vt.getUser();
    }

    @Override
    @Transactional
    public void markTokenAsUsed(String token) {
        // Sửa: chỉ truyền token, không truyền LocalDateTime
        verificationTokenRepository.markAsUsed(token);
        log.info("Token marked as used: {}", token);
    }

    @Override
    @Transactional
    public int deleteExpiredTokens() {
        int deleted = verificationTokenRepository.deleteExpiredTokens(LocalDateTime.now());
        if (deleted > 0) {
            log.info("Deleted {} expired tokens", deleted);
        }
        return deleted;
    }

    @Override
    public VerificationTokenResponse getTokenInfo(String token) {
        VerificationToken vt = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Token", "token", token));
        return VerificationTokenResponse.from(vt);
    }

    @Override
    @Transactional
    public void invalidateUserTokens(UUID userId) {
        verificationTokenRepository.invalidateAllUserTokens(userId);
        log.info("Invalidated all tokens for user: {}", userId);
    }
}