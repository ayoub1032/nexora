package tn.esprit.entities;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Transaction {
    private long transactionId;
    private long walletId;
    private BigDecimal amount;
    private String transactionType; // DEPOSIT, WITHDRAW, RESERVE, RELEASE
    private Long referenceId;
    private String status; // CONFIRMED, PENDING, FAILED
    private Timestamp createdAt;

    // Constructors
    public Transaction() {}

    public Transaction(long walletId, BigDecimal amount, String transactionType, Long referenceId, String status) {
        this.walletId = walletId;
        this.amount = amount;
        this.transactionType = transactionType;
        this.referenceId = referenceId;
        this.status = status;
    }

    // Getters & Setters
    public long getTransactionId() { return transactionId; }
    public void setTransactionId(long transactionId) { this.transactionId = transactionId; }

    public long getWalletId() { return walletId; }
    public void setWalletId(long walletId) { this.walletId = walletId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }

    public Long getReferenceId() { return referenceId; }
    public void setReferenceId(Long referenceId) { this.referenceId = referenceId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "Transaction{" +
                "transactionId=" + transactionId +
                ", walletId=" + walletId +
                ", amount=" + amount +
                ", transactionType='" + transactionType + '\'' +
                ", referenceId=" + referenceId +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
