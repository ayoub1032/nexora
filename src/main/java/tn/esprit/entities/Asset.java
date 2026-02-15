package tn.esprit.entities;

public class Asset {
    private long assetId;
    private String name;
    private String symbol;
    private double value;
    private String type;

    public Asset() {}

    public Asset(String name, String symbol, double value, String type) {
        this.name = name;
        this.symbol = symbol;
        this.value = value;
        this.type = type;
    }

    public long getAssetId() {
        return assetId;
    }

    public void setAssetId(long assetId) {
        this.assetId = assetId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "Asset{" +
                "assetId=" + assetId +
                ", name='" + name + '\'' +
                ", symbol='" + symbol + '\'' +
                ", value=" + value +
                ", type='" + type + '\'' +
                '}';
    }
}
