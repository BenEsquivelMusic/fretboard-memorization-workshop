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
    @Serial
    private static final long serialVersionUID = 1L;
    private String guitarInputPort;
    private int numberOfFrets;
    private String dataSaveLocation;
    private float inputSampleRate;
    private int inputBufferSize;

    /**
     * Default constructor with standard guitar settings.
     */
    public UserSettings() {
        this.numberOfFrets = DEFAULT_FRET_COUNT;
        this.inputSampleRate = 44100.0f;
        this.inputBufferSize = 4096;
    }

    /**
     * Creates user settings with specified values.
     *
     * @param guitarInputPort  the audio input port for the guitar
     * @param numberOfFrets    the number of frets on the guitar (12-36)
     * @param dataSaveLocation the location to save user data
     */
    public UserSettings(String guitarInputPort, int numberOfFrets, String dataSaveLocation) {
        this();
        this.guitarInputPort = guitarInputPort;
        setNumberOfFrets(numberOfFrets);
        this.dataSaveLocation = dataSaveLocation;
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

    /**
     * Sets the number of frets, clamping the value between MIN_FRET_COUNT and MAX_FRET_COUNT.
     *
     * @param numberOfFrets the number of frets (will be clamped to valid range)
     */
    public void setNumberOfFrets(int numberOfFrets) {
        this.numberOfFrets = Math.max(MIN_FRET_COUNT, Math.min(MAX_FRET_COUNT, numberOfFrets));
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
                ", dataSaveLocation='" + dataSaveLocation + '\'' +
                '}';
    }
}