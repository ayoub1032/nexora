package tn.esprit.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import tn.esprit.entities.User;
import tn.esprit.utils.DBConnection;

public class UserService {
    private final Connection conn;

    public UserService() {
        conn = DBConnection.getConnection();
    }

    // CREATE
    public long addUser(User user) {
        String sql = "INSERT INTO users (full_name, email, password_hash, role, verification_status, account_status, face_id_data) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPasswordHash());
            ps.setString(4, user.getRole());
            ps.setString(5, user.getVerificationStatus() != null ? user.getVerificationStatus() : "UNVERIFIED");
            ps.setString(6, user.getAccountStatus() != null ? user.getAccountStatus() : "ACTIVE");
            ps.setString(7, user.getFaceIdData());

            ps.executeUpdate();
            
            // Get the generated ID
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error adding user: " + e.getMessage(), e);
        }
        return -1;
    }

    // READ ALL
    public List<User> getAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY user_id DESC";
        
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                User user = mapResultSetToUser(rs);
                users.add(user);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching users: " + e.getMessage(), e);
        }
        return users;
    }

    // READ BY ID
    public User findById(long userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding user: " + e.getMessage(), e);
        }
        return null;
    }

    // READ BY EMAIL
    public User findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding user by email: " + e.getMessage(), e);
        }
        return null;
    }

    // UPDATE
    public void update(User user) {
        String sql = "UPDATE users SET full_name=?, email=?, password_hash=?, role=?, " +
                     "verification_status=?, account_status=?, face_id_data=? WHERE user_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPasswordHash());
            ps.setString(4, user.getRole());
            ps.setString(5, user.getVerificationStatus());
            ps.setString(6, user.getAccountStatus());
            ps.setString(7, user.getFaceIdData());
            ps.setLong(8, user.getUserId());

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating user: " + e.getMessage(), e);
        }
    }

    // DELETE
    public void delete(long userId) {
        String sql = "DELETE FROM users WHERE user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting user: " + e.getMessage(), e);
        }
    }

    // Helper method to map ResultSet to User
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        return new User(
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
    }
}
