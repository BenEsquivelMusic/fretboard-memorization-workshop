package com.fretboard.service;

import com.fretboard.model.UserSettings;
import com.fretboard.module.TrainingModule;

import javax.sound.sampled.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service for managing audio input from the guitar.
 * Detects available audio input ports and captures audio data.
 */
public final class AudioInputService {

    private static final Logger LOGGER = Logger.getLogger(AudioInputService.class.getName());

    private static AudioInputService instance;
    private final List<TrainingModule> audioListeners;
    private TargetDataLine targetDataLine;
    private AudioFormat audioFormat;
    private ExecutorService captureExecutor;
    private volatile boolean capturing;
    private String currentPortName;
    private float sampleRate;
    private int bufferSize;

    private AudioInputService() {
        this.audioListeners = new CopyOnWriteArrayList<>();
        this.capturing = false;
        this.sampleRate = 44100.0f;
        this.bufferSize = 4096;
    }

    /**
     * Gets the singleton instance of AudioInputService.
     *
     * @return the AudioInputService instance
     */
    public static synchronized AudioInputService getInstance() {
        if (instance == null) {
            instance = new AudioInputService();
        }
        return instance;
    }

    /**
     * Gets a list of available audio input port names.
     *
     * @return list of available input port names
     */
    public List<String> getAvailableInputPorts() {
        List<String> ports = new ArrayList<>();

        Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
        for (Mixer.Info mixerInfo : mixerInfos) {
            Mixer mixer = AudioSystem.getMixer(mixerInfo);

            Line.Info[] targetLineInfos = mixer.getTargetLineInfo();
            for (Line.Info lineInfo : targetLineInfos) {
                if (lineInfo instanceof DataLine.Info dataLineInfo) {
                    if (TargetDataLine.class.isAssignableFrom(dataLineInfo.getLineClass())) {
                        ports.add(mixerInfo.getName());
                        break;
                    }
                }
            }
        }

        return ports;
    }

    /**
     * Initializes the audio input service with the given settings.
     *
     * @param settings the user settings containing audio configuration
     * @return true if initialization was successful
     */
    public boolean initialize(UserSettings settings) {
        if (settings == null || !settings.isGuitarInputConfigured()) {
            LOGGER.warning("Cannot initialize: no guitar input port configured");
            return false;
        }

        this.sampleRate = settings.getInputSampleRate();
        this.bufferSize = settings.getInputBufferSize();

        return selectInputPort(settings.getGuitarInputPort());
    }

    /**
     * Selects and opens the specified audio input port.
     *
     * @param portName the name of the port to select
     * @return true if the port was successfully selected and opened
     */
    public boolean selectInputPort(String portName) {
        stopCapture();
        closeCurrentLine();

        Optional<Mixer.Info> mixerInfo = findMixerByName(portName);
        if (mixerInfo.isEmpty()) {
            LOGGER.warning("Audio input port not found: " + portName);
            return false;
        }

        try {
            Mixer mixer = AudioSystem.getMixer(mixerInfo.get());

            this.audioFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    sampleRate,
                    16,
                    1,
                    2,
                    sampleRate,
                    false
            );

            DataLine.Info lineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);

            if (!mixer.isLineSupported(lineInfo)) {
                LOGGER.warning("Audio format not supported by: " + portName);
                return false;
            }

            this.targetDataLine = (TargetDataLine) mixer.getLine(lineInfo);
            this.targetDataLine.open(audioFormat, bufferSize);
            this.currentPortName = portName;

            LOGGER.info("Audio input port selected: " + portName);
            return true;

        } catch (LineUnavailableException e) {
            LOGGER.log(Level.SEVERE, "Failed to open audio input port: " + portName, e);
            return false;
        }
    }

    /**
     * Starts capturing audio input.
     *
     * @return true if capture started successfully
     */
    public boolean startCapture() {
        if (targetDataLine == null) {
            LOGGER.warning("Cannot start capture: no audio input port selected");
            return false;
        }

        if (capturing) {
            return true;
        }

        try {
            targetDataLine.start();
            capturing = true;

            captureExecutor = Executors.newSingleThreadExecutor();
            captureExecutor.submit(this::captureLoop);

            LOGGER.info("Audio capture started");
            return true;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to start audio capture", e);
            return false;
        }
    }

    /**
     * Stops capturing audio input.
     */
    public void stopCapture() {
        capturing = false;

        if (captureExecutor != null) {
            captureExecutor.shutdownNow();
            captureExecutor = null;
        }

        if (targetDataLine != null && targetDataLine.isRunning()) {
            targetDataLine.stop();
            targetDataLine.flush();
        }

        LOGGER.info("Audio capture stopped");
    }

    /**
     * Registers a training module to receive audio input data.
     *
     * @param module the module to register
     */
    public void registerAudioListener(TrainingModule module) {
        if (!audioListeners.contains(module)) {
            audioListeners.add(module);
        }
    }

    /**
     * Unregisters a training module from receiving audio input data.
     *
     * @param module the module to unregister
     */
    public void unregisterAudioListener(TrainingModule module) {
        audioListeners.remove(module);
    }

    /**
     * Checks if audio capture is currently active.
     *
     * @return true if capturing
     */
    public boolean isCapturing() {
        return capturing;
    }

    /**
     * Gets the current audio input port name.
     *
     * @return the current port name, or null if none selected
     */
    public String getCurrentPortName() {
        return currentPortName;
    }

    /**
     * Gets the current audio format.
     *
     * @return the audio format, or null if not initialized
     */
    public AudioFormat getAudioFormat() {
        return audioFormat;
    }

    /**
     * Closes the audio input service and releases resources.
     */
    public void close() {
        stopCapture();
        closeCurrentLine();
        audioListeners.clear();
        LOGGER.info("AudioInputService closed");
    }

    private void captureLoop() {
        byte[] buffer = new byte[bufferSize];

        while (capturing && !Thread.currentThread().isInterrupted()) {
            int bytesRead = targetDataLine.read(buffer, 0, buffer.length);

            if (bytesRead > 0) {
                byte[] audioData = new byte[bytesRead];
                System.arraycopy(buffer, 0, audioData, 0, bytesRead);

                for (TrainingModule listener : audioListeners) {
                    try {
                        listener.onAudioInput(audioData, sampleRate);
                    } catch (Exception e) {
                        LOGGER.log(Level.WARNING, "Error in audio listener", e);
                    }
                }
            }
        }
    }

    private Optional<Mixer.Info> findMixerByName(String name) {
        Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
        for (Mixer.Info info : mixerInfos) {
            if (info.getName().equals(name)) {
                return Optional.of(info);
            }
        }
        return Optional.empty();
    }

    private void closeCurrentLine() {
        if (targetDataLine != null) {
            targetDataLine.close();
            targetDataLine = null;
            currentPortName = null;
        }
    }
}