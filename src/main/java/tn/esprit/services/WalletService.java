package tn.esprit.services;

import tn.esprit.entities.Wallet;
import tn.esprit.utils.DBConnection;

import java.sql.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class WalletService {

    public WalletService() {
    }

    // CREATE - Auto-called when user is created
    public long createWallet(long userId) {
        String sql = "INSERT INTO wallet (user_id, balance, reserved_balance) VALUES (?, 0, 0)";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, userId);
            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error creating wallet: " + e.getMessage(), e);
        }
        return -1;
    }

    // READ BY WALLET ID
    public Wallet findById(long walletId) {
        String sql = "SELECT * FROM wallet WHERE wallet_id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, walletId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToWallet(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding wallet: " + e.getMessage(), e);
        }
        return null;
    }

    // READ BY USER ID
    public Wallet findByUserId(long userId) {
        String sql = "SELECT * FROM wallet WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToWallet(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding wallet by user_id: " + e.getMessage(), e);
        }
        return null;
    }

    // READ ALL
    public List<Wallet> getAll() {
        List<Wallet> wallets = new ArrayList<>();
        String sql = "SELECT * FROM wallet ORDER BY wallet_id DESC";
        try (Connection conn = DBConnection.getConnection();
                Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                wallets.add(mapResultSetToWallet(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching wallets: " + e.getMessage(), e);
        }
        return wallets;
    }

    // UPDATE BALANCE
    public void updateBalance(long walletId, BigDecimal balance) {
        String sql = "UPDATE wallet SET balance = ? WHERE wallet_id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, balance);
            ps.setLong(2, walletId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating balance: " + e.getMessage(), e);
        }
    }

    // UPDATE RESERVED BALANCE
    public void updateReservedBalance(long walletId, BigDecimal reservedBalance) {
        String sql = "UPDATE wallet SET reserved_balance = ? WHERE wallet_id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, reservedBalance);
            ps.setLong(2, walletId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating reserved balance: " + e.getMessage(), e);
        }
    }

    // DELETE - Called when user is deleted
    public void deleteByUserId(long userId) {
        String sql = "DELETE FROM wallet WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting wallet: " + e.getMessage(), e);
        }
    }

    // Helper method to map ResultSet to Wallet
    private Wallet mapResultSetToWallet(ResultSet rs) throws SQLException {
        return new Wallet(
                rs.getLong("wallet_id"),
                rs.getLong("user_id"),
                rs.getBigDecimal("balance"),
                rs.getBigDecimal("reserved_balance"),
                rs.getString("created_at"));
    }
}
