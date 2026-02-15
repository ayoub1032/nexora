package tn.esprit.services;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import tn.esprit.entities.Order;
import tn.esprit.entities.PortfolioAsset;
import tn.esprit.utils.DBConnection;

public class OrderService {

    private final Connection conn;
    private final PortfolioAssetService portfolioAssetService;
    private final PortfolioService portfolioService;

    public OrderService() {
        conn = DBConnection.getConnection();
        portfolioAssetService = new PortfolioAssetService();
        portfolioService = new PortfolioService();
    }

    public long add(Order order) {
        String sql = "INSERT INTO orders (asset_id, user_id, quantity, price, type) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, order.getAssetId());
            ps.setLong(2, order.getUserId());
            ps.setInt(3, order.getQuantity());
            ps.setDouble(4, order.getPrice());
            ps.setString(5, order.getType());
            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur add order: " + e.getMessage(), e);
        }
        return -1;
    }

    public Order findById(long orderId) {
        String sql = "SELECT * FROM orders WHERE order_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, orderId);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return map(rs);
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findById: " + e.getMessage(), e);
        }
    }

    public List<Order> getAll() {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT * FROM orders ORDER BY order_id DESC";
        try (Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next())
                list.add(map(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Erreur getAll orders: " + e.getMessage(), e);
        }
        return list;
    }

    public List<Order> findByUserId(long userId) {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE user_id = ? ORDER BY order_id DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                list.add(map(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findByUserId: " + e.getMessage(), e);
        }
        return list;
    }

    public List<Order> findByAssetId(long assetId) {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE asset_id = ? ORDER BY order_id DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, assetId);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                list.add(map(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findByAssetId: " + e.getMessage(), e);
        }
        return list;
    }

    public void update(Order order) {
        String sql = "UPDATE orders SET asset_id = ?, user_id = ?, quantity = ?, price = ?, type = ? WHERE order_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, order.getAssetId());
            ps.setLong(2, order.getUserId());
            ps.setInt(3, order.getQuantity());
            ps.setDouble(4, order.getPrice());
            ps.setString(5, order.getType());
            ps.setLong(6, order.getOrderId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur update order: " + e.getMessage(), e);
        }
    }

    public void delete(long orderId) {
        String sql = "DELETE FROM orders WHERE order_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, orderId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur delete order: " + e.getMessage(), e);
        }
    }

    /**
     * Process an order (BUY/SELL) with atomic transaction handling
     * BUY: Check wallet balance, deduct money, add assets to portfolio, record
     * transaction
     * SELL: Check portfolio assets, add money to wallet, remove assets, record
     * transaction
     */
    public boolean processOrder(long orderId) {
        Order order = findById(orderId);
        if (order == null) {
            throw new RuntimeException("Order not found: " + orderId);
        }

        try {
            conn.setAutoCommit(false);

            if ("BUY".equalsIgnoreCase(order.getType())) {
                processBuyOrder(order);
            } else if ("SELL".equalsIgnoreCase(order.getType())) {
                processSellOrder(order);
            } else {
                throw new RuntimeException("Invalid order type: " + order.getType());
            }

            // Update order status to FILLED
            String updateOrderSql = "UPDATE orders SET status = 'FILLED' WHERE order_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(updateOrderSql)) {
                ps.setLong(1, orderId);
                ps.executeUpdate();
            }

            conn.commit();
            System.out.println("✅ Order " + orderId + " processed successfully");
            return true;

        } catch (Exception e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            throw new RuntimeException("Error processing order: " + e.getMessage(), e);
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Process BUY order: deduct from wallet, add to portfolio
     */
    private void processBuyOrder(Order order) throws SQLException {
        double totalCost = order.getPrice() * order.getQuantity();
        BigDecimal amount = new BigDecimal(totalCost);

        // Check wallet balance
        String walletSql = "SELECT balance FROM wallet WHERE user_id = ? FOR UPDATE";
        try (PreparedStatement ps = conn.prepareStatement(walletSql)) {
            ps.setLong(1, order.getUserId());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new RuntimeException("Wallet not found for user: " + order.getUserId());
                }
                BigDecimal balance = rs.getBigDecimal("balance");
                if (balance.compareTo(amount) < 0) {
                    throw new RuntimeException(
                            "Insufficient wallet balance. Need: " + totalCost + ", Have: " + balance);
                }
            }
        }

        // Deduct from wallet
        String deductSql = "UPDATE wallet SET balance = balance - ? WHERE user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(deductSql)) {
            ps.setBigDecimal(1, amount);
            ps.setLong(2, order.getUserId());
            ps.executeUpdate();
        }

        // Get portfolio_id for user
        String portfolioSql = "SELECT portfolio_id FROM portfolio WHERE user_id = ?";
        long portfolioId = -1;
        try (PreparedStatement ps = conn.prepareStatement(portfolioSql)) {
            ps.setLong(1, order.getUserId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    portfolioId = rs.getLong("portfolio_id");
                }
            }
        }

        if (portfolioId == -1) {
            throw new RuntimeException("Portfolio not found for user: " + order.getUserId());
        }

        // Check if asset already in portfolio
        PortfolioAsset existingAsset = portfolioAssetService.findByPortfolioAndAsset(portfolioId, order.getAssetId());

        if (existingAsset != null) {
            // Update existing asset: increase quantity and recalculate avg price
            int newQuantity = existingAsset.getQuantity() + order.getQuantity();
            double avgPrice = (existingAsset.getAvgPrice() * existingAsset.getQuantity()
                    + order.getPrice() * order.getQuantity()) / newQuantity;
            portfolioAssetService.updateQuantity(portfolioId, order.getAssetId(), order.getQuantity(), avgPrice);
        } else {
            // Create new portfolio asset entry
            PortfolioAsset newAsset = new PortfolioAsset(portfolioId, order.getAssetId(), order.getQuantity(),
                    order.getPrice());
            portfolioAssetService.add(newAsset);
        }

        // Record transaction
        String txSql = "INSERT INTO `transaction` (wallet_id, amount, transaction_type, reference_id, status) " +
                "SELECT wallet_id, ?, 'WITHDRAW', ?, 'CONFIRMED' FROM wallet WHERE user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(txSql)) {
            ps.setBigDecimal(1, amount);
            ps.setLong(2, order.getOrderId());
            ps.setLong(3, order.getUserId());
            ps.executeUpdate();
        }

        System.out.println("✅ BUY order processed: " + order.getQuantity() + " assets purchased for " + totalCost);

        // Update portfolio total value
        portfolioService.updateTotalValue(portfolioId);
    }

    /**
     * Process SELL order: remove from portfolio, add to wallet
     */
    private void processSellOrder(Order order) throws SQLException {
        double totalValue = order.getPrice() * order.getQuantity();
        BigDecimal amount = new BigDecimal(totalValue);

        // Get portfolio_id for user
        String portfolioSql = "SELECT portfolio_id FROM portfolio WHERE user_id = ?";
        long portfolioId = -1;
        try (PreparedStatement ps = conn.prepareStatement(portfolioSql)) {
            ps.setLong(1, order.getUserId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    portfolioId = rs.getLong("portfolio_id");
                }
            }
        }

        if (portfolioId == -1) {
            throw new RuntimeException("Portfolio not found for user: " + order.getUserId());
        }

        // Check portfolio has enough assets
        PortfolioAsset existingAsset = portfolioAssetService.findByPortfolioAndAsset(portfolioId, order.getAssetId());
        if (existingAsset == null || existingAsset.getQuantity() < order.getQuantity()) {
            int available = existingAsset != null ? existingAsset.getQuantity() : 0;
            throw new RuntimeException("Insufficient assets. Need: " + order.getQuantity() + ", Have: " + available);
        }

        // Remove from portfolio_assets
        int newQuantity = existingAsset.getQuantity() - order.getQuantity();
        if (newQuantity > 0) {
            // Keep the avg_price, just reduce quantity
            portfolioAssetService.updateQuantity(portfolioId, order.getAssetId(), -order.getQuantity(),
                    existingAsset.getAvgPrice());
        } else {
            // Delete the asset entry if quantity becomes 0
            portfolioAssetService.delete(existingAsset.getId());
        }

        // Add to wallet
        String addBalanceSql = "UPDATE wallet SET balance = balance + ? WHERE user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(addBalanceSql)) {
            ps.setBigDecimal(1, amount);
            ps.setLong(2, order.getUserId());
            ps.executeUpdate();
        }

        // Record transaction
        String txSql = "INSERT INTO `transaction` (wallet_id, amount, transaction_type, reference_id, status) " +
                "SELECT wallet_id, ?, 'DEPOSIT', ?, 'CONFIRMED' FROM wallet WHERE user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(txSql)) {
            ps.setBigDecimal(1, amount);
            ps.setLong(2, order.getOrderId());
            ps.setLong(3, order.getUserId());
            ps.executeUpdate();
        }

        System.out.println("✅ SELL order processed: " + order.getQuantity() + " assets sold for " + totalValue);

        // Update portfolio total value
        portfolioService.updateTotalValue(portfolioId);
    }

    private Order map(ResultSet rs) throws SQLException {
        Order o = new Order();
        o.setOrderId(rs.getLong("order_id"));
        o.setAssetId(rs.getLong("asset_id"));
        o.setUserId(rs.getLong("user_id"));
        o.setQuantity(rs.getInt("quantity"));
        o.setPrice(rs.getDouble("price"));
        o.setType(rs.getString("type"));
        return o;
    }
}
