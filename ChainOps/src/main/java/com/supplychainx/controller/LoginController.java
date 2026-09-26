package com.supplychainx.controller;

import com.supplychainx.exception.AuthenticationException;
import com.supplychainx.model.User;
import com.supplychainx.service.AuthService;
import com.supplychainx.ui.AlertUtil;
import com.supplychainx.ui.NavigationManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

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
