package tn.esprit.utils;

import java.util.function.UnaryOperator;

import javafx.scene.control.TextFormatter;

public class ValidationUtil {

    // digits only (for IDs)
    public static TextFormatter<String> numericLongFormatter() {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();
            return newText.matches("\\d*") ? change : null;
        };
        return new TextFormatter<>(filter);
    }

    // positive decimal formatter (for values, prices)
    public static TextFormatter<String> positiveDecimalFormatter() {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String t = change.getControlNewText();
            if (t.isEmpty()) return change;
            return t.matches("\\d{0,10}([\\.]\\d{0,2})?") ? change : null;
        };
        return new TextFormatter<>(filter);
    }

    // positive integer formatter (for quantity)
    public static TextFormatter<String> positiveIntegerFormatter() {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();
            return newText.matches("\\d*") ? change : null;
        };
        return new TextFormatter<>(filter);
    }

    public static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    // Email validation
    public static boolean isValidEmail(String email) {
        if (isEmpty(email)) return false;
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }

    // Password strength validation (8+ chars, uppercase, lowercase, digit, special char)
    public static boolean isStrongPassword(String password) {
        if (isEmpty(password) || password.length() < 8) return false;
        boolean hasUppercase = password.matches(".*[A-Z].*");
        boolean hasLowercase = password.matches(".*[a-z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecialChar = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};:'\",.<>?/\\\\|`~].*");
        return hasUppercase && hasLowercase && hasDigit && hasSpecialChar;
    }

    // Age validation (18+ years old)
    public static boolean is18Plus(java.time.LocalDate birthDate) {
        if (birthDate == null) return false;
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.Period period = java.time.Period.between(birthDate, today);
        return period.getYears() >= 18;
    }

    // Phone validation (basic international format)
    public static boolean isValidPhone(String phone) {
        if (isEmpty(phone)) return false;
        String phoneRegex = "^[+]?[0-9]{10,15}$";
        return phone.replaceAll("[\\s()-]", "").matches(phoneRegex);
    }

    // Empty string validation
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    // SQL injection prevention - sanitize input
    public static String sanitize(String input) {
        if (input == null) return "";
        // Remove potentially dangerous characters
        return input.replaceAll("[<>\"'%;()&+]", "");
    }
}
