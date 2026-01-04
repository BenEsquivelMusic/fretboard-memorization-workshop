package com.fretboard.util;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;

/**
 * Utility class for displaying JavaFX dialog boxes.
 * Consolidates duplicate dialog display logic from multiple training modules.
 * All methods ensure execution on the JavaFX Application Thread.
 */
public final class DialogUtil {

    private DialogUtil() {
        // Utility class - prevent instantiation
    }

    /**
     * Shows a warning dialog with the specified title and message.
     *
     * @param title the dialog title
     * @param message the dialog message
     */
    public static void showWarning(String title, String message) {
        showAlert(Alert.AlertType.WARNING, title, message);
    }

    /**
     * Shows an information dialog with the specified title and message.
     *
     * @param title the dialog title
     * @param message the dialog message
     */
    public static void showInfo(String title, String message) {
        showAlert(Alert.AlertType.INFORMATION, title, message);
    }

    /**
     * Shows an error dialog with the specified title and message.
     *
     * @param title the dialog title
     * @param message the dialog message
     */
    public static void showError(String title, String message) {
        showAlert(Alert.AlertType.ERROR, title, message);
    }

    /**
     * Shows a confirmation dialog with the specified title and message.
     *
     * @param title the dialog title
     * @param message the dialog message
     * @return true if the user clicked OK, false otherwise
     */
    public static boolean showConfirmation(String title, String message) {
        if (Platform.isFxApplicationThread()) {
            return showConfirmationInternal(title, message);
        } else {
            final boolean[] result = {false};
            Platform.runLater(() -> result[0] = showConfirmationInternal(title, message));
            return result[0];
        }
    }

    /**
     * Shows an alert dialog of the specified type.
     *
     * @param alertType the type of alert
     * @param title the dialog title
     * @param message the dialog message
     */
    public static void showAlert(Alert.AlertType alertType, String title, String message) {
        if (Platform.isFxApplicationThread()) {
            showAlertInternal(alertType, title, message);
        } else {
            Platform.runLater(() -> showAlertInternal(alertType, title, message));
        }
    }

    private static void showAlertInternal(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private static boolean showConfirmationInternal(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
}
