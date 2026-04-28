package com.hoangdinh.delta_shop_app.security;

import com.hoangdinh.delta_shop_app.entity.User;
import com.hoangdinh.delta_shop_app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("permissionEvaluator")
@RequiredArgsConstructor
public class PermissionEvaluator {

    private final UserRepository userRepository;

    public boolean canAccessUser(Authentication auth, UUID userId) {
        // SUPER_ADMIN có thể truy cập tất cả
        if (hasRole(auth, "SUPER_ADMIN")) {
            return true;
        }

        // ADMIN có thể truy cập tất cả users
        if (hasRole(auth, "ADMIN")) {
            return true;
        }

        // CUSTOMER chỉ được truy cập thông tin của chính mình
        String currentUserEmail = auth.getName();
        String targetUserEmail = getEmailByUserId(userId);

        return currentUserEmail.equals(targetUserEmail);
    }

    public boolean isSuperAdmin(Authentication auth) {
        return hasRole(auth, "SUPER_ADMIN");
    }

    public boolean isAdminOrSuperAdmin(Authentication auth) {
        return hasRole(auth, "ADMIN") || hasRole(auth, "SUPER_ADMIN");
    }

    private boolean hasRole(Authentication auth, String role) {
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> authority.equals("ROLE_" + role));
    }

    private String getEmailByUserId(UUID userId) {
        return userRepository.findById(userId)
                .map(User::getEmail)
                .orElse(null);
    }
}