package com.hoangdinh.delta_shop_app.service.impl;

import com.hoangdinh.delta_shop_app.dto.request.auth.RegisterRequest;
import com.hoangdinh.delta_shop_app.entity.User;
import com.hoangdinh.delta_shop_app.entity.VerificationToken;
import com.hoangdinh.delta_shop_app.exception.BusinessException;
import com.hoangdinh.delta_shop_app.repository.RefreshTokenRepository;
import com.hoangdinh.delta_shop_app.repository.UserRepository;
import com.hoangdinh.delta_shop_app.repository.VerificationTokenRepository;
import com.hoangdinh.delta_shop_app.security.jwt.JwtService;
import com.hoangdinh.delta_shop_app.service.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private VerificationTokenRepository verificationTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void registerThrowsWhenVerificationEmailCannotBeSent() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("customer@example.com");
        request.setPassword("Password1");
        request.setConfirmPassword("Password1");
        request.setFirstName("Customer");
        request.setLastName("One");
        request.setPhone("0912345678");

        when(userRepository.existsByEmailIgnoreCase(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(UUID.randomUUID());
            return user;
        });
        when(verificationTokenRepository.save(any(VerificationToken.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doThrow(new RuntimeException("SMTP failed"))
                .when(emailService)
                .sendEmailVerification(anyString(), anyString(), anyString());

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Không thể gửi email xác thực");
    }

    @Test
    void resendVerificationEmailCreatesSixDigitOtpToken() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("customer@example.com")
                .firstName("Customer")
                .emailVerified(false)
                .build();
        ArgumentCaptor<VerificationToken> tokenCaptor = ArgumentCaptor.forClass(VerificationToken.class);

        when(userRepository.findByEmailIgnoreCase(user.getEmail())).thenReturn(Optional.of(user));
        when(verificationTokenRepository.save(tokenCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        authService.resendVerificationEmail(user.getEmail());

        VerificationToken token = tokenCaptor.getValue();
        assertThat(token.getToken()).matches("\\d{6}");
        assertThat(token.getType()).isEqualTo("EMAIL_VERIFICATION");
        assertThat(token.getUser()).isEqualTo(user);
        verify(verificationTokenRepository).deleteByUserIdAndType(user.getId(), "EMAIL_VERIFICATION");
        verify(emailService).sendEmailVerification(user.getEmail(), user.getFirstName(), token.getToken());
    }
}
