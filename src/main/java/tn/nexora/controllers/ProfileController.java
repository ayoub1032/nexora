package tn.nexora.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import tn.nexora.MainFX;
import tn.nexora.entities.User;
import tn.nexora.services.AuthService;
import tn.nexora.services.UserService;
import tn.nexora.utils.PasswordHasher;
import tn.nexora.utils.SessionManager;
import tn.nexora.utils.ValidationUtils;

import java.sql.*;
import java.time.format.DateTimeFormatter;

/**
 * Controller for the Profile screen
 */
public class ProfileController {

    // Top bar
    @FXML
    private Label userNameLabel;
    @FXML
    private Label verifiedBadge;

    // Profile card
    @FXML
    private Label profileNameLabel;
    @FXML
    private Label profileEmailLabel;
    @FXML
    private Label memberSinceLabel;
    @FXML
    private TextField fullNameField;
    @FXML
    private TextField emailProfileField;
    @FXML
    private TextField phoneProfileField;

    // Session info
    @FXML
    private Label sessionDeviceLabel;
    @FXML
    private Label sessionLocationLabel;

    // Password management
    @FXML
    private PasswordField currentPasswordField;
    @FXML
    private PasswordField newPasswordField;
    @FXML
    private PasswordField confirmNewPasswordField;

    // Login history table
    @FXML
    private TableView<LoginHistoryEntry> loginHistoryTable;
    @FXML
    private TableColumn<LoginHistoryEntry, String> actionColumn;
    @FXML
    private TableColumn<LoginHistoryEntry, String> deviceColumn;
    @FXML
    private TableColumn<LoginHistoryEntry, String> dateColumn;

    @FXML
    private Button logoutButton;

    private User currentUser;
    private UserService userService;
    private AuthService authService;

    @FXML
    public void initialize() {
        userService = new UserService();
        authService = new AuthService();
        currentUser = SessionManager.getInstance().getCurrentUser();

        if (currentUser != null) {
            loadUserProfile();
            loadLoginHistory();
        } else {
            // No user logged in, redirect to login
            MainFX.showLoginScreen();
        }
    }

    /**
     * Load user profile data
     */
    private void loadUserProfile() {
        try {
            // Top bar
            userNameLabel.setText(currentUser.getFull_name());
            if ("VERIFIE_18".equals(currentUser.getVerification_status())) {
                verifiedBadge.setText("● Verified Account");
                verifiedBadge.setStyle("-fx-text-fill: #10B981; -fx-font-size: 12px;");
            } else {
                verifiedBadge.setText("● Not Verified");
                verifiedBadge.setStyle("-fx-text-fill: #F59E0B; -fx-font-size: 12px;");
            }

            // Profile card
            profileNameLabel.setText(currentUser.getFull_name());
            profileEmailLabel.setText(currentUser.getEmail());

            // Format created_at date
            if (currentUser.getCreated_at() != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy");
                String year = currentUser.getCreated_at().toLocalDateTime().format(formatter);
                memberSinceLabel.setText("Member since " + year);
            }

            // Editable fields
            fullNameField.setText(currentUser.getFull_name());
            emailProfileField.setText(currentUser.getEmail());
            phoneProfileField.setText("+1 (555) 000-0000"); // Placeholder - phone not in User entity

            // Session info
            sessionDeviceLabel.setText(System.getProperty("os.name") + " PC");
            sessionLocationLabel.setText("JavaFX Desktop • Local Machine");
        } catch (Exception e) {
            System.err.println("❌ Error loading profile: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Failed to load profile data: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Load login history from database
     */
    private void loadLoginHistory() {
        ObservableList<LoginHistoryEntry> data = FXCollections.observableArrayList();

        String sql = "SELECT * FROM login_history WHERE user_id = ? ORDER BY created_at DESC LIMIT 10";

        try {
            Connection conn = tn.nexora.utils.MyConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setLong(1, currentUser.getUser_id());
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String action = rs.getString("status").equals("SUCCESS") ? "Login Attempt" : "Failed Login";
                String device = rs.getString("device_info");
                Timestamp timestamp = rs.getTimestamp("created_at");

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a");
                String date = timestamp.toLocalDateTime().format(formatter);

                data.add(new LoginHistoryEntry(action, device, date));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Setup table columns
        actionColumn.setCellValueFactory(new PropertyValueFactory<>("action"));
        deviceColumn.setCellValueFactory(new PropertyValueFactory<>("device"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));

        loginHistoryTable.setItems(data);
    }

    /**
     * Handle save profile button
     */
    @FXML
    private void handleSaveProfile() {
        String newName = fullNameField.getText().trim();
        String newEmail = emailProfileField.getText().trim();

        // Validate
        if (!ValidationUtils.isNotEmpty(newName)) {
            showAlert("Validation Error", "Name cannot be empty", Alert.AlertType.ERROR);
            return;
        }

        if (!ValidationUtils.isValidEmail(newEmail)) {
            showAlert("Validation Error", "Invalid email format", Alert.AlertType.ERROR);
            return;
        }

        // Update user object
        currentUser.setFull_name(newName);
        currentUser.setEmail(newEmail);

        // Update in database
        userService.updateUser(currentUser);

        // Reload profile
        loadUserProfile();

        showAlert("Success", "Profile updated successfully!", Alert.AlertType.INFORMATION);
    }

    /**
     * Handle update password button
     */
    @FXML
    private void handleUpdatePassword() {
        String currentPassword = currentPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmNewPasswordField.getText();

        // Validate current password
        if (!PasswordHasher.verify(currentPassword, currentUser.getPassword_hash())) {
            showAlert("Error", "Current password is incorrect", Alert.AlertType.ERROR);
            return;
        }

        // Validate new password
        if (!ValidationUtils.isStrongPassword(newPassword)) {
            showAlert("Weak Password",
                    "Password must be 8+ characters with uppercase, lowercase, number & special character",
                    Alert.AlertType.ERROR);
            return;
        }

        // Check passwords match
        if (!newPassword.equals(confirmPassword)) {
            showAlert("Error", "New passwords do not match", Alert.AlertType.ERROR);
            return;
        }

        // Hash new password
        String hashedPassword = PasswordHasher.hash(newPassword);

        // Update in database
        String sql = "UPDATE users SET password_hash = ? WHERE user_id = ?";
        try {
            Connection conn = tn.nexora.utils.MyConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, hashedPassword);
            ps.setLong(2, currentUser.getUser_id());
            ps.executeUpdate();

            // Update current user
            currentUser.setPassword_hash(hashedPassword);

            // Clear fields
            currentPasswordField.clear();
            newPasswordField.clear();
            confirmNewPasswordField.clear();

            showAlert("Success", "Password updated successfully!", Alert.AlertType.INFORMATION);
        } catch (SQLException e) {
            showAlert("Error", "Failed to update password: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Handle forgot password link
     */
    @FXML
    private void handleForgotPassword() {
        showAlert("Password Recovery", "Please contact support at support@nexora.com to reset your password.",
                Alert.AlertType.INFORMATION);
    }

    /**
     * Handle logout button
     */
    @FXML
    private void handleLogout() {
        authService.logout();
        MainFX.showLoginScreen();
    }

    /**
     * Show alert dialog
     */
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Inner class for login history table entries
     */
    public static class LoginHistoryEntry {
        private String action;
        private String device;
        private String date;

        public LoginHistoryEntry(String action, String device, String date) {
            this.action = action;
            this.device = device;
            this.date = date;
        }

        public String getAction() {
            return action;
        }

        public String getDevice() {
            return device;
        }

        public String getDate() {
            return date;
        }
    }
}
