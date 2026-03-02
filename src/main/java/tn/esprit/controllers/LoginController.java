package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import tn.esprit.entities.User;
import tn.esprit.services.AuthService;
import tn.esprit.utils.SceneNavigator;
import tn.esprit.utils.UserContext;

/**
 * Controller for login screen
 */
public class LoginController {

    @FXML
    private TextField tfEmail;
    @FXML
    private PasswordField tfPassword;
    @FXML
    private Label lblErrorMessage;

    private AuthService authService;

    @FXML
    public void initialize() {
        authService = new AuthService();
        lblErrorMessage.setText("");
    }

    /**
     * Handle login button click
     */
    @FXML
    public void handleLogin() {
        String email = tfEmail.getText().trim();
        String password = tfPassword.getText();

        if (email.isEmpty() || password.isEmpty()) {
            lblErrorMessage.setText("❌ Email and password are required");
            return;
        }

        try {
            User user = authService.login(email, password);

            if (user != null) {
                // Set user context
                UserContext.setCurrentUserId(user.getUserId());
                UserContext.setRole(user.getRole());

                // Navigate based on role
                if (user.getRole().equalsIgnoreCase("ADMIN")) {
                    SceneNavigator.goTo("AdminView.fxml", "PiWeb - Admin Dashboard", 1000, 600);
                } else {
                    SceneNavigator.goTo("UserMainLayout.fxml", "PiWeb - User Dashboard", 1300, 800);
                }
            } else {
                lblErrorMessage.setText("❌ Invalid email or password");
                tfPassword.clear();
            }
        } catch (Exception e) {
            lblErrorMessage.setText("❌ Login error: " + e.getMessage());
        }
    }

    /**
     * Handle register button click - navigate to registration screen
     */
    @FXML
    public void handleRegister() {
        SceneNavigator.goTo("RegisterView.fxml", "Create Account", 560, 600);
    }

    /**
     * Clear error message when user starts typing
     */
    @FXML
    public void onEmailChanged() {
        lblErrorMessage.setText("");
    }

    @FXML
    public void onPasswordChanged() {
        lblErrorMessage.setText("");
    }
}
