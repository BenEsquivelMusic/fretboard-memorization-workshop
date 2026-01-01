package com.fretboard.service;

import com.fretboard.model.UserData;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service class for managing user data persistence using binary file storage.
 * Implements singleton pattern for application-wide access.
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

    public static synchronized UserDataService getInstance() {
        if (instance == null) {
            instance = new UserDataService();
        }
        return instance;
    }

    public String getFileExtension() {
        return FILE_EXTENSION;
    }

    public UserData getCurrentUserData() {
        return currentUserData;
    }

    public Optional<Path> getCurrentFilePath() {
        return Optional.ofNullable(currentFilePath);
    }

    public boolean hasUnsavedData() {
        return hasUnsavedChanges;
    }

    public void markAsModified() {
        this.hasUnsavedChanges = true;
        this.currentUserData.updateLastModified();
    }

    public void createNewUserData() {
        this.currentUserData = new UserData();
        this.currentFilePath = null;
        this.hasUnsavedChanges = false;
    }

    public boolean saveToFile(Path filePath) {
        Path targetPath = ensureFileExtension(filePath);
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new BufferedOutputStream(Files.newOutputStream(targetPath)))) {
            oos.writeObject(currentUserData);
            this.currentFilePath = targetPath;
            this.hasUnsavedChanges = false;
            currentUserData.getSettings().setDataSaveLocation(targetPath.toString());
            LOGGER.info("User data saved to: " + targetPath);
            return true;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to save user data", e);
            return false;
        }
    }

    public boolean save() {
        if (currentFilePath != null) {
            return saveToFile(currentFilePath);
        }
        return false;
    }

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
            LOGGER.log(Level.SEVERE, "Failed to load user data", e);
            return Optional.empty();
        }
    }

    public boolean deleteFile(Path filePath) {
        try {
            boolean deleted = Files.deleteIfExists(filePath);
            if (deleted && filePath.equals(currentFilePath)) {
                currentFilePath = null;
            }
            return deleted;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to delete file", e);
            return false;
        }
    }

    private Path ensureFileExtension(Path filePath) {
        String fileName = filePath.getFileName().toString();
        if (!fileName.toLowerCase().endsWith(FILE_EXTENSION)) {
            return filePath.resolveSibling(fileName + FILE_EXTENSION);
        }
        return filePath;
    }
}