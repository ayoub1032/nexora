package tn.esprit.services;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import tn.esprit.entities.P2PContract;
import tn.esprit.services.UserReputationService;
import tn.esprit.utils.DBConnection;

/**
 * Service for handling P2P contract acceptance with atomic transactions
 * Ensures wallet and portfolio are always synchronized
 */
public class P2PContractAcceptanceService {

    private final P2PContractService contractService;

    public P2PContractAcceptanceService() {
        this.contractService = new P2PContractService();
    }

    /**
     * Accept a P2P contract with full validation and atomic transaction
     * SELL: Acceptor buys assets, pays the creator
     * BUY: Acceptor sells assets, receives payment from creator
     */
    public void acceptContract(long contractId, long acceptorId) {
        // contractService.findById uses its own connection, which is fine as it is a
        // read operation before transaction
        P2PContract contract = contractService.findById(contractId);

        if (contract == null) {
            throw new RuntimeException("Contract not found: " + contractId);
        }

        if (!"OPEN".equals(contract.getStatus())) {
            throw new RuntimeException("Contract is not open for acceptance");
        }

        if (contract.getCreatorId() == acceptorId) {
            throw new RuntimeException("Cannot accept your own contract");
        }

        try (Connection conn = DBConnection.getConnection()) {
            try {
                conn.setAutoCommit(false);

                if ("SELL".equalsIgnoreCase(contract.getContractType())) {
                    // Creator is selling, acceptor is buying
                    processBuyerAcceptance(contract, acceptorId, conn);
                } else if ("BUY".equalsIgnoreCase(contract.getContractType())) {
                    // Creator is buying, acceptor is selling
                    processSellerAcceptance(contract, acceptorId, conn);
                } else {
                    throw new RuntimeException("Invalid contract type: " + contract.getContractType());
                }

                // Mark contract as accepted using the SAME connection
                contractService.acceptContract(contractId, acceptorId, conn);
                contractService.completeContract(contractId, conn);

                conn.commit();
                System.out.println("✅ Contract " + contractId + " accepted and completed successfully");

                // Update reputation: both parties gain a completed contract
                UserReputationService reputationService = new UserReputationService();
                reputationService.incrementCompleted(contract.getCreatorId());
                reputationService.incrementCompleted(acceptorId);

            } catch (SQLException e) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
                throw new RuntimeException("Error accepting contract: " + e.getMessage(), e);
            } finally {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error accepting contract (Connection failed): " + e.getMessage(), e);
        }
    }

    /**
     * Buyer acceptance: Acceptor buys assets from creator
     * Acceptor pays money → Creator receives money
     * Creator loses assets → Acceptor gains assets
     */
    private void processBuyerAcceptance(P2PContract contract, long acceptorId, Connection conn) throws SQLException {
        long creatorId = contract.getCreatorId();
        double totalCost = contract.getPricePerUnit() * contract.getQuantity();
        BigDecimal amount = new BigDecimal(totalCost);

        // 1. Validate acceptor (buyer) has enough money
        String buyerWalletSql = "SELECT balance FROM wallet WHERE user_id = ? FOR UPDATE";
        try (PreparedStatement ps = conn.prepareStatement(buyerWalletSql)) {
            ps.setLong(1, acceptorId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new RuntimeException("Buyer wallet not found");
                }
                BigDecimal balance = rs.getBigDecimal("balance");
                if (balance.compareTo(amount) < 0) {
                    throw new RuntimeException("Insufficient funds. Need: " + totalCost + ", Have: " + balance);
                }
            }
        }

        // 2. Get creator (seller) portfolio_id and validate has assets
        String creatorPortfolioSql = "SELECT portfolio_id FROM portfolio WHERE user_id = ?";
        long creatorPortfolioId = -1;
        try (PreparedStatement ps = conn.prepareStatement(creatorPortfolioSql)) {
            ps.setLong(1, creatorId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    creatorPortfolioId = rs.getLong("portfolio_id");
                }
            }
        }

        if (creatorPortfolioId == -1) {
            throw new RuntimeException("Creator portfolio not found");
        }

        // Check creator has assets (with FOR UPDATE lock)
        String checkAssetsSql = "SELECT quantity, avg_price FROM portfolio_assets WHERE portfolio_id = ? AND asset_id = ? FOR UPDATE";
        int creatorQty = 0;
        try (PreparedStatement ps = conn.prepareStatement(checkAssetsSql)) {
            ps.setLong(1, creatorPortfolioId);
            ps.setLong(2, contract.getAssetId());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next() || rs.getInt("quantity") < contract.getQuantity()) {
                    throw new RuntimeException("Creator insufficient assets. Need: " + contract.getQuantity());
                }
                creatorQty = rs.getInt("quantity");
            }
        }

        // 3. Deduct money from buyer
        String deductBuyerSql = "UPDATE wallet SET balance = balance - ? WHERE user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(deductBuyerSql)) {
            ps.setBigDecimal(1, amount);
            ps.setLong(2, acceptorId);
            ps.executeUpdate();
        }

        // 4. Add money to seller (creator)
        String addCreatorSql = "UPDATE wallet SET balance = balance + ? WHERE user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(addCreatorSql)) {
            ps.setBigDecimal(1, amount);
            ps.setLong(2, creatorId);
            ps.executeUpdate();
        }

        // 5. Remove assets from creator's portfolio
        int newCreatorQty = creatorQty - contract.getQuantity();
        if (newCreatorQty > 0) {
            String updateQtySql = "UPDATE portfolio_assets SET quantity = quantity - ? WHERE portfolio_id = ? AND asset_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(updateQtySql)) {
                ps.setInt(1, contract.getQuantity());
                ps.setLong(2, creatorPortfolioId);
                ps.setLong(3, contract.getAssetId());
                ps.executeUpdate();
            }
        } else {
            String deleteSql = "DELETE FROM portfolio_assets WHERE portfolio_id = ? AND asset_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(deleteSql)) {
                ps.setLong(1, creatorPortfolioId);
                ps.setLong(2, contract.getAssetId());
                ps.executeUpdate();
            }
        }

        // 6. Get buyer portfolio and add assets
        String buyerPortfolioSql = "SELECT portfolio_id FROM portfolio WHERE user_id = ?";
        long buyerPortfolioId = -1;
        try (PreparedStatement ps = conn.prepareStatement(buyerPortfolioSql)) {
            ps.setLong(1, acceptorId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    buyerPortfolioId = rs.getLong("portfolio_id");
                }
            }
        }

        if (buyerPortfolioId == -1) {
            throw new RuntimeException("Buyer portfolio not found");
        }

        // Check if buyer already has this asset
        String checkBuyerAssetSql = "SELECT quantity, avg_price FROM portfolio_assets WHERE portfolio_id = ? AND asset_id = ? FOR UPDATE";
        boolean buyerHasAsset = false;
        int buyerQty = 0;
        double buyerAvgPrice = 0;
        try (PreparedStatement ps = conn.prepareStatement(checkBuyerAssetSql)) {
            ps.setLong(1, buyerPortfolioId);
            ps.setLong(2, contract.getAssetId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    buyerHasAsset = true;
                    buyerQty = rs.getInt("quantity");
                    buyerAvgPrice = rs.getDouble("avg_price");
                }
            }
        }

        if (buyerHasAsset) {
            // Update existing asset with new average price
            int newBuyerQty = buyerQty + contract.getQuantity();
            double newAvgPrice = (buyerAvgPrice * buyerQty + contract.getPricePerUnit() * contract.getQuantity())
                    / newBuyerQty;
            String updateBuyerSql = "UPDATE portfolio_assets SET quantity = ?, avg_price = ? WHERE portfolio_id = ? AND asset_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(updateBuyerSql)) {
                ps.setInt(1, newBuyerQty);
                ps.setDouble(2, newAvgPrice);
                ps.setLong(3, buyerPortfolioId);
                ps.setLong(4, contract.getAssetId());
                ps.executeUpdate();
            }
        } else {
            // Insert new asset
            String insertBuyerSql = "INSERT INTO portfolio_assets (portfolio_id, asset_id, quantity, avg_price) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertBuyerSql)) {
                ps.setLong(1, buyerPortfolioId);
                ps.setLong(2, contract.getAssetId());
                ps.setInt(3, contract.getQuantity());
                ps.setDouble(4, contract.getPricePerUnit());
                ps.executeUpdate();
            }
        }

        // 7. Update portfolio total values (on same transaction connection)
        updatePortfolioTotalValue(creatorPortfolioId, conn);
        updatePortfolioTotalValue(buyerPortfolioId, conn);

        System.out.println(
                "✅ Buyer acceptance processed: " + contract.getQuantity() + " assets transferred for " + totalCost);
    }

    /**
     * Seller acceptance: Acceptor sells assets to creator
     * Acceptor loses assets → Creator gains assets
     * Creator pays money → Acceptor receives money
     */
    private void processSellerAcceptance(P2PContract contract, long acceptorId, Connection conn) throws SQLException {
        long creatorId = contract.getCreatorId();
        double totalCost = contract.getPricePerUnit() * contract.getQuantity();
        BigDecimal amount = new BigDecimal(totalCost);

        // 1. Validate creator (buyer) has enough money
        String creatorWalletSql = "SELECT balance FROM wallet WHERE user_id = ? FOR UPDATE";
        try (PreparedStatement ps = conn.prepareStatement(creatorWalletSql)) {
            ps.setLong(1, creatorId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new RuntimeException("Buyer wallet not found");
                }
                BigDecimal balance = rs.getBigDecimal("balance");
                if (balance.compareTo(amount) < 0) {
                    throw new RuntimeException("Buyer insufficient funds. Need: " + totalCost + ", Have: " + balance);
                }
            }
        }

        // 2. Get acceptor (seller) portfolio and validate has assets
        String sellerPortfolioSql = "SELECT portfolio_id FROM portfolio WHERE user_id = ?";
        long sellerPortfolioId = -1;
        try (PreparedStatement ps = conn.prepareStatement(sellerPortfolioSql)) {
            ps.setLong(1, acceptorId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    sellerPortfolioId = rs.getLong("portfolio_id");
                }
            }
        }

        if (sellerPortfolioId == -1) {
            throw new RuntimeException("Seller portfolio not found");
        }

        // Check seller has assets (with FOR UPDATE lock)
        String checkAssetsSql = "SELECT quantity, avg_price FROM portfolio_assets WHERE portfolio_id = ? AND asset_id = ? FOR UPDATE";
        int sellerQty = 0;
        try (PreparedStatement ps = conn.prepareStatement(checkAssetsSql)) {
            ps.setLong(1, sellerPortfolioId);
            ps.setLong(2, contract.getAssetId());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next() || rs.getInt("quantity") < contract.getQuantity()) {
                    throw new RuntimeException("Seller insufficient assets. Need: " + contract.getQuantity());
                }
                sellerQty = rs.getInt("quantity");
            }
        }

        // 3. Deduct money from buyer (creator)
        String deductBuyerSql = "UPDATE wallet SET balance = balance - ? WHERE user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(deductBuyerSql)) {
            ps.setBigDecimal(1, amount);
            ps.setLong(2, creatorId);
            ps.executeUpdate();
        }

        // 4. Add money to seller (acceptor)
        String addSellerSql = "UPDATE wallet SET balance = balance + ? WHERE user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(addSellerSql)) {
            ps.setBigDecimal(1, amount);
            ps.setLong(2, acceptorId);
            ps.executeUpdate();
        }

        // 5. Remove assets from seller's portfolio
        int newSellerQty = sellerQty - contract.getQuantity();
        if (newSellerQty > 0) {
            String updateQtySql = "UPDATE portfolio_assets SET quantity = quantity - ? WHERE portfolio_id = ? AND asset_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(updateQtySql)) {
                ps.setInt(1, contract.getQuantity());
                ps.setLong(2, sellerPortfolioId);
                ps.setLong(3, contract.getAssetId());
                ps.executeUpdate();
            }
        } else {
            String deleteSql = "DELETE FROM portfolio_assets WHERE portfolio_id = ? AND asset_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(deleteSql)) {
                ps.setLong(1, sellerPortfolioId);
                ps.setLong(2, contract.getAssetId());
                ps.executeUpdate();
            }
        }

        // 6. Get buyer portfolio and add assets
        String buyerPortfolioSql = "SELECT portfolio_id FROM portfolio WHERE user_id = ?";
        long buyerPortfolioId = -1;
        try (PreparedStatement ps = conn.prepareStatement(buyerPortfolioSql)) {
            ps.setLong(1, creatorId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    buyerPortfolioId = rs.getLong("portfolio_id");
                }
            }
        }

        if (buyerPortfolioId == -1) {
            throw new RuntimeException("Buyer portfolio not found");
        }

        // Check if buyer already has this asset
        String checkBuyerAssetSql = "SELECT quantity, avg_price FROM portfolio_assets WHERE portfolio_id = ? AND asset_id = ? FOR UPDATE";
        boolean buyerHasAsset = false;
        int buyerQty = 0;
        double buyerAvgPrice = 0;
        try (PreparedStatement ps = conn.prepareStatement(checkBuyerAssetSql)) {
            ps.setLong(1, buyerPortfolioId);
            ps.setLong(2, contract.getAssetId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    buyerHasAsset = true;
                    buyerQty = rs.getInt("quantity");
                    buyerAvgPrice = rs.getDouble("avg_price");
                }
            }
        }

        if (buyerHasAsset) {
            // Update existing asset with new average price
            int newBuyerQty = buyerQty + contract.getQuantity();
            double newAvgPrice = (buyerAvgPrice * buyerQty + contract.getPricePerUnit() * contract.getQuantity())
                    / newBuyerQty;
            String updateBuyerSql = "UPDATE portfolio_assets SET quantity = ?, avg_price = ? WHERE portfolio_id = ? AND asset_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(updateBuyerSql)) {
                ps.setInt(1, newBuyerQty);
                ps.setDouble(2, newAvgPrice);
                ps.setLong(3, buyerPortfolioId);
                ps.setLong(4, contract.getAssetId());
                ps.executeUpdate();
            }
        } else {
            // Insert new asset
            String insertBuyerSql = "INSERT INTO portfolio_assets (portfolio_id, asset_id, quantity, avg_price) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertBuyerSql)) {
                ps.setLong(1, buyerPortfolioId);
                ps.setLong(2, contract.getAssetId());
                ps.setInt(3, contract.getQuantity());
                ps.setDouble(4, contract.getPricePerUnit());
                ps.executeUpdate();
            }
        }

        // 7. Update portfolio total values (on same transaction connection)
        updatePortfolioTotalValue(sellerPortfolioId, conn);
        updatePortfolioTotalValue(buyerPortfolioId, conn);

        System.out.println(
                "✅ Seller acceptance processed: " + contract.getQuantity() + " assets transferred for " + totalCost);
    }

    /**
     * Update portfolio total value on the current transaction connection
     * Calculates total as SUM(quantity * avg_price) - cost basis of what was
     * invested
     */
    private void updatePortfolioTotalValue(long portfolioId, Connection conn) throws SQLException {
        String sql = "UPDATE portfolio SET total_value = (" +
                "SELECT COALESCE(SUM(pa.quantity * pa.avg_price), 0) " +
                "FROM portfolio_assets pa " +
                "WHERE pa.portfolio_id = ?" +
                ") WHERE portfolio_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, portfolioId);
            ps.setLong(2, portfolioId);
            ps.executeUpdate();
        }
    }
}
