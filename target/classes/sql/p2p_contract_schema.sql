-- P2P Contract Table for peer-to-peer asset trading
-- Users can create contracts to buy or sell assets at freely set price and quantity

CREATE TABLE IF NOT EXISTS p2p_contract (
    contract_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    creator_id BIGINT NOT NULL,
    asset_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    price_per_unit DOUBLE NOT NULL,
    contract_type ENUM('BUY', 'SELL') NOT NULL COMMENT 'BUY = creator wants to buy, SELL = creator wants to sell',
    status ENUM('OPEN', 'ACCEPTED', 'CANCELLED', 'COMPLETED') DEFAULT 'OPEN',
    accepted_by BIGINT COMMENT 'User who accepted the contract',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    accepted_at TIMESTAMP NULL,
    completed_at TIMESTAMP NULL,
    
    FOREIGN KEY (creator_id) REFERENCES user(user_id) ON DELETE CASCADE,
    FOREIGN KEY (asset_id) REFERENCES asset(asset_id) ON DELETE CASCADE,
    FOREIGN KEY (accepted_by) REFERENCES user(user_id) ON DELETE SET NULL,
    
    INDEX idx_creator (creator_id),
    INDEX idx_asset (asset_id),
    INDEX idx_status (status),
    INDEX idx_accepted_by (accepted_by)
);

-- Contract Type Explanation:
-- SELL: Creator is selling assets. Acceptor buys by paying money to creator.
--       Creator must have quantity in portfolio. Acceptor must have enough money.
--
-- BUY: Creator is buying assets. Acceptor sells by giving assets to creator.
--       Creator must have enough money. Acceptor must have quantity in portfolio.
