package com.fretboard.module;

import com.fretboard.model.TrainingModuleProgress;
import com.fretboard.model.UserSettings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * Training module that displays a simple guitar fretboard visualization.
 * The fretboard is rendered based on the user's configured number of strings and frets.
 */
public class FretboardDisplayModule implements TrainingModule {

    private static final String MODULE_ID = "fretboard-display";
    private static final String MODULE_NAME = "Fretboard Display";
    private static final String MODULE_DESCRIPTION = "Displays a visual representation of the guitar fretboard based on your settings.";

    private static final double FRET_WIDTH = 60.0;
    private static final double STRING_SPACING = 30.0;
    private static final double NUT_WIDTH = 8.0;
    private static final double FRET_LINE_WIDTH = 2.0;
    private static final double STRING_LINE_WIDTH = 1.5;
    private static final double PADDING = 40.0;
    private static final double FRET_MARKER_RADIUS = 6.0;

    private static final int[] SINGLE_FRET_MARKERS = {3, 5, 7, 9, 15, 17, 19, 21};
    private static final int[] DOUBLE_FRET_MARKERS = {12, 24};

    private final UserSettings userSettings;
    private BorderPane rootPane;
    private Canvas fretboardCanvas;
    private TrainingModuleProgress progress;
    private boolean running;

    /**
     * Creates a new FretboardDisplayModule with the given user settings.
     *
     * @param userSettings the user settings containing fret and string configuration
     */
    public FretboardDisplayModule(UserSettings userSettings) {
        this.userSettings = userSettings;
        this.running = false;
        initializeUI();
    }

    private void initializeUI() {
        rootPane = new BorderPane();
        rootPane.setPadding(new Insets(20));

        // Title section
        VBox headerBox = new VBox(10);
        headerBox.setAlignment(Pos.CENTER);
        
        Text titleText = new Text(MODULE_NAME);
        titleText.setFont(Font.font("System", 24));
        
        Text descriptionText = new Text(MODULE_DESCRIPTION);
        descriptionText.setFont(Font.font("System", 14));
        
        Text settingsInfo = new Text(String.format("Strings: %d | Frets: %d", 
                userSettings.getNumberOfStrings(), 
                userSettings.getNumberOfFrets()));
        settingsInfo.setFont(Font.font("System", 12));
        settingsInfo.setFill(Color.GRAY);
        
        headerBox.getChildren().addAll(titleText, descriptionText, settingsInfo);
        rootPane.setTop(headerBox);
        BorderPane.setMargin(headerBox, new Insets(0, 0, 20, 0));

        // Create canvas for fretboard
        double canvasWidth = calculateCanvasWidth();
        double canvasHeight = calculateCanvasHeight();
        fretboardCanvas = new Canvas(canvasWidth, canvasHeight);
        
        VBox canvasContainer = new VBox(fretboardCanvas);
        canvasContainer.setAlignment(Pos.CENTER);
        rootPane.setCenter(canvasContainer);

        renderFretboard();
    }

    private double calculateCanvasWidth() {
        return (userSettings.getNumberOfFrets() * FRET_WIDTH) + NUT_WIDTH + (PADDING * 2);
    }

    private double calculateCanvasHeight() {
        return ((userSettings.getNumberOfStrings() - 1) * STRING_SPACING) + (PADDING * 2);
    }

    private void renderFretboard() {
        GraphicsContext gc = fretboardCanvas.getGraphicsContext2D();
        int numFrets = userSettings.getNumberOfFrets();
        int numStrings = userSettings.getNumberOfStrings();
        
        double fretboardWidth = numFrets * FRET_WIDTH;
        double fretboardHeight = (numStrings - 1) * STRING_SPACING;
        
        // Clear canvas
        gc.clearRect(0, 0, fretboardCanvas.getWidth(), fretboardCanvas.getHeight());
        
        // Draw fretboard background
        gc.setFill(Color.rgb(139, 90, 43)); // Wood brown color
        gc.fillRect(PADDING, PADDING, fretboardWidth + NUT_WIDTH, fretboardHeight);
        
        // Draw nut (the zero fret)
        gc.setFill(Color.rgb(245, 245, 220)); // Bone/ivory color
        gc.fillRect(PADDING, PADDING, NUT_WIDTH, fretboardHeight);
        
        // Draw frets
        gc.setStroke(Color.rgb(192, 192, 192)); // Silver color for frets
        gc.setLineWidth(FRET_LINE_WIDTH);
        
        for (int fret = 1; fret <= numFrets; fret++) {
            double x = PADDING + NUT_WIDTH + (fret * FRET_WIDTH);
            gc.strokeLine(x, PADDING, x, PADDING + fretboardHeight);
        }
        
        // Draw fret markers
        gc.setFill(Color.rgb(255, 255, 255, 0.8)); // White with slight transparency
        
        for (int marker : SINGLE_FRET_MARKERS) {
            if (marker <= numFrets) {
                drawSingleFretMarker(gc, marker, numStrings, fretboardHeight);
            }
        }
        
        for (int marker : DOUBLE_FRET_MARKERS) {
            if (marker <= numFrets) {
                drawDoubleFretMarker(gc, marker, numStrings, fretboardHeight);
            }
        }
        
        // Draw strings
        gc.setStroke(Color.rgb(210, 180, 140)); // Tan/string color
        gc.setLineWidth(STRING_LINE_WIDTH);
        
        for (int string = 0; string < numStrings; string++) {
            double y = PADDING + (string * STRING_SPACING);
            double stringThickness = STRING_LINE_WIDTH + (string * 0.3); // Thicker strings at bottom
            gc.setLineWidth(stringThickness);
            gc.strokeLine(PADDING, y, PADDING + NUT_WIDTH + fretboardWidth, y);
        }
        
        // Draw fret numbers
        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("System", 10));
        
        for (int fret = 1; fret <= numFrets; fret++) {
            double x = PADDING + NUT_WIDTH + ((fret - 0.5) * FRET_WIDTH);
            double y = PADDING + fretboardHeight + 15;
            String fretNumber = String.valueOf(fret);
            gc.fillText(fretNumber, x - (fretNumber.length() * 3), y);
        }
        
        // Draw "Open" label
        gc.fillText("0", PADDING + (NUT_WIDTH / 2) - 3, PADDING + fretboardHeight + 15);
    }

    private void drawSingleFretMarker(GraphicsContext gc, int fret, int numStrings, double fretboardHeight) {
        double x = PADDING + NUT_WIDTH + ((fret - 0.5) * FRET_WIDTH);
        double y = PADDING + (fretboardHeight / 2);
        gc.fillOval(x - FRET_MARKER_RADIUS, y - FRET_MARKER_RADIUS, 
                FRET_MARKER_RADIUS * 2, FRET_MARKER_RADIUS * 2);
    }

    private void drawDoubleFretMarker(GraphicsContext gc, int fret, int numStrings, double fretboardHeight) {
        double x = PADDING + NUT_WIDTH + ((fret - 0.5) * FRET_WIDTH);
        double y1 = PADDING + (fretboardHeight / 3);
        double y2 = PADDING + (fretboardHeight * 2 / 3);
        gc.fillOval(x - FRET_MARKER_RADIUS, y1 - FRET_MARKER_RADIUS, 
                FRET_MARKER_RADIUS * 2, FRET_MARKER_RADIUS * 2);
        gc.fillOval(x - FRET_MARKER_RADIUS, y2 - FRET_MARKER_RADIUS, 
                FRET_MARKER_RADIUS * 2, FRET_MARKER_RADIUS * 2);
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
        // Re-render in case settings changed
        double newWidth = calculateCanvasWidth();
        double newHeight = calculateCanvasHeight();
        fretboardCanvas.setWidth(newWidth);
        fretboardCanvas.setHeight(newHeight);
        renderFretboard();
    }

    @Override
    public TrainingModuleProgress getProgress() {
        return progress;
    }

    @Override
    public void start() {
        running = true;
        if (progress != null) {
            progress.incrementSessions();
        }
    }

    @Override
    public void pause() {
        running = false;
    }

    @Override
    public void stop() {
        running = false;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public void cleanup() {
        running = false;
    }

    @Override
    public String getIconPath() {
        return null;
    }
}
