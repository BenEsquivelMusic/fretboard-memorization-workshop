package com.fretboard.service;

/**
 * Service for detecting pitch from raw audio data.
 * Uses autocorrelation algorithm for fundamental frequency detection.
 */
public final class PitchDetectionService {

    // Frequency bounds for guitar (lowest note on 8-string ~24Hz, highest ~1320Hz)
    private static final double MIN_FREQUENCY = 20.0;
    private static final double MAX_FREQUENCY = 1400.0;
    
    // Minimum confidence threshold for pitch detection
    private static final double MIN_CONFIDENCE = 0.8;
    
    // Noise gate threshold (RMS amplitude below this is considered silence)
    private static final double NOISE_GATE_THRESHOLD = 0.01;

    /**
     * Detects the fundamental frequency from raw audio data.
     *
     * @param audioData  the raw audio data (16-bit signed PCM mono)
     * @param sampleRate the sample rate of the audio
     * @return the detected frequency in Hz, or null if no clear pitch detected
     */
    public Double detectPitch(byte[] audioData, float sampleRate) {
        if (audioData == null || audioData.length < 4) {
            return null;
        }
        
        // Convert bytes to samples (16-bit signed PCM)
        double[] samples = bytesToSamples(audioData);
        
        if (samples.length < 256) {
            return null;
        }
        
        // Check if signal is above noise gate
        double rms = calculateRMS(samples);
        if (rms < NOISE_GATE_THRESHOLD) {
            return null;
        }
        
        // Apply Hanning window to reduce spectral leakage
        applyHanningWindow(samples);
        
        // Autocorrelation-based pitch detection
        return detectPitchAutocorrelation(samples, sampleRate);
    }

    private double[] bytesToSamples(byte[] audioData) {
        int numSamples = audioData.length / 2;
        double[] samples = new double[numSamples];
        
        for (int i = 0; i < numSamples; i++) {
            int low = audioData[i * 2] & 0xFF;
            int high = audioData[i * 2 + 1];
            int value = (high << 8) | low;
            samples[i] = value / 32768.0;
        }
        
        return samples;
    }

    private double calculateRMS(double[] samples) {
        double sum = 0;
        for (double sample : samples) {
            sum += sample * sample;
        }
        return Math.sqrt(sum / samples.length);
    }

    private void applyHanningWindow(double[] samples) {
        int n = samples.length;
        for (int i = 0; i < n; i++) {
            double multiplier = 0.5 * (1 - Math.cos(2 * Math.PI * i / (n - 1)));
            samples[i] *= multiplier;
        }
    }

    private Double detectPitchAutocorrelation(double[] samples, float sampleRate) {
        int n = samples.length;
        
        // Calculate lag bounds based on frequency limits
        int minLag = (int) (sampleRate / MAX_FREQUENCY);
        int maxLag = Math.min(n / 2, (int) (sampleRate / MIN_FREQUENCY));
        
        if (minLag >= maxLag || maxLag <= 0) {
            return null;
        }
        
        // Calculate autocorrelation for each lag
        double[] autocorr = new double[maxLag + 1];
        double maxCorr = 0;
        int bestLag = 0;
        
        // Normalize factor (autocorrelation at lag 0)
        double acf0 = 0;
        for (double sample : samples) {
            acf0 += sample * sample;
        }
        
        if (acf0 == 0) {
            return null;
        }
        
        for (int lag = minLag; lag <= maxLag; lag++) {
            double sum = 0;
            for (int i = 0; i < n - lag; i++) {
                sum += samples[i] * samples[i + lag];
            }
            autocorr[lag] = sum / acf0;
            
            if (autocorr[lag] > maxCorr) {
                maxCorr = autocorr[lag];
                bestLag = lag;
            }
        }
        
        // Check if the correlation peak is strong enough
        if (maxCorr < MIN_CONFIDENCE) {
            return null;
        }
        
        // Refine the peak location using parabolic interpolation
        double refinedLag = bestLag;
        if (bestLag > minLag && bestLag < maxLag) {
            double y1 = autocorr[bestLag - 1];
            double y2 = autocorr[bestLag];
            double y3 = autocorr[bestLag + 1];
            
            double delta = (y3 - y1) / (2 * (2 * y2 - y1 - y3));
            if (Math.abs(delta) < 1) {
                refinedLag = bestLag + delta;
            }
        }
        
        // Calculate frequency from lag
        double frequency = sampleRate / refinedLag;
        
        // Validate frequency is within expected range
        if (frequency < MIN_FREQUENCY || frequency > MAX_FREQUENCY) {
            return null;
        }
        
        return frequency;
    }
}
