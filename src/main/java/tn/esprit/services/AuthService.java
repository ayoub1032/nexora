package tn.esprit.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

import tn.esprit.entities.User;
import tn.esprit.utils.DBConnection;
import tn.esprit.utils.PasswordHasher;
import tn.esprit.utils.SessionManager;
import tn.esprit.utils.UserContext;

/**
 * Service for handling user authentication
 */
public class AuthService {
    private final Connection conn;

    public AuthService() {
        conn = DBConnection.getConnection();
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
            PreparedStatement ps = conn.prepareStatement(sql);
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
                        rs.getString("created_at")
                    );

                    // Set user session
                    SessionManager.getInstance().login(user);
                    UserContext.setCurrentUserId(user.getUserId());
                    UserContext.setRole(user.getRole());

                    System.out.println("✅ Login successful for: " + email);
                    return user;
                } else {
                    System.out.println("❌ Invalid password for: " + email);
                }
            } else {
                System.out.println("❌ User not found: " + email);
            }

        } catch (SQLException e) {
            Logger.getLogger(AuthService.class.getName()).log(java.util.logging.Level.SEVERE, "Login error", e);
        }

        return null;
    }

    /**
     * Register a new user with auto-created wallet and portfolio
     * 
     * @param user User object with credentials
     * @return User object if registration successful, null otherwise
     */
    public User register(User user) {
        String checkSql = "SELECT user_id FROM users WHERE email = ?";
        String insertSql = "INSERT INTO users (full_name, email, password_hash, role, verification_status, account_status) VALUES (?, ?, ?, ?, ?, ?)";
        String insertWalletSql = "INSERT INTO wallet (user_id, balance, reserved_balance) VALUES (?, 0.0000, 0.0000)";
        String insertPortfolioSql = "INSERT INTO portfolio (user_id, total_value) VALUES (?, 0.0000)";

        try {
            // Check if email already exists (before starting transaction)
            try (PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
                checkPs.setString(1, user.getEmail());
                try (ResultSet rs = checkPs.executeQuery()) {
                    if (rs.next()) {
                        System.out.println("❌ Email already registered: " + user.getEmail());
                        return null;
                    }
                }
            }

            // Hash password
            String hashedPassword = PasswordHasher.hash(user.getPasswordHash());

            // Start transaction
            conn.setAutoCommit(false);
            
            long userId = -1;
            try {
                // Insert new user
                try (PreparedStatement insertPs = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                    insertPs.setString(1, user.getFullName());
                    insertPs.setString(2, user.getEmail());
                    insertPs.setString(3, hashedPassword);
                    insertPs.setString(4, user.getRole() != null ? user.getRole() : "USER");
                    insertPs.setString(5, "UNVERIFIED");
                    insertPs.setString(6, "ACTIVE");
                    insertPs.executeUpdate();

                    try (ResultSet generatedKeys = insertPs.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            userId = generatedKeys.getLong(1);
                            user.setUserId(userId);
                            user.setPasswordHash(hashedPassword);
                        }
                    }
                }

                if (userId == -1) {
                    conn.rollback();
                    conn.setAutoCommit(true);
                    System.out.println("❌ Failed to get generated user ID");
                    return null;
                }

                // Auto-create wallet for new user
                try (PreparedStatement walletPs = conn.prepareStatement(insertWalletSql)) {
                    walletPs.setLong(1, userId);
                    walletPs.executeUpdate();
                }

                // Auto-create portfolio for new user
                try (PreparedStatement portfolioPs = conn.prepareStatement(insertPortfolioSql)) {
                    portfolioPs.setLong(1, userId);
                    portfolioPs.executeUpdate();
                }

                // Commit transaction if everything succeeded
                conn.commit();
                conn.setAutoCommit(true);

                System.out.println("✅ Registration successful for: " + user.getEmail() + " (ID: " + userId + ")");
                return user;

            } catch (SQLException e) {
                // Rollback on any error during transaction
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    Logger.getLogger(AuthService.class.getName()).log(java.util.logging.Level.SEVERE, "Rollback error", rollbackEx);
                }
                throw e; // Re-throw to be caught by outer catch
            } finally {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException ex) {
                    Logger.getLogger(AuthService.class.getName()).log(java.util.logging.Level.SEVERE, "setAutoCommit error", ex);
                }
            }

        } catch (SQLException e) {
            Logger.getLogger(AuthService.class.getName()).log(java.util.logging.Level.SEVERE, "Registration error", e);
        }

        return null;
    }

    /**
     * Logout the current user
     */
    public void logout() {
        SessionManager.getInstance().logout();
        UserContext.setCurrentUserId(0);
        UserContext.setRole(null);
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
     * Get currently logged in user
     * 
     * @return Current user or null if not authenticated
     */
    public User getCurrentUser() {
        return SessionManager.getInstance().getCurrentUser();
    }

    /**
     * Update user password
     * 
     * @param userId    User ID
     * @param oldPassword Old password (for verification)
     * @param newPassword New password
     * @return true if password was updated successfully
     */
    public boolean updatePassword(long userId, String oldPassword, String newPassword) {
        String selectSql = "SELECT password_hash FROM users WHERE user_id = ?";
        String updateSql = "UPDATE users SET password_hash = ? WHERE user_id = ?";

        try {
            // Verify old password
            PreparedStatement selectPs = conn.prepareStatement(selectSql);
            selectPs.setLong(1, userId);
            ResultSet rs = selectPs.executeQuery();

            if (!rs.next()) {
                System.out.println("❌ User not found");
                return false;
            }

            String storedHash = rs.getString("password_hash");
            if (!PasswordHasher.verify(oldPassword, storedHash)) {
                System.out.println("❌ Old password is incorrect");
                return false;
            }

            // Update with new password
            String newHash = PasswordHasher.hash(newPassword);
            PreparedStatement updatePs = conn.prepareStatement(updateSql);
            updatePs.setString(1, newHash);
            updatePs.setLong(2, userId);
            updatePs.executeUpdate();

            System.out.println("✅ Password updated successfully");
            return true;

        } catch (SQLException e) {
            Logger.getLogger(AuthService.class.getName()).log(java.util.logging.Level.SEVERE, "Password update error", e);
        }

        return false;
    }
}
