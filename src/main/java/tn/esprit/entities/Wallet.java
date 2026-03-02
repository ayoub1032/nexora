package tn.esprit.entities;

import java.math.BigDecimal;

public class Wallet {
    private long walletId;
    private long userId;
    private BigDecimal balance;
    private BigDecimal reservedBalance;
    private String createdAt;

    // Constructors
    public Wallet() {}

    public Wallet(long userId, BigDecimal balance, BigDecimal reservedBalance) {
        this.userId = userId;
        this.balance = balance;
        this.reservedBalance = reservedBalance;
    }

    public Wallet(long walletId, long userId, BigDecimal balance, BigDecimal reservedBalance, String createdAt) {
        this.walletId = walletId;
        this.userId = userId;
        this.balance = balance;
        this.reservedBalance = reservedBalance;
        this.createdAt = createdAt;
    }

    // Getters & Setters
    public long getWalletId() { return walletId; }
    public void setWalletId(long walletId) { this.walletId = walletId; }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public BigDecimal getReservedBalance() { return reservedBalance; }
    public void setReservedBalance(BigDecimal reservedBalance) { this.reservedBalance = reservedBalance; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public BigDecimal getAvailableBalance() {
        return balance.subtract(reservedBalance);
    }

    @Override
    public String toString() {
        return "Wallet{" + "walletId=" + walletId + ", userId=" + userId +
               ", balance=" + balance + ", reserved=" + reservedBalance + '}';
    }
}
