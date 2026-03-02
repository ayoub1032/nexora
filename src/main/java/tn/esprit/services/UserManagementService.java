package tn.esprit.services;

import java.util.List;

import tn.esprit.entities.Order;
import tn.esprit.entities.Portfolio;
import tn.esprit.entities.User;
import tn.esprit.entities.Wallet;

/**
 * Orchestrates user lifecycle management - creates users with auto-generated wallet and portfolio,
 * and handles cascade deletion of associated resources
 */
public class UserManagementService {
    private final UserService userService;
    private final WalletService walletService;
    private final PortfolioService portfolioService;
    private final OrderService orderService;

    public UserManagementService() {
        this.userService = new UserService();
        this.walletService = new WalletService();
        this.portfolioService = new PortfolioService();
        this.orderService = new OrderService();
    }

    /**
     * Create a new user with automatically generated wallet and portfolio
     */
    public User createUserWithDependencies(User user) {
        try {
            // 1. Create the user
            long userId = userService.addUser(user);
            if (userId == -1) {
                throw new RuntimeException("Failed to create user");
            }

            // 2. Auto-create wallet (empty, balance = 0)
            long walletId = walletService.createWallet(userId);
            if (walletId == -1) {
                // Rollback user creation if wallet fails
                userService.delete(userId);
                throw new RuntimeException("Failed to create wallet for user");
            }

            // 3. Auto-create portfolio (empty)
            Portfolio portfolio = new Portfolio(userId);
            long portfolioId = portfolioService.add(portfolio);
            if (portfolioId == -1) {
                // Rollback if portfolio fails
                userService.delete(userId);
                walletService.deleteByUserId(userId);
                throw new RuntimeException("Failed to create portfolio for user");
            }

            // Retrieve and return the created user
            user.setUserId(userId);
            return userService.findById(userId);

        } catch (Exception e) {
            throw new RuntimeException("Error in user creation process: " + e.getMessage(), e);
        }
    }

    /**
     * Delete a user and cascade-delete their wallet, portfolio, and orders
     */
    public void deleteUserWithDependencies(long userId) {
        try {
            // 1. Delete all orders for this user
            List<Order> userOrders = orderService.findByUserId(userId);
            for (Order order : userOrders) {
                orderService.delete(order.getOrderId());
            }

            // 2. Delete portfolio
            Portfolio portfolio = portfolioService.findByUserId(userId);
            if (portfolio != null) {
                portfolioService.delete(portfolio.getPortfolioId());
            }

            // 3. Delete wallet
            walletService.deleteByUserId(userId);

            // 4. Delete user
            userService.delete(userId);

        } catch (Exception e) {
            throw new RuntimeException("Error deleting user and dependencies: " + e.getMessage(), e);
        }
    }

    /**
     * Update user role (ADMIN, USER)
     */
    public void updateUserRole(long userId, String role) {
        User user = userService.findById(userId);
        if (user != null) {
            user.setRole(role);
            userService.update(user);
        } else {
            throw new RuntimeException("User not found with ID: " + userId);
        }
    }

    /**
     * Update user account status (ACTIVE, INACTIVE)
     */
    public void updateUserStatus(long userId, String status) {
        User user = userService.findById(userId);
        if (user != null) {
            user.setAccountStatus(status);
            userService.update(user);
        } else {
            throw new RuntimeException("User not found with ID: " + userId);
        }
    }

    /**
     * Get user with their wallet and portfolio info
     */
    public UserProfile getUserProfile(long userId) {
        User user = userService.findById(userId);
        if (user == null) {
            return null;
        }

        Wallet wallet = walletService.findByUserId(userId);
        Portfolio portfolio = portfolioService.findByUserId(userId);

        return new UserProfile(user, wallet, portfolio);
    }

    /**
     * Get all users
     */
    public List<User> getAllUsers() {
        return userService.getAll();
    }

    /**
     * Find user by email
     */
    public User findUserByEmail(String email) {
        return userService.findByEmail(email);
    }

    /**
     * Inner class to hold user with wallet and portfolio data
     */
    public static class UserProfile {
        public User user;
        public Wallet wallet;
        public Portfolio portfolio;

        public UserProfile(User user, Wallet wallet, Portfolio portfolio) {
            this.user = user;
            this.wallet = wallet;
            this.portfolio = portfolio;
        }
    }
}
