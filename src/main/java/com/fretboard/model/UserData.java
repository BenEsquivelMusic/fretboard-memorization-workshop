package com.fretboard.model;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Model class for storing all user data including settings and training progress.
 * This class is serializable for binary file persistence.
 */
public class UserData implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private UserSettings settings;
    private Map<String, TrainingModuleProgress> moduleProgress;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    private String version;

    /**
     * Default constructor for creating new user data.
     */
    public UserData() {
        this.settings = new UserSettings();
        this.moduleProgress = new HashMap<>();
        this.createdDate = LocalDateTime.now();
        this.lastModifiedDate = LocalDateTime.now();
        this.version = "1.0";
    }

    /**
     * Creates user data with existing settings.
     *
     * @param settings the user settings
     */
    public UserData(UserSettings settings) {
        this();
        this.settings = settings;
    }

    // Getters and setters

    public UserSettings getSettings() {
        return settings;
    }

    public void setSettings(UserSettings settings) {
        this.settings = settings;
        updateLastModified();
    }

    public Map<String, TrainingModuleProgress> getModuleProgress() {
        return moduleProgress;
    }

    public void setModuleProgress(Map<String, TrainingModuleProgress> moduleProgress) {
        this.moduleProgress = moduleProgress != null ? moduleProgress : new HashMap<>();
        updateLastModified();
    }

    /**
     * Gets progress for a specific module.
     *
     * @param moduleId the module identifier
     * @return the progress data, or a new instance if none exists
     */
    public TrainingModuleProgress getProgressForModule(String moduleId) {
        return moduleProgress.computeIfAbsent(moduleId, TrainingModuleProgress::new);
    }

    /**
     * Sets progress for a specific module.
     *
     * @param moduleId the module identifier
     * @param progress the progress data
     */
    public void setProgressForModule(String moduleId, TrainingModuleProgress progress) {
        moduleProgress.put(moduleId, progress);
        updateLastModified();
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(LocalDateTime lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Updates the last modified timestamp to the current time.
     */
    public void updateLastModified() {
        this.lastModifiedDate = LocalDateTime.now();
    }

    /**
     * Checks if any training sessions have been completed.
     *
     * @return true if at least one module has session data
     */
    public boolean hasTrainingData() {
        return moduleProgress.values().stream()
                .anyMatch(progress -> progress.getTotalSessions() > 0);
    }

    /**
     * Gets the total number of training sessions across all modules.
     *
     * @return the total session count
     */
    public int getTotalSessions() {
        return moduleProgress.values().stream()
                .mapToInt(TrainingModuleProgress::getTotalSessions)
                .sum();
    }

    /**
     * Gets the total time spent training across all modules in seconds.
     *
     * @return the total time in seconds
     */
    public long getTotalTimeSpentSeconds() {
        return moduleProgress.values().stream()
                .mapToLong(TrainingModuleProgress::getTotalTimeSpentSeconds)
                .sum();
    }

    @Override
    public String toString() {
        return "UserData{" +
                "settings=" + settings +
                ", modulesWithProgress=" + moduleProgress.size() +
                ", totalSessions=" + getTotalSessions() +
                ", version='" + version + '\'' +
                '}';
    }
}

