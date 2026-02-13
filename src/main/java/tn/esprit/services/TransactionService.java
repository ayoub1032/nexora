package tn.esprit.services;

import tn.esprit.entities.Transaction;
import tn.esprit.utils.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionService {

    private final Connection conn;

    public TransactionService() {
        conn = DBConnection.getConnection();
    }

    // ✅ DEPOSIT (atomic)
    public void deposit(long walletId, BigDecimal amount, Long referenceId) {
        validateAmount(amount);

        String selectForUpdate = "SELECT balance FROM wallet WHERE wallet_id = ? FOR UPDATE";
        String updateWallet = "UPDATE wallet SET balance = balance + ? WHERE wallet_id = ?";
        String insertTx = """
                INSERT INTO `transaction` (wallet_id, amount, transaction_type, reference_id, status)
                VALUES (?, ?, 'DEPOSIT', ?, 'CONFIRMED')
                """;

        executeAtomic(walletId, amount, referenceId, selectForUpdate, updateWallet, insertTx, true);
    }

    // ✅ WITHDRAW (atomic + check)
    public void withdraw(long walletId, BigDecimal amount, Long referenceId) {
        validateAmount(amount);

        String selectForUpdate = "SELECT balance FROM wallet WHERE wallet_id = ? FOR UPDATE";
        String updateWallet = "UPDATE wallet SET balance = balance - ? WHERE wallet_id = ?";
        String insertTx = """
                INSERT INTO `transaction` (wallet_id, amount, transaction_type, reference_id, status)
                VALUES (?, ?, 'WITHDRAW', ?, 'CONFIRMED')
                """;

        executeAtomic(walletId, amount, referenceId, selectForUpdate, updateWallet, insertTx, false);
    }

    private void executeAtomic(long walletId, BigDecimal amount, Long referenceId,
            String selectForUpdate, String updateWallet, String insertTx,
            boolean isDeposit) {
        try {
            conn.setAutoCommit(false);

            BigDecimal balance;
            try (PreparedStatement ps = conn.prepareStatement(selectForUpdate)) {
                ps.setLong(1, walletId);
                ResultSet rs = ps.executeQuery();
                if (!rs.next())
                    throw new RuntimeException("Wallet introuvable (wallet_id=" + walletId + ")");
                balance = rs.getBigDecimal("balance");
            }

            if (!isDeposit && balance.compareTo(amount) < 0) {
                throw new RuntimeException("Solde insuffisant. Balance=" + balance + ", Withdraw=" + amount);
            }

            try (PreparedStatement ps = conn.prepareStatement(updateWallet)) {
                ps.setBigDecimal(1, amount);
                ps.setLong(2, walletId);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement(insertTx)) {
                ps.setLong(1, walletId);
                ps.setBigDecimal(2, amount);
                if (referenceId == null)
                    ps.setNull(3, Types.BIGINT);
                else
                    ps.setLong(3, referenceId);
                ps.executeUpdate();
            }

            conn.commit();

        } catch (Exception e) {
            try {
                conn.rollback();
            } catch (SQLException ignored) {
            }
            throw new RuntimeException("Erreur " + (isDeposit ? "deposit" : "withdraw") + ": " + e.getMessage(), e);
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException ignored) {
            }
        }
    }

    public List<Transaction> getAll() {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT * FROM `transaction` ORDER BY created_at DESC";
        try (Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next())
                list.add(map(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Erreur getAll transactions: " + e.getMessage(), e);
        }
        return list;
    }

    public List<Transaction> getByWallet(long walletId) {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT * FROM `transaction` WHERE wallet_id = ? ORDER BY created_at DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, walletId);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                list.add(map(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Erreur getByWallet: " + e.getMessage(), e);
        }
        return list;
    }

    private Transaction map(ResultSet rs) throws SQLException {
        Transaction t = new Transaction();
        t.setTransactionId(rs.getLong("transaction_id"));
        t.setWalletId(rs.getLong("wallet_id"));
        t.setAmount(rs.getBigDecimal("amount"));
        t.setTransactionType(rs.getString("transaction_type"));

        Object ref = rs.getObject("reference_id");
        t.setReferenceId(ref == null ? null : ((Number) ref).longValue());

        t.setStatus(rs.getString("status"));
        t.setCreatedAt(rs.getTimestamp("created_at"));
        return t;
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Montant invalide (doit être > 0).");
        }
    }

    // ✅ compatibilité si tu gardes des anciens controllers
    public List<Transaction> findByWallet(long walletId) {
        return getByWallet(walletId);
    }

    public void updateStatus(long transactionId, String status) {
        if (status == null)
            throw new RuntimeException("Status null.");
        status = status.toUpperCase().trim();
        if (!status.equals("CONFIRMED") && !status.equals("CANCELED")) {
            throw new RuntimeException("Status invalide (CONFIRMED/CANCELED).");
        }
        String sql = "UPDATE `transaction` SET status = ? WHERE transaction_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setLong(2, transactionId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur updateStatus: " + e.getMessage(), e);
        }
    }

    public void delete(long transactionId) {
        // First delete any transactions that reference this one (e.g. transfers)
        String sqlDependencies = "DELETE FROM `transaction` WHERE reference_id = ?";
        String sql = "DELETE FROM `transaction` WHERE transaction_id = ?";

        try {
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(sqlDependencies)) {
                ps.setLong(1, transactionId);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, transactionId);
                ps.executeUpdate();
            }

            conn.commit();
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ignored) {
            }
            throw new RuntimeException("Erreur delete transaction (et dépendances): " + e.getMessage(), e);
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException ignored) {
            }
        }
    }

    public void deleteByWallet(long walletId) {
        // 1. Delete transactions referencing any transaction of this wallet
        // MySQL trick: You can't delete from T where id in (select id from T). Need a
        // temp table alias.
        String sqlRefs = "DELETE FROM `transaction` WHERE reference_id IN " +
                "(SELECT transaction_id FROM (SELECT transaction_id FROM `transaction` WHERE wallet_id = ?) AS tmp)";

        // 2. Delete transactions of this wallet
        String sqlWalletTxs = "DELETE FROM `transaction` WHERE wallet_id = ?";

        try {
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(sqlRefs)) {
                ps.setLong(1, walletId);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement(sqlWalletTxs)) {
                ps.setLong(1, walletId);
                ps.executeUpdate();
            }

            conn.commit();
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ignored) {
            }
            throw new RuntimeException("Erreur deleteByWallet: " + e.getMessage(), e);
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException ignored) {
            }
        }
    }
}