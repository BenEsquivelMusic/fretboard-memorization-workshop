package com.fretboard.controller;

import com.fretboard.ApplicationIcons;
import com.fretboard.model.UserData;
import com.fretboard.model.UserSettings;
import com.fretboard.module.FretboardDisplayModule;
import com.fretboard.module.TrainingModule;
import com.fretboard.module.TrainingModuleRegistry;
import com.fretboard.service.AudioInputService;
import com.fretboard.service.UserDataService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for the main application window.
 * Manages navigation between training modules and application-level actions.
 */
public final class MainController {

    private static final Logger LOGGER = Logger.getLogger(MainController.class.getName());
    private final UserDataService userDataService;
    private final AudioInputService audioInputService;
    private final TrainingModuleRegistry moduleRegistry;
    @FXML
    private BorderPane rootPane;
    @FXML
    private VBox moduleListPane;
    @FXML
    private BorderPane contentPane;
    @FXML
    private Label statusLabel;
    @FXML
    private MenuBar menuBar;
    private Stage primaryStage;
    private TrainingModule activeModule;

    public MainController() {
        this.userDataService = UserDataService.getInstance();
        this.audioInputService = AudioInputService.getInstance();
        this.moduleRegistry = TrainingModuleRegistry.getInstance();
    }

    @FXML
    public void initialize() {
        registerTrainingModules();
        updateModuleList();
        updateStatus("Ready");
    }

    /**
     * Registers all available training modules with the registry.
     */
    private void registerTrainingModules() {
        UserData userData = userDataService.getCurrentUserData();
        
        // Register the Fretboard Display Module
        if (!moduleRegistry.isModuleRegistered("fretboard-display")) {
            moduleRegistry.registerModule(new FretboardDisplayModule(userData.getSettings()));
        }
        
        // Additional modules can be registered here in the future
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    @FXML
    public void handleNewFile() {
        if (confirmUnsavedChanges()) {
            // Show dialog to prompt for user name and save location
            Optional<NewFileDialogResult> result = showNewFileDialog();
            if (result.isPresent()) {
                NewFileDialogResult dialogResult = result.get();
                userDataService.createNewUserData();
                
                // Store the user's name and save location in UserSettings
                UserData userData = userDataService.getCurrentUserData();
                UserSettings settings = userData.getSettings();
                settings.setUserName(dialogResult.userName());
                settings.setDataSaveLocation(dialogResult.saveLocation());
                
                updateStatus("New file created for " + dialogResult.userName());
            }
        }
    }

    /**
     * Shows a dialog to prompt the user for their name and save location.
     *
     * @return an Optional containing the dialog result if the user confirmed, empty otherwise
     */
    private Optional<NewFileDialogResult> showNewFileDialog() {
        Dialog<NewFileDialogResult> dialog = new Dialog<>();
        dialog.setTitle("New User Profile");
        dialog.setHeaderText("Enter your information to create a new profile");
        dialog.initOwner(primaryStage);
        dialog.initModality(Modality.APPLICATION_MODAL);

        // Set the button types
        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        // Create the form fields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        nameField.setPromptText("Your name");
        
        TextField saveLocationField = new TextField();
        saveLocationField.setPromptText("Select a folder...");
        saveLocationField.setEditable(false);
        
        Button browseButton = new Button("Browse...");
        browseButton.setOnAction(e -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Select Save Location");
            File selectedDirectory = directoryChooser.showDialog(dialog.getOwner());
            if (selectedDirectory != null) {
                saveLocationField.setText(selectedDirectory.getAbsolutePath());
            }
        });

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Save Location:"), 0, 1);
        grid.add(saveLocationField, 1, 1);
        grid.add(browseButton, 2, 1);

        dialog.getDialogPane().setContent(grid);

        // Enable/Disable the create button depending on whether fields are filled
        javafx.scene.Node createButton = dialog.getDialogPane().lookupButton(createButtonType);
        createButton.setDisable(true);

        // Validation: enable button only when both fields have content
        nameField.textProperty().addListener((observable, oldValue, newValue) -> {
            createButton.setDisable(newValue.trim().isEmpty() || saveLocationField.getText().isEmpty());
        });
        saveLocationField.textProperty().addListener((observable, oldValue, newValue) -> {
            createButton.setDisable(nameField.getText().trim().isEmpty() || newValue.isEmpty());
        });

        // Request focus on the name field by default
        Platform.runLater(nameField::requestFocus);

        // Convert the result to NewFileDialogResult when the create button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                return new NewFileDialogResult(nameField.getText().trim(), saveLocationField.getText());
            }
            return null;
        });

        return dialog.showAndWait();
    }

    /**
     * Record to hold the result of the new file dialog.
     */
    private record NewFileDialogResult(String userName, String saveLocation) {}

    @FXML
    public void handleOpenFile() {
        if (!confirmUnsavedChanges()) {
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open User Data");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fretboard Data", "*" + userDataService.getFileExtension())
        );

        File file = fileChooser.showOpenDialog(primaryStage);
        if (file != null) {
            Optional<UserData> loaded = userDataService.loadFromFile(file.toPath());
            if (loaded.isPresent()) {
                updateStatus("Loaded: " + file.getName());
                initializeAudioIfConfigured();
            } else {
                showError("Failed to Load", "Could not load the user data file.");
            }
        }
    }

    @FXML
    public void handleSaveFile() {
        if (userDataService.getCurrentFilePath().isPresent()) {
            if (userDataService.save()) {
                updateStatus("Saved");
            } else {
                showError("Save Failed", "Could not save the file.");
            }
        } else {
            handleSaveFileAs();
        }
    }

    @FXML
    public void handleSaveFileAs() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save User Data");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fretboard Data", "*" + userDataService.getFileExtension())
        );

        File file = fileChooser.showSaveDialog(primaryStage);
        if (file != null) {
            if (userDataService.saveToFile(file.toPath())) {
                updateStatus("Saved: " + file.getName());
            } else {
                showError("Save Failed", "Could not save the file.");
            }
        }
    }

    @FXML
    public void handleSettings() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SettingsView.fxml"));
            Parent settingsRoot = loader.load();

            SettingsController settingsController = loader.getController();

            Stage settingsStage = new Stage();
            settingsStage.setTitle("Settings");
            settingsStage.initModality(Modality.APPLICATION_MODAL);
            settingsStage.initOwner(primaryStage);
            settingsStage.setScene(new Scene(settingsRoot));
            settingsStage.setResizable(false);

            settingsStage.getIcons().add(
                    new Image(
                            Optional.ofNullable(MainController.class.getResourceAsStream(ApplicationIcons.SETTINGS_ICON))
                                    .orElseThrow()));

            settingsController.setStage(settingsStage);

            settingsStage.showAndWait();

            if (settingsController.isSettingsChanged()) {
                initializeAudioIfConfigured();
                refreshModulesWithNewSettings();
                updateStatus("Settings updated");
            }

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to open settings", e);
            showError("Error", "Could not open settings window.");
        }
    }

    /**
     * Refreshes training modules when user settings change.
     * This recreates modules that depend on settings like fret count or string count.
     */
    private void refreshModulesWithNewSettings() {
        UserData userData = userDataService.getCurrentUserData();
        
        // Unregister and re-register the FretboardDisplayModule with new settings
        if (moduleRegistry.isModuleRegistered("fretboard-display")) {
            moduleRegistry.unregisterModule("fretboard-display");
            moduleRegistry.registerModule(new FretboardDisplayModule(userData.getSettings()));
        }
        
        // Update the module list in the UI
        updateModuleList();
        
        // If the fretboard display module was active, re-select it
        if (activeModule != null && "fretboard-display".equals(activeModule.getModuleId())) {
            moduleRegistry.getModule("fretboard-display").ifPresent(this::selectModule);
        }
    }

    @FXML
    public void handleExit() {
        if (confirmUnsavedChanges()) {
            if (activeModule != null) {
                activeModule.stop();
                activeModule.cleanup();
            }
            Platform.exit();
        }
    }

    @FXML
    public void handleAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("Fretboard Memorization Workshop");
        alert.setContentText("""
                A training application for guitar fretboard memorization.
                
                Built with JavaFX 25""");
        alert.showAndWait();
    }

    public void selectModule(TrainingModule module) {
        if (activeModule != null) {
            activeModule.stop();
        }

        activeModule = module;
        UserData userData = userDataService.getCurrentUserData();
        module.initialize(userData.getProgressForModule(module.getModuleId()));

        contentPane.setCenter(module.getView());
        updateStatus("Module: " + module.getDisplayName());
    }

    private void updateModuleList() {
        moduleListPane.getChildren().clear();

        for (TrainingModule module : moduleRegistry.getAllModules()) {
            Button moduleButton = new Button(module.getDisplayName());
            moduleButton.setMaxWidth(Double.MAX_VALUE);
            moduleButton.setOnAction(_ -> selectModule(module));
            moduleButton.getStyleClass().add("module-button");

            Tooltip tooltip = new Tooltip(module.getDescription());
            moduleButton.setTooltip(tooltip);

            moduleListPane.getChildren().add(moduleButton);
        }

        if (moduleRegistry.getModuleCount() == 0) {
            Label noModulesLabel = new Label("No training modules available");
            noModulesLabel.getStyleClass().add("no-modules-label");
            moduleListPane.getChildren().add(noModulesLabel);
        }
    }

    private void initializeAudioIfConfigured() {
        UserData userData = userDataService.getCurrentUserData();
        if (userData.getSettings().isGuitarInputConfigured()) {
            boolean success = audioInputService.initialize(userData.getSettings());
            if (!success) {
                showError("Audio Error", "Could not initialize audio input. Please check your settings.");
            }
        }
    }

    private boolean confirmUnsavedChanges() {
        if (userDataService.hasUnsavedData() && userDataService.getCurrentUserData().hasTrainingData()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Unsaved Changes");
            alert.setHeaderText("You have unsaved changes");
            alert.setContentText("Do you want to save your changes before continuing?");

            ButtonType saveButton = new ButtonType("Save");
            ButtonType dontSaveButton = new ButtonType("Don't Save");
            ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

            alert.getButtonTypes().setAll(saveButton, dontSaveButton, cancelButton);

            Optional<ButtonType> result = alert.showAndWait();

            if (result.isPresent()) {
                if (result.get() == saveButton) {
                    handleSaveFile();
                    return true;
                } else return result.get() == dontSaveButton;
            }
            return false;
        }
        return true;
    }

    private void updateStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}