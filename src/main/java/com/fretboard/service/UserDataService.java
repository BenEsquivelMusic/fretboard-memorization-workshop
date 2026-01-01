package com.fretboard.service;

import com.fretboard.model.UserData;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service for managing user data persistence using binary serialization.
 * Supports saving and loading user data to/from files chosen by the user.
 */
public class UserDataService {

    private static final Logger LOGGER = Logger.getLogger(UserDataService.class.getName());
    private static final String FILE_EXTENSION = ".fmw";

    private static UserDataService instance;

    private UserData currentUserData;
    private Path currentFilePath;
    private boolean hasUnsavedChanges;

    private UserDataService() {
        this.currentUserData = new UserData();
        this.hasUnsavedChanges = false;
    }

    /**
     * Gets the singleton instance of UserDataService.
     *
     * @return the UserDataService instance
     */
    public static synchronized UserDataService getInstance() {
        if (instance == null) {
            instance = new UserDataService();
        }
        return instance;
    }

    /**
     * Gets the file extension for user data files.
     *
     * @return the file extension including the dot
     */
    public String getFileExtension() {
        return FILE_EXTENSION;
    }

    /**
     * Gets the current user data.
     *
     * @return the current UserData object
     */
    public UserData getCurrentUserData() {
        return currentUserData;
    }

    /**
     * Gets the current file path if one is set.
     *
     * @return an Optional containing the current file path
     */
    public Optional<Path> getCurrentFilePath() {
        return Optional.ofNullable(currentFilePath);
    }

    /**
     * Creates new user data, replacing any existing data.
     */
    public void createNewUserData() {
        this.currentUserData = new UserData();
        this.currentFilePath = null;
        this.hasUnsavedChanges = false;
    }

    /**
     * Saves the current user data to a binary file at the specified path.
     *
     * @param filePath the path to save to
     * @return true if save was successful
     */
    public boolean saveToFile(Path filePath) {
        Path targetPath = ensureFileExtension(filePath);

        try {
            Files.createDirectories(targetPath.getParent());

            currentUserData.updateLastModified();
            currentUserData.getSettings().setDataSaveLocation(targetPath.toString());

            try (ObjectOutputStream oos = new ObjectOutputStream(
                    new BufferedOutputStream(Files.newOutputStream(targetPath)))) {
                oos.writeObject(currentUserData);
            }

            this.currentFilePath = targetPath;
            this.hasUnsavedChanges = false;

            LOGGER.info("User data saved to: " + targetPath);
            return true;

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to save user data to: " + targetPath, e);
            return false;
        }
    }

    /**
     * Saves the current user data to the current file path.
     *
     * @return true if save was successful, false if no current file path or save failed
     */
    public boolean save() {
        if (currentFilePath == null) {
            LOGGER.warning("Cannot save: no current file path set");
            return false;
        }
        return saveToFile(currentFilePath);
    }

    /**
     * Loads user data from a binary file at the specified path.
     *
     * @param filePath the path to load from
     * @return an Optional containing the loaded UserData, or empty if loading failed
     */
    public Optional<UserData> loadFromFile(Path filePath) {
        if (!Files.exists(filePath)) {
            LOGGER.warning("File does not exist: " + filePath);
            return Optional.empty();
        }

        try (ObjectInputStream ois = new ObjectInputStream(
                new BufferedInputStream(Files.newInputStream(filePath)))) {

            UserData loadedData = (UserData) ois.readObject();

            this.currentUserData = loadedData;
            this.currentFilePath = filePath;
            this.hasUnsavedChanges = false;

            LOGGER.info("User data loaded from: " + filePath);
            return Optional.of(loadedData);

        } catch (IOException | ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "Failed to load user data from: " + filePath, e);
            return Optional.empty();
        }
    }

    /**
     * Marks that the current user data has unsaved changes.
     */
    public void markAsModified() {
        this.hasUnsavedChanges = true;
        this.currentUserData.updateLastModified();
    }

    /**
     * Checks if there are unsaved changes.
     *
     * @return true if there are unsaved changes
     */
    public boolean hasUnsavedChanges() {
        return hasUnsavedChanges;
    }

    /**
     * Checks if there is unsaved data (new data without a file path).
     *
     * @return true if there is data but no current file path
     */
    public boolean hasUnsavedData() {
        return currentFilePath == null || hasUnsavedChanges;
    }

    /**
     * Ensures the file path has the correct extension.
     *
     * @param filePath the original file path
     * @return the file path with the correct extension
     */
    private Path ensureFileExtension(Path filePath) {
        String fileName = filePath.getFileName().toString();
        if (!fileName.toLowerCase().endsWith(FILE_EXTENSION)) {
            return filePath.resolveSibling(fileName + FILE_EXTENSION);
        }
        return filePath;
    }

    /**
     * Exports user data to a specified path without changing the current file path.
     *
     * @param filePath the path to export to
     * @return true if export was successful
     */
    public boolean exportToFile(Path filePath) {
        Path targetPath = ensureFileExtension(filePath);

        try {
            Files.createDirectories(targetPath.getParent());

            try (ObjectOutputStream oos = new ObjectOutputStream(
                    new BufferedOutputStream(Files.newOutputStream(targetPath)))) {
                oos.writeObject(currentUserData);
            }

            LOGGER.info("User data exported to: " + targetPath);
            return true;

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to export user data to: " + targetPath, e);
            return false;
        }
    }
}