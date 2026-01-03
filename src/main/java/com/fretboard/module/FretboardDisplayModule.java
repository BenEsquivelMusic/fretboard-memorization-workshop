package com.fretboard.module;

import com.fretboard.model.Frequency;
import com.fretboard.model.Note;
import com.fretboard.model.TrainingModuleProgress;
import com.fretboard.model.UserSettings;
import com.fretboard.model.WoodGrain;
import com.fretboard.model.string.GuitarString;
import com.fretboard.model.string.GuitarStrings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.CheckBox;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

/**
 * Training module that displays a simple guitar fretboard visualization.
 * The fretboard is rendered based on the user's configured number of strings, frets, and wood grain.
 * The rendering starts at a small default size suitable for small screens and dynamically
 * scales up to fit the available canvas window size.
 * Supports both standard and fanned-fret (multi-scale) fretboard styles.
 * 
 * Features frequency display overlay with interactive note highlighting:
 * - Toggle to show/hide all frequencies (note name + octave) on each fret
 * - Click on a frequency to highlight it in green
 * - All other frequencies with the same note are highlighted in blue
 * 
 * For fanned frets, the rendering mimics real multi-scale guitars where:
 * - A "neutral fret" (typically around fret 7-9) is perpendicular to the strings
 * - Frets toward the nut angle in one direction (bass side extends further toward headstock)
 * - Frets toward the bridge angle in the opposite direction (bass side extends further toward bridge)
 */
public class FretboardDisplayModule implements TrainingModule {

    private static final String MODULE_ID = "fretboard-display";
    private static final String MODULE_NAME = "Fretboard Display";
    private static final String MODULE_DESCRIPTION = "Displays a visual representation of the guitar fretboard based on your settings.";

    // Base dimensions used for scaling calculations - these are intentionally small
    // to ensure the guitar is fully visible on small screens by default
    private static final double BASE_FRET_WIDTH = 30.0;
    private static final double BASE_STRING_SPACING = 15.0;
    private static final double BASE_NUT_WIDTH = 4.0;
    private static final double BASE_FRET_LINE_WIDTH = 1.0;
    private static final double BASE_STRING_WIDTH = 1.25;
    private static final double BASE_STRING_WIDTH_INCREMENT = 0.3;
    private static final double BASE_PADDING = 20.0;
    private static final double BASE_FRET_MARKER_RADIUS = 3.0;
    private static final double BASE_FONT_SIZE = 8.0;
    // Extra space below fretboard for fret number labels
    private static final double BASE_LABEL_AREA_HEIGHT = 12.0;

    // Fanned fret configuration - the angle creates a fan effect across the fretboard
    // This represents how much the fret slants per string (in pixels at base scale)
    private static final double BASE_FAN_OFFSET_PER_STRING = 3.0;
    
    // The neutral fret ratio determines where the perpendicular fret is located.
    // On real multi-scale guitars like Strandberg, Dingwall, and Kiesel, this is typically
    // around fret 7-9 (roughly 35% of the way down a 24-fret neck).
    // At the neutral fret, the fret line is perpendicular to the strings.
    // Frets before it angle one way, frets after angle the other way.
    private static final double NEUTRAL_FRET_RATIO = 0.35;

    // Slightly lighter canvas background to blend better with the guitar
    private static final Color CANVAS_BACKGROUND_COLOR = Color.rgb(42, 42, 50);
    
    // Colors for frequency highlighting
    private static final Color SELECTED_FREQUENCY_COLOR = Color.rgb(34, 197, 94); // Green for selected
    private static final Color MATCHING_NOTE_COLOR = Color.rgb(59, 130, 246); // Blue for matching notes

    private static final int[] SINGLE_FRET_MARKERS = {3, 5, 7, 9, 15, 17, 19, 21};
    private static final int[] DOUBLE_FRET_MARKERS = {12, 24};

    private final UserSettings userSettings;
    private BorderPane rootPane;
    private Canvas fretboardCanvas;
    private StackPane canvasContainer;
    private TrainingModuleProgress progress;
    private boolean running;
    
    // Frequency display state
    private boolean showFrequencies = false;
    private boolean showOctave = true;
    private Frequency selectedFrequency = null;
    private int selectedStringIndex = -1;
    private int selectedFretIndex = -1;
    private List<FrequencyClickRegion> frequencyClickRegions = new ArrayList<>();
    private GuitarStrings guitarStrings;

    /**
     * Creates a new FretboardDisplayModule with the given user settings.
     *
     * @param userSettings the user settings containing fret, string, and wood grain configuration
     */
    public FretboardDisplayModule(UserSettings userSettings) {
        this.userSettings = userSettings;
        this.running = false;
        this.guitarStrings = new GuitarStrings(userSettings);
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
        String fretStyle = userSettings.isFannedFret() ? "Fanned (Multi-Scale)" : "Standard";
        Text settingsInfo = new Text(String.format("Strings: %d | Frets: %d | Wood: %s | Style: %s", 
                userSettings.getNumberOfStrings(), 
                userSettings.getNumberOfFrets(),
                woodGrain.getDisplayName(),
                fretStyle));
        settingsInfo.setFont(Font.font("System", 12));
        settingsInfo.setFill(Color.GRAY);
        
        // Controls section
        HBox controlsBox = new HBox(20);
        controlsBox.setAlignment(Pos.CENTER);
        controlsBox.setPadding(new Insets(10, 0, 0, 0));
        
        CheckBox showFrequenciesCheckBox = new CheckBox("Show Notes");
        showFrequenciesCheckBox.setStyle("-fx-text-fill: white;");
        showFrequenciesCheckBox.setSelected(showFrequencies);
        showFrequenciesCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            showFrequencies = newVal;
            if (!showFrequencies) {
                clearFrequencySelection();
            }
            renderFretboard();
        });
        
        CheckBox showOctaveCheckBox = new CheckBox("Show Octave");
        showOctaveCheckBox.setStyle("-fx-text-fill: white;");
        showOctaveCheckBox.setSelected(showOctave);
        showOctaveCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            showOctave = newVal;
            renderFretboard();
        });
        
        controlsBox.getChildren().addAll(showFrequenciesCheckBox, showOctaveCheckBox);
        
        headerBox.getChildren().addAll(titleText, descriptionText, settingsInfo, controlsBox);
        rootPane.setTop(headerBox);
        BorderPane.setMargin(headerBox, new Insets(0, 0, 20, 0));

        // Create canvas for fretboard with initial small base dimensions
        double canvasWidth = calculateBaseCanvasWidth();
        double canvasHeight = calculateBaseCanvasHeight();
        fretboardCanvas = new Canvas(canvasWidth, canvasHeight);
        
        // Add mouse click handler for frequency selection
        fretboardCanvas.setOnMouseClicked(this::handleCanvasClick);
        
        // Use StackPane for better centering during resize
        canvasContainer = new StackPane(fretboardCanvas);
        canvasContainer.setAlignment(Pos.CENTER);
        canvasContainer.setStyle("-fx-background-color: #2a2a32;");
        rootPane.setCenter(canvasContainer);

        // Add listeners for dynamic resizing - scales up to fill available space
        canvasContainer.widthProperty().addListener((obs, oldVal, newVal) -> resizeAndRender());
        canvasContainer.heightProperty().addListener((obs, oldVal, newVal) -> resizeAndRender());

        renderFretboard();
    }
    
    /**
     * Handles mouse clicks on the canvas for frequency selection.
     */
    private void handleCanvasClick(MouseEvent event) {
        if (!showFrequencies) {
            return;
        }
        
        double clickX = event.getX();
        double clickY = event.getY();
        
        // Check if click is within any frequency label region
        for (FrequencyClickRegion region : frequencyClickRegions) {
            if (region.contains(clickX, clickY)) {
                // If clicking on the same frequency, deselect it
                if (selectedStringIndex == region.stringIndex && selectedFretIndex == region.fretIndex) {
                    clearFrequencySelection();
                } else {
                    selectedFrequency = region.frequency;
                    selectedStringIndex = region.stringIndex;
                    selectedFretIndex = region.fretIndex;
                }
                renderFretboard();
                return;
            }
        }
        
        // Clicked outside any frequency - clear selection
        clearFrequencySelection();
        renderFretboard();
    }
    
    /**
     * Clears the current frequency selection.
     */
    private void clearFrequencySelection() {
        selectedFrequency = null;
        selectedStringIndex = -1;
        selectedFretIndex = -1;
    }

    /**
     * Calculates the base canvas width without any scaling applied.
     * Uses small default dimensions suitable for small screens.
     */
    private double calculateBaseCanvasWidth() {
        // Add extra width for fanned frets to accommodate the angled bridge end
        double extraWidth = userSettings.isFannedFret() ? 
                BASE_FAN_OFFSET_PER_STRING * (userSettings.getNumberOfStrings() - 1) : 0;
        return (userSettings.getNumberOfFrets() * BASE_FRET_WIDTH) + BASE_NUT_WIDTH + (BASE_PADDING * 2) + extraWidth;
    }

    /**
     * Calculates the base canvas height without any scaling applied.
     * Uses small default dimensions suitable for small screens.
     * Includes space for the fret number labels below the fretboard.
     */
    private double calculateBaseCanvasHeight() {
        return ((userSettings.getNumberOfStrings() - 1) * BASE_STRING_SPACING) + (BASE_PADDING * 2) + BASE_LABEL_AREA_HEIGHT;
    }

    /**
     * Resizes the canvas to fit the available container space and re-renders the fretboard.
     * Scales up from the small default size to fill the available space while maintaining
     * aspect ratio. This ensures the full guitar is always visible, starting small for
     * small screens and growing to fill larger windows.
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

        // Use the smaller scale to maintain aspect ratio and ensure full visibility
        // This allows the guitar to scale up to fill the available space
        double scale = Math.min(scaleX, scaleY);

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
    
    /**
     * Calculates the fan offset for a given fret position relative to the neutral fret.
     * The neutral fret (determined by NEUTRAL_FRET_RATIO) has zero offset and is perpendicular.
     * Frets before the neutral fret have negative offset (angled toward headstock on bass side).
     * Frets after the neutral fret have positive offset (angled toward bridge on bass side).
     * 
     * @param fretPosition the fret position (can be fractional for positions between frets)
     * @param numFrets total number of frets
     * @param fanOffsetPerString the maximum fan offset per string at full scale
     * @param numStrings number of strings
     * @return the fan offset in pixels (positive = bass side toward bridge, negative = bass side toward nut)
     */
    private double calculateFanOffset(double fretPosition, int numFrets, double fanOffsetPerString, int numStrings) {
        double neutralFret = numFrets * NEUTRAL_FRET_RATIO;
        double distanceFromNeutral = fretPosition - neutralFret;
        // Normalize to range [-1, 1] based on distance from neutral fret
        double normalizedDistance = distanceFromNeutral / (numFrets * (1 - NEUTRAL_FRET_RATIO));
        return normalizedDistance * fanOffsetPerString * (numStrings - 1);
    }
    
    /**
     * Calculates the X coordinate for a point on a fanned fret line at a given string position.
     * 
     * @param fret the fret number
     * @param stringIndex the string index (0 = top/treble string, numStrings-1 = bottom/bass string)
     * @param padding canvas padding
     * @param nutWidth width of the nut
     * @param fretWidth width of each fret space
     * @param numFrets total number of frets
     * @param fanOffsetPerString fan offset per string
     * @param numStrings number of strings
     * @return X coordinate for the fret at the given string position
     */
    private double calculateFannedFretX(double fret, int stringIndex, double padding, double nutWidth,
            double fretWidth, int numFrets, double fanOffsetPerString, int numStrings) {
        // Base X position (as if it were a standard fret)
        double baseX = padding + nutWidth + (fret * fretWidth);
        // Calculate fan offset for this fret
        double fanOffset = calculateFanOffset(fret, numFrets, fanOffsetPerString, numStrings);
        // Apply offset proportionally based on string position (0 at top, full at bottom)
        double stringProgress = (double) stringIndex / (numStrings - 1);
        return baseX + (fanOffset * stringProgress);
    }

    private void renderFretboard() {
        GraphicsContext gc = fretboardCanvas.getGraphicsContext2D();
        int numFrets = userSettings.getNumberOfFrets();
        int numStrings = userSettings.getNumberOfStrings();
        WoodGrain woodGrain = userSettings.getFretboardWoodGrain();
        boolean isFannedFret = userSettings.isFannedFret();

        // Clear click regions for fresh render
        frequencyClickRegions.clear();

        // Calculate scale factor for dynamic rendering
        double scale = calculateScaleFactor();

        // Apply scaling to all dimensions
        double padding = BASE_PADDING * scale;
        double fretWidth = BASE_FRET_WIDTH * scale;
        double stringSpacing = BASE_STRING_SPACING * scale;
        double nutWidth = BASE_NUT_WIDTH * scale;
        double fretLineWidth = Math.max(1.0, BASE_FRET_LINE_WIDTH * scale);
        double stringWidth = BASE_STRING_WIDTH * scale;
        double stringWidthIncrement = BASE_STRING_WIDTH_INCREMENT * scale;
        double fretMarkerRadius = BASE_FRET_MARKER_RADIUS * scale;
        double fontSize = Math.max(6.0, BASE_FONT_SIZE * scale);
        double labelAreaHeight = BASE_LABEL_AREA_HEIGHT * scale;
        double fanOffsetPerString = BASE_FAN_OFFSET_PER_STRING * scale;
        
        double fretboardWidth = numFrets * fretWidth;
        double fretboardHeight = (numStrings - 1) * stringSpacing;
        
        // Clear canvas with lighter background
        gc.setFill(CANVAS_BACKGROUND_COLOR);
        gc.fillRect(0, 0, fretboardCanvas.getWidth(), fretboardCanvas.getHeight());
        
        // Draw fretboard background with wood grain colors
        if (isFannedFret) {
            drawFannedWoodGrainBackground(gc, woodGrain, fretboardWidth, fretboardHeight, 
                    padding, nutWidth, scale, numFrets, numStrings, fretWidth, stringSpacing, fanOffsetPerString);
        } else {
            drawWoodGrainBackground(gc, woodGrain, fretboardWidth, fretboardHeight, padding, nutWidth, scale);
        }
        
        // Draw nut (the zero fret) - for fanned frets, this is also angled
        gc.setFill(Color.rgb(245, 245, 220)); // Bone/ivory color
        if (isFannedFret) {
            // Draw angled nut for fanned frets
            // Calculate the fan offset at fret 0
            double nutFanOffset = calculateFanOffset(0, numFrets, fanOffsetPerString, numStrings);
            double nutTopX = padding + nutWidth;
            double nutBottomX = padding + nutWidth + nutFanOffset;
            double[] xPoints = {padding, nutTopX, nutBottomX, padding + nutFanOffset};
            double[] yPoints = {padding, padding, padding + fretboardHeight, padding + fretboardHeight};
            gc.fillPolygon(xPoints, yPoints, 4);
        } else {
            gc.fillRect(padding, padding, nutWidth, fretboardHeight);
        }
        
        // Draw frets
        gc.setStroke(Color.rgb(192, 192, 192)); // Silver color for frets
        gc.setLineWidth(fretLineWidth);
        
        for (int fret = 1; fret <= numFrets; fret++) {
            if (isFannedFret) {
                // Calculate fanned fret positions using the neutral fret concept
                double topX = calculateFannedFretX(fret, 0, padding, nutWidth, fretWidth, numFrets, fanOffsetPerString, numStrings);
                double bottomX = calculateFannedFretX(fret, numStrings - 1, padding, nutWidth, fretWidth, numFrets, fanOffsetPerString, numStrings);
                gc.strokeLine(topX, padding, bottomX, padding + fretboardHeight);
            } else {
                double x = padding + nutWidth + (fret * fretWidth);
                gc.strokeLine(x, padding, x, padding + fretboardHeight);
            }
        }
        
        // Draw fret markers
        gc.setFill(Color.rgb(255, 255, 255, 0.9)); // White with slight transparency
        
        for (int marker : SINGLE_FRET_MARKERS) {
            if (marker <= numFrets) {
                if (isFannedFret) {
                    drawFannedSingleFretMarker(gc, marker, fretboardHeight, padding, nutWidth, 
                            fretWidth, fretMarkerRadius, numFrets, numStrings, fanOffsetPerString);
                } else {
                    drawSingleFretMarker(gc, marker, fretboardHeight, padding, nutWidth, fretWidth, fretMarkerRadius);
                }
            }
        }
        
        for (int marker : DOUBLE_FRET_MARKERS) {
            if (marker <= numFrets) {
                if (isFannedFret) {
                    drawFannedDoubleFretMarker(gc, marker, fretboardHeight, padding, nutWidth, 
                            fretWidth, fretMarkerRadius, numFrets, numStrings, stringSpacing, fanOffsetPerString);
                } else {
                    drawDoubleFretMarker(gc, marker, fretboardHeight, padding, nutWidth, fretWidth, fretMarkerRadius);
                }
            }
        }
        
        // Draw strings - more pronounced with metallic colors
        for (int string = 0; string < numStrings; string++) {
            double y = padding + (string * stringSpacing);
            double stringThickness = Math.max(1.0, stringWidth + (string * stringWidthIncrement));
            
            // Calculate string start and end X positions for fanned frets
            double startX;
            double endX;
            
            if (isFannedFret) {
                // For fanned frets, strings start at the nut position for this string
                // and end at the bridge position for this string
                double nutOffset = calculateFanOffset(0, numFrets, fanOffsetPerString, numStrings);
                double bridgeOffset = calculateFanOffset(numFrets, numFrets, fanOffsetPerString, numStrings);
                double stringProgress = (double) string / (numStrings - 1);
                startX = padding + (nutOffset * stringProgress);
                endX = padding + nutWidth + fretboardWidth + (bridgeOffset * stringProgress);
            } else {
                startX = padding;
                endX = padding + nutWidth + fretboardWidth;
            }
            
            // Draw string shadow for depth
            gc.setStroke(Color.rgb(40, 40, 40, 0.5));
            gc.setLineWidth(stringThickness + Math.max(0.5, scale * 0.5));
            gc.strokeLine(startX, y + Math.max(0.5, scale * 0.5), endX, y + Math.max(0.5, scale * 0.5));
            
            // Draw main string with metallic silver color
            gc.setStroke(Color.rgb(200, 200, 210)); // Bright metallic silver
            gc.setLineWidth(stringThickness);
            gc.strokeLine(startX, y, endX, y);
            
            // Draw string highlight for 3D effect
            gc.setStroke(Color.rgb(255, 255, 255, 0.6));
            gc.setLineWidth(Math.max(0.5, stringThickness * 0.3));
            gc.strokeLine(startX, y - (stringThickness * 0.2), endX, y - (stringThickness * 0.2));
        }
        
        // Draw frequency labels if enabled
        if (showFrequencies) {
            drawFrequencyLabels(gc, numFrets, numStrings, padding, nutWidth, fretWidth, 
                    stringSpacing, fontSize, scale, isFannedFret, fanOffsetPerString);
        }
        
        // Draw fret numbers below the fretboard
        gc.setFill(Color.LIGHTGRAY);
        gc.setFont(Font.font("System", fontSize));
        
        double labelY = padding + fretboardHeight + (labelAreaHeight * 0.75);
        
        for (int fret = 1; fret <= numFrets; fret++) {
            double x;
            if (isFannedFret) {
                // For fanned frets, position label at the center of the angled fret
                double topX = calculateFannedFretX(fret - 0.5, 0, padding, nutWidth, fretWidth, numFrets, fanOffsetPerString, numStrings);
                double bottomX = calculateFannedFretX(fret - 0.5, numStrings - 1, padding, nutWidth, fretWidth, numFrets, fanOffsetPerString, numStrings);
                x = (topX + bottomX) / 2;
            } else {
                x = padding + nutWidth + ((fret - 0.5) * fretWidth);
            }
            String fretNumber = String.valueOf(fret);
            gc.fillText(fretNumber, x - (fretNumber.length() * 2.5 * scale), labelY);
        }
        
        // Draw "Open" label (fret 0)
        gc.fillText("0", padding + (nutWidth / 2) - (2.5 * scale), labelY);
    }
    
    /**
     * Draws frequency labels on each fret position for all strings.
     */
    private void drawFrequencyLabels(GraphicsContext gc, int numFrets, int numStrings,
            double padding, double nutWidth, double fretWidth, double stringSpacing,
            double fontSize, double scale, boolean isFannedFret, double fanOffsetPerString) {
        
        double frequencyFontSize = Math.max(5.0, fontSize * 0.75);
        gc.setFont(Font.font("System", frequencyFontSize));
        
        GuitarString[] strings = guitarStrings.getAll();
        
        for (int stringIndex = 0; stringIndex < numStrings && stringIndex < strings.length; stringIndex++) {
            GuitarString guitarString = strings[stringIndex];
            double y = padding + (stringIndex * stringSpacing);
            
            // Draw open string frequency (fret 0)
            Frequency openFrequency = guitarString.getOpenString();
            double openX;
            if (isFannedFret) {
                double nutOffset = calculateFanOffset(0, numFrets, fanOffsetPerString, numStrings);
                double stringProgress = (double) stringIndex / (numStrings - 1);
                openX = padding + (nutOffset * stringProgress) + (nutWidth / 2);
            } else {
                openX = padding + (nutWidth / 2);
            }
            drawFrequencyLabel(gc, openFrequency, openX, y, frequencyFontSize, scale, stringIndex, 0);
            
            // Draw fretted frequencies
            List<Frequency> fretFrequencies = guitarString.getFretBoardFrequencies();
            for (int fretIndex = 0; fretIndex < fretFrequencies.size() && fretIndex < numFrets; fretIndex++) {
                Frequency frequency = fretFrequencies.get(fretIndex);
                int fret = fretIndex + 1;
                
                double x;
                if (isFannedFret) {
                    double topX = calculateFannedFretX(fret - 0.5, 0, padding, nutWidth, fretWidth, numFrets, fanOffsetPerString, numStrings);
                    double bottomX = calculateFannedFretX(fret - 0.5, numStrings - 1, padding, nutWidth, fretWidth, numFrets, fanOffsetPerString, numStrings);
                    double stringProgress = (double) stringIndex / (numStrings - 1);
                    x = topX + (bottomX - topX) * stringProgress;
                } else {
                    x = padding + nutWidth + ((fret - 0.5) * fretWidth);
                }
                
                drawFrequencyLabel(gc, frequency, x, y, frequencyFontSize, scale, stringIndex, fret);
            }
        }
    }
    
    /**
     * Draws a single frequency label with appropriate highlighting.
     */
    private void drawFrequencyLabel(GraphicsContext gc, Frequency frequency, double x, double y,
            double fontSize, double scale, int stringIndex, int fretIndex) {
        
        // Determine label text based on showOctave setting
        String label = showOctave ? frequency.note().getDisplayNote() + frequency.octaveNumber() 
                                  : frequency.note().getDisplayNote();
        double labelWidth = label.length() * fontSize * 0.6;
        double labelHeight = fontSize * 1.2;
        
        // Calculate label bounds
        double labelX = x - (labelWidth / 2);
        double labelY = y - (labelHeight / 2);
        
        // Store click region (always stored, even if label not drawn)
        frequencyClickRegions.add(new FrequencyClickRegion(
                labelX, labelY, labelWidth, labelHeight, frequency, stringIndex, fretIndex));
        
        // Check if we should skip drawing this label
        if (selectedFrequency != null) {
            boolean isSelected = (stringIndex == selectedStringIndex && fretIndex == selectedFretIndex);
            boolean isMatchingNote = (frequency.note() == selectedFrequency.note());
            if (!isSelected && !isMatchingNote) {
                // Still store click region but don't draw the label
                return;
            }
        }
        
        // Determine background color based on selection
        Color bgColor;
        if (selectedFrequency != null) {
            if (stringIndex == selectedStringIndex && fretIndex == selectedFretIndex) {
                // This is the selected frequency
                bgColor = SELECTED_FREQUENCY_COLOR;
            } else if (frequency.note() == selectedFrequency.note()) {
                // This frequency has the same note as the selected one
                bgColor = MATCHING_NOTE_COLOR;
            } else {
                // Not selected, not matching
                bgColor = Color.rgb(60, 60, 70, 0.8);
            }
        } else {
            // No selection - default background
            bgColor = Color.rgb(60, 60, 70, 0.8);
        }
        
        // Draw background
        gc.setFill(bgColor);
        gc.fillRoundRect(labelX, labelY, labelWidth, labelHeight, 4 * scale, 4 * scale);
        
        // Draw text
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("System", fontSize));
        gc.fillText(label, labelX + (2 * scale), y + (fontSize * 0.35));
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
        gc.setLineWidth(Math.max(0.5, scale * 0.5));
        
        double grainSpacing = Math.max(4.0, 8.0 * scale);
        for (double y = padding; y < padding + fretboardHeight; y += grainSpacing) {
            // Add slight variation to make it look more natural
            double offset = Math.sin(y * 0.1 / Math.max(0.1, scale)) * 2 * scale;
            gc.setGlobalAlpha(0.3);
            gc.strokeLine(padding + offset, y, padding + nutWidth + fretboardWidth + offset, y);
        }
        gc.setGlobalAlpha(1.0);
    }

    /**
     * Draws wood grain background for fanned fret fretboards.
     * The background shape follows the realistic multi-scale fret pattern with
     * a perpendicular neutral fret and fanning in both directions.
     */
    private void drawFannedWoodGrainBackground(GraphicsContext gc, WoodGrain woodGrain, double fretboardWidth, 
            double fretboardHeight, double padding, double nutWidth, double scale, int numFrets, 
            int numStrings, double fretWidth, double stringSpacing, double fanOffsetPerString) {
        Color primaryColor = woodGrain.getPrimaryColor();
        Color secondaryColor = woodGrain.getSecondaryColor();
        
        // Calculate the polygon shape for fanned fret fretboard
        // The shape is determined by the nut and bridge positions for each string
        double nutFanOffset = calculateFanOffset(0, numFrets, fanOffsetPerString, numStrings);
        double bridgeFanOffset = calculateFanOffset(numFrets, numFrets, fanOffsetPerString, numStrings);
        
        // Top edge (treble side, string index 0)
        double topLeft = padding + (nutFanOffset * 0); // Nut position for top string
        double topRight = padding + nutWidth + fretboardWidth + (bridgeFanOffset * 0); // Bridge position for top string
        
        // Bottom edge (bass side, string index numStrings-1)
        double bottomLeft = padding + nutFanOffset; // Nut position for bottom string
        double bottomRight = padding + nutWidth + fretboardWidth + bridgeFanOffset; // Bridge position for bottom string
        
        // Draw base wood color as a polygon
        gc.setFill(primaryColor);
        double[] xPoints = {topLeft, topRight, bottomRight, bottomLeft};
        double[] yPoints = {padding, padding, padding + fretboardHeight, padding + fretboardHeight};
        gc.fillPolygon(xPoints, yPoints, 4);
        
        // Draw subtle wood grain lines for texture
        gc.setStroke(secondaryColor);
        gc.setLineWidth(Math.max(0.5, scale * 0.5));
        
        double grainSpacing = Math.max(4.0, 8.0 * scale);
        for (double y = padding; y < padding + fretboardHeight; y += grainSpacing) {
            // Calculate the X offset based on Y position for fanned effect
            double yProgress = (y - padding) / fretboardHeight;
            double leftX = topLeft + (bottomLeft - topLeft) * yProgress;
            double rightX = topRight + (bottomRight - topRight) * yProgress;
            
            // Add slight variation to make it look more natural
            double offset = Math.sin(y * 0.1 / Math.max(0.1, scale)) * 2 * scale;
            gc.setGlobalAlpha(0.3);
            gc.strokeLine(leftX + offset, y, rightX + offset, y);
        }
        gc.setGlobalAlpha(1.0);
    }

    private void drawSingleFretMarker(GraphicsContext gc, int fret, double fretboardHeight, 
            double padding, double nutWidth, double fretWidth, double fretMarkerRadius) {
        double x = padding + nutWidth + ((fret - 0.5) * fretWidth);
        double y = padding + (fretboardHeight / 2);
        double radius = Math.max(2.0, fretMarkerRadius);
        gc.fillOval(x - radius, y - radius, radius * 2, radius * 2);
    }

    /**
     * Draws a single fret marker for fanned fret fretboards.
     * The marker is positioned along the angled fret line at the center of the fretboard.
     */
    private void drawFannedSingleFretMarker(GraphicsContext gc, int fret, double fretboardHeight, 
            double padding, double nutWidth, double fretWidth, double fretMarkerRadius,
            int numFrets, int numStrings, double fanOffsetPerString) {
        // Calculate position at the middle of the fretboard (middle string position)
        int middleStringIndex = (numStrings - 1) / 2;
        double fretPosition = fret - 0.5; // Center of the fret space
        
        // Calculate X at the middle string position
        double topX = calculateFannedFretX(fretPosition, 0, padding, nutWidth, fretWidth, numFrets, fanOffsetPerString, numStrings);
        double bottomX = calculateFannedFretX(fretPosition, numStrings - 1, padding, nutWidth, fretWidth, numFrets, fanOffsetPerString, numStrings);
        double x = (topX + bottomX) / 2; // Center of the fret line
        
        double y = padding + (fretboardHeight / 2);
        double radius = Math.max(2.0, fretMarkerRadius);
        gc.fillOval(x - radius, y - radius, radius * 2, radius * 2);
    }

    private void drawDoubleFretMarker(GraphicsContext gc, int fret, double fretboardHeight, 
            double padding, double nutWidth, double fretWidth, double fretMarkerRadius) {
        double x = padding + nutWidth + ((fret - 0.5) * fretWidth);
        double y1 = padding + (fretboardHeight / 3);
        double y2 = padding + (fretboardHeight * 2 / 3);
        double radius = Math.max(2.0, fretMarkerRadius);
        gc.fillOval(x - radius, y1 - radius, radius * 2, radius * 2);
        gc.fillOval(x - radius, y2 - radius, radius * 2, radius * 2);
    }

    /**
     * Draws double fret markers for fanned fret fretboards.
     * The markers are positioned along the angled fret line at 1/3 and 2/3 positions.
     */
    private void drawFannedDoubleFretMarker(GraphicsContext gc, int fret, double fretboardHeight, 
            double padding, double nutWidth, double fretWidth, double fretMarkerRadius,
            int numFrets, int numStrings, double stringSpacing, double fanOffsetPerString) {
        double fretPosition = fret - 0.5; // Center of the fret space
        
        // Calculate Y positions (1/3 and 2/3 of fretboard height)
        double y1 = padding + (fretboardHeight / 3);
        double y2 = padding + (fretboardHeight * 2 / 3);
        
        // Calculate X positions along the angled fret line
        // The string positions at 1/3 and 2/3 of the fretboard height
        double stringProgress1 = 1.0 / 3.0;
        double stringProgress2 = 2.0 / 3.0;
        
        double topX = calculateFannedFretX(fretPosition, 0, padding, nutWidth, fretWidth, numFrets, fanOffsetPerString, numStrings);
        double bottomX = calculateFannedFretX(fretPosition, numStrings - 1, padding, nutWidth, fretWidth, numFrets, fanOffsetPerString, numStrings);
        
        double x1 = topX + (bottomX - topX) * stringProgress1;
        double x2 = topX + (bottomX - topX) * stringProgress2;
        
        double radius = Math.max(2.0, fretMarkerRadius);
        gc.fillOval(x1 - radius, y1 - radius, radius * 2, radius * 2);
        gc.fillOval(x2 - radius, y2 - radius, radius * 2, radius * 2);
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
    
    /**
     * Helper class to track clickable regions for frequency labels.
     */
    private static class FrequencyClickRegion {
        final double x;
        final double y;
        final double width;
        final double height;
        final Frequency frequency;
        final int stringIndex;
        final int fretIndex;
        
        FrequencyClickRegion(double x, double y, double width, double height,
                Frequency frequency, int stringIndex, int fretIndex) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.frequency = frequency;
            this.stringIndex = stringIndex;
            this.fretIndex = fretIndex;
        }
        
        boolean contains(double px, double py) {
            return px >= x && px <= x + width && py >= y && py <= y + height;
        }
    }
}
