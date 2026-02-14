package tn.esprit.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import tn.esprit.entities.Order;
import tn.esprit.utils.DBConnection;

public class OrderService {

    private final Connection conn;

    public OrderService() {
        conn = DBConnection.getConnection();
    }

    public void add(Order order) {
        String sql = "INSERT INTO `order` (asset_id, user_id, quantity, price, type) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, order.getAssetId());
            ps.setLong(2, order.getUserId());
            ps.setInt(3, order.getQuantity());
            ps.setDouble(4, order.getPrice());
            ps.setString(5, order.getType());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur add order: " + e.getMessage(), e);
        }
    }

    public Order findById(long orderId) {
        String sql = "SELECT * FROM `order` WHERE order_id = ?";
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
        String sql = "SELECT * FROM `order` ORDER BY order_id DESC";
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
        String sql = "SELECT * FROM `order` WHERE user_id = ? ORDER BY order_id DESC";
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
        String sql = "SELECT * FROM `order` WHERE asset_id = ? ORDER BY order_id DESC";
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
        String sql = "UPDATE `order` SET asset_id = ?, user_id = ?, quantity = ?, price = ?, type = ? WHERE order_id = ?";
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
        String sql = "DELETE FROM `order` WHERE order_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, orderId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur delete order: " + e.getMessage(), e);
        }
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
