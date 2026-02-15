package tn.esprit.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import tn.esprit.entities.PortfolioAsset;
import tn.esprit.utils.DBConnection;

public class PortfolioAssetService {
    private Connection conn;

    public PortfolioAssetService() {
        this.conn = DBConnection.getConnection();
    }

    public long add(PortfolioAsset asset) {
        String sql = "INSERT INTO portfolio_assets (portfolio_id, asset_id, quantity, avg_price) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, asset.getPortfolioId());
            ps.setLong(2, asset.getAssetId());
            ps.setInt(3, asset.getQuantity());
            ps.setDouble(4, asset.getAvgPrice());
            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error adding portfolio asset: " + e.getMessage(), e);
        }
        return -1;
    }

    public PortfolioAsset findById(long id) {
        String sql = "SELECT * FROM portfolio_assets WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return map(rs);
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding portfolio asset: " + e.getMessage(), e);
        }
    }

    public PortfolioAsset findByPortfolioAndAsset(long portfolioId, long assetId) {
        String sql = "SELECT * FROM portfolio_assets WHERE portfolio_id = ? AND asset_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, portfolioId);
            ps.setLong(2, assetId);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return map(rs);
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding portfolio asset: " + e.getMessage(), e);
        }
    }

    public List<PortfolioAsset> getByPortfolio(long portfolioId) {
        List<PortfolioAsset> list = new ArrayList<>();
        String sql = "SELECT * FROM portfolio_assets WHERE portfolio_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, portfolioId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting portfolio assets: " + e.getMessage(), e);
        }
        return list;
    }

    public List<PortfolioAsset> getAll() {
        List<PortfolioAsset> list = new ArrayList<>();
        String sql = "SELECT * FROM portfolio_assets ORDER BY id DESC";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting all portfolio assets: " + e.getMessage(), e);
        }
        return list;
    }

    public void update(PortfolioAsset asset) {
        String sql = "UPDATE portfolio_assets SET portfolio_id = ?, asset_id = ?, quantity = ?, avg_price = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, asset.getPortfolioId());
            ps.setLong(2, asset.getAssetId());
            ps.setInt(3, asset.getQuantity());
            ps.setDouble(4, asset.getAvgPrice());
            ps.setLong(5, asset.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating portfolio asset: " + e.getMessage(), e);
        }
    }

    public void updateQuantity(long portfolioId, long assetId, int quantityChange, double price) {
        String sql = "UPDATE portfolio_assets SET quantity = quantity + ?, avg_price = ? WHERE portfolio_id = ? AND asset_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantityChange);
            ps.setDouble(2, price);
            ps.setLong(3, portfolioId);
            ps.setLong(4, assetId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating portfolio asset quantity: " + e.getMessage(), e);
        }
    }

    public void delete(long id) {
        String sql = "DELETE FROM portfolio_assets WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting portfolio asset: " + e.getMessage(), e);
        }
    }

    private PortfolioAsset map(ResultSet rs) throws SQLException {
        PortfolioAsset asset = new PortfolioAsset();
        asset.setId(rs.getLong("id"));
        asset.setPortfolioId(rs.getLong("portfolio_id"));
        asset.setAssetId(rs.getLong("asset_id"));
        asset.setQuantity(rs.getInt("quantity"));
        asset.setAvgPrice(rs.getDouble("avg_price"));
        return asset;
    }
}
