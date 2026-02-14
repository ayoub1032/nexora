package tn.esprit.utils;

public class UserContext {
    private static String userRole = "ADMIN"; // "ADMIN" or "USER"

    public static String getRole() {
        return userRole;
    }

    public static void setRole(String role) {
        userRole = role;
    }

    public static boolean isAdmin() {
        return "ADMIN".equals(userRole);
    }
}
