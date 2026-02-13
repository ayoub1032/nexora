package tn.nexora.controllers;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import tn.nexora.MainFX;
import tn.nexora.entities.User;
import tn.nexora.services.AuthService;
import tn.nexora.services.UserService;
import tn.nexora.utils.PasswordHasher;
import tn.nexora.utils.ValidationUtils;

import java.time.LocalDate;

/**
 * Controller for the multi-step registration wizard
 */
public class RegisterController {

    @FXML
    private VBox contentContainer;
    @FXML
    private VBox step1Box, step2Box, step3Box, step4Box;
    @FXML
    private Label step1Number, step2Number, step3Number, step4Number;

    private int currentStep = 1;

    // Step 1 fields
    private TextField emailField;
    private PasswordField passwordField, confirmPasswordField;
    private Label emailError, passwordError, confirmPasswordError;

    // Step 2 fields
    private TextField firstNameField, lastNameField, streetAddressField;
    private TextField cityField, postalCodeField, phoneField;
    private DatePicker birthDatePicker;
    private ComboBox<String> nationalityCombo;
    private Label step2Error;

    // Step 3 fields
    private Label verificationMessage;

    // Step 4 fields
    private CheckBox twoFactorCheckbox, faceIdCheckbox, termsCheckbox;
    private Label step4Error;

    // Services
    private UserService userService;
    private AuthService authService;

    // User data storage
    private String registrationEmail;
    private String registrationPassword;
    private String registrationFullName;
    private LocalDate birthDate;
    private String nationality;
    private String address;
    private String phone;

    @FXML
    public void initialize() {
        userService = new UserService();
        authService = new AuthService();
        loadStep1();
    }

    /**
     * Load Step 1: Account Setup
     */
    private void loadStep1() {
        currentStep = 1;
        updateSidebarProgress();

        VBox step = new VBox(25);
        step.setAlignment(Pos.CENTER);
        step.setMaxWidth(500);

        // Header
        Label title = new Label("Create Your Account");
        title.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #1F2937;");

        Label subtitle = new Label("Enter your credentials to get started with Nexora");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #6B7280;");

        // Email field
        VBox emailBox = createFieldBox("EMAIL ADDRESS", emailField = createTextField("name@example.com"),
                emailError = createErrorLabel());

        // Password field
        VBox passwordBox = createFieldBox("PASSWORD", passwordField = createPasswordField("Minimum 8 characters"),
                passwordError = createErrorLabel());

        // Confirm Password field
        VBox confirmPasswordBox = createFieldBox("CONFIRM PASSWORD",
                confirmPasswordField = createPasswordField("Re-enter password"),
                confirmPasswordError = createErrorLabel());

        // Navigation buttons
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);

        Button backButton = new Button("← Back to Login");
        backButton.getStyleClass().addAll("btn", "btn-secondary");
        backButton.setPrefWidth(200);
        backButton.setOnAction(e -> MainFX.showLoginScreen());

        Button continueButton = new Button("Continue →");
        continueButton.getStyleClass().addAll("btn", "btn-primary");
        continueButton.setPrefWidth(200);
        continueButton.setOnAction(e -> validateStep1());

        buttonBox.getChildren().addAll(backButton, continueButton);

        step.getChildren().addAll(title, subtitle, emailBox, passwordBox, confirmPasswordBox, buttonBox);
        contentContainer.getChildren().clear();
        contentContainer.getChildren().add(step);
    }

    /**
     * Validate Step 1 and proceed to Step 2
     */
    private void validateStep1() {
        clearErrors();

        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        boolean isValid = true;

        // Email validation
        if (!ValidationUtils.isValidEmail(email)) {
            emailError.setText("Please enter a valid email address");
            emailError.setVisible(true);
            isValid = false;
        } else if (authService.emailExists(email)) {
            emailError.setText("This email is already registered");
            emailError.setVisible(true);
            isValid = false;
        }

        // Password validation
        if (!ValidationUtils.isStrongPassword(password)) {
            passwordError.setText("Password must be 8+ chars with uppercase, lowercase, number & special char");
            passwordError.setVisible(true);
            isValid = false;
        }

        // Confirm password
        if (!password.equals(confirmPassword)) {
            confirmPasswordError.setText("Passwords do not match");
            confirmPasswordError.setVisible(true);
            isValid = false;
        }

        if (isValid) {
            registrationEmail = email;
            registrationPassword = password;
            loadStep2();
        }
    }

    /**
     * Load Step 2: Personal Information
     */
    private void loadStep2() {
        currentStep = 2;
        updateSidebarProgress();

        VBox step = new VBox(20);
        step.setAlignment(Pos.TOP_CENTER);
        step.setMaxWidth(600);

        // Header
        Label title = new Label("Tell us about yourself");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #1F2937;");

        Label subtitle = new Label(
                "We are required by law to collect this information for KYC compliance and to secure your account.");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #6B7280;");
        subtitle.setWrapText(true);

        // Name fields - side by side
        HBox nameBox = new HBox(15);
        firstNameField = createTextField("e.g. Jonathan");
        lastNameField = createTextField("e.g. Smith");
        nameBox.getChildren().addAll(
                createFieldBox("LEGAL FIRST NAME", firstNameField, createErrorLabel()),
                createFieldBox("LEGAL LAST NAME", lastNameField, createErrorLabel()));

        // Birth date and nationality
        HBox birthNatBox = new HBox(15);
        birthDatePicker = new DatePicker();
        birthDatePicker.getStyleClass().add("input-field");
        birthDatePicker.setPromptText("mm/dd/yyyy");
        birthDatePicker.setPrefWidth(280);

        nationalityCombo = new ComboBox<>();
        nationalityCombo.getItems().addAll("United States", "Canada", "United Kingdom", "France", "Germany", "Tunisia",
                "Other");
        nationalityCombo.getStyleClass().add("input-field");
        nationalityCombo.setPrefWidth(280);

        birthNatBox.getChildren().addAll(
                createFieldBox("DATE OF BIRTH", birthDatePicker, createErrorLabel()),
                createFieldBox("NATIONALITY", nationalityCombo, createErrorLabel()));

        // Address
        VBox addressBox = createFieldBox("STREET ADDRESS",
                streetAddressField = createTextField("123 Crypto Blvd, Suite 100"), createErrorLabel());

        // City and postal code
        HBox cityPostalBox = new HBox(15);
        cityField = createTextField("");
        postalCodeField = createTextField("");
        cityPostalBox.getChildren().addAll(
                createFieldBox("CITY", cityField, createErrorLabel()),
                createFieldBox("POSTAL CODE", postalCodeField, createErrorLabel()));

        // Phone number
        VBox phoneBox = createFieldBox("PHONE NUMBER", phoneField = createTextField("+1 (555) 987-6543"),
                createErrorLabel());
        Label phoneNote = new Label("ℹ Used for account recovery and security alerts only.");
        phoneNote.setStyle("-fx-text-fill: #3B82F6; -fx-font-size: 11px;");
        phoneBox.getChildren().add(phoneNote);

        step2Error = createErrorLabel();

        // Navigation
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        VBox.setMargin(buttonBox, new Insets(20, 0, 0, 0));

        Button backButton = new Button("← Back");
        backButton.getStyleClass().addAll("btn", "btn-secondary");
        backButton.setPrefWidth(150);
        backButton.setOnAction(e -> loadStep1());

        Button continueButton = new Button("Continue →");
        continueButton.getStyleClass().addAll("btn", "btn-primary");
        continueButton.setPrefWidth(150);
        continueButton.setOnAction(e -> validateStep2());

        buttonBox.getChildren().addAll(backButton, continueButton);

        step.getChildren().addAll(title, subtitle, nameBox, birthNatBox, addressBox, cityPostalBox, phoneBox,
                step2Error, buttonBox);
        contentContainer.getChildren().clear();
        contentContainer.getChildren().add(step);
    }

    /**
     * Validate Step 2 and proceed to Step 3
     */
    private void validateStep2() {
        clearErrors();

        boolean isValid = true;

        if (!ValidationUtils.isNotEmpty(firstNameField.getText())) {
            step2Error.setText("First name is required");
            step2Error.setVisible(true);
            isValid = false;
        } else if (!ValidationUtils.isNotEmpty(lastNameField.getText())) {
            step2Error.setText("Last name is required");
            step2Error.setVisible(true);
            isValid = false;
        } else if (birthDatePicker.getValue() == null) {
            step2Error.setText("Date of birth is required");
            step2Error.setVisible(true);
            isValid = false;
        } else if (!ValidationUtils.is18Plus(birthDatePicker.getValue())) {
            step2Error.setText("❌ You must be 18 years or older to create an account");
            step2Error.setVisible(true);
            isValid = false;
        } else if (nationalityCombo.getValue() == null) {
            step2Error.setText("Please select your nationality");
            step2Error.setVisible(true);
            isValid = false;
        } else if (!ValidationUtils.isValidPhone(phoneField.getText())) {
            step2Error.setText("Please enter a valid phone number");
            step2Error.setVisible(true);
            isValid = false;
        }

        if (isValid) {
            registrationFullName = firstNameField.getText() + " " + lastNameField.getText();
            birthDate = birthDatePicker.getValue();
            nationality = nationalityCombo.getValue();
            address = streetAddressField.getText() + ", " + cityField.getText() + ", " + postalCodeField.getText();
            phone = phoneField.getText();
            loadStep3();
        }
    }

    /**
     * Load Step 3: Identity Verification
     */
    private void loadStep3() {
        currentStep = 3;
        updateSidebarProgress();

        VBox step = new VBox(25);
        step.setAlignment(Pos.CENTER);
        step.setMaxWidth(500);

        Label title = new Label("Identity Verification");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #1F2937;");

        Label subtitle = new Label("KYC Compliance Check");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #6B7280;");

        // Verification info
        VBox infoBox = new VBox(15);
        infoBox.setStyle("-fx-background-color: #F0FDF4; -fx-background-radius: 8px; -fx-padding: 20px;");
        infoBox.setAlignment(Pos.CENTER);

        Label checkIcon = new Label("✓");
        checkIcon.setStyle("-fx-font-size: 48px; -fx-text-fill: #10B981;");

        verificationMessage = new Label("Age Verification: PASSED\nYou are eligible for Nexora account");
        verificationMessage.setStyle(
                "-fx-font-size: 14px; -fx-text-fill: #059669; -fx-font-weight: bold; -fx-text-alignment: center;");
        verificationMessage.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        Label note = new Label("Your verification status will be set to VERIFIE_18");
        note.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7280;");

        infoBox.getChildren().addAll(checkIcon, verificationMessage, note);

        // Navigation
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);

        Button backButton = new Button("← Back");
        backButton.getStyleClass().addAll("btn", "btn-secondary");
        backButton.setPrefWidth(150);
        backButton.setOnAction(e -> loadStep2());

        Button continueButton = new Button("Continue →");
        continueButton.getStyleClass().addAll("btn", "btn-primary");
        continueButton.setPrefWidth(150);
        continueButton.setOnAction(e -> loadStep4());

        buttonBox.getChildren().addAll(backButton, continueButton);

        step.getChildren().addAll(title, subtitle, infoBox, buttonBox);
        contentContainer.getChildren().clear();
        contentContainer.getChildren().add(step);
    }

    /**
     * Load Step 4: Security Settings
     */
    private void loadStep4() {
        currentStep = 4;
        updateSidebarProgress();

        VBox step = new VBox(25);
        step.setAlignment(Pos.CENTER);
        step.setMaxWidth(500);

        Label title = new Label("Security Settings");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #1F2937;");

        Label subtitle = new Label("Protect your account with additional security features");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #6B7280;");

        // Security options
        VBox optionsBox = new VBox(20);

        twoFactorCheckbox = new CheckBox("Enable Two-Factor Authentication (2FA)");
        twoFactorCheckbox.setStyle("-fx-font-size: 14px; -fx-text-fill: #1F2937;");
        Label twoFactorNote = new Label("Secure your account with an authentication app like Google Authenticator");
        twoFactorNote.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7280; -fx-padding: 0 0 0 25px;");

        faceIdCheckbox = new CheckBox("Enable Facial Recognition Login");
        faceIdCheckbox.setStyle("-fx-font-size: 14px; -fx-text-fill: #1F2937;");
        Label faceIdNote = new Label("Use biometric data for faster login (requires a compatible webcam)");
        faceIdNote.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7280; -fx-padding: 0 0 0 25px;");

        optionsBox.getChildren().addAll(twoFactorCheckbox, twoFactorNote, faceIdCheckbox, faceIdNote);

        // Terms & Conditions
        termsCheckbox = new CheckBox();
        Label termsLabel = new Label("I agree to the Terms & Conditions and Privacy Policy");
        termsLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #1F2937;");
        HBox termsBox = new HBox(10, termsCheckbox, termsLabel);
        termsBox.setAlignment(Pos.CENTER_LEFT);

        step4Error = createErrorLabel();

        // Navigation
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);

        Button backButton = new Button("← Back");
        backButton.getStyleClass().addAll("btn", "btn-secondary");
        backButton.setPrefWidth(150);
        backButton.setOnAction(e -> loadStep3());

        Button createAccountButton = new Button("Create Account");
        createAccountButton.getStyleClass().addAll("btn", "btn-primary");
        createAccountButton.setPrefWidth(200);
        createAccountButton.setOnAction(e -> createAccount());

        buttonBox.getChildren().addAll(backButton, createAccountButton);

        step.getChildren().addAll(title, subtitle, optionsBox, termsBox, step4Error, buttonBox);
        contentContainer.getChildren().clear();
        contentContainer.getChildren().add(step);
    }

    /**
     * Create the user account
     */
    private void createAccount() {
        clearErrors();

        if (!termsCheckbox.isSelected()) {
            step4Error.setText("You must accept the Terms & Conditions to proceed");
            step4Error.setVisible(true);
            return;
        }

        // Hash password
        String hashedPassword = PasswordHasher.hash(registrationPassword);

        // Create user object
        User newUser = new User(
                registrationFullName,
                registrationEmail,
                hashedPassword,
                "TRADER",
                "VERIFIE_18", // Since we verified they're 18+
                "ACTIVE",
                faceIdCheckbox.isSelected() ? "ENROLLED" : null);

        // Add user to database
        userService.addUser(newUser);

        // Show success message
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Account Created!");
        alert.setHeaderText("Welcome to Nexora!");
        alert.setContentText("Your account has been successfully created.\n\nEmail: " + registrationEmail + "\nName: "
                + registrationFullName);
        alert.showAndWait();

        // Navigate to login
        MainFX.showLoginScreen();
    }

    /**
     * Update sidebar progress based on current step
     */
    private void updateSidebarProgress() {
        // Reset all steps
        step1Box.getStyleClass().clear();
        step2Box.getStyleClass().clear();
        step3Box.getStyleClass().clear();
        step4Box.getStyleClass().clear();

        step1Box.getStyleClass().addAll("sidebar-step",
                currentStep >= 1 ? (currentStep == 1 ? "active" : "completed") : "");
        step2Box.getStyleClass().addAll("sidebar-step",
                currentStep >= 2 ? (currentStep == 2 ? "active" : "completed") : "");
        step3Box.getStyleClass().addAll("sidebar-step",
                currentStep >= 3 ? (currentStep == 3 ? "active" : "completed") : "");
        step4Box.getStyleClass().addAll("sidebar-step", currentStep == 4 ? "active" : "");

        // Update step numbers
        updateStepNumber(step1Number, currentStep > 1);
        updateStepNumber(step2Number, currentStep > 2);
        updateStepNumber(step3Number, currentStep > 3);
        updateStepNumber(step4Number, currentStep > 4);
    }

    private void updateStepNumber(Label label, boolean completed) {
        if (completed) {
            label.setText("✓");
            label.setStyle(
                    "-fx-background-color: #2DD4BF; -fx-background-radius: 50%; -fx-text-fill: white; -fx-min-width: 30px; -fx-min-height: 30px; -fx-alignment: center; -fx-font-weight: bold;");
        }
    }

    // Helper methods
    private VBox createFieldBox(String labelText, Control field, Label errorLabel) {
        VBox box = new VBox(8);
        box.setPrefWidth(280);

        Label label = new Label(labelText);
        label.setStyle(
                "-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #374151; -fx-letter-spacing: 0.5px;");

        box.getChildren().addAll(label, field, errorLabel);
        return box;
    }

    private TextField createTextField(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.getStyleClass().add("input-field");
        field.setPrefHeight(40);
        return field;
    }

    private PasswordField createPasswordField(String prompt) {
        PasswordField field = new PasswordField();
        field.setPromptText(prompt);
        field.getStyleClass().add("password-field");
        field.setPrefHeight(40);
        return field;
    }

    private Label createErrorLabel() {
        Label label = new Label();
        label.getStyleClass().add("error-label");
        label.setVisible(false);
        label.setWrapText(true);
        return label;
    }

    private void clearErrors() {
        if (emailError != null)
            emailError.setVisible(false);
        if (passwordError != null)
            passwordError.setVisible(false);
        if (confirmPasswordError != null)
            confirmPasswordError.setVisible(false);
        if (step2Error != null)
            step2Error.setVisible(false);
        if (step4Error != null)
            step4Error.setVisible(false);
    }
}
