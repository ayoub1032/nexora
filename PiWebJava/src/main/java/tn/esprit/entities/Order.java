package tn.esprit.entities;

public class Order {
    private long orderId;
    private long assetId;
    private long userId;
    private int quantity;
    private double price;
    private String type;

    public Order() {}

    public Order(long assetId, long userId, int quantity, double price, String type) {
        this.assetId = assetId;
        this.userId = userId;
        this.quantity = quantity;
        this.price = price;
        this.type = type;
    }

    public long getOrderId() {
        return orderId;
    }

    public void setOrderId(long orderId) {
        this.orderId = orderId;
    }

    public long getAssetId() {
        return assetId;
    }

    public void setAssetId(long assetId) {
        this.assetId = assetId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderId=" + orderId +
                ", assetId=" + assetId +
                ", userId=" + userId +
                ", quantity=" + quantity +
                ", price=" + price +
                ", type='" + type + '\'' +
                '}';
    }
}
