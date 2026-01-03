package com.fretboard.module;

import com.fretboard.model.TrainingModuleProgress;
import com.fretboard.model.UserSettings;
import com.fretboard.model.WoodGrain;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.layout.VBox;

/**
 * Training module that displays a simple guitar fretboard visualization.
 * The fretboard is rendered based on the user's configured number of strings, frets, and wood grain.
 * The rendering dynamically scales to fit the available canvas window size.
 */
public class FretboardDisplayModule implements TrainingModule {

    private static final String MODULE_ID = "fretboard-display";
    private static final String MODULE_NAME = "Fretboard Display";
    private static final String MODULE_DESCRIPTION = "Displays a visual representation of the guitar fretboard based on your settings.";

    // Base dimensions used for scaling calculations
    private static final double BASE_FRET_WIDTH = 60.0;
    private static final double BASE_STRING_SPACING = 30.0;
    private static final double BASE_NUT_WIDTH = 8.0;
    private static final double BASE_FRET_LINE_WIDTH = 2.0;
    private static final double BASE_STRING_WIDTH = 2.5;
    private static final double BASE_STRING_WIDTH_INCREMENT = 0.6;
    private static final double BASE_PADDING = 40.0;
    private static final double BASE_FRET_MARKER_RADIUS = 6.0;
    private static final double BASE_FONT_SIZE = 10.0;

    // Slightly lighter canvas background to blend better with the guitar
    private static final Color CANVAS_BACKGROUND_COLOR = Color.rgb(42, 42, 50);

    private static final int[] SINGLE_FRET_MARKERS = {3, 5, 7, 9, 15, 17, 19, 21};
    private static final int[] DOUBLE_FRET_MARKERS = {12, 24};

    // Minimum and maximum scale factors to prevent extreme sizes
    private static final double MIN_SCALE = 0.5;
    private static final double MAX_SCALE = 2.0;

    private final UserSettings userSettings;
    private BorderPane rootPane;
    private Canvas fretboardCanvas;
    private StackPane canvasContainer;
    private TrainingModuleProgress progress;
    private boolean running;

    /**
     * Creates a new FretboardDisplayModule with the given user settings.
     *
     * @param userSettings the user settings containing fret, string, and wood grain configuration
     */
    public FretboardDisplayModule(UserSettings userSettings) {
        this.userSettings = userSettings;
        this.running = false;
        initializeUI();
    }

    private void initializeUI() {
        rootPane = new BorderPane();
        rootPane.setPadding(new Insets(20));
        rootPane.setStyle("-fx-background-color: #2a2a32;");

        // Title section
        VBox headerBox = new VBox(10);
        headerBox.setAlignment(Pos.CENTER);
        
        Text titleText = new Text(MODULE_NAME);
        titleText.setFont(Font.font("System", 24));
        titleText.setFill(Color.WHITE);
        
        Text descriptionText = new Text(MODULE_DESCRIPTION);
        descriptionText.setFont(Font.font("System", 14));
        descriptionText.setFill(Color.LIGHTGRAY);
        
        WoodGrain woodGrain = userSettings.getFretboardWoodGrain();
        Text settingsInfo = new Text(String.format("Strings: %d | Frets: %d | Wood: %s", 
                userSettings.getNumberOfStrings(), 
                userSettings.getNumberOfFrets(),
                woodGrain.getDisplayName()));
        settingsInfo.setFont(Font.font("System", 12));
        settingsInfo.setFill(Color.GRAY);
        
        headerBox.getChildren().addAll(titleText, descriptionText, settingsInfo);
        rootPane.setTop(headerBox);
        BorderPane.setMargin(headerBox, new Insets(0, 0, 20, 0));

        // Create canvas for fretboard with initial base dimensions
        double canvasWidth = calculateBaseCanvasWidth();
        double canvasHeight = calculateBaseCanvasHeight();
        fretboardCanvas = new Canvas(canvasWidth, canvasHeight);
        
        // Use StackPane for better centering during resize
        canvasContainer = new StackPane(fretboardCanvas);
        canvasContainer.setAlignment(Pos.CENTER);
        canvasContainer.setStyle("-fx-background-color: #2a2a32;");
        rootPane.setCenter(canvasContainer);

        // Add listeners for dynamic resizing
        canvasContainer.widthProperty().addListener((obs, oldVal, newVal) -> resizeAndRender());
        canvasContainer.heightProperty().addListener((obs, oldVal, newVal) -> resizeAndRender());

        renderFretboard();
    }

    /**
     * Calculates the base canvas width without any scaling applied.
     */
    private double calculateBaseCanvasWidth() {
        return (userSettings.getNumberOfFrets() * BASE_FRET_WIDTH) + BASE_NUT_WIDTH + (BASE_PADDING * 2);
    }

    /**
     * Calculates the base canvas height without any scaling applied.
     */
    private double calculateBaseCanvasHeight() {
        return ((userSettings.getNumberOfStrings() - 1) * BASE_STRING_SPACING) + (BASE_PADDING * 2);
    }

    /**
     * Resizes the canvas to fit the available container space and re-renders the fretboard.
     */
    private void resizeAndRender() {
        double containerWidth = canvasContainer.getWidth();
        double containerHeight = canvasContainer.getHeight();
        
        if (containerWidth <= 0 || containerHeight <= 0) {
            return;
        }

        double baseWidth = calculateBaseCanvasWidth();
        double baseHeight = calculateBaseCanvasHeight();

        // Calculate scale factors for width and height
        double scaleX = containerWidth / baseWidth;
        double scaleY = containerHeight / baseHeight;

        // Use the smaller scale to maintain aspect ratio
        double scale = Math.min(scaleX, scaleY);

        // Clamp scale to min/max bounds
        scale = Math.max(MIN_SCALE, Math.min(MAX_SCALE, scale));

        // Apply scaling to canvas dimensions
        double newWidth = baseWidth * scale;
        double newHeight = baseHeight * scale;

        fretboardCanvas.setWidth(newWidth);
        fretboardCanvas.setHeight(newHeight);

        renderFretboard();
    }

    /**
     * Calculates the current scale factor based on canvas size relative to base dimensions.
     */
    private double calculateScaleFactor() {
        double baseWidth = calculateBaseCanvasWidth();
        double currentWidth = fretboardCanvas.getWidth();
        return currentWidth / baseWidth;
    }

    private void renderFretboard() {
        GraphicsContext gc = fretboardCanvas.getGraphicsContext2D();
        int numFrets = userSettings.getNumberOfFrets();
        int numStrings = userSettings.getNumberOfStrings();
        WoodGrain woodGrain = userSettings.getFretboardWoodGrain();

        // Calculate scale factor for dynamic rendering
        double scale = calculateScaleFactor();

        // Apply scaling to all dimensions
        double padding = BASE_PADDING * scale;
        double fretWidth = BASE_FRET_WIDTH * scale;
        double stringSpacing = BASE_STRING_SPACING * scale;
        double nutWidth = BASE_NUT_WIDTH * scale;
        double fretLineWidth = BASE_FRET_LINE_WIDTH * scale;
        double stringWidth = BASE_STRING_WIDTH * scale;
        double stringWidthIncrement = BASE_STRING_WIDTH_INCREMENT * scale;
        double fretMarkerRadius = BASE_FRET_MARKER_RADIUS * scale;
        double fontSize = BASE_FONT_SIZE * scale;
        
        double fretboardWidth = numFrets * fretWidth;
        double fretboardHeight = (numStrings - 1) * stringSpacing;
        
        // Clear canvas with lighter background
        gc.setFill(CANVAS_BACKGROUND_COLOR);
        gc.fillRect(0, 0, fretboardCanvas.getWidth(), fretboardCanvas.getHeight());
        
        // Draw fretboard background with wood grain colors
        drawWoodGrainBackground(gc, woodGrain, fretboardWidth, fretboardHeight, padding, nutWidth, scale);
        
        // Draw nut (the zero fret)
        gc.setFill(Color.rgb(245, 245, 220)); // Bone/ivory color
        gc.fillRect(padding, padding, nutWidth, fretboardHeight);
        
        // Draw frets
        gc.setStroke(Color.rgb(192, 192, 192)); // Silver color for frets
        gc.setLineWidth(fretLineWidth);
        
        for (int fret = 1; fret <= numFrets; fret++) {
            double x = padding + nutWidth + (fret * fretWidth);
            gc.strokeLine(x, padding, x, padding + fretboardHeight);
        }
        
        // Draw fret markers
        gc.setFill(Color.rgb(255, 255, 255, 0.9)); // White with slight transparency
        
        for (int marker : SINGLE_FRET_MARKERS) {
            if (marker <= numFrets) {
                drawSingleFretMarker(gc, marker, fretboardHeight, padding, nutWidth, fretWidth, fretMarkerRadius);
            }
        }
        
        for (int marker : DOUBLE_FRET_MARKERS) {
            if (marker <= numFrets) {
                drawDoubleFretMarker(gc, marker, fretboardHeight, padding, nutWidth, fretWidth, fretMarkerRadius);
            }
        }
        
        // Draw strings - more pronounced with metallic colors
        for (int string = 0; string < numStrings; string++) {
            double y = padding + (string * stringSpacing);
            double stringThickness = stringWidth + (string * stringWidthIncrement);
            
            // Draw string shadow for depth
            gc.setStroke(Color.rgb(40, 40, 40, 0.5));
            gc.setLineWidth(stringThickness + scale);
            gc.strokeLine(padding, y + scale, padding + nutWidth + fretboardWidth, y + scale);
            
            // Draw main string with metallic silver color
            gc.setStroke(Color.rgb(200, 200, 210)); // Bright metallic silver
            gc.setLineWidth(stringThickness);
            gc.strokeLine(padding, y, padding + nutWidth + fretboardWidth, y);
            
            // Draw string highlight for 3D effect
            gc.setStroke(Color.rgb(255, 255, 255, 0.6));
            gc.setLineWidth(Math.max(scale, stringThickness * 0.3));
            gc.strokeLine(padding, y - (stringThickness * 0.2), padding + nutWidth + fretboardWidth, y - (stringThickness * 0.2));
        }
        
        // Draw fret numbers
        gc.setFill(Color.LIGHTGRAY);
        gc.setFont(Font.font("System", fontSize));
        
        for (int fret = 1; fret <= numFrets; fret++) {
            double x = padding + nutWidth + ((fret - 0.5) * fretWidth);
            double y = padding + fretboardHeight + (15 * scale);
            String fretNumber = String.valueOf(fret);
            gc.fillText(fretNumber, x - (fretNumber.length() * 3 * scale), y);
        }
        
        // Draw "Open" label
        gc.fillText("0", padding + (nutWidth / 2) - (3 * scale), padding + fretboardHeight + (15 * scale));
    }

    private void drawWoodGrainBackground(GraphicsContext gc, WoodGrain woodGrain, double fretboardWidth, 
            double fretboardHeight, double padding, double nutWidth, double scale) {
        Color primaryColor = woodGrain.getPrimaryColor();
        Color secondaryColor = woodGrain.getSecondaryColor();
        
        // Draw base wood color
        gc.setFill(primaryColor);
        gc.fillRect(padding, padding, fretboardWidth + nutWidth, fretboardHeight);
        
        // Draw subtle wood grain lines for texture
        gc.setStroke(secondaryColor);
        gc.setLineWidth(scale);
        
        double grainSpacing = 8.0 * scale;
        for (double y = padding; y < padding + fretboardHeight; y += grainSpacing) {
            // Add slight variation to make it look more natural
            double offset = Math.sin(y * 0.1 / scale) * 2 * scale;
            gc.setGlobalAlpha(0.3);
            gc.strokeLine(padding + offset, y, padding + nutWidth + fretboardWidth + offset, y);
        }
        gc.setGlobalAlpha(1.0);
    }

    private void drawSingleFretMarker(GraphicsContext gc, int fret, double fretboardHeight, 
            double padding, double nutWidth, double fretWidth, double fretMarkerRadius) {
        double x = padding + nutWidth + ((fret - 0.5) * fretWidth);
        double y = padding + (fretboardHeight / 2);
        gc.fillOval(x - fretMarkerRadius, y - fretMarkerRadius, 
                fretMarkerRadius * 2, fretMarkerRadius * 2);
    }

    private void drawDoubleFretMarker(GraphicsContext gc, int fret, double fretboardHeight, 
            double padding, double nutWidth, double fretWidth, double fretMarkerRadius) {
        double x = padding + nutWidth + ((fret - 0.5) * fretWidth);
        double y1 = padding + (fretboardHeight / 3);
        double y2 = padding + (fretboardHeight * 2 / 3);
        gc.fillOval(x - fretMarkerRadius, y1 - fretMarkerRadius, 
                fretMarkerRadius * 2, fretMarkerRadius * 2);
        gc.fillOval(x - fretMarkerRadius, y2 - fretMarkerRadius, 
                fretMarkerRadius * 2, fretMarkerRadius * 2);
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
        // Trigger resize and render to adapt to current container size
        resizeAndRender();
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
