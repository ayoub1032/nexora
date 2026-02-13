package tn.nexora.utils;

import java.time.LocalDate;
import java.time.Period;
import java.util.regex.Pattern;

/**
 * Utility class for validating user inputs
 */
public class ValidationUtils {

    // Email regex pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    // Phone number pattern (international format)
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^\\+?[1-9]\\d{1,14}$");

    /**
     * Validate email format
     * 
     * @param email Email to validate
     * @return true if email is valid
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Validate password strength
     * Requirements:
     * - Minimum 8 characters
     * - At least 1 uppercase letter
     * - At least 1 lowercase letter
     * - At least 1 digit
     * - At least 1 special character
     * 
     * @param password Password to validate
     * @return true if password meets requirements
     */
    public static boolean isStrongPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }

        boolean hasUppercase = false;
        boolean hasLowercase = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c))
                hasUppercase = true;
            else if (Character.isLowerCase(c))
                hasLowercase = true;
            else if (Character.isDigit(c))
                hasDigit = true;
            else
                hasSpecial = true;
        }

        return hasUppercase && hasLowercase && hasDigit && hasSpecial;
    }

    /**
     * Validate if user is 18 years or older
     * 
     * @param birthDate Date of birth
     * @return true if user is 18+
     */
    public static boolean is18Plus(LocalDate birthDate) {
        if (birthDate == null) {
            return false;
        }
        LocalDate today = LocalDate.now();
        Period age = Period.between(birthDate, today);
        return age.getYears() >= 18;
    }

    /**
     * Validate phone number format (international)
     * 
     * @param phone Phone number to validate
     * @return true if phone is valid
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        // Remove spaces and dashes for validation
        String cleanPhone = phone.replaceAll("[\\s-]", "");
        return PHONE_PATTERN.matcher(cleanPhone).matches();
    }

    /**
     * Validate if string is not null or empty
     * 
     * @param value String to validate
     * @return true if string is not null/empty
     */
    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
