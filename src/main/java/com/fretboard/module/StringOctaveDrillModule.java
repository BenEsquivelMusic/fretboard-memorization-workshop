package com.fretboard.module;

import com.fretboard.model.Frequency;
import com.fretboard.model.Note;
import com.fretboard.model.TrainingModuleProgress;
import com.fretboard.model.UserSettings;
import com.fretboard.model.string.GuitarString;
import com.fretboard.model.string.GuitarStrings;
import com.fretboard.service.AudioInputService;
import com.fretboard.service.FrequencyMapService;
import com.fretboard.service.PitchDetectionService;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Training module for octave note recognition across guitar strings.
 * 
 * This exercise begins on the highest string number (lowest pitch string) and works
 * down to the HighEString (string 1). For each string, the user is shown 10 randomly
 * generated notes one at a time. For each note displayed, the user must play ALL octaves
 * of that note that exist on the current string.
 * 
 * The program will not progress to the next note until all octaves are played through
 * the configured audio input. Each string is timed with a goal of under 25 seconds.
 * 
 * This is an ongoing training exercise - all attempts are saved for analysis.
 */
public final class StringOctaveDrillModule implements TrainingModule {

    private static final Logger LOGGER = Logger.getLogger(StringOctaveDrillModule.class.getName());
    
    private static final String MODULE_ID = "string-octave-drill";
    private static final String MODULE_NAME = "String Octave Drill";
    private static final String MODULE_DESCRIPTION = 
            "Practice finding all octaves of a note on each string. " +
            "Start from the lowest string and work up to the high E. " +
            "Goal: complete each string in under 25 seconds.";
    
    private static final int NOTES_PER_STRING = 10;
    private static final long TARGET_TIME_SECONDS = 25;
    private static final double FREQUENCY_TOLERANCE_CENTS = 50.0;

    private final UserSettings userSettings;
    private final GuitarStrings guitarStrings;
    private final FrequencyMapService frequencyMapService;
    private final AudioInputService audioInputService;
    private final PitchDetectionService pitchDetectionService;
    private final Random random;
    
    private BorderPane rootPane;
    private VBox mainContent;
    private Label currentStringLabel;
    private Label currentNoteLabel;
    private Label timerLabel;
    private Label progressLabel;
    private Label octavesRemainingLabel;
    private VBox historyBox;
    private Button startButton;
    private Button stopButton;
    
    private TrainingModuleProgress progress;
    private boolean running;
    private boolean sessionActive;
    
    // Current drill state
    private int currentStringIndex;
    private GuitarString currentString;
    private List<Note> notesForCurrentString;
    private int currentNoteIndex;
    private Note currentNote;
    private Set<Frequency> remainingOctaves;
    private long stringStartTimeNanos;
    private AnimationTimer timer;
    private List<StringAttemptResult> currentSessionResults;

    /**
     * Creates a new StringOctaveDrillModule.
     *
     * @param userSettings the user settings
     */
    public StringOctaveDrillModule(UserSettings userSettings) {
        this.userSettings = userSettings;
        this.guitarStrings = new GuitarStrings(userSettings);
        this.frequencyMapService = FrequencyMapService.getService();
        this.audioInputService = AudioInputService.getInstance();
        this.pitchDetectionService = new PitchDetectionService();
        this.random = new Random();
        this.running = false;
        this.sessionActive = false;
        this.currentSessionResults = new ArrayList<>();
        initializeUI();
    }

    private void initializeUI() {
        rootPane = new BorderPane();
        rootPane.setPadding(new Insets(20));
        rootPane.setStyle("-fx-background-color: #2a2a32;");

        // Header
        VBox headerBox = new VBox(10);
        headerBox.setAlignment(Pos.CENTER);
        
        Text titleText = new Text(MODULE_NAME);
        titleText.setFont(Font.font("System", FontWeight.BOLD, 28));
        titleText.setFill(Color.WHITE);
        
        Text descriptionText = new Text(MODULE_DESCRIPTION);
        descriptionText.setFont(Font.font("System", 14));
        descriptionText.setFill(Color.LIGHTGRAY);
        descriptionText.setWrappingWidth(600);
        
        headerBox.getChildren().addAll(titleText, descriptionText);
        rootPane.setTop(headerBox);

        // Main content area
        mainContent = new VBox(20);
        mainContent.setAlignment(Pos.CENTER);
        mainContent.setPadding(new Insets(30));
        
        // Current string display
        currentStringLabel = new Label("Ready to start");
        currentStringLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        currentStringLabel.setTextFill(Color.LIGHTGRAY);
        
        // Current note display (large)
        currentNoteLabel = new Label("-");
        currentNoteLabel.setFont(Font.font("System", FontWeight.BOLD, 72));
        currentNoteLabel.setTextFill(Color.web("#22c55e")); // Green
        
        // Timer display
        timerLabel = new Label("00:00.0");
        timerLabel.setFont(Font.font("Monospace", FontWeight.BOLD, 36));
        timerLabel.setTextFill(Color.WHITE);
        
        // Progress indicator
        progressLabel = new Label("Note 0/10");
        progressLabel.setFont(Font.font("System", 16));
        progressLabel.setTextFill(Color.LIGHTGRAY);
        
        // Octaves remaining
        octavesRemainingLabel = new Label("Play all octaves of the note on this string");
        octavesRemainingLabel.setFont(Font.font("System", 14));
        octavesRemainingLabel.setTextFill(Color.GRAY);
        
        // Control buttons
        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);
        
        startButton = new Button("Start Drill");
        startButton.setStyle("-fx-background-color: #22c55e; -fx-text-fill: white; -fx-font-size: 16px; -fx-padding: 10 30;");
        startButton.setOnAction(e -> startDrill());
        
        stopButton = new Button("Stop");
        stopButton.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-size: 16px; -fx-padding: 10 30;");
        stopButton.setOnAction(e -> stopDrill());
        stopButton.setDisable(true);
        
        buttonBox.getChildren().addAll(startButton, stopButton);
        
        mainContent.getChildren().addAll(
                currentStringLabel,
                currentNoteLabel,
                timerLabel,
                progressLabel,
                octavesRemainingLabel,
                buttonBox
        );
        
        rootPane.setCenter(mainContent);
        
        // History panel (right side)
        VBox historyContainer = new VBox(10);
        historyContainer.setPadding(new Insets(10));
        historyContainer.setStyle("-fx-background-color: #1f1f27; -fx-background-radius: 8;");
        historyContainer.setPrefWidth(300);
        
        Label historyTitle = new Label("Previous Attempts");
        historyTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        historyTitle.setTextFill(Color.WHITE);
        
        historyBox = new VBox(5);
        historyBox.setPadding(new Insets(5));
        
        ScrollPane historyScroll = new ScrollPane(historyBox);
        historyScroll.setFitToWidth(true);
        historyScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        historyScroll.setPrefHeight(400);
        
        historyContainer.getChildren().addAll(historyTitle, historyScroll);
        rootPane.setRight(historyContainer);
        
        // Initialize timer
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                updateTimer();
            }
        };
    }

    private void startDrill() {
        if (!audioInputService.isCapturing()) {
            if (!audioInputService.startCapture()) {
                showMessage("Audio Error", "Could not start audio capture. Check your audio settings.");
                return;
            }
        }
        
        audioInputService.registerAudioListener(this);
        
        sessionActive = true;
        running = true;
        currentSessionResults.clear();
        
        // Start from the highest string number (lowest pitch)
        GuitarString[] allStrings = guitarStrings.getAll();
        currentStringIndex = allStrings.length - 1; // Start at highest string number (lowest pitch)
        
        startButton.setDisable(true);
        stopButton.setDisable(false);
        
        startNextString();
    }

    private void stopDrill() {
        sessionActive = false;
        running = false;
        timer.stop();
        
        audioInputService.unregisterAudioListener(this);
        
        startButton.setDisable(false);
        stopButton.setDisable(true);
        
        currentStringLabel.setText("Drill stopped");
        currentNoteLabel.setText("-");
        progressLabel.setText("");
        octavesRemainingLabel.setText("");
        
        // Save results if any were recorded
        if (!currentSessionResults.isEmpty()) {
            saveSessionResults();
        }
    }

    private void startNextString() {
        GuitarString[] allStrings = guitarStrings.getAll();
        
        if (currentStringIndex < 0) {
            // Completed all strings
            completeDrill();
            return;
        }
        
        currentString = allStrings[currentStringIndex];
        currentNoteIndex = 0;
        notesForCurrentString = generateRandomNotes();
        
        String stringName = getStringName(currentString);
        currentStringLabel.setText("String: " + stringName + " (String " + currentString.getStringNumber() + ")");
        currentStringLabel.setTextFill(Color.LIGHTGRAY);
        
        stringStartTimeNanos = System.nanoTime();
        timer.start();
        
        showNextNote();
    }

    private void showNextNote() {
        if (currentNoteIndex >= NOTES_PER_STRING) {
            // Completed this string
            completeString();
            return;
        }
        
        currentNote = notesForCurrentString.get(currentNoteIndex);
        remainingOctaves = findOctavesOnString(currentNote, currentString);
        
        currentNoteLabel.setText(currentNote.getDisplayNote());
        progressLabel.setText("Note " + (currentNoteIndex + 1) + "/" + NOTES_PER_STRING);
        updateOctavesRemainingDisplay();
    }

    private void updateOctavesRemainingDisplay() {
        if (remainingOctaves.isEmpty()) {
            octavesRemainingLabel.setText("All octaves found!");
            octavesRemainingLabel.setTextFill(Color.web("#22c55e"));
        } else {
            StringBuilder sb = new StringBuilder("Remaining octaves: ");
            List<String> octaveNames = new ArrayList<>();
            for (Frequency f : remainingOctaves) {
                octaveNames.add(f.note().getDisplayNote() + f.octaveNumber());
            }
            Collections.sort(octaveNames);
            sb.append(String.join(", ", octaveNames));
            octavesRemainingLabel.setText(sb.toString());
            octavesRemainingLabel.setTextFill(Color.LIGHTGRAY);
        }
    }

    private void updateTimer() {
        if (!running || stringStartTimeNanos == 0) return;
        
        long elapsedNanos = System.nanoTime() - stringStartTimeNanos;
        long elapsedMillis = elapsedNanos / 1_000_000;
        long seconds = elapsedMillis / 1000;
        long tenths = (elapsedMillis % 1000) / 100;
        
        String timeStr = String.format("%02d:%02d.%d", seconds / 60, seconds % 60, tenths);
        
        // Color code based on target time
        if (seconds >= TARGET_TIME_SECONDS) {
            timerLabel.setTextFill(Color.web("#ef4444")); // Red
        } else if (seconds >= TARGET_TIME_SECONDS - 5) {
            timerLabel.setTextFill(Color.web("#f59e0b")); // Yellow/Orange
        } else {
            timerLabel.setTextFill(Color.WHITE);
        }
        
        Platform.runLater(() -> timerLabel.setText(timeStr));
    }

    private void completeString() {
        timer.stop();
        
        long elapsedNanos = System.nanoTime() - stringStartTimeNanos;
        double elapsedSeconds = elapsedNanos / 1_000_000_000.0;
        
        StringAttemptResult result = new StringAttemptResult(
                currentString.getStringNumber(),
                getStringName(currentString),
                elapsedSeconds,
                elapsedSeconds <= TARGET_TIME_SECONDS,
                LocalDateTime.now()
        );
        currentSessionResults.add(result);
        
        // Move to next string (lower string number = higher pitch)
        currentStringIndex--;
        
        // Brief pause then continue
        Platform.runLater(() -> {
            String message = String.format("Completed %s in %.1fs", 
                    result.stringName(), result.timeSeconds());
            if (result.underTarget()) {
                message += " ✓";
                currentStringLabel.setTextFill(Color.web("#22c55e"));
            } else {
                message += " (over target)";
                currentStringLabel.setTextFill(Color.web("#f59e0b"));
            }
            currentStringLabel.setText(message);
            
            // Wait a moment then continue
            new Thread(() -> {
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                if (sessionActive) {
                    Platform.runLater(this::startNextString);
                }
            }).start();
        });
    }

    private void completeDrill() {
        sessionActive = false;
        running = false;
        timer.stop();
        
        audioInputService.unregisterAudioListener(this);
        
        startButton.setDisable(false);
        stopButton.setDisable(true);
        
        currentStringLabel.setText("Drill Complete!");
        currentStringLabel.setTextFill(Color.web("#22c55e"));
        currentNoteLabel.setText("✓");
        currentNoteLabel.setTextFill(Color.web("#22c55e"));
        
        // Calculate summary
        long stringsUnderTarget = currentSessionResults.stream()
                .filter(StringAttemptResult::underTarget)
                .count();
        double totalTime = currentSessionResults.stream()
                .mapToDouble(StringAttemptResult::timeSeconds)
                .sum();
        
        progressLabel.setText(String.format("%d/%d strings under %ds goal", 
                stringsUnderTarget, currentSessionResults.size(), TARGET_TIME_SECONDS));
        octavesRemainingLabel.setText(String.format("Total time: %.1f seconds", totalTime));
        
        saveSessionResults();
        updateHistoryDisplay();
    }

    private List<Note> generateRandomNotes() {
        List<Note> notes = new ArrayList<>();
        Note[] allNotes = Note.values();
        
        // Generate NOTES_PER_STRING unique random notes
        List<Note> availableNotes = new ArrayList<>(List.of(allNotes));
        Collections.shuffle(availableNotes, random);
        
        for (int i = 0; i < NOTES_PER_STRING && i < availableNotes.size(); i++) {
            notes.add(availableNotes.get(i));
        }
        
        return notes;
    }

    private Set<Frequency> findOctavesOnString(Note note, GuitarString string) {
        Set<Frequency> octaves = new HashSet<>();
        
        // Check open string
        if (string.getOpenString().note() == note) {
            octaves.add(string.getOpenString());
        }
        
        // Check all frets
        for (Frequency freq : string.getFretBoardFrequencies()) {
            if (freq.note() == note) {
                octaves.add(freq);
            }
        }
        
        return octaves;
    }

    private String getStringName(GuitarString string) {
        return string.getStringName();
    }

    @Override
    public void onAudioInput(byte[] audioData, float sampleRate) {
        if (!running || !sessionActive || remainingOctaves == null || remainingOctaves.isEmpty()) {
            return;
        }
        
        // Detect pitch from audio
        Double detectedFrequency = pitchDetectionService.detectPitch(audioData, sampleRate);
        
        if (detectedFrequency != null && detectedFrequency > 0) {
            // Check if the detected frequency matches any remaining octave
            Frequency matchedOctave = null;
            
            for (Frequency target : remainingOctaves) {
                double targetHz = frequencyMapService.getMappedFrequencyNumericValue(target);
                double cents = 1200 * Math.log(detectedFrequency / targetHz) / Math.log(2);
                
                if (Math.abs(cents) <= FREQUENCY_TOLERANCE_CENTS) {
                    matchedOctave = target;
                    break;
                }
            }
            
            if (matchedOctave != null) {
                final Frequency foundOctave = matchedOctave;
                remainingOctaves.remove(foundOctave);
                
                Platform.runLater(() -> {
                    updateOctavesRemainingDisplay();
                    
                    if (remainingOctaves.isEmpty()) {
                        // Move to next note after brief delay
                        new Thread(() -> {
                            try {
                                Thread.sleep(300);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                            if (sessionActive) {
                                currentNoteIndex++;
                                Platform.runLater(this::showNextNote);
                            }
                        }).start();
                    }
                });
            }
        }
    }

    private void saveSessionResults() {
        if (progress == null || currentSessionResults.isEmpty()) return;
        
        // Store in custom properties
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        StringBuilder resultData = new StringBuilder();
        
        for (StringAttemptResult result : currentSessionResults) {
            resultData.append(String.format("%d|%s|%.2f|%b;", 
                    result.stringNumber(), 
                    result.stringName(), 
                    result.timeSeconds(), 
                    result.underTarget()));
        }
        
        // Append to history
        String historyKey = "drill_history";
        String existingHistory = progress.getCustomProperties().getOrDefault(historyKey, "");
        String newEntry = timestamp + ":" + resultData.toString();
        
        if (!existingHistory.isEmpty()) {
            existingHistory += "\n";
        }
        progress.getCustomProperties().put(historyKey, existingHistory + newEntry);
        
        // Update stats
        progress.incrementSessions();
        progress.setLastSessionDate(LocalDateTime.now());
        
        long totalTime = (long) currentSessionResults.stream()
                .mapToDouble(StringAttemptResult::timeSeconds)
                .sum();
        progress.addTimeSpent(totalTime);
        
        // Track best times per string
        for (StringAttemptResult result : currentSessionResults) {
            String bestKey = "best_string_" + result.stringNumber();
            String currentBest = progress.getCustomProperties().get(bestKey);
            if (currentBest == null || result.timeSeconds() < Double.parseDouble(currentBest)) {
                progress.getCustomProperties().put(bestKey, String.valueOf(result.timeSeconds()));
            }
        }
    }

    private void updateHistoryDisplay() {
        if (progress == null) return;
        
        Platform.runLater(() -> {
            historyBox.getChildren().clear();
            
            String history = progress.getCustomProperties().get("drill_history");
            if (history == null || history.isEmpty()) {
                Label noHistory = new Label("No previous attempts");
                noHistory.setTextFill(Color.GRAY);
                historyBox.getChildren().add(noHistory);
                return;
            }
            
            String[] sessions = history.split("\n");
            // Show most recent first, limit to last 10
            int startIndex = Math.max(0, sessions.length - 10);
            for (int i = sessions.length - 1; i >= startIndex; i--) {
                String session = sessions[i];
                String[] parts = session.split(":");
                if (parts.length >= 2) {
                    VBox sessionBox = new VBox(2);
                    sessionBox.setStyle("-fx-background-color: #2a2a32; -fx-padding: 5; -fx-background-radius: 4;");
                    
                    Label dateLabel = new Label(parts[0].replace("T", " "));
                    dateLabel.setTextFill(Color.GRAY);
                    dateLabel.setFont(Font.font("System", 10));
                    
                    sessionBox.getChildren().add(dateLabel);
                    historyBox.getChildren().add(sessionBox);
                }
            }
        });
    }

    private void showMessage(String title, String message) {
        Platform.runLater(() -> {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.WARNING);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    @Override
    public String getModuleId() {
        return MODULE_ID;
    }

    @Override
    public String getDisplayName() {
        return MODULE_NAME;
    }

    @Override
    public String getDescription() {
        return MODULE_DESCRIPTION;
    }

    @Override
    public Node getView() {
        return rootPane;
    }

    @Override
    public void initialize(TrainingModuleProgress progress) {
        this.progress = progress != null ? progress : new TrainingModuleProgress(MODULE_ID);
        updateHistoryDisplay();
    }

    @Override
    public TrainingModuleProgress getProgress() {
        return progress;
    }

    @Override
    public void start() {
        running = true;
    }

    @Override
    public void pause() {
        running = false;
    }

    @Override
    public void stop() {
        stopDrill();
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public void cleanup() {
        stopDrill();
        if (timer != null) {
            timer.stop();
        }
    }

    /**
     * Record for storing individual string attempt results.
     */
    public record StringAttemptResult(
            int stringNumber,
            String stringName,
            double timeSeconds,
            boolean underTarget,
            LocalDateTime timestamp
    ) implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
    }
}
