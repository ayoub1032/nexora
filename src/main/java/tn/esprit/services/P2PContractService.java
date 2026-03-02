package tn.esprit.services;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import tn.esprit.entities.P2PContract;
import tn.esprit.entities.Portfolio;
import tn.esprit.entities.PortfolioAsset;
import tn.esprit.entities.Wallet;
import tn.esprit.utils.DBConnection;

public class P2PContractService {

    private final PortfolioService portfolioService;
    private final PortfolioAssetService portfolioAssetService;
    private final WalletService walletService;

    public P2PContractService() {
        this.portfolioService = new PortfolioService();
        this.portfolioAssetService = new PortfolioAssetService();
        this.walletService = new WalletService();
    }

    public long add(P2PContract contract) {
        // Validation Logic
        if ("SELL".equalsIgnoreCase(contract.getContractType())) {
            validateSellContract(contract);
        } else if ("BUY".equalsIgnoreCase(contract.getContractType())) {
            validateBuyContract(contract);
        }

        String sql = "INSERT INTO p2p_contract (creator_id, asset_id, quantity, price_per_unit, contract_type, status) "
                +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, contract.getCreatorId());
            ps.setLong(2, contract.getAssetId());
            ps.setInt(3, contract.getQuantity());
            ps.setDouble(4, contract.getPricePerUnit());
            ps.setString(5, contract.getContractType());
            ps.setString(6, "OPEN"); // Force status to OPEN
            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error adding P2P contract: " + e.getMessage(), e);
        }
        return -1;
    }

    private void validateSellContract(P2PContract contract) {
        // Creator wants to SELL assets. Must have assets in portfolio.
        Portfolio portfolio = portfolioService.findByUserId(contract.getCreatorId());
        if (portfolio == null) {
            throw new RuntimeException("Validation Error: User has no portfolio.");
        }

        PortfolioAsset asset = portfolioAssetService.findByPortfolioAndAsset(portfolio.getPortfolioId(),
                contract.getAssetId());
        if (asset == null || asset.getQuantity() < contract.getQuantity()) {
            int available = asset != null ? asset.getQuantity() : 0;
            throw new RuntimeException("Validation Error: Insufficient assets to create SELL contract. Have: "
                    + available + ", Need: " + contract.getQuantity());
        }
    }

    private void validateBuyContract(P2PContract contract) {
        // Creator wants to BUY assets. Must have money in wallet.
        Wallet wallet = walletService.findByUserId(contract.getCreatorId());
        if (wallet == null) {
            throw new RuntimeException("Validation Error: User has no wallet.");
        }

        double totalCost = contract.getPricePerUnit() * contract.getQuantity();
        if (wallet.getBalance().compareTo(new BigDecimal(totalCost)) < 0) {
            throw new RuntimeException("Validation Error: Insufficient funds to create BUY contract. Have: "
                    + wallet.getBalance() + ", Need: " + totalCost);
        }
    }

    public P2PContract findById(long contractId) {
        String sql = "SELECT * FROM p2p_contract WHERE contract_id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, contractId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding P2P contract: " + e.getMessage(), e);
        }
        return null;
    }

    public List<P2PContract> getOpenContracts() {
        List<P2PContract> list = new ArrayList<>();
        String sql = "SELECT * FROM p2p_contract WHERE status = 'OPEN' ORDER BY created_at DESC";
        try (Connection conn = DBConnection.getConnection();
                Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting open contracts: " + e.getMessage(), e);
        }
        return list;
    }

    public List<P2PContract> getContractsByCreator(long creatorId) {
        List<P2PContract> list = new ArrayList<>();
        String sql = "SELECT * FROM p2p_contract WHERE creator_id = ? ORDER BY created_at DESC";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, creatorId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting contracts by creator: " + e.getMessage(), e);
        }
        return list;
    }

    public List<P2PContract> getAll() {
        List<P2PContract> list = new ArrayList<>();
        String sql = "SELECT * FROM p2p_contract ORDER BY contract_id DESC";
        try (Connection conn = DBConnection.getConnection();
                Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting all contracts: " + e.getMessage(), e);
        }
        return list;
    }

    public void acceptContract(long contractId, long acceptorId) {
        String sql = "UPDATE p2p_contract SET accepted_by = ?, status = 'ACCEPTED', accepted_at = NOW() WHERE contract_id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, acceptorId);
            ps.setLong(2, contractId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error accepting contract: " + e.getMessage(), e);
        }
    }

    public void acceptContract(long contractId, long acceptorId, Connection conn) throws SQLException {
        String sql = "UPDATE p2p_contract SET accepted_by = ?, status = 'ACCEPTED', accepted_at = NOW() WHERE contract_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, acceptorId);
            ps.setLong(2, contractId);
            ps.executeUpdate();
        }
    }

    public void completeContract(long contractId) {
        String sql = "UPDATE p2p_contract SET status = 'COMPLETED', completed_at = NOW() WHERE contract_id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, contractId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error completing contract: " + e.getMessage(), e);
        }
    }

    public void completeContract(long contractId, Connection conn) throws SQLException {
        String sql = "UPDATE p2p_contract SET status = 'COMPLETED', completed_at = NOW() WHERE contract_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, contractId);
            ps.executeUpdate();
        }
    }

    public void cancelContract(long contractId) {
        String sql = "UPDATE p2p_contract SET status = 'CANCELLED' WHERE contract_id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, contractId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error cancelling contract: " + e.getMessage(), e);
        }
    }

    public void cancelContract(long contractId, Connection conn) throws SQLException {
        String sql = "UPDATE p2p_contract SET status = 'CANCELLED' WHERE contract_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, contractId);
            ps.executeUpdate();
        }
    }

    public void delete(long contractId) {
        String sql = "DELETE FROM p2p_contract WHERE contract_id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, contractId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting contract: " + e.getMessage(), e);
        }
    }

    public void delete(long contractId, Connection conn) throws SQLException {
        String sql = "DELETE FROM p2p_contract WHERE contract_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, contractId);
            ps.executeUpdate();
        }
    }

    private P2PContract map(ResultSet rs) throws SQLException {
        P2PContract contract = new P2PContract();
        contract.setContractId(rs.getLong("contract_id"));
        contract.setCreatorId(rs.getLong("creator_id"));
        contract.setAssetId(rs.getLong("asset_id"));
        contract.setQuantity(rs.getInt("quantity"));
        contract.setPricePerUnit(rs.getDouble("price_per_unit"));
        contract.setContractType(rs.getString("contract_type"));
        contract.setStatus(rs.getString("status"));

        long acceptedBy = rs.getLong("accepted_by");
        contract.setAcceptedBy(rs.wasNull() ? null : acceptedBy);

        Timestamp acceptedAt = rs.getTimestamp("accepted_at");
        contract.setAcceptedAt(acceptedAt != null ? acceptedAt.toLocalDateTime() : null);

        Timestamp createdAt = rs.getTimestamp("created_at");
        contract.setCreatedAt(createdAt != null ? createdAt.toLocalDateTime() : null);

        Timestamp completedAt = rs.getTimestamp("completed_at");
        contract.setCompletedAt(completedAt != null ? completedAt.toLocalDateTime() : null);

        return contract;
    }
}
