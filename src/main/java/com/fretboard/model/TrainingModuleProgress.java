package com.fretboard.model;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Model class for storing progress data for a specific training module.
 * This class is serializable for binary file persistence.
 */
public final class TrainingModuleProgress implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String moduleId;
    private int totalSessions;
    private int totalCorrectAnswers;
    private int totalAttempts;
    private long totalTimeSpentSeconds;
    private LocalDateTime lastSessionDate;
    private int currentStreak;
    private int bestStreak;
    private double highScore;
    private Map<String, String> customProperties;

    /**
     * Default constructor for creating new progress data.
     */
    public TrainingModuleProgress() {
        this.totalSessions = 0;
        this.totalCorrectAnswers = 0;
        this.totalAttempts = 0;
        this.totalTimeSpentSeconds = 0;
        this.currentStreak = 0;
        this.bestStreak = 0;
        this.highScore = 0.0;
        this.customProperties = new HashMap<>();
    }

    /**
     * Creates progress data for a specific module.
     *
     * @param moduleId the module identifier
     */
    public TrainingModuleProgress(String moduleId) {
        this();
        this.moduleId = moduleId;
    }

    // Getters and setters

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }

    public int getTotalSessions() {
        return totalSessions;
    }

    public void setTotalSessions(int totalSessions) {
        this.totalSessions = totalSessions;
    }

    public void incrementSessions() {
        this.totalSessions++;
    }

    public int getTotalCorrectAnswers() {
        return totalCorrectAnswers;
    }

    public void setTotalCorrectAnswers(int totalCorrectAnswers) {
        this.totalCorrectAnswers = totalCorrectAnswers;
    }

    public void addCorrectAnswers(int count) {
        this.totalCorrectAnswers += count;
    }

    public int getTotalAttempts() {
        return totalAttempts;
    }

    public void setTotalAttempts(int totalAttempts) {
        this.totalAttempts = totalAttempts;
    }

    public void addAttempts(int count) {
        this.totalAttempts += count;
    }

    public long getTotalTimeSpentSeconds() {
        return totalTimeSpentSeconds;
    }

    public void setTotalTimeSpentSeconds(long totalTimeSpentSeconds) {
        this.totalTimeSpentSeconds = totalTimeSpentSeconds;
    }

    public void addTimeSpent(long seconds) {
        this.totalTimeSpentSeconds += seconds;
    }

    public LocalDateTime getLastSessionDate() {
        return lastSessionDate;
    }

    public void setLastSessionDate(LocalDateTime lastSessionDate) {
        this.lastSessionDate = lastSessionDate;
    }

    public int getCurrentStreak() {
        return currentStreak;
    }

    public void setCurrentStreak(int currentStreak) {
        this.currentStreak = currentStreak;
        if (this.currentStreak > this.bestStreak) {
            this.bestStreak = this.currentStreak;
        }
    }

    public void incrementStreak() {
        this.currentStreak++;
        if (this.currentStreak > this.bestStreak) {
            this.bestStreak = this.currentStreak;
        }
    }

    public void resetStreak() {
        this.currentStreak = 0;
    }

    public int getBestStreak() {
        return bestStreak;
    }

    public void setBestStreak(int bestStreak) {
        this.bestStreak = bestStreak;
    }

    public double getHighScore() {
        return highScore;
    }

    public void setHighScore(double highScore) {
        this.highScore = highScore;
    }

    public void updateHighScore(double score) {
        if (score > this.highScore) {
            this.highScore = score;
        }
    }

    public Map<String, String> getCustomProperties() {
        return customProperties;
    }

    public void setCustomProperties(Map<String, String> customProperties) {
        this.customProperties = customProperties != null ? customProperties : new HashMap<>();
    }

    public void setCustomProperty(String key, String value) {
        this.customProperties.put(key, value);
    }

    public String getCustomProperty(String key) {
        return this.customProperties.get(key);
    }

    public String getCustomProperty(String key, String defaultValue) {
        return this.customProperties.getOrDefault(key, defaultValue);
    }

    /**
     * Calculates the accuracy percentage based on correct answers and total attempts.
     *
     * @return the accuracy percentage (0-100), or 0 if no attempts have been made
     */
    public double getAccuracyPercentage() {
        if (totalAttempts == 0) {
            return 0.0;
        }
        return (double) totalCorrectAnswers / totalAttempts * 100.0;
    }

    @Override
    public String toString() {
        return "TrainingModuleProgress{" +
                "moduleId='" + moduleId + '\'' +
                ", totalSessions=" + totalSessions +
                ", accuracy=" + String.format("%.1f%%", getAccuracyPercentage()) +
                ", bestStreak=" + bestStreak +
                '}';
    }
}
