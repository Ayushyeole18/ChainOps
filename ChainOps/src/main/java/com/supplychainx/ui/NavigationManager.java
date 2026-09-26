package com.supplychainx.ui;

import com.supplychainx.controller.MainLayoutController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;

public class NavigationManager {

    private static final Logger logger = LoggerFactory.getLogger(NavigationManager.class);
    private static Stage primaryStage;
    private static MainLayoutController mainController;

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void setMainController(MainLayoutController controller) {
        mainController = controller;
    }

    public static void showLoginView() {
        try {
            FXMLLoader loader = new FXMLLoader(NavigationManager.class.getResource("/fxml/Login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 960, 680);
            scene.getStylesheets().add(NavigationManager.class.getResource("/css/style.css").toExternalForm());
            primaryStage.setTitle("ChainOps – Enterprise Supply Chain Management");
            primaryStage.setScene(scene);
            primaryStage.centerOnScreen();
            primaryStage.show();
        } catch (IOException e) {
            logger.error("Failed to load Login view", e);
            AlertUtil.showError("Application Load Error", "Could not initialize login window: " + e.getMessage());
        }
    }

    public static void showMainApp() {
        try {
            FXMLLoader loader = new FXMLLoader(NavigationManager.class.getResource("/fxml/MainLayout.fxml"));
            Parent root = loader.load();
            mainController = loader.getController();

            Scene scene = new Scene(root, 1280, 800);
            scene.getStylesheets().add(NavigationManager.class.getResource("/css/style.css").toExternalForm());
            primaryStage.setTitle("ChainOps – Enterprise Supply Chain Management System");
            primaryStage.setScene(scene);
            primaryStage.centerOnScreen();
            primaryStage.show();

            // Default to Dashboard
            switchView("/fxml/Dashboard.fxml", "Dashboard");
        } catch (IOException e) {
            logger.error("Failed to load Main layout", e);
            AlertUtil.showError("Navigation Error", "Could not initialize main workspace: " + e.getMessage());
        }
    }

    public static void switchView(String fxmlPath, String viewTitle) {
        if (mainController == null) {
            logger.warn("Main controller is null, cannot switch view to {}", fxmlPath);
            return;
        }

        try {
            URL resource = NavigationManager.class.getResource(fxmlPath);
            if (resource == null) {
                AlertUtil.showError("Resource Missing", "FXML view not found: " + fxmlPath);
                return;
            }
            FXMLLoader loader = new FXMLLoader(resource);
            Node viewNode = loader.load();
            mainController.setContent(viewNode, viewTitle);
        } catch (Exception e) {
            logger.error("Error switching view to {}", fxmlPath, e);
            AlertUtil.showError("View Load Failure", "Failed to display view [" + viewTitle + "]: " + e.getMessage());
        }
    }
}
