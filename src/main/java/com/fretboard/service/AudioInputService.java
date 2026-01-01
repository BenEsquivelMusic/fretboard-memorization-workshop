package com.fretboard.service;

import com.fretboard.model.UserSettings;

import javax.sound.sampled.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service class for managing audio input from the guitar.
 * Handles detection of available audio input ports and audio capture.
 */
public class AudioInputService {

    private static final Logger LOGGER = Logger.getLogger(AudioInputService.class.getName());
    private static AudioInputService instance;

    private TargetDataLine targetDataLine;
    private AudioFormat audioFormat;
    private String currentInputPort;
    private boolean isCapturing;
    private List<AudioInputListener> listeners;

    public interface AudioInputListener {
        void onAudioData(byte[] data, float sampleRate);
    }

    private AudioInputService() {
        this.listeners = new ArrayList<>();
        this.isCapturing = false;
    }

    public static synchronized AudioInputService getInstance() {
        if (instance == null) {
            instance = new AudioInputService();
        }
        return instance;
    }

    public List<String> getAvailableInputPorts() {
        List<String> ports = new ArrayList<>();
        Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
        for (Mixer.Info info : mixerInfos) {
            Mixer mixer = AudioSystem.getMixer(info);
            Line.Info[] targetLineInfos = mixer.getTargetLineInfo();
            for (Line.Info lineInfo : targetLineInfos) {
                if (lineInfo instanceof DataLine.Info) {
                    ports.add(info.getName());
                    break;
                }
            }
        }
        return ports;
    }

    public Optional<Mixer.Info> findMixerByName(String name) {
        Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
        for (Mixer.Info info : mixerInfos) {
            if (info.getName().equals(name)) {
                return Optional.of(info);
            }
        }
        return Optional.empty();
    }

    public boolean initialize(UserSettings settings) {
        if (settings.getGuitarInputPort() == null || settings.getGuitarInputPort().isBlank()) {
            LOGGER.warning("No guitar input port configured");
            return false;
        }
        stop();
        this.currentInputPort = settings.getGuitarInputPort();
        this.audioFormat = new AudioFormat(
                settings.getInputSampleRate(),
                16,
                1,
                true,
                false
        );
        Optional<Mixer.Info> mixerInfo = findMixerByName(currentInputPort);
        if (mixerInfo.isEmpty()) {
            LOGGER.warning("Audio input port not found: " + currentInputPort);
            return false;
        }
        try {
            Mixer mixer = AudioSystem.getMixer(mixerInfo.get());
            DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);
            if (!mixer.isLineSupported(dataLineInfo)) {
                LOGGER.warning("Audio format not supported by mixer");
                return false;
            }
            targetDataLine = (TargetDataLine) mixer.getLine(dataLineInfo);
            targetDataLine.open(audioFormat, settings.getInputBufferSize());
            LOGGER.info("Audio input initialized: " + currentInputPort);
            return true;
        } catch (LineUnavailableException e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize audio input", e);
            return false;
        }
    }

    public void startCapture() {
        if (targetDataLine == null || isCapturing) return;
        targetDataLine.start();
        isCapturing = true;
        Thread captureThread = new Thread(this::captureLoop, "AudioCapture");
        captureThread.setDaemon(true);
        captureThread.start();
        LOGGER.info("Audio capture started");
    }

    private void captureLoop() {
        byte[] buffer = new byte[targetDataLine.getBufferSize() / 4];
        while (isCapturing && targetDataLine.isOpen()) {
            int bytesRead = targetDataLine.read(buffer, 0, buffer.length);
            if (bytesRead > 0) {
                byte[] data = new byte[bytesRead];
                System.arraycopy(buffer, 0, data, 0, bytesRead);
                notifyListeners(data);
            }
        }
    }

    private void notifyListeners(byte[] data) {
        float sampleRate = audioFormat != null ? audioFormat.getSampleRate() : 44100.0f;
        for (AudioInputListener listener : listeners) {
            listener.onAudioData(data, sampleRate);
        }
    }

    public void stopCapture() {
        isCapturing = false;
        if (targetDataLine != null) {
            targetDataLine.stop();
            LOGGER.info("Audio capture stopped");
        }
    }

    public void stop() {
        stopCapture();
        if (targetDataLine != null) {
            targetDataLine.close();
            targetDataLine = null;
        }
        currentInputPort = null;
    }

    public void addListener(AudioInputListener listener) {
        if (!listeners.contains(listener)) listeners.add(listener);
    }

    public void removeListener(AudioInputListener listener) {
        listeners.remove(listener);
    }

    public boolean isCapturing() { return isCapturing; }
    public String getCurrentInputPort() { return currentInputPort; }
    public boolean isInitialized() { return targetDataLine != null; }
}