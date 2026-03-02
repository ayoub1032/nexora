-- Portfolio Assets Table (if not already created)
-- This table tracks the assets owned by each portfolio

CREATE TABLE IF NOT EXISTS portfolio_assets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    portfolio_id BIGINT NOT NULL,
    asset_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 0,
    avg_price DOUBLE NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    UNIQUE KEY unique_portfolio_asset (portfolio_id, asset_id),
    FOREIGN KEY (portfolio_id) REFERENCES portfolio(portfolio_id) ON DELETE CASCADE,
    FOREIGN KEY (asset_id) REFERENCES asset(asset_id) ON DELETE CASCADE,
    
    INDEX idx_portfolio (portfolio_id),
    INDEX idx_asset (asset_id)
);
