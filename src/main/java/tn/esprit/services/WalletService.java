package tn.esprit.services;

import tn.esprit.entities.Wallet;
import tn.esprit.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

public class WalletService {

    private final Connection conn;

    public WalletService() {
        conn = DBConnection.getConnection();
    }

    // ✅ Admin: create wallet balance=0 reserved=0
    public void createWallet(long userId) {
        String sql = "INSERT INTO wallet (user_id, balance, reserved_balance) VALUES (?, 0, 0)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur createWallet (user_id unique ?): " + e.getMessage(), e);
        }
    }

    public Wallet findById(long walletId) {
        String sql = "SELECT * FROM wallet WHERE wallet_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, walletId);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return map(rs);
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findById: " + e.getMessage(), e);
        }
    }

    public List<Wallet> getAll() {
        List<Wallet> list = new ArrayList<>();
        String sql = "SELECT * FROM wallet ORDER BY wallet_id DESC";
        try (Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next())
                list.add(map(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Erreur getAll wallets: " + e.getMessage(), e);
        }
        return list;
    }

    public void delete(long walletId) {
        // Cascade delete: remove Reference IDs then Transactions first
        TransactionService txService = new TransactionService();
        txService.deleteByWallet(walletId);

        String sql = "DELETE FROM wallet WHERE wallet_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, walletId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur delete wallet: " + e.getMessage(), e);
        }
    }

    private Wallet map(ResultSet rs) throws SQLException {
        Wallet w = new Wallet();
        w.setWalletId(rs.getLong("wallet_id"));
        w.setUserId(rs.getLong("user_id"));
        w.setBalance(rs.getBigDecimal("balance"));
        w.setReservedBalance(rs.getBigDecimal("reserved_balance"));
        return w;
    }

    // ✅ compatibilité si tu gardes encore l'ancien WalletController console
    public void findAll() {
        for (Wallet w : getAll()) {
            System.out.println("wallet_id=" + w.getWalletId() + ", user_id=" + w.getUserId() +
                    ", balance=" + w.getBalance() + ", reserved=" + w.getReservedBalance());
        }
    }

    // ✅ compatibilité si tu gardes encore un add() ancien
    public void add(Wallet wallet) {
        // règle métier: balance forcée à 0
        createWallet(wallet.getUserId());
    }

    // (Optionnel) si ancien code l’appelle
    public void updateBalance(long walletId, BigDecimal balance) {
        String sql = "UPDATE wallet SET balance = ? WHERE wallet_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, balance);
            ps.setLong(2, walletId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur updateBalance: " + e.getMessage(), e);
        }
    }
}