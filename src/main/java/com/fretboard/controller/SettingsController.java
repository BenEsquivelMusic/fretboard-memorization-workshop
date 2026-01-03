package com.fretboard.controller;

import com.fretboard.model.UserSettings;
import com.fretboard.service.AudioInputService;
import com.fretboard.service.UserDataService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.List;
import java.util.logging.Logger;

/**
 * Controller for the settings window.
 * Manages guitar input port selection, fret count, and string count configuration.
 */
public final class SettingsController {

    private static final Logger LOGGER = Logger.getLogger(SettingsController.class.getName());
    private final UserDataService userDataService;
    private final AudioInputService audioInputService;
    @FXML
    private ComboBox<String> inputPortComboBox;
    @FXML
    private Spinner<Integer> fretCountSpinner;
    @FXML
    private Spinner<Integer> stringCountSpinner;
    //@FXML
    //private TextField saveLocationField;
    @FXML
    private Button browseButton;
    @FXML
    private Button refreshPortsButton;
    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;
    @FXML
    private Label statusLabel;
    private Stage stage;
    private boolean settingsChanged;

    public SettingsController() {
        this.userDataService = UserDataService.getInstance();
        this.audioInputService = AudioInputService.getInstance();
        this.settingsChanged = false;
    }

    @FXML
    public void initialize() {
        setupFretCountSpinner();
        setupStringCountSpinner();
        refreshInputPorts();
        loadCurrentSettings();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public boolean isSettingsChanged() {
        return settingsChanged;
    }

    @FXML
    public void handleRefreshPorts() {
        refreshInputPorts();
        updateStatus("Audio input ports refreshed");
    }

    /*@FXML
    public void handleBrowseSaveLocation() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Save Location");

        String currentLocation = saveLocationField.getText();
        if (currentLocation != null && !currentLocation.isBlank()) {
            File currentDir = new File(currentLocation);
            if (currentDir.exists() && currentDir.isDirectory()) {
                directoryChooser.setInitialDirectory(currentDir);
            }
        }

        File selectedDir = directoryChooser.showDialog(stage);
        if (selectedDir != null) {
            saveLocationField.setText(selectedDir.getAbsolutePath());
        }
    }*/

    @FXML
    public void handleSave() {
        if (!validateSettings()) {
            return;
        }

        UserSettings settings = userDataService.getCurrentUserData().getSettings();

        String selectedPort = inputPortComboBox.getValue();
        if (selectedPort != null && !selectedPort.equals("-- Select Input Port --")) {
            settings.setGuitarInputPort(selectedPort);
        }

        settings.setNumberOfFrets(fretCountSpinner.getValue());
        settings.setNumberOfStrings(stringCountSpinner.getValue());

        /*String saveLocation = saveLocationField.getText();
        if (saveLocation != null && !saveLocation.isBlank()) {
            settings.setDataSaveLocation(saveLocation);
        }*/

        userDataService.markAsModified();
        settingsChanged = true;

        LOGGER.info("Settings saved: " + settings);
        updateStatus("Settings saved");

        if (stage != null) {
            stage.close();
        }
    }

    @FXML
    public void handleCancel() {
        if (stage != null) {
            stage.close();
        }
    }

    @FXML
    public void handleTestAudio() {
        String selectedPort = inputPortComboBox.getValue();
        if (selectedPort == null || selectedPort.equals("-- Select Input Port --")) {
            showError("No Port Selected", "Please select an audio input port to test.");
            return;
        }

        boolean success = audioInputService.selectInputPort(selectedPort);
        if (success) {
            updateStatus("Audio port test successful: " + selectedPort);
            showInfo("Test Successful", "Successfully connected to audio input port:\n" + selectedPort);
        } else {
            showError("Test Failed", "Could not connect to the selected audio input port.\n" +
                    "Please ensure your guitar interface is connected and try again.");
        }
    }

    private void setupFretCountSpinner() {
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(
                UserSettings.MIN_FRET_COUNT,
                UserSettings.MAX_FRET_COUNT,
                UserSettings.DEFAULT_FRET_COUNT
        );
        fretCountSpinner.setValueFactory(valueFactory);
        fretCountSpinner.setEditable(true);

        fretCountSpinner.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                fretCountSpinner.getEditor().setText(oldValue);
            }
        });
    }

    private void setupStringCountSpinner() {
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(
                UserSettings.MIN_STRING_COUNT,
                UserSettings.MAX_STRING_COUNT,
                UserSettings.DEFAULT_STRING_COUNT
        );
        stringCountSpinner.setValueFactory(valueFactory);
        stringCountSpinner.setEditable(true);

        stringCountSpinner.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                stringCountSpinner.getEditor().setText(oldValue);
            }
        });
    }

    private void refreshInputPorts() {
        List<String> ports = audioInputService.getAvailableInputPorts();

        inputPortComboBox.getItems().clear();
        inputPortComboBox.getItems().add("-- Select Input Port --");
        inputPortComboBox.getItems().addAll(ports);
        inputPortComboBox.getSelectionModel().selectFirst();

        if (ports.isEmpty()) {
            updateStatus("No audio input ports detected");
        } else {
            updateStatus(ports.size() + " audio input port(s) detected");
        }
    }

    private void loadCurrentSettings() {
        UserSettings settings = userDataService.getCurrentUserData().getSettings();

        if (settings.isGuitarInputConfigured()) {
            String configuredPort = settings.getGuitarInputPort();
            if (inputPortComboBox.getItems().contains(configuredPort)) {
                inputPortComboBox.setValue(configuredPort);
            }
        }

        fretCountSpinner.getValueFactory().setValue(settings.getNumberOfFrets());
        stringCountSpinner.getValueFactory().setValue(settings.getNumberOfStrings());

        /*if (settings.isSaveLocationConfigured()) {
            saveLocationField.setText(settings.getDataSaveLocation());
        }*/
    }

    private boolean validateSettings() {
        String selectedPort = inputPortComboBox.getValue();
        if (selectedPort == null || selectedPort.equals("-- Select Input Port --")) {
            showWarning("Input Port Required",
                    "Please select a guitar input port.\n\n" +
                            "If no ports are shown, ensure your guitar interface is connected and click 'Refresh'.");
            return false;
        }

        Integer fretCount = fretCountSpinner.getValue();
        if (fretCount == null || fretCount < UserSettings.MIN_FRET_COUNT || fretCount > UserSettings.MAX_FRET_COUNT) {
            showWarning("Invalid Fret Count",
                    "Fret count must be between " + UserSettings.MIN_FRET_COUNT +
                            " and " + UserSettings.MAX_FRET_COUNT + ".");
            return false;
        }

        Integer stringCount = stringCountSpinner.getValue();
        if (stringCount == null || stringCount < UserSettings.MIN_STRING_COUNT || stringCount > UserSettings.MAX_STRING_COUNT) {
            showWarning("Invalid String Count",
                    "String count must be between " + UserSettings.MIN_STRING_COUNT +
                            " and " + UserSettings.MAX_STRING_COUNT + ".");
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

    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
