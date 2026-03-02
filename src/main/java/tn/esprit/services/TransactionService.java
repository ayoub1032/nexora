package tn.esprit.services;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import tn.esprit.entities.Transaction;
import tn.esprit.utils.DBConnection;

public class TransactionService {
    private final Connection conn;

    public TransactionService() {
        conn = DBConnection.getConnection();
    }

    // ✅ DEPOSIT - Atomic transaction with lock
    public void deposit(long walletId, BigDecimal amount, Long referenceId) {
        validateAmount(amount);

        String selectForUpdate = "SELECT balance FROM wallet WHERE wallet_id = ? FOR UPDATE";
        String updateWallet = "UPDATE wallet SET balance = balance + ? WHERE wallet_id = ?";
        String insertTx = "INSERT INTO `transaction` (wallet_id, amount, transaction_type, reference_id, status) VALUES (?, ?, 'DEPOSIT', ?, 'CONFIRMED')";

        executeAtomic(walletId, amount, referenceId, selectForUpdate, updateWallet, insertTx, true);
    }

    // ✅ WITHDRAW - Atomic transaction with balance check
    public void withdraw(long walletId, BigDecimal amount, Long referenceId) {
        validateAmount(amount);

        String selectForUpdate = "SELECT balance FROM wallet WHERE wallet_id = ? FOR UPDATE";
        String updateWallet = "UPDATE wallet SET balance = balance - ? WHERE wallet_id = ?";
        String insertTx = "INSERT INTO `transaction` (wallet_id, amount, transaction_type, reference_id, status) VALUES (?, ?, 'WITHDRAW', ?, 'CONFIRMED')";

        executeAtomic(walletId, amount, referenceId, selectForUpdate, updateWallet, insertTx, false);
    }

    // ✅ RESERVE - Lock funds without removing them
    public void reserve(long walletId, BigDecimal amount, Long referenceId) {
        validateAmount(amount);

        String selectForUpdate = "SELECT balance, reserved_balance FROM wallet WHERE wallet_id = ? FOR UPDATE";
        String updateWallet = "UPDATE wallet SET reserved_balance = reserved_balance + ? WHERE wallet_id = ?";
        String insertTx = "INSERT INTO `transaction` (wallet_id, amount, transaction_type, reference_id, status) VALUES (?, ?, 'RESERVE', ?, 'CONFIRMED')";

        executeAtomicReserve(walletId, amount, referenceId, selectForUpdate, updateWallet, insertTx);
    }

    // ✅ RELEASE - Unlock reserved funds
    public void release(long walletId, BigDecimal amount, Long referenceId) {
        validateAmount(amount);

        String selectForUpdate = "SELECT reserved_balance FROM wallet WHERE wallet_id = ? FOR UPDATE";
        String updateWallet = "UPDATE wallet SET reserved_balance = reserved_balance - ? WHERE wallet_id = ?";
        String insertTx = "INSERT INTO `transaction` (wallet_id, amount, transaction_type, reference_id, status) VALUES (?, ?, 'RELEASE', ?, 'CONFIRMED')";

        executeAtomicReserve(walletId, amount, referenceId, selectForUpdate, updateWallet, insertTx);
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
                    throw new RuntimeException("Wallet not found (wallet_id=" + walletId + ")");
                balance = rs.getBigDecimal("balance");
            }

            if (!isDeposit && balance.compareTo(amount) < 0) {
                throw new RuntimeException("Insufficient balance. Balance=" + balance + ", Withdraw=" + amount);
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

        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ignored) {}
            throw new RuntimeException("Error " + (isDeposit ? "deposit" : "withdraw") + ": " + e.getMessage(), e);
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException ignored) {}
        }
    }

    private void executeAtomicReserve(long walletId, BigDecimal amount, Long referenceId,
                                     String selectForUpdate, String updateWallet, String insertTx) {
        try {
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(selectForUpdate)) {
                ps.setLong(1, walletId);
                ResultSet rs = ps.executeQuery();
                if (!rs.next())
                    throw new RuntimeException("Wallet not found (wallet_id=" + walletId + ")");
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

        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ignored) {}
            throw new RuntimeException("Error in reserve/release: " + e.getMessage(), e);
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException ignored) {}
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
            throw new RuntimeException("Error getAll transactions: " + e.getMessage(), e);
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
            throw new RuntimeException("Error getByWallet: " + e.getMessage(), e);
        }
        return list;
    }

    public Transaction findById(long transactionId) {
        String sql = "SELECT * FROM `transaction` WHERE transaction_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, transactionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding transaction: " + e.getMessage(), e);
        }
        return null;
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
            throw new RuntimeException("Invalid amount (must be > 0).");
        }
    }

    // Compatibility alias
    public List<Transaction> findByWallet(long walletId) {
        return getByWallet(walletId);
    }
}
