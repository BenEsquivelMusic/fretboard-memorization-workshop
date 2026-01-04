package com.fretboard;

import com.fretboard.constants.ApplicationIcons;
import com.fretboard.controller.MainController;
import com.fretboard.service.AudioInputService;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main application class for the Fretboard Memorization Workshop.
 * Initializes the JavaFX application and loads the main window.
 */
public final class FretboardApplication extends Application {

    private static final Logger LOGGER = Logger.getLogger(FretboardApplication.class.getName());
    private static final String APP_TITLE = "Fretboard Memorization Workshop";
    private static final int MIN_WIDTH = 800;
    private static final int MIN_HEIGHT = 600;

    private MainController mainController;

    /**
     * Main entry point for the application.
     *
     * @param args command line arguments
     */
    static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"));
            Parent root = loader.load();

            mainController = loader.getController();
            mainController.setPrimaryStage(primaryStage);

            Scene scene = new Scene(root, 1024, 768);

            String cssResource = getClass().getResource("/css/styles.css") != null
                    ? getClass().getResource("/css/styles.css").toExternalForm()
                    : null;
            if (cssResource != null) {
                scene.getStylesheets().add(cssResource);
            }

            primaryStage.setTitle(APP_TITLE);
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(MIN_WIDTH);
            primaryStage.setMinHeight(MIN_HEIGHT);

            primaryStage.getIcons().add(
                    new Image(
                            Optional.ofNullable(FretboardApplication.class.getResourceAsStream(ApplicationIcons.APPLICATION_ICON))
                                    .orElseThrow()));

            primaryStage.setOnCloseRequest(event -> {
                event.consume();
                mainController.handleExit();
            });

            primaryStage.show();

            LOGGER.info("Fretboard Memorization Workshop started");

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load main view", e);
            showFatalError("Failed to start application", e.getMessage());
        }
    }

    @Override
    public void stop() {
        LOGGER.info("Shutting down application...");

        AudioInputService.getInstance().close();

        LOGGER.info("Fretboard Memorization Workshop stopped");
    }

    private void showFatalError(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText("Application Error");
        alert.setContentText(message);
        alert.showAndWait();
        Platform.exit();
    }
}
