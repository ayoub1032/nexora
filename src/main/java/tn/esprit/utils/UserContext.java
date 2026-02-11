package tn.esprit.utils;

public class UserContext {
    private static Long walletId;

    public static Long getWalletId() { return walletId; }
    public static void setWalletId(Long id) { walletId = id; }
    public static void clear() { walletId = null; }
}