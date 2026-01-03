package com.fretboard.model;

import java.io.Serial;
import java.io.Serializable;

/**
 * Model class for storing user settings related to guitar configuration.
 * This class is serializable for binary file persistence.
 */
public final class UserSettings implements Serializable {

    public static final int DEFAULT_FRET_COUNT = 24;
    public static final int MIN_FRET_COUNT = 12;
    public static final int MAX_FRET_COUNT = 36;

    public static final int DEFAULT_STRING_COUNT = 6;
    public static final int MIN_STRING_COUNT = 6;
    public static final int MAX_STRING_COUNT = 8;

    public static final WoodGrain DEFAULT_WOOD_GRAIN = WoodGrain.ROSEWOOD;

    @Serial
    private static final long serialVersionUID = 2L;
    private String guitarInputPort;
    private int numberOfFrets;
    private int numberOfStrings;
    private String dataSaveLocation;
    private float inputSampleRate;
    private int inputBufferSize;
    private WoodGrain fretboardWoodGrain;

    /**
     * Default constructor with standard guitar settings.
     */
    public UserSettings() {
        this.numberOfFrets = DEFAULT_FRET_COUNT;
        this.inputSampleRate = 44100.0f;
        this.inputBufferSize = 4096;
        this.numberOfStrings = DEFAULT_STRING_COUNT;
        this.fretboardWoodGrain = DEFAULT_WOOD_GRAIN;
        validateSettings();
    }

    // Getters and setters

    public String getGuitarInputPort() {
        return guitarInputPort;
    }

    public void setGuitarInputPort(String guitarInputPort) {
        this.guitarInputPort = guitarInputPort;
    }

    public int getNumberOfFrets() {
        return numberOfFrets;
    }

    public void setNumberOfFrets(int numberOfFrets) {
        validateFretCount(numberOfFrets);
        this.numberOfFrets = numberOfFrets;
    }

    public int getNumberOfStrings() {
        return numberOfStrings;
    }

    public void setNumberOfStrings(int numberOfStrings) {
        validateStringCount(numberOfStrings);
        this.numberOfStrings = numberOfStrings;
    }

    public String getDataSaveLocation() {
        return dataSaveLocation;
    }

    public void setDataSaveLocation(String dataSaveLocation) {
        this.dataSaveLocation = dataSaveLocation;
    }

    public float getInputSampleRate() {
        return inputSampleRate;
    }

    public void setInputSampleRate(float inputSampleRate) {
        this.inputSampleRate = inputSampleRate;
    }

    public int getInputBufferSize() {
        return inputBufferSize;
    }

    public void setInputBufferSize(int inputBufferSize) {
        this.inputBufferSize = inputBufferSize;
    }

    public WoodGrain getFretboardWoodGrain() {
        return fretboardWoodGrain != null ? fretboardWoodGrain : DEFAULT_WOOD_GRAIN;
    }

    public void setFretboardWoodGrain(WoodGrain fretboardWoodGrain) {
        this.fretboardWoodGrain = fretboardWoodGrain != null ? fretboardWoodGrain : DEFAULT_WOOD_GRAIN;
    }

    /**
     * Checks if the guitar input port has been configured.
     *
     * @return true if a guitar input port is set
     */
    public boolean isGuitarInputConfigured() {
        return guitarInputPort != null && !guitarInputPort.isBlank();
    }

    /**
     * Checks if the data save location has been configured.
     *
     * @return true if a save location is set
     */
    public boolean isSaveLocationConfigured() {
        return dataSaveLocation != null && !dataSaveLocation.isBlank();
    }

    @Override
    public String toString() {
        return "UserSettings{" +
                "guitarInputPort='" + guitarInputPort + '\'' +
                ", numberOfFrets=" + numberOfFrets +
                ", numberOfStrings=" + numberOfStrings +
                ", fretboardWoodGrain=" + fretboardWoodGrain +
                ", dataSaveLocation='" + dataSaveLocation + '\'' +
                '}';
    }

    private void validateSettings() {
        validateFretCount(numberOfFrets);
        validateStringCount(numberOfStrings);
    }

    private void validateFretCount(int fretCount) {
        if (fretCount < MIN_FRET_COUNT || fretCount > MAX_FRET_COUNT) {
            throw new IllegalArgumentException("Invalid fret count provided: " + fretCount);
        }
    }

    private void validateStringCount(int stringCount) {
        if (stringCount < MIN_STRING_COUNT || stringCount > MAX_STRING_COUNT) {
            throw new IllegalArgumentException("Invalid string count provided: " + stringCount);
        }
    }
}
