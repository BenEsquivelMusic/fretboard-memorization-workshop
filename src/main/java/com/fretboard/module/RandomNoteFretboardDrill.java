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
import java.util.*;
import java.util.logging.Logger;

/**
 * Training module for full fretboard note recognition.
 * 
 * This exercise shows the user 10 random notes one at a time. For each note displayed,
 * the user must play ALL variations of that note across ALL strings on the entire fretboard.
 * 
 * The program will not progress to the next note until all variations are played through
 * the configured audio input. Each note is timed with a goal of under 10 seconds.
 * 
 * This is an ongoing training exercise - all attempts are saved for analysis.
 */
public final class RandomNoteFretboardDrill implements TrainingModule {

    private static final Logger LOGGER = Logger.getLogger(RandomNoteFretboardDrill.class.getName());
    
    public static final String MODULE_ID = "random-note-fretboard-drill";
    private static final String MODULE_NAME = "Random Note Fretboard Drill";
    private static final String MODULE_DESCRIPTION = 
            "Practice finding all instances of a note across the entire fretboard. " +
            "You will be shown 10 random notes - find every occurrence on all strings. " +
            "Goal: complete each note in under 10 seconds.";
    
    private static final int TOTAL_NOTES = 10;
    private static final long TARGET_TIME_SECONDS = 10;
    private static final double FREQUENCY_TOLERANCE_CENTS = 50.0;

    private final UserSettings userSettings;
    private final GuitarStrings guitarStrings;
    private final FrequencyMapService frequencyMapService;
    private final AudioInputService audioInputService;
    private final PitchDetectionService pitchDetectionService;
    private final Random random;
    
    private BorderPane rootPane;
    private VBox mainContent;
    private Label instructionLabel;
    private Label currentNoteLabel;
    private Label timerLabel;
    private Label progressLabel;
    private Label locationsRemainingLabel;
    private VBox historyBox;
    private Button startButton;
    private Button stopButton;
    
    private TrainingModuleProgress progress;
    private boolean running;
    private boolean sessionActive;
    
    // Current drill state
    private List<Note> notesForSession;
    private int currentNoteIndex;
    private Note currentNote;
    private Set<Frequency> remainingLocations;
    private long noteStartTimeNanos;
    private AnimationTimer timer;
    private List<NoteAttemptResult> currentSessionResults;

    /**
     * Creates a new RandomNoteFretboardDrill.
     *
     * @param userSettings the user settings
     */
    public RandomNoteFretboardDrill(UserSettings userSettings) {
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
        
        // Instruction display
        instructionLabel = new Label("Ready to start");
        instructionLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        instructionLabel.setTextFill(Color.LIGHTGRAY);
        
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
        
        // Locations remaining
        locationsRemainingLabel = new Label("Play all instances of the note on the fretboard");
        locationsRemainingLabel.setFont(Font.font("System", 14));
        locationsRemainingLabel.setTextFill(Color.GRAY);
        
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
                instructionLabel,
                currentNoteLabel,
                timerLabel,
                progressLabel,
                locationsRemainingLabel,
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
        LOGGER.info("Starting fretboard drill");
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
        
        // Generate random notes for the session
        notesForSession = generateRandomNotes();
        currentNoteIndex = 0;
        
        startButton.setDisable(true);
        stopButton.setDisable(false);
        
        showNextNote();
    }

    private void stopDrill() {
        LOGGER.info("Stopping fretboard drill");
        sessionActive = false;
        running = false;
        timer.stop();
        
        audioInputService.unregisterAudioListener(this);
        
        startButton.setDisable(false);
        stopButton.setDisable(true);
        
        instructionLabel.setText("Drill stopped");
        currentNoteLabel.setText("-");
        progressLabel.setText("");
        locationsRemainingLabel.setText("");
        
        // Save results if any were recorded
        if (!currentSessionResults.isEmpty()) {
            saveSessionResults();
        }
    }

    private void showNextNote() {
        if (currentNoteIndex >= TOTAL_NOTES) {
            // Completed all notes
            completeDrill();
            return;
        }
        
        currentNote = notesForSession.get(currentNoteIndex);
        remainingLocations = findAllLocationsOnFretboard(currentNote);
        
        instructionLabel.setText("Find all " + currentNote.getDisplayNote() + " notes on the fretboard");
        instructionLabel.setTextFill(Color.LIGHTGRAY);
        currentNoteLabel.setText(currentNote.getDisplayNote());
        progressLabel.setText("Note " + (currentNoteIndex + 1) + "/" + TOTAL_NOTES);
        updateLocationsRemainingDisplay();
        
        noteStartTimeNanos = System.nanoTime();
        timer.start();
    }

    private void updateLocationsRemainingDisplay() {
        if (remainingLocations.isEmpty()) {
            locationsRemainingLabel.setText("All locations found!");
            locationsRemainingLabel.setTextFill(Color.web("#22c55e"));
        } else {
            StringBuilder sb = new StringBuilder("Remaining locations (" + remainingLocations.size() + "): ");
            List<String> locationNames = new ArrayList<>();
            for (Frequency f : remainingLocations) {
                locationNames.add(f.note().getDisplayNote() + f.octaveNumber());
            }
            Collections.sort(locationNames);
            // Show only first few to avoid cluttering the UI
            int showCount = Math.min(6, locationNames.size());
            sb.append(String.join(", ", locationNames.subList(0, showCount)));
            if (locationNames.size() > showCount) {
                sb.append(" ...");
            }
            locationsRemainingLabel.setText(sb.toString());
            locationsRemainingLabel.setTextFill(Color.LIGHTGRAY);
        }
    }

    private void updateTimer() {
        if (!running || noteStartTimeNanos == 0) return;
        
        long elapsedNanos = System.nanoTime() - noteStartTimeNanos;
        long elapsedMillis = elapsedNanos / 1_000_000;
        long seconds = elapsedMillis / 1000;
        long tenths = (elapsedMillis % 1000) / 100;
        
        String timeStr = String.format("%02d:%02d.%d", seconds / 60, seconds % 60, tenths);
        
        // Color code based on target time
        if (seconds >= TARGET_TIME_SECONDS) {
            timerLabel.setTextFill(Color.web("#ef4444")); // Red
        } else if (seconds >= TARGET_TIME_SECONDS - 3) {
            timerLabel.setTextFill(Color.web("#f59e0b")); // Yellow/Orange
        } else {
            timerLabel.setTextFill(Color.WHITE);
        }
        
        Platform.runLater(() -> timerLabel.setText(timeStr));
    }

    private void completeNote() {
        timer.stop();
        
        long elapsedNanos = System.nanoTime() - noteStartTimeNanos;
        double elapsedSeconds = elapsedNanos / 1_000_000_000.0;
        
        NoteAttemptResult result = new NoteAttemptResult(
                currentNote.getDisplayNote(),
                elapsedSeconds,
                elapsedSeconds <= TARGET_TIME_SECONDS,
                LocalDateTime.now()
        );
        currentSessionResults.add(result);
        
        // Move to next note
        currentNoteIndex++;
        
        // Brief pause then continue
        Platform.runLater(() -> {
            String message = String.format("Completed %s in %.1fs", 
                    result.noteName(), result.timeSeconds());
            if (result.underTarget()) {
                message += " ✓";
                instructionLabel.setTextFill(Color.web("#22c55e"));
            } else {
                message += " (over target)";
                instructionLabel.setTextFill(Color.web("#f59e0b"));
            }
            instructionLabel.setText(message);
            
            // Wait a moment then continue
            new Thread(() -> {
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                if (sessionActive) {
                    Platform.runLater(this::showNextNote);
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
        
        instructionLabel.setText("Drill Complete!");
        instructionLabel.setTextFill(Color.web("#22c55e"));
        currentNoteLabel.setText("✓");
        currentNoteLabel.setTextFill(Color.web("#22c55e"));
        
        // Calculate summary
        long notesUnderTarget = currentSessionResults.stream()
                .filter(NoteAttemptResult::underTarget)
                .count();
        double totalTime = currentSessionResults.stream()
                .mapToDouble(NoteAttemptResult::timeSeconds)
                .sum();
        
        progressLabel.setText(String.format("%d/%d notes under %ds goal", 
                notesUnderTarget, currentSessionResults.size(), TARGET_TIME_SECONDS));
        locationsRemainingLabel.setText(String.format("Total time: %.1f seconds", totalTime));
        
        saveSessionResults();
        updateHistoryDisplay();
    }

    private List<Note> generateRandomNotes() {
        List<Note> notes = new ArrayList<>();
        Note[] allNotes = Note.values();
        
        // Generate TOTAL_NOTES unique random notes
        List<Note> availableNotes = new ArrayList<>(List.of(allNotes));
        Collections.shuffle(availableNotes, random);
        
        for (int i = 0; i < TOTAL_NOTES && i < availableNotes.size(); i++) {
            notes.add(availableNotes.get(i));
        }
        
        return notes;
    }

    private Set<Frequency> findAllLocationsOnFretboard(Note note) {
        Set<Frequency> locations = new HashSet<>();
        
        // Check all strings
        for (GuitarString string : guitarStrings.getAll()) {
            // Check open string
            if (string.getOpenString().note() == note) {
                locations.add(string.getOpenString());
            }
            
            // Check all frets
            for (Frequency freq : string.getFretBoardFrequencies()) {
                if (freq.note() == note) {
                    locations.add(freq);
                }
            }
        }
        
        return locations;
    }

    @Override
    public void onAudioInput(byte[] audioData, float sampleRate) {
        if (!running || !sessionActive || remainingLocations == null || remainingLocations.isEmpty()) {
            return;
        }
        
        // Detect pitch from audio
        Double detectedFrequency = pitchDetectionService.detectPitch(audioData, sampleRate);
        
        if (detectedFrequency != null && detectedFrequency > 0) {
            // Check if the detected frequency matches any remaining location
            Frequency matchedLocation = null;
            
            for (Frequency target : remainingLocations) {
                double targetHz = frequencyMapService.getMappedFrequencyNumericValue(target);
                double cents = 1200 * Math.log(detectedFrequency / targetHz) / Math.log(2);
                
                if (Math.abs(cents) <= FREQUENCY_TOLERANCE_CENTS) {
                    matchedLocation = target;
                    break;
                }
            }
            
            if (matchedLocation != null) {
                final Frequency foundLocation = matchedLocation;
                remainingLocations.remove(foundLocation);
                
                Platform.runLater(() -> {
                    updateLocationsRemainingDisplay();
                    
                    if (remainingLocations.isEmpty()) {
                        // Move to next note after brief delay
                        new Thread(() -> {
                            try {
                                Thread.sleep(300);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                            if (sessionActive) {
                                Platform.runLater(this::completeNote);
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
        
        for (NoteAttemptResult result : currentSessionResults) {
            resultData.append(String.format("%s|%.2f|%b;", 
                    result.noteName(), 
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
                .mapToDouble(NoteAttemptResult::timeSeconds)
                .sum();
        progress.addTimeSpent(totalTime);
        
        // Track best times per note
        for (NoteAttemptResult result : currentSessionResults) {
            String bestKey = "best_note_" + result.noteName();
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
     * Record for storing individual note attempt results.
     */
    public record NoteAttemptResult(
            String noteName,
            double timeSeconds,
            boolean underTarget,
            LocalDateTime timestamp
    ) implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
    }
}
