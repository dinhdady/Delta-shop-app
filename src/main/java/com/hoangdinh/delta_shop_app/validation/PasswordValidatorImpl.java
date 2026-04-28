// PasswordValidatorImpl.java
package com.hoangdinh.delta_shop_app.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordValidatorImpl implements ConstraintValidator<PasswordValidator, String> {

    private PasswordValidator annotation;

    @Override
    public void initialize(PasswordValidator constraintAnnotation) {
        this.annotation = constraintAnnotation;
    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null) return false;

        if (password.length() < annotation.minLength()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "Mật khẩu phải có ít nhất " + annotation.minLength() + " ký tự"
            ).addConstraintViolation();
            return false;
        }

        if (password.length() > annotation.maxLength()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "Mật khẩu không được vượt quá " + annotation.maxLength() + " ký tự"
            ).addConstraintViolation();
            return false;
        }

        if (annotation.requireUppercase() && !password.matches(".*[A-Z].*")) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "Mật khẩu phải chứa ít nhất 1 chữ hoa"
            ).addConstraintViolation();
            return false;
        }

        if (annotation.requireLowercase() && !password.matches(".*[a-z].*")) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "Mật khẩu phải chứa ít nhất 1 chữ thường"
            ).addConstraintViolation();
            return false;
        }

        if (annotation.requireDigit() && !password.matches(".*[0-9].*")) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "Mật khẩu phải chứa ít nhất 1 số"
            ).addConstraintViolation();
            return false;
        }

        if (annotation.requireSpecialChar() && !password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "Mật khẩu phải chứa ít nhất 1 ký tự đặc biệt"
            ).addConstraintViolation();
            return false;
        }

        return true;
    }
}