package tn.esprit.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import tn.esprit.entities.Portfolio;
import tn.esprit.utils.DBConnection;

public class PortfolioService {

    private final Connection conn;

    public PortfolioService() {
        conn = DBConnection.getConnection();
    }

    public long add(Portfolio portfolio) {
        String sql = "INSERT INTO portfolio (user_id, total_value) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, portfolio.getUserId());
            ps.setDouble(2, portfolio.getTotalValue());
            ps.executeUpdate();
            
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur add portfolio: " + e.getMessage(), e);
        }
        return -1;
    }

    public Portfolio findById(long portfolioId) {
        String sql = "SELECT * FROM portfolio WHERE portfolio_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, portfolioId);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return map(rs);
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findById: " + e.getMessage(), e);
        }
    }

    public Portfolio findByUserId(long userId) {
        String sql = "SELECT * FROM portfolio WHERE user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return map(rs);
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findByUserId: " + e.getMessage(), e);
        }
    }

    public List<Portfolio> getAll() {
        List<Portfolio> list = new ArrayList<>();
        String sql = "SELECT * FROM portfolio ORDER BY portfolio_id DESC";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next())
                list.add(map(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Erreur getAll portfolios: " + e.getMessage(), e);
        }
        return list;
    }

    public void update(Portfolio portfolio) {
        String sql = "UPDATE portfolio SET user_id = ?, total_value = ? WHERE portfolio_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, portfolio.getUserId());
            ps.setDouble(2, portfolio.getTotalValue());
            ps.setLong(3, portfolio.getPortfolioId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur update portfolio: " + e.getMessage(), e);
        }
    }

    public void delete(long portfolioId) {
        String sql = "DELETE FROM portfolio WHERE portfolio_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, portfolioId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur delete portfolio: " + e.getMessage(), e);
        }
    }

    public void addAssetToPortfolio(long portfolioId, long assetId, int quantity) {
        String sql = "INSERT INTO portfolio_assets (portfolio_id, asset_id, quantity) VALUES (?, ?, ?) " +
                 "ON DUPLICATE KEY UPDATE quantity = quantity + ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, portfolioId);
            ps.setLong(2, assetId);
            ps.setInt(3, quantity);
            ps.setInt(4, quantity);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur addAssetToPortfolio: " + e.getMessage(), e);
        }
    }

    public void removeAssetFromPortfolio(long portfolioId, long assetId, int quantity) {
        String sql = "UPDATE portfolio_assets SET quantity = quantity - ? WHERE portfolio_id = ? AND asset_id = ? AND quantity > 0";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setLong(2, portfolioId);
            ps.setLong(3, assetId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur removeAssetFromPortfolio: " + e.getMessage(), e);
        }
    }

    public void updateTotalValue(long portfolioId) {
        // Calculate total value as sum of (quantity * avg_price) for all assets in portfolio
        // This shows the cost basis/investment amount
        String sql = "UPDATE portfolio SET total_value = (" +
                    "SELECT COALESCE(SUM(pa.quantity * pa.avg_price), 0) " +
                    "FROM portfolio_assets pa " +
                    "WHERE pa.portfolio_id = ?" +
                    ") WHERE portfolio_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, portfolioId);
            ps.setLong(2, portfolioId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating portfolio total value: " + e.getMessage(), e);
        }
    }

    private Portfolio map(ResultSet rs) throws SQLException {
        Portfolio p = new Portfolio();
        p.setPortfolioId(rs.getLong("portfolio_id"));
        p.setUserId(rs.getLong("user_id"));
        p.setTotalValue(rs.getDouble("total_value"));
        // Asset quantities would be loaded separately via JOIN if needed
        p.setAssetQuantities(new HashMap<>());
        return p;
    }
}
