package tn.nexora.services;

import tn.nexora.entities.User;
import tn.nexora.utils.MyConnection;
import tn.nexora.utils.PasswordHasher;
import tn.nexora.utils.SessionManager;

import java.sql.*;

/**
 * Service for handling user authentication
 */
public class AuthService {

    private Connection connection;

    public AuthService() {
        connection = MyConnection.getInstance().getConnection();
    }

    /**
     * Authenticate a user with email and password
     * 
     * @param email         User's email
     * @param plainPassword User's plain text password
     * @return User object if authentication successful, null otherwise
     */
    public User login(String email, String plainPassword) {
        String sql = "SELECT * FROM users WHERE email = ?";

        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password_hash");

                // Verify password
                if (PasswordHasher.verify(plainPassword, storedHash)) {
                    User user = new User(
                            rs.getLong("user_id"),
                            rs.getString("full_name"),
                            rs.getString("email"),
                            rs.getString("password_hash"),
                            rs.getString("role"),
                            rs.getString("verification_status"),
                            rs.getString("account_status"),
                            rs.getString("face_id_data"),
                            rs.getTimestamp("created_at"));

                    // Set user session
                    SessionManager.getInstance().login(user);

                    // Record login history
                    recordLoginHistory(user.getUser_id(), "SUCCESS");

                    System.out.println("✅ Login successful for: " + email);
                    return user;
                } else {
                    // Record failed login
                    recordLoginHistory(rs.getLong("user_id"), "FAILED");
                    System.out.println("❌ Invalid password for: " + email);
                }
            } else {
                System.out.println("❌ User not found: " + email);
            }

        } catch (SQLException e) {
            System.err.println("❌ Login error: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Logout the current user
     */
    public void logout() {
        SessionManager.getInstance().logout();
        System.out.println("🚪 User logged out");
    }

    /**
     * Check if a user is currently authenticated
     * 
     * @return true if user is logged in
     */
    public boolean isAuthenticated() {
        return SessionManager.getInstance().isAuthenticated();
    }

    /**
     * Record login attempt in login_history table
     * 
     * @param userId User ID
     * @param status SUCCESS or FAILED
     */
    private void recordLoginHistory(long userId, String status) {
        String sql = "INSERT INTO login_history (user_id, ip_address, device_info, status) VALUES (?, ?, ?, ?)";

        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setLong(1, userId);
            ps.setString(2, "127.0.0.1"); // TODO: Get real IP
            ps.setString(3, getSystemInfo());
            ps.setString(4, status);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("⚠️ Failed to record login history: " + e.getMessage());
        }
    }

    /**
     * Get system information for login history
     * 
     * @return System info string
     */
    private String getSystemInfo() {
        String os = System.getProperty("os.name");
        String version = System.getProperty("os.version");
        return os + " " + version + " (JavaFX Desktop)";
    }

    /**
     * Check if email already exists in database
     * 
     * @param email Email to check
     * @return true if email exists
     */
    public boolean emailExists(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";

        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }
}
