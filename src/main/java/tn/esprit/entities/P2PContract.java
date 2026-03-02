package tn.esprit.entities;

import java.io.Serializable;
import java.time.LocalDateTime;

public class P2PContract implements Serializable {
    private long contractId;
    private long creatorId;
    private long assetId;
    private int quantity;
    private double pricePerUnit;
    private String contractType; // BUY or SELL
    private String status; // OPEN, ACCEPTED, CANCELLED, COMPLETED
    private Long acceptedBy;
    private LocalDateTime createdAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime completedAt;

    public P2PContract() {
    }

    public P2PContract(long creatorId, long assetId, int quantity, double pricePerUnit, String contractType) {
        this.creatorId = creatorId;
        this.assetId = assetId;
        this.quantity = quantity;
        this.pricePerUnit = pricePerUnit;
        this.contractType = contractType;
        this.status = "OPEN";
    }

    public long getContractId() { return contractId; }
    public void setContractId(long contractId) { this.contractId = contractId; }

    public long getCreatorId() { return creatorId; }
    public void setCreatorId(long creatorId) { this.creatorId = creatorId; }

    public long getAssetId() { return assetId; }
    public void setAssetId(long assetId) { this.assetId = assetId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getPricePerUnit() { return pricePerUnit; }
    public void setPricePerUnit(double pricePerUnit) { this.pricePerUnit = pricePerUnit; }

    public String getContractType() { return contractType; }
    public void setContractType(String contractType) { this.contractType = contractType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getAcceptedBy() { return acceptedBy; }
    public void setAcceptedBy(Long acceptedBy) { this.acceptedBy = acceptedBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getAcceptedAt() { return acceptedAt; }
    public void setAcceptedAt(LocalDateTime acceptedAt) { this.acceptedAt = acceptedAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public double getTotalValue() {
        return pricePerUnit * quantity;
    }

    @Override
    public String toString() {
        return "P2PContract{" +
                "contractId=" + contractId +
                ", creatorId=" + creatorId +
                ", assetId=" + assetId +
                ", quantity=" + quantity +
                ", pricePerUnit=" + pricePerUnit +
                ", contractType='" + contractType + '\'' +
                ", status='" + status + '\'' +
                ", acceptedBy=" + acceptedBy +
                ", createdAt=" + createdAt +
                '}';
    }
}
