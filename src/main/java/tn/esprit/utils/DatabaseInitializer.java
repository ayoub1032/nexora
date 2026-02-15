package tn.esprit.utils;

import java.sql.Connection;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void initializeDatabase() {
        try (Connection conn = DBConnection.getConnection()) {
            if (conn != null) {
                try (Statement stmt = conn.createStatement()) {
                    // Create portfolio_assets table if it doesn't exist
                    String portfolioAssetsSQL = "CREATE TABLE IF NOT EXISTS portfolio_assets (" +
                            "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                            "portfolio_id BIGINT NOT NULL, " +
                            "asset_id BIGINT NOT NULL, " +
                            "quantity INT NOT NULL DEFAULT 0, " +
                            "avg_price DOUBLE NOT NULL DEFAULT 0, " +
                            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                            "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, " +
                            "UNIQUE KEY unique_portfolio_asset (portfolio_id, asset_id), " +
                            "FOREIGN KEY (portfolio_id) REFERENCES portfolio(portfolio_id) ON DELETE CASCADE, " +
                            "FOREIGN KEY (asset_id) REFERENCES asset(asset_id) ON DELETE CASCADE, "+
                            "INDEX idx_portfolio (portfolio_id), " +
                            "INDEX idx_asset (asset_id)" +
                            ")";

                    stmt.execute(portfolioAssetsSQL);
                    
                    // Create p2p_contract table if it doesn't exist
                    String p2pContractSQL = "CREATE TABLE IF NOT EXISTS p2p_contract (" +
                            "contract_id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                            "creator_id BIGINT NOT NULL, " +
                            "asset_id BIGINT NOT NULL, " +
                            "quantity INT NOT NULL, " +
                            "price_per_unit DOUBLE NOT NULL, " +
                            "contract_type ENUM('BUY', 'SELL') NOT NULL, " +
                            "status ENUM('OPEN', 'ACCEPTED', 'CANCELLED', 'COMPLETED') DEFAULT 'OPEN', " +
                            "accepted_by BIGINT, " +
                            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                            "accepted_at TIMESTAMP NULL, " +
                            "completed_at TIMESTAMP NULL, " +
                            "FOREIGN KEY (creator_id) REFERENCES user(user_id) ON DELETE CASCADE, " +
                            "FOREIGN KEY (asset_id) REFERENCES asset(asset_id) ON DELETE CASCADE, " +
                            "FOREIGN KEY (accepted_by) REFERENCES user(user_id) ON DELETE SET NULL, " +
                            "INDEX idx_creator (creator_id), " +
                            "INDEX idx_asset (asset_id), " +
                            "INDEX idx_status (status), " +
                            "INDEX idx_accepted_by (accepted_by)" +
                            ")";

                    stmt.execute(p2pContractSQL);
                    System.out.println("✅ Database tables initialized successfully");
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ Warning: Could not initialize database tables: " + e.getMessage());
            // Don't crash the app if tables already exist or other issues
        }
    }
}
