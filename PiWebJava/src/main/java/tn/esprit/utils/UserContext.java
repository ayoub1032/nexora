package tn.esprit.utils;

public class UserContext {
    private static String userRole = "ADMIN"; // "ADMIN" or "USER"
    private static long currentUserId = 1; // Default user ID for user mode

    public static String getRole() {
        return userRole;
    }

    public static void setRole(String role) {
        userRole = role;
    }

    public static boolean isAdmin() {
        return "ADMIN".equals(userRole);
    }

    public static long getCurrentUserId() {
        return currentUserId;
    }

    public static void setCurrentUserId(long userId) {
        currentUserId = userId;
    }
}
