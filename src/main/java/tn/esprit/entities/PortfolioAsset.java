package tn.esprit.entities;

import java.io.Serializable;

public class PortfolioAsset implements Serializable {
    private long id;
    private long portfolioId;
    private long assetId;
    private int quantity;
    private double avgPrice;

    public PortfolioAsset() {
    }

    public PortfolioAsset(long portfolioId, long assetId, int quantity, double avgPrice) {
        this.portfolioId = portfolioId;
        this.assetId = assetId;
        this.quantity = quantity;
        this.avgPrice = avgPrice;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getPortfolioId() {
        return portfolioId;
    }

    public void setPortfolioId(long portfolioId) {
        this.portfolioId = portfolioId;
    }

    public long getAssetId() {
        return assetId;
    }

    public void setAssetId(long assetId) {
        this.assetId = assetId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getAvgPrice() {
        return avgPrice;
    }

    public void setAvgPrice(double avgPrice) {
        this.avgPrice = avgPrice;
    }

    @Override
    public String toString() {
        return "PortfolioAsset{" +
                "id=" + id +
                ", portfolioId=" + portfolioId +
                ", assetId=" + assetId +
                ", quantity=" + quantity +
                ", avgPrice=" + avgPrice +
                '}';
    }
}
