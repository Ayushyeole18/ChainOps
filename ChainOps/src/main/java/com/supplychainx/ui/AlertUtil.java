package com.supplychainx.ui;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.stage.Stage;

import java.util.Optional;

/**
 * Reusable dialog and notification helper maintaining enterprise visual styles.
 */
public class AlertUtil {

    public static void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title != null ? title : "Operation Successful");
        alert.setHeaderText("Success");
        alert.setContentText(message);
        styleDialog(alert);
        alert.showAndWait();
    }

    public static void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title != null ? title : "System Notice");
        alert.setHeaderText("Error / Notice");
        alert.setContentText(message);
        styleDialog(alert);
        alert.showAndWait();
    }

    public static void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title != null ? title : "Attention Required");
        alert.setHeaderText("Warning");
        alert.setContentText(message);
        styleDialog(alert);
        alert.showAndWait();
    }

    public static boolean confirm(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        styleDialog(alert);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    private static void styleDialog(Alert alert) {
        DialogPane pane = alert.getDialogPane();
        try {
            pane.getStylesheets().add(AlertUtil.class.getResource("/css/style.css").toExternalForm());
        } catch (Exception ignored) {}
        Stage stage = (Stage) pane.getScene().getWindow();
        stage.setAlwaysOnTop(true);
    }
}
