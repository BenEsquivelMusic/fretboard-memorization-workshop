package com.fretboard.module;

import com.fretboard.model.TrainingModuleProgress;
import javafx.scene.Node;

/**
 * Interface defining the contract for pluggable guitar training modules.
 * Each training module provides a specific type of fretboard training exercise.
 */
public interface TrainingModule {

    /**
     * Gets the unique identifier for this module.
     *
     * @return the module ID
     */
    String getModuleId();

    /**
     * Gets the display name of this module.
     *
     * @return the display name
     */
    String getDisplayName();

    /**
     * Gets a description of what this module does.
     *
     * @return the module description
     */
    String getDescription();

    /**
     * Gets the JavaFX node representing the UI for this module.
     * This node will be displayed in the main application when the module is active.
     *
     * @return the JavaFX node for this module's UI
     */
    Node getView();

    /**
     * Initializes the module with the given progress data.
     * Called when the module is loaded or when the user navigates to it.
     *
     * @param progress the saved progress data for this module, or null if none exists
     */
    void initialize(TrainingModuleProgress progress);

    /**
     * Gets the current progress data for this module.
     * This data will be saved to the user's data file.
     *
     * @return the current progress data
     */
    TrainingModuleProgress getProgress();

    /**
     * Starts the training exercise.
     */
    void start();

    /**
     * Pauses the training exercise.
     */
    void pause();

    /**
     * Stops the training exercise and resets to initial state.
     */
    void stop();

    /**
     * Checks if the module is currently running.
     *
     * @return true if the module is active
     */
    boolean isRunning();

    /**
     * Called when the module is being unloaded or the application is closing.
     * Use this to clean up resources.
     */
    void cleanup();

    /**
     * Called when audio input is received from the guitar.
     * Modules can override this to process guitar input.
     *
     * @param audioData the raw audio data from the guitar input
     * @param sampleRate the sample rate of the audio
     */
    default void onAudioInput(byte[] audioData, float sampleRate) {
        // Default implementation does nothing
    }

    /**
     * Gets the icon resource path for this module.
     *
     * @return the icon resource path, or null if no icon is available
     */
    default String getIconPath() {
        return null;
    }
}