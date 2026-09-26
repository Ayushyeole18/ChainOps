package com.supplychainx.controller;

import com.supplychainx.dao.UserDao;
import com.supplychainx.dao.UserDaoImpl;
import com.supplychainx.model.Role;
import com.supplychainx.model.User;
import com.supplychainx.ui.AlertUtil;
import com.supplychainx.util.PasswordUtil;
import com.supplychainx.util.ValidationUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class UserController {

    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, Integer> colId;
    @FXML private TableColumn<User, String> colUsername;
    @FXML private TableColumn<User, String> colFullName;
    @FXML private TableColumn<User, String> colEmail;
    @FXML private TableColumn<User, String> colRole;
    @FXML private TableColumn<User, String> colStatus;

    // Form
    @FXML private TextField usernameField;
    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<Role> roleCombo;
    @FXML private ComboBox<String> statusCombo;
    @FXML private Label formTitleLabel;

    private final UserDao userDao = new UserDaoImpl();
    private final ObservableList<User> userData = FXCollections.observableArrayList();
    private User selectedUser;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("userId"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colFullName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colRole.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getRole() != null ? cell.getValue().getRole().getDisplayName() : "Unknown"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        userTable.setItems(userData);

        userTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                populateForm(newVal);
            }
        });

        roleCombo.setItems(FXCollections.observableArrayList(Role.values()));
        roleCombo.setValue(Role.WAREHOUSE_MANAGER);

        statusCombo.setItems(FXCollections.observableArrayList("ACTIVE", "INACTIVE", "SUSPENDED"));
        statusCombo.setValue("ACTIVE");

        loadUsers();
    }

    private void loadUsers() {
        userData.clear();
        userData.addAll(userDao.findAll());
    }

    @FXML
    private void handleNewUser() {
        clearForm();
    }

    @FXML
    private void handleSaveUser() {
        try {
            boolean isNew = (selectedUser == null);
            User u = isNew ? new User() : selectedUser;

            ValidationUtil.requireNonEmpty(usernameField.getText(), "Username");
            ValidationUtil.requireNonEmpty(fullNameField.getText(), "Full Name");
            ValidationUtil.validateEmail(emailField.getText());

            u.setUsername(usernameField.getText().trim());
            u.setFullName(fullNameField.getText().trim());
            u.setEmail(emailField.getText().trim());
            u.setRole(roleCombo.getValue());
            u.setStatus(statusCombo.getValue());

            String rawPass = passwordField.getText();
            if (isNew) {
                if (rawPass == null || rawPass.trim().isEmpty()) {
                    AlertUtil.showWarning("Missing Password", "Password is required for new accounts.");
                    return;
                }
                u.setPasswordHash(PasswordUtil.hashPassword(rawPass.trim()));
                userDao.insert(u);
                AlertUtil.showSuccess("User Registered", "Account " + u.getUsername() + " created successfully.");
            } else {
                userDao.update(u);
                if (rawPass != null && !rawPass.trim().isEmpty()) {
                    userDao.updatePassword(u.getUserId(), PasswordUtil.hashPassword(rawPass.trim()));
                }
                AlertUtil.showSuccess("User Updated", "User " + u.getUsername() + " updated successfully.");
            }

            clearForm();
            loadUsers();
        } catch (Exception e) {
            AlertUtil.showError("Save Error", e.getMessage());
        }
    }

    @FXML
    private void handleDeleteUser() {
        User u = userTable.getSelectionModel().getSelectedItem();
        if (u == null) {
            AlertUtil.showWarning("Selection Required", "Please select a user to remove.");
            return;
        }

        if (u.getUserId() == 1) {
            AlertUtil.showWarning("Protected Account", "Default super admin account cannot be deleted.");
            return;
        }

        if (AlertUtil.confirm("Delete User", "Delete " + u.getUsername() + "?", "This will permanently remove the user credentials.")) {
            try {
                userDao.delete(u.getUserId());
                AlertUtil.showSuccess("User Deleted", "User account removed.");
                clearForm();
                loadUsers();
            } catch (Exception e) {
                AlertUtil.showError("Delete Error", e.getMessage());
            }
        }
    }

    private void populateForm(User u) {
        selectedUser = u;
        formTitleLabel.setText("Edit User (" + u.getUsername() + ")");
        usernameField.setText(u.getUsername());
        fullNameField.setText(u.getFullName());
        emailField.setText(u.getEmail());
        passwordField.clear();
        passwordField.setPromptText("Leave empty to keep existing password");
        roleCombo.setValue(u.getRole());
        statusCombo.setValue(u.getStatus());
    }

    private void clearForm() {
        selectedUser = null;
        formTitleLabel.setText("Register New User");
        usernameField.clear();
        fullNameField.clear();
        emailField.clear();
        passwordField.clear();
        passwordField.setPromptText("Enter password (e.g. admin123)");
        roleCombo.setValue(Role.WAREHOUSE_MANAGER);
        statusCombo.setValue("ACTIVE");
        userTable.getSelectionModel().clearSelection();
    }
}
