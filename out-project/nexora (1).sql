-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Feb 13, 2026 at 12:37 PM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `nexora`
--

-- --------------------------------------------------------

--
-- Table structure for table `assets`
--

CREATE TABLE `assets` (
  `asset_id` bigint(20) NOT NULL,
  `symbol` varchar(10) NOT NULL,
  `name` varchar(50) NOT NULL,
  `market_price` decimal(18,8) NOT NULL,
  `is_active` tinyint(1) DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `login_history`
--

CREATE TABLE `login_history` (
  `log_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `ip_address` varchar(45) NOT NULL,
  `device_info` varchar(255) NOT NULL,
  `status` enum('SUCCESS','FAILED') NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `orders`
--

CREATE TABLE `orders` (
  `order_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `asset_id` bigint(20) NOT NULL,
  `side` enum('BUY','SELL') NOT NULL,
  `order_type` enum('MARKET','LIMIT') NOT NULL,
  `price` decimal(18,8) DEFAULT NULL,
  `quantity` decimal(18,8) NOT NULL,
  `status` enum('PENDING','EXECUTED','CANCELED') DEFAULT 'PENDING',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `p2p_contracts`
--

CREATE TABLE `p2p_contracts` (
  `contract_id` bigint(20) NOT NULL,
  `p2p_order_id` bigint(20) NOT NULL,
  `buyer_id` bigint(20) NOT NULL,
  `seller_id` bigint(20) NOT NULL,
  `agreed_price` decimal(18,8) NOT NULL,
  `agreed_quantity` decimal(18,8) NOT NULL,
  `total_amount` decimal(18,8) NOT NULL,
  `payment_proof_path` varchar(255) DEFAULT NULL,
  `status` enum('ACTIVE','PAID','CONFIRMED','DISPUTE','CANCELED') DEFAULT 'ACTIVE',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `expires_at` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `p2p_messages`
--

CREATE TABLE `p2p_messages` (
  `message_id` bigint(20) NOT NULL,
  `contract_id` bigint(20) NOT NULL,
  `sender_id` bigint(20) NOT NULL,
  `content` text NOT NULL,
  `is_flagged` tinyint(1) DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `p2p_orders`
--

CREATE TABLE `p2p_orders` (
  `p2p_order_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `asset_id` bigint(20) NOT NULL,
  `order_type` enum('BUY','SELL') NOT NULL,
  `price` decimal(18,8) NOT NULL,
  `quantity` decimal(18,8) NOT NULL,
  `min_limit` decimal(18,8) NOT NULL,
  `max_limit` decimal(18,8) NOT NULL,
  `status` enum('OPEN','PARTIAL','CLOSED','CANCELED') DEFAULT 'OPEN',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `reclamation`
--

CREATE TABLE `reclamation` (
  `reclamation_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `subject` varchar(150) NOT NULL,
  `description` text NOT NULL,
  `type` enum('TRADING','WALLET','P2P') NOT NULL,
  `attachment_path` varchar(255) DEFAULT NULL,
  `status` enum('OPEN','IN_PROGRESS','RESOLVED') DEFAULT 'OPEN',
  `admin_response` text DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `transactions`
--

CREATE TABLE `transactions` (
  `transaction_id` bigint(20) NOT NULL,
  `wallet_id` bigint(20) NOT NULL,
  `asset_id` bigint(20) DEFAULT NULL,
  `amount` decimal(18,8) NOT NULL,
  `transaction_type` enum('BUY','SELL','P2P','FEE') NOT NULL,
  `reference_id` bigint(20) DEFAULT NULL,
  `invoice_number` varchar(50) DEFAULT NULL,
  `status` enum('CONFIRMED','CANCELED') DEFAULT 'CONFIRMED',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `user_id` bigint(20) NOT NULL,
  `full_name` varchar(100) NOT NULL,
  `email` varchar(150) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `role` enum('TRADER','ADMIN') DEFAULT 'TRADER',
  `verification_status` enum('NON_VERIFIE','VERIFIE_18') DEFAULT 'NON_VERIFIE',
  `account_status` enum('ACTIVE','SUSPENDED') DEFAULT 'ACTIVE',
  `face_id_data` text DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wallets`
--

CREATE TABLE `wallets` (
  `wallet_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `balance` decimal(18,8) NOT NULL DEFAULT 0.00000000,
  `reserved_balance` decimal(18,8) NOT NULL DEFAULT 0.00000000,
  `qr_code_data` varchar(255) NOT NULL,
  `risk_status` enum('LOW','MEDIUM','HIGH') DEFAULT 'LOW',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `assets`
--
ALTER TABLE `assets`
  ADD PRIMARY KEY (`asset_id`),
  ADD UNIQUE KEY `symbol` (`symbol`);

--
-- Indexes for table `login_history`
--
ALTER TABLE `login_history`
  ADD PRIMARY KEY (`log_id`),
  ADD KEY `fk_login_user` (`user_id`);

--
-- Indexes for table `orders`
--
ALTER TABLE `orders`
  ADD PRIMARY KEY (`order_id`),
  ADD KEY `fk_order_user` (`user_id`),
  ADD KEY `fk_order_asset` (`asset_id`);

--
-- Indexes for table `p2p_contracts`
--
ALTER TABLE `p2p_contracts`
  ADD PRIMARY KEY (`contract_id`),
  ADD KEY `fk_contract_order` (`p2p_order_id`),
  ADD KEY `fk_contract_buyer` (`buyer_id`),
  ADD KEY `fk_contract_seller` (`seller_id`);

--
-- Indexes for table `p2p_messages`
--
ALTER TABLE `p2p_messages`
  ADD PRIMARY KEY (`message_id`),
  ADD KEY `fk_message_contract` (`contract_id`),
  ADD KEY `fk_message_sender` (`sender_id`);

--
-- Indexes for table `p2p_orders`
--
ALTER TABLE `p2p_orders`
  ADD PRIMARY KEY (`p2p_order_id`),
  ADD KEY `fk_p2p_order_user` (`user_id`),
  ADD KEY `fk_p2p_order_asset` (`asset_id`);

--
-- Indexes for table `reclamation`
--
ALTER TABLE `reclamation`
  ADD PRIMARY KEY (`reclamation_id`),
  ADD KEY `fk_reclamation_user` (`user_id`);

--
-- Indexes for table `transactions`
--
ALTER TABLE `transactions`
  ADD PRIMARY KEY (`transaction_id`),
  ADD UNIQUE KEY `invoice_number` (`invoice_number`),
  ADD KEY `fk_transaction_wallet` (`wallet_id`),
  ADD KEY `fk_transaction_asset` (`asset_id`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`user_id`),
  ADD UNIQUE KEY `email` (`email`);

--
-- Indexes for table `wallets`
--
ALTER TABLE `wallets`
  ADD PRIMARY KEY (`wallet_id`),
  ADD UNIQUE KEY `user_id` (`user_id`),
  ADD UNIQUE KEY `qr_code_data` (`qr_code_data`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `assets`
--
ALTER TABLE `assets`
  MODIFY `asset_id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `login_history`
--
ALTER TABLE `login_history`
  MODIFY `log_id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `orders`
--
ALTER TABLE `orders`
  MODIFY `order_id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `p2p_contracts`
--
ALTER TABLE `p2p_contracts`
  MODIFY `contract_id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `p2p_messages`
--
ALTER TABLE `p2p_messages`
  MODIFY `message_id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `p2p_orders`
--
ALTER TABLE `p2p_orders`
  MODIFY `p2p_order_id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `reclamation`
--
ALTER TABLE `reclamation`
  MODIFY `reclamation_id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `transactions`
--
ALTER TABLE `transactions`
  MODIFY `transaction_id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `user_id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wallets`
--
ALTER TABLE `wallets`
  MODIFY `wallet_id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `login_history`
--
ALTER TABLE `login_history`
  ADD CONSTRAINT `fk_login_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE;

--
-- Constraints for table `orders`
--
ALTER TABLE `orders`
  ADD CONSTRAINT `fk_order_asset` FOREIGN KEY (`asset_id`) REFERENCES `assets` (`asset_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_order_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE;

--
-- Constraints for table `p2p_contracts`
--
ALTER TABLE `p2p_contracts`
  ADD CONSTRAINT `fk_contract_buyer` FOREIGN KEY (`buyer_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_contract_order` FOREIGN KEY (`p2p_order_id`) REFERENCES `p2p_orders` (`p2p_order_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_contract_seller` FOREIGN KEY (`seller_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE;

--
-- Constraints for table `p2p_messages`
--
ALTER TABLE `p2p_messages`
  ADD CONSTRAINT `fk_message_contract` FOREIGN KEY (`contract_id`) REFERENCES `p2p_contracts` (`contract_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_message_sender` FOREIGN KEY (`sender_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE;

--
-- Constraints for table `p2p_orders`
--
ALTER TABLE `p2p_orders`
  ADD CONSTRAINT `fk_p2p_order_asset` FOREIGN KEY (`asset_id`) REFERENCES `assets` (`asset_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_p2p_order_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE;

--
-- Constraints for table `reclamation`
--
ALTER TABLE `reclamation`
  ADD CONSTRAINT `fk_reclamation_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE;

--
-- Constraints for table `transactions`
--
ALTER TABLE `transactions`
  ADD CONSTRAINT `fk_transaction_asset` FOREIGN KEY (`asset_id`) REFERENCES `assets` (`asset_id`) ON DELETE SET NULL,
  ADD CONSTRAINT `fk_transaction_wallet` FOREIGN KEY (`wallet_id`) REFERENCES `wallets` (`wallet_id`) ON DELETE CASCADE;

--
-- Constraints for table `wallets`
--
ALTER TABLE `wallets`
  ADD CONSTRAINT `fk_wallet_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
