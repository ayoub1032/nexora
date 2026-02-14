package tn.nexora.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import tn.nexora.MainFX;
import tn.nexora.entities.User;
import tn.nexora.services.AuthService;
import tn.nexora.utils.ValidationUtils;

/**
 * Controller for the Login screen
 */
public class LoginController {

    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button loginButton;
    @FXML
    private Button faceIdButton;
    @FXML
    private Hyperlink signUpLink;
    @FXML
    private Hyperlink forgotPasswordLink;
    @FXML
    private Label emailError;
    @FXML
    private Label passwordError;
    @FXML
    private Label errorMessage;

    private AuthService authService;

    @FXML
    public void initialize() {
        authService = new AuthService();

        // Add Enter key listener on password field
        passwordField.setOnAction(event -> handleLogin());
    }

    /**
     * Handle login button click
     */
    @FXML
    private void handleLogin() {
        // Clear previous errors
        clearErrors();

        String email = emailField.getText().trim();
        String password = passwordField.getText();

        // Validate inputs
        boolean isValid = true;

        if (!ValidationUtils.isValidEmail(email)) {
            emailError.setText("Please enter a valid email address");
            emailError.setVisible(true);
            isValid = false;
        }

        if (password.isEmpty()) {
            passwordError.setText("Password is required");
            passwordError.setVisible(true);
            isValid = false;
        }

        if (!isValid) {
            return;
        }

        // Disable button during authentication
        loginButton.setDisable(true);
        loginButton.setText("Logging in...");

        // Attempt login
        User user = authService.login(email, password);

        if (user != null) {
            // Check account status
            if ("SUSPENDED".equals(user.getAccount_status())) {
                errorMessage.setText("❌ Your account has been suspended. Please contact support.");
                errorMessage.setVisible(true);
                loginButton.setDisable(false);
                loginButton.setText("🔐 Log In");
                return;
            }

            // Login successful - navigate to main layout
            System.out.println("✅ Redirecting to main layout...");
            try {
                MainFX.showMainLayout();
            } catch (Exception e) {
                System.err.println("❌ Error loading main layout: " + e.getMessage());
                e.printStackTrace();
                errorMessage.setText("❌ Error loading main layout. Please restart the application.");
                errorMessage.setVisible(true);
                loginButton.setDisable(false);
                loginButton.setText("🔐 Log In");
            }
        } else {
            // Login failed
            errorMessage.setText("❌ Invalid email or password. Please try again.");
            errorMessage.setVisible(true);
            loginButton.setDisable(false);
            loginButton.setText("🔐 Log In");
        }
    }

    /**
     * Handle Face ID login (placeholder for future implementation)
     */
    @FXML
    private void handleFaceIdLogin() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Face ID Login");
        alert.setHeaderText("Feature Coming Soon");
        alert.setContentText("Face ID authentication will be available in a future update.");
        alert.showAndWait();
    }

    /**
     * Handle Sign Up link click
     */
    @FXML
    private void handleSignUp() {
        MainFX.showRegisterScreen();
    }

    /**
     * Handle Forgot Password link
     */
    @FXML
    private void handleForgotPassword() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Password Recovery");
        alert.setHeaderText("Reset Your Password");
        alert.setContentText("Please contact support at support@nexora.com to reset your password.");
        alert.showAndWait();
    }

    /**
     * Handle Need Assistance link
     */
    @FXML
    private void handleNeedAssistance() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Need Help?");
        alert.setHeaderText("Customer Support");
        alert.setContentText("For assistance, please email: support@nexora.com\nOr call: +1 (555) 987-6543");
        alert.showAndWait();
    }

    /**
     * Clear all error messages
     */
    private void clearErrors() {
        emailError.setVisible(false);
        passwordError.setVisible(false);
        errorMessage.setVisible(false);
    }
}
