-- ========================================
-- Create Admin Account Setup
-- ========================================
-- 
-- Default Admin Credentials:
-- Email: admin@piwebapp.com
-- Password: Admin@12345! (change this after login)
-- 
-- Instructions:
-- 1. Generate BCrypt hash by running the HashGeneratorUtil.java in your IDE
-- 2. Copy the generated hash
-- 3. Replace 'GENERATED_BCRYPT_HASH_HERE' below with the actual hash
-- 4. Run this SQL in your MySQL database
--
-- ========================================

INSERT INTO users (full_name, email, password_hash, role, verification_status, account_status, created_at)
VALUES (
  'System Administrator',
  'admin@piwebapp.com',
  '$2a$12$Cm3VKaoWXvcqVrzX2y7/W.fI9oZ3WKm2.yzwkRu3tCzM2EJv7p2fm',
  'ADMIN',
  'VERIFIED',
  'ACTIVE',
  CURRENT_TIMESTAMP
);

-- Get the newly created admin user ID (run this to see it)
SELECT @admin_id := user_id FROM users WHERE email = 'admin@piwebapp.com' LIMIT 1;

-- Auto-create wallet for admin
INSERT INTO wallet (user_id, balance, reserved_balance, created_at)
VALUES (@admin_id, 10000.0000, 0.0000, CURRENT_TIMESTAMP);

-- Auto-create portfolio for admin
INSERT INTO portfolio (user_id, total_value, created_at)
VALUES (@admin_id, 0.0000, CURRENT_TIMESTAMP);

-- Verify creation (run this to confirm)
SELECT u.user_id, u.full_name, u.email, u.role, w.balance, p.portfolio_id
FROM users u
LEFT JOIN wallet w ON u.user_id = w.user_id
LEFT JOIN portfolio p ON u.user_id = p.user_id
WHERE u.email = 'admin@piwebapp.com';
