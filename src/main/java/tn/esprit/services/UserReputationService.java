package tn.esprit.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import tn.esprit.entities.UserReputation;
import tn.esprit.utils.DBConnection;

/**
 * Service for managing P2P user reputation (completed/canceled contracts and
 * star ratings).
 */
public class UserReputationService {

    /**
     * Gets the reputation record for a user, creating one if it doesn't exist.
     */
    public UserReputation getOrCreateReputation(long userId) {
        String selectSql = "SELECT * FROM user_reputation WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(selectSql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new UserReputation(
                            rs.getLong("reputation_id"),
                            rs.getLong("user_id"),
                            rs.getInt("completed_contracts"),
                            rs.getInt("canceled_contracts"),
                            rs.getInt("total_score"),
                            rs.getInt("rating_count"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching reputation: " + e.getMessage());
        }

        // Row didn't exist — create one
        String insertSql = "INSERT IGNORE INTO user_reputation (user_id) VALUES (?)";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(insertSql)) {
            ps.setLong(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error creating reputation row: " + e.getMessage());
        }

        return new UserReputation(0, userId, 0, 0, 0, 0);
    }

    /**
     * Increments completed contracts counter for a user.
     */
    public void incrementCompleted(long userId) {
        String sql = "UPDATE user_reputation SET completed_contracts = completed_contracts + 1 WHERE user_id = ?";
        executeUpdate(sql, userId);
    }

    /**
     * Increments canceled contracts counter for a user.
     */
    public void incrementCanceled(long userId) {
        String sql = "UPDATE user_reputation SET canceled_contracts = canceled_contracts + 1 WHERE user_id = ?";
        executeUpdate(sql, userId);
    }

    /**
     * Records a star rating (1–5) from one user to another.
     */
    public void addRating(long ratedUserId, int stars) {
        if (stars < 1 || stars > 5) {
            throw new IllegalArgumentException("Stars must be between 1 and 5");
        }
        String sql = "UPDATE user_reputation SET total_score = total_score + ?, rating_count = rating_count + 1 WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, stars);
            ps.setLong(2, ratedUserId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error adding rating: " + e.getMessage());
        }
    }

    private void executeUpdate(String sql, long userId) {
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating reputation: " + e.getMessage());
        }
    }

    /**
     * Returns a user-friendly reputation label for display in the marketplace
     * table.
     * Example: "⭐ 4.5 | 12 ✔ | 1 ✗"
     */
    public String getReputationLabel(long userId) {
        UserReputation rep = getOrCreateReputation(userId);
        String stars = rep.getRatingCount() == 0
                ? "Not Rated"
                : String.format("⭐ %.1f", rep.getAverageRating());
        return stars + " | " + rep.getCompletedContracts() + " ✔ | " + rep.getCanceledContracts() + " ✗";
    }
}
