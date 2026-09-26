package com.supplychainx;

import com.supplychainx.config.DatabaseConnection;
import com.supplychainx.ui.NavigationManager;
import javafx.application.Application;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main extends Application {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    @Override
    public void start(Stage primaryStage) {
        logger.info("Initializing ChainOps – Supply Chain Management System...");

        // Store primary stage in navigation manager
        NavigationManager.setPrimaryStage(primaryStage);

        // Test database connectivity
        boolean dbOk = DatabaseConnection.testConnection();
        if (dbOk) {
            logger.info("Database connection successfully verified.");
        } else {
            logger.warn("Database connection could not be established immediately. Ensure MySQL is running on port 3306 with schema 'supplychainx'.");
        }

        // Display login view
        NavigationManager.showLoginView();
    }

    @Override
    public void stop() {
        logger.info("Shutting down ChainOps application...");
        DatabaseConnection.shutdown();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
