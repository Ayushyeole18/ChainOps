package com.supplychainx.controller;

import com.supplychainx.exception.AuthenticationException;
import com.supplychainx.model.User;
import com.supplychainx.service.AuthService;
import com.supplychainx.ui.AlertUtil;
import com.supplychainx.ui.NavigationManager;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.util.Optional;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private final AuthService authService = new AuthService();

    @FXML
    public void initialize() {
        errorLabel.setVisible(false);
    }

    @FXML
    private void handleLogin() {
        String usernameOrEmail = usernameField.getText();
        String password = passwordField.getText();

        try {
            errorLabel.setVisible(false);
            User user = authService.authenticate(usernameOrEmail, password);
            NavigationManager.showMainApp();
        } catch (AuthenticationException e) {
            errorLabel.setText(e.getMessage());
            errorLabel.setVisible(true);
        } catch (Exception e) {
            errorLabel.setText("System error during login. Check database connection.");
            errorLabel.setVisible(true);
        }
    }

    @FXML
    private void handleShowSignUp() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("ChainOps - Create New Account");
        dialog.setHeaderText("Register a new user in the supply chain system");

        ButtonType registerButtonType = new ButtonType("Create Account", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(registerButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 10));

        TextField fullNameField = new TextField();
        fullNameField.setPromptText("e.g. Jordan Mitchell");
        TextField newUsernameField = new TextField();
        newUsernameField.setPromptText("e.g. jmitchell");
        TextField newEmailField = new TextField();
        newEmailField.setPromptText("e.g. jmitchell@supplychainx.com");
        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("•••••••• (min 6 chars)");
        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll("Administrator", "Warehouse Manager", "Procurement Manager", "Sales Manager");
        roleCombo.setValue("Warehouse Manager");

        grid.add(new Label("Full Name:"), 0, 0);
        grid.add(fullNameField, 1, 0);
        grid.add(new Label("Username:"), 0, 1);
        grid.add(newUsernameField, 1, 1);
        grid.add(new Label("Corporate Email:"), 0, 2);
        grid.add(newEmailField, 1, 2);
        grid.add(new Label("Password:"), 0, 3);
        grid.add(newPasswordField, 1, 3);
        grid.add(new Label("Role:"), 0, 4);
        grid.add(roleCombo, 1, 4);

        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == registerButtonType) {
            try {
                int roleId = switch (roleCombo.getValue()) {
                    case "Administrator" -> 1;
                    case "Warehouse Manager" -> 2;
                    case "Procurement Manager" -> 3;
                    case "Sales Manager" -> 4;
                    default -> 2;
                };
                User newUser = authService.registerUser(
                    fullNameField.getText(),
                    newUsernameField.getText(),
                    newEmailField.getText(),
                    newPasswordField.getText(),
                    roleId
                );
                AlertUtil.showInformation("Account Created", "User @" + newUser.getUsername() + " registered successfully! Launching ChainOps...");
                NavigationManager.showMainApp();
            } catch (Exception e) {
                AlertUtil.showError("Registration Failed", e.getMessage());
            }
        }
    }

    @FXML
    private void fillAdminCredentials() {
        usernameField.setText("admin@supplychainx.com");
        passwordField.setText("admin123");
        errorLabel.setVisible(false);
    }

    @FXML
    private void fillWarehouseCredentials() {
        usernameField.setText("warehouse@supplychainx.com");
        passwordField.setText("warehouse123");
        errorLabel.setVisible(false);
    }

    @FXML
    private void fillSalesCredentials() {
        usernameField.setText("sales@supplychainx.com");
        passwordField.setText("sales123");
        errorLabel.setVisible(false);
    }
}
