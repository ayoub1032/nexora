package tn.esprit.entities;

import java.util.HashMap;
import java.util.Map;

public class Portfolio {
    private long portfolioId;
    private long userId;
    private Map<Long, Integer> assetQuantities; // assetId to quantity
    private double totalValue;

    public Portfolio() {
        this.assetQuantities = new HashMap<>();
    }

    public Portfolio(long userId) {
        this.userId = userId;
        this.assetQuantities = new HashMap<>();
        this.totalValue = 0.0;
    }

    public long getPortfolioId() {
        return portfolioId;
    }

    public void setPortfolioId(long portfolioId) {
        this.portfolioId = portfolioId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public Map<Long, Integer> getAssetQuantities() {
        return assetQuantities;
    }

    public void setAssetQuantities(Map<Long, Integer> assetQuantities) {
        this.assetQuantities = assetQuantities;
    }

    public double getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(double totalValue) {
        this.totalValue = totalValue;
    }

    @Override
    public String toString() {
        return "Portfolio{" +
                "portfolioId=" + portfolioId +
                ", userId=" + userId +
                ", assetQuantities=" + assetQuantities +
                ", totalValue=" + totalValue +
                '}';
    }
}
