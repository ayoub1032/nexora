package tn.esprit.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import tn.esprit.entities.Asset;
import tn.esprit.utils.DBConnection;
import tn.esprit.utils.LivePriceService;

public class AssetService {

    private final Connection conn;

    public AssetService() {
        conn = DBConnection.getConnection();
    }

    public void add(Asset asset) {
        String sql = "INSERT INTO asset (name, symbol, value, type) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, asset.getName());
            ps.setString(2, asset.getSymbol());
            ps.setDouble(3, asset.getValue());
            ps.setString(4, asset.getType());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur add asset: " + e.getMessage(), e);
        }
    }

    public Asset findById(long assetId) {
        String sql = "SELECT * FROM asset WHERE asset_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, assetId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Asset asset = map(rs);
                injectLivePrice(asset);
                return asset;
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findById: " + e.getMessage(), e);
        }
    }

    public List<Asset> getAll() {
        List<Asset> list = new ArrayList<>();
        String sql = "SELECT * FROM asset ORDER BY asset_id DESC";
        try (Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Asset asset = map(rs);
                injectLivePrice(asset);
                list.add(asset);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur getAll assets: " + e.getMessage(), e);
        }
        return list;
    }

    private void injectLivePrice(Asset asset) {
        // Only fetch live prices for Crypto to avoid spamming the API and failing on
        // Stocks/Fiat
        if ("CRYPTO".equalsIgnoreCase(asset.getType())) {
            double livePrice = LivePriceService.getPrice(asset.getSymbol());
            if (livePrice > 0) {
                asset.setValue(livePrice); // Transient memory override
            }
        }
    }

    public void update(Asset asset) {
        String sql = "UPDATE asset SET name = ?, symbol = ?, value = ?, type = ? WHERE asset_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, asset.getName());
            ps.setString(2, asset.getSymbol());
            ps.setDouble(3, asset.getValue());
            ps.setString(4, asset.getType());
            ps.setLong(5, asset.getAssetId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur update asset: " + e.getMessage(), e);
        }
    }

    public void delete(long assetId) {
        String sql = "DELETE FROM asset WHERE asset_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, assetId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur delete asset: " + e.getMessage(), e);
        }
    }

    private Asset map(ResultSet rs) throws SQLException {
        Asset a = new Asset();
        a.setAssetId(rs.getLong("asset_id"));
        a.setName(rs.getString("name"));
        a.setSymbol(rs.getString("symbol"));
        a.setValue(rs.getDouble("value"));
        a.setType(rs.getString("type"));
        return a;
    }
}
