package com.hoangdinh.delta_shop_app.service.impl;

import com.hoangdinh.delta_shop_app.dto.request.auth.*;
import com.hoangdinh.delta_shop_app.dto.request.auth.ChangePasswordRequest;
import com.hoangdinh.delta_shop_app.dto.response.auth.AuthResponse;
import com.hoangdinh.delta_shop_app.entity.RefreshToken;
import com.hoangdinh.delta_shop_app.entity.User;
import com.hoangdinh.delta_shop_app.entity.VerificationToken;
import com.hoangdinh.delta_shop_app.enums.UserRole;
import com.hoangdinh.delta_shop_app.enums.UserStatus;
import com.hoangdinh.delta_shop_app.exception.BusinessException;
import com.hoangdinh.delta_shop_app.exception.ResourceNotFoundException;
import com.hoangdinh.delta_shop_app.repository.RefreshTokenRepository;
import com.hoangdinh.delta_shop_app.repository.UserRepository;
import com.hoangdinh.delta_shop_app.repository.VerificationTokenRepository;
import com.hoangdinh.delta_shop_app.security.jwt.JwtService;
import com.hoangdinh.delta_shop_app.service.AuthService;
import com.hoangdinh.delta_shop_app.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private static final String EMAIL_VERIFICATION_TYPE = "EMAIL_VERIFICATION";
    private static final String PASSWORD_RESET_TYPE = "PASSWORD_RESET";
    private static final SecureRandom OTP_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final EmailService emailService;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("=== START REGISTRATION for email: {} ===", request.getEmail());

        // Validate if email exists
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new BusinessException("Email đã được đăng ký");
        }

        // Validate if phone exists
        if (request.getPhone() != null && userRepository.existsByPhone(request.getPhone())) {
            throw new BusinessException("Số điện thoại đã được đăng ký");
        }

        // Validate passwords match
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException("Mật khẩu xác nhận không khớp");
        }

        // Validate password strength
        if (!isValidPassword(request.getPassword())) {
            throw new BusinessException("Mật khẩu phải có ít nhất 6 ký tự, bao gồm chữ hoa, chữ thường và số");
        }

        // Create user with PENDING status
        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .role(UserRole.CUSTOMER)
                .status(UserStatus.PENDING_VERIFICATION)
                .emailVerified(false)
                .phoneVerified(false)
                .loyaltyPoints(0)
                .totalSpent(BigDecimal.ZERO)
                .createdAt(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(user);
        log.info("User registered with PENDING status: {}", savedUser.getEmail());

        String otp = generateOtp();
        VerificationToken token = VerificationToken.builder()
                .token(otp)
                .user(savedUser)
                .type(EMAIL_VERIFICATION_TYPE)
                .expiryDate(LocalDateTime.now().plusHours(24))
                .build();
        verificationTokenRepository.save(token);

        // Send verification email
        try {
            emailService.sendEmailVerification(
                    savedUser.getEmail(),
                    savedUser.getFirstName(),
                    otp
            );
            log.info("Verification email sent to: {}", savedUser.getEmail());
        } catch (Exception e) {
            log.error("Failed to send verification email to {}", savedUser.getEmail(), e);
            throw new BusinessException("Không thể gửi email xác thực. Vui lòng kiểm tra cấu hình SMTP và thử lại.");
        }

        // Return response without tokens
        return AuthResponse.forRegistration(
                savedUser,
                "Đăng ký thành công! Vui lòng kiểm tra email để xác thực tài khoản.",
                true
        );
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request, String ipAddress, String userAgent) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail().toLowerCase(), request.getPassword()));
        } catch (DisabledException e) {
            throw new BusinessException("Tài khoản chưa được kích hoạt hoặc đã bị vô hiệu hóa. Vui lòng kiểm tra email xác thực hoặc liên hệ quản trị viên.");
        } catch (BadCredentialsException e) {
            throw new BusinessException("Email hoặc mật khẩu không chính xác");
        }

        User user = userRepository.findByEmailIgnoreCase(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", request.getEmail()));

        if (user.getStatus() == UserStatus.BANNED) {
            throw new BusinessException("Tài khoản đã bị khóa. Vui lòng liên hệ hỗ trợ.");
        }

        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new BusinessException("Tài khoản chưa được kích hoạt.");
        }

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        refreshTokenRepository.revokeAllByUserId(user.getId());

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = saveRefreshToken(user, ipAddress, userAgent);

        log.info("User logged in: {} from {}", user.getEmail(), ipAddress);
        return buildAuthResponse(user, accessToken, refreshToken);
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken token = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new BusinessException("Refresh token không hợp lệ"));

        if (token.isRevoked()) {
            throw new BusinessException("Refresh token đã bị thu hồi");
        }

        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Refresh token đã hết hạn");
        }

        User user = token.getUser();
        token.setRevoked(true);
        refreshTokenRepository.save(token);

        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = saveRefreshToken(user);

        return buildAuthResponse(user, newAccessToken, newRefreshToken);
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        refreshTokenRepository.findByToken(refreshToken)
                .ifPresent(token -> {
                    token.setRevoked(true);
                    refreshTokenRepository.save(token);
                    log.info("User logged out: {}", token.getUser().getEmail());
                });
    }
    private boolean isValidPassword(String password) {
        if (password == null || password.length() < 6) return false;
        // Kiểm tra mật khẩu phải có ít nhất 1 chữ hoa, 1 chữ thường, 1 số
        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasDigit = password.matches(".*[0-9].*");
        return hasUpper && hasLower && hasDigit;
    }
    @Override
    @Transactional
    public void verifyEmail(VerifyEmailRequest request) {
        log.info("=== START VERIFY EMAIL ===");

        User user = userRepository.findByEmailIgnoreCase(request.getEmail())
                .orElseThrow(() -> new BusinessException("Email không tồn tại"));

        VerificationToken verificationToken = verificationTokenRepository.findByToken(request.getOtp())
                .orElseThrow(() -> new BusinessException("Mã OTP không hợp lệ"));

        if (!verificationToken.getUser().getId().equals(user.getId()) || !EMAIL_VERIFICATION_TYPE.equals(verificationToken.getType())) {
            throw new BusinessException("Mã OTP không hợp lệ cho tài khoản này");
        }

        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Mã OTP đã hết hạn. Vui lòng yêu cầu gửi lại email xác thực.");
        }

        if (user.isEmailVerified()) {
            throw new BusinessException("Email đã được xác thực trước đó");
        }

        // Update user status
        user.setEmailVerified(true);
        user.setStatus(UserStatus.ACTIVE);  // Change from PENDING to ACTIVE
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        // Delete used token
        verificationTokenRepository.delete(verificationToken);

        log.info("Email verified successfully for user: {}", user.getEmail());
    }

    @Override
    @Transactional
    public void requestPasswordReset(ForgotPasswordRequest request) {
        userRepository.findByEmailIgnoreCase(request.getEmail()).ifPresent(user -> {
            // Invalidate old password reset tokens
            verificationTokenRepository.deleteByUserIdAndType(user.getId(), PASSWORD_RESET_TYPE);

            String otp = generateOtp();
            
            VerificationToken vt = VerificationToken.builder()
                    .user(user)
                    .token(otp)
                    .type(PASSWORD_RESET_TYPE)
                    .expiryDate(LocalDateTime.now().plusMinutes(15))
                    .build();
            verificationTokenRepository.save(vt);
            emailService.sendPasswordReset(user.getEmail(), user.getFirstName(), otp);
            log.info("Password reset OTP requested for user: {}", user.getEmail());
        });
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.getEmail())
                .orElseThrow(() -> new BusinessException("Email không tồn tại"));

        VerificationToken vt = verificationTokenRepository.findByToken(request.getOtp())
                .orElseThrow(() -> new BusinessException("Mã OTP không hợp lệ"));

        if (!vt.getUser().getId().equals(user.getId()) || !PASSWORD_RESET_TYPE.equals(vt.getType())) {
            throw new BusinessException("Mã OTP không hợp lệ cho tài khoản này");
        }

        if (vt.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Mã OTP đã hết hạn");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        
        verificationTokenRepository.delete(vt);
        refreshTokenRepository.revokeAllByUserId(user.getId());
        
        log.info("Password reset successfully for user: {}", user.getEmail());
    }

    @Override
    @Transactional
    public void resendVerificationEmail(String email) {
        log.info("Resending verification email to: {}", email);

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BusinessException("Email không tồn tại"));

        if (user.isEmailVerified()) {
            throw new BusinessException("Email đã được xác thực");
        }

        verificationTokenRepository.deleteByUserIdAndType(user.getId(), EMAIL_VERIFICATION_TYPE);

        String otp = generateOtp();
        VerificationToken verificationToken = VerificationToken.builder()
                .token(otp)
                .user(user)
                .type(EMAIL_VERIFICATION_TYPE)
                .expiryDate(LocalDateTime.now().plusHours(24))
                .build();
        verificationTokenRepository.save(verificationToken);

        emailService.sendEmailVerification(user.getEmail(), user.getFirstName(), otp);

        log.info("New verification email sent to: {}", email);
    }
    @Override
    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException("Mật khẩu xác nhận không khớp");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BusinessException("Mật khẩu cũ không chính xác");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        refreshTokenRepository.revokeAllByUserId(userId);
        log.info("Password changed for user: {}", user.getEmail());
    }

    @Override
    public UserDetails verifyToken(String token) {
        String username = jwtService.extractUsername(token);
        if (username != null && jwtService.isTokenValid(token, userDetailsService.loadUserByUsername(username))) {
            return userDetailsService.loadUserByUsername(username);
        }
        throw new BusinessException("Token không hợp lệ hoặc đã hết hạn");
    }

    @Override
    public User getCurrentUser(String token) {
        String username = jwtService.extractUsername(token);
        return userRepository.findByEmailIgnoreCase(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", username));
    }

    @Override
    public boolean validateRefreshToken(String refreshToken) {
        return refreshTokenRepository.findByToken(refreshToken)
                .map(token -> !token.isRevoked() && token.getExpiresAt().isAfter(LocalDateTime.now()))
                .orElse(false);
    }

    @Override
    @Transactional
    public void revokeAllUserSessions(UUID userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
        log.info("All sessions revoked for user: {}", userId);
    }

    private String saveRefreshToken(User user) {
        return saveRefreshToken(user, null, null);
    }

    private String saveRefreshToken(User user, String ipAddress, String userAgent) {
        String token = jwtService.generateRefreshToken(user);

        InetAddress inetAddress = null;
        if (ipAddress != null && !ipAddress.isEmpty()) {
            try {
                inetAddress = InetAddress.getByName(ipAddress);
            } catch (UnknownHostException e) {
                log.warn("Could not parse IP address: {}", ipAddress);
            }
        }

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(token)
                .expiresAt(LocalDateTime.now().plusSeconds(jwtService.getRefreshExpirationTime() / 1000))
                .ipAddress(inetAddress)
                .userAgent(userAgent)
                .revoked(false)
                .build();
        refreshTokenRepository.save(refreshToken);
        return token;
    }

    private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationTime() / 1000)
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole().name())
                .emailVerified(user.isEmailVerified())
                .build();
    }

    private String generateOtp() {
        return String.format("%06d", OTP_RANDOM.nextInt(1_000_000));
    }
}
