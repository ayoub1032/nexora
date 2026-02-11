package tn.esprit.entities;

import java.math.BigDecimal;

public class Wallet {

    private long walletId;
    private long userId;
    private BigDecimal balance;
    private BigDecimal reservedBalance;

    public Wallet() {}

    public long getWalletId() { return walletId; }
    public void setWalletId(long walletId) { this.walletId = walletId; }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public BigDecimal getReservedBalance() { return reservedBalance; }
    public void setReservedBalance(BigDecimal reservedBalance) { this.reservedBalance = reservedBalance; }
}