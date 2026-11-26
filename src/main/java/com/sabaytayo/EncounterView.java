package com.sabaytayo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

// Visual Novel Encounter screen: DDLC-inspired proportions adapted to Sabay Tayo's twilight bus aesthetic.
public class EncounterView extends StackPane {

    private static final double ACTIVE_BRIGHTNESS = 0.0;
    private static final double LISTENING_BRIGHTNESS = -0.15;
    private static final double NARRATION_BRIGHTNESS = -0.06;
    private static final double CLOSEUP_BG_SCALE = 1.22;
    private static final double CLOSEUP_BG_OFFSET_X_RATIO = 0.012;
    private static final double SPRITE_SEAT_OFFSET_RATIO = 0.155;
    private static final double CONDUCTOR_AISLE_OFFSET_RATIO = -0.32;
    private static final double THREE_SPRITE_MALE_OFFSET_RATIO = -0.02;
    private static final double THREE_SPRITE_FEM_OFFSET_RATIO = 0.25;
    private static final int AUTO_BASE_DELAY_MS = 1100;
    private static final int AUTO_MS_PER_CHAR = 20;
    private static final int AUTO_MIN_DELAY_MS = 1400;
    private static final int AUTO_MAX_DELAY_MS = 4200;

    private final GameApplication app;
    private final RelationshipStats stats;
    private final Momentum momentum;
    private final Encounter encounter;

    // Stage & camera layers
    private final StackPane stageLayer;
    private final StackPane cameraRig;
    private final ImageView bgView;
    private final ImageView cgCrossfadeView;
    private final ImageView cgView;
    private final Region headlightBeamOverlay;
    private final Region headlightWashOverlay;
    private final StackPane cgPlaceholderOverlay;
    private final Label cgPlaceholderTagLabel;
    private final Label cgPlaceholderFileLabel;
    private final Label cgPlaceholderDescLabel;
    private final StackPane spriteLayer;
    private final StackPane conductorSlot;
    private final StackPane maleSlot;
    private final StackPane femSlot;
    private final ImageView conductorSpriteView;
    private final ImageView maleSpriteView;
    private final ImageView femSpriteView;
    private final BusAtmosphereLayer atmosphereLayer;
    private final Region bgVignetteOverlay;
    private final Region flashOverlay;
    private final Region bgFadeOverlay;

    // Cached background, CG, and sprite images
    private final Image bgWideShotImage;
    private final Image bgCloseUpImage;
    private Image conductorImage;
    private final SpriteSet sprites = new SpriteSet();
    private final Map<String, Image> cgCache = new HashMap<>();
    private String activeCgPath = null;

    // Background & sprite lighting effects
    private final ColorAdjust bgCloseUpTone = new ColorAdjust();
    private final ColorAdjust cgTone = new ColorAdjust();
    private final ColorAdjust conductorTone = new ColorAdjust();
    private final ColorAdjust maleTone = new ColorAdjust();
    private final ColorAdjust femTone = new ColorAdjust();

    // Dialogue & HUD controls
    private final HBox topBar;
    private final StackPane dialogueWrapper;
    private final VBox textCard;
    private final HBox nameplate;
    private final Label speakerLabel;
    private final Label dialogueLabel;
    private final Label nextIndicator;
    private final Label momentumBadge;
    private final StackPane choicesOverlay;
    private final VBox choicesContainer;

    // Inline DDLC-style bottom toolbar buttons
    private final Button backBtn;
    private final Button autoBtn;
    private final Button musicBtn;

    // Modal overlay (Settings)
    private final StackPane modalOverlay;
    private boolean modalOpen = false;

    // Timelines & VN playback states
    private final Typewriter typewriter;
    private Timeline conductorAnimTimeline;
    private Timeline stageShiftTimeline;
    private Timeline femAnimTimeline;
    private Timeline maleAnimTimeline;
    private Timeline focusFadeTimeline;
    private Timeline maleIdleTimeline;
    private Timeline femIdleTimeline;
    private Timeline bgFadeTimeline;
    private Timeline busHeadlightTimeline;
    private Timeline suddenBrakeTimeline;
    private Timeline endingFadeTimeline;
    private Timeline nextPulseTimeline;
    private PauseTransition autoAdvanceTimer;
    private Runnable pendingFadeMidpoint;
    private Runnable pendingBrakeReveal;
    private boolean awaitingBlackResetReveal = false;
    private boolean initialFadePlayed = false;
    private boolean conductorOnStage = false;
    private boolean transitioningToEnding = false;

    private String pendingNextSceneId = null;

    private final List<DialogueBeat> currentBeats = new ArrayList<>();
    private final List<StageCue> currentBeatCues = new ArrayList<>();
    private final List<StepSnapshot> historyStack = new ArrayList<>();
    private int currentBeatIndex = 0;
    private boolean showingChoices = false;
    private boolean autoMode = false;

    // Warm smile rotation: starts at 2 so the first smile of the game flips to variant 1
    private int warmSmileVariant = 2;
    private String lastFemMood = null;

    // Captures a full beat/choice state so debug Back/Rewind can undo choices and scene jumps cleanly.
    private static class StepSnapshot {
        final String sceneId;
        final int[] statsSnapshot;
        final int momentumValue;
        final List<DialogueBeat> beats;
        final List<StageCue> cues;
        final int beatIndex;
        final String pendingNextSceneId;
        final boolean showingChoices;
        final int warmSmileVariant;
        final String lastFemMood;

        StepSnapshot(String sceneId, int[] statsSnapshot, int momentumValue,
                     List<DialogueBeat> beats, List<StageCue> cues,
                     int beatIndex, String pendingNextSceneId, boolean showingChoices,
                     int warmSmileVariant, String lastFemMood) {
            this.warmSmileVariant = warmSmileVariant;
            this.lastFemMood = lastFemMood;
            this.sceneId = sceneId;
            this.statsSnapshot = statsSnapshot;
            this.momentumValue = momentumValue;
            this.beats = new ArrayList<>(beats);
            this.cues = new ArrayList<>(cues);
            this.beatIndex = beatIndex;
            this.pendingNextSceneId = pendingNextSceneId;
            this.showingChoices = showingChoices;
        }
    }

    private final EventHandler<KeyEvent> keyAdvanceHandler = this::handleKeyAdvance;

    public EncounterView(GameApplication app, RelationshipStats stats, Momentum momentum, Encounter encounter) {
        this.app = app;
        this.typewriter = new Typewriter(app);
        this.stats = stats;
        this.momentum = momentum;
        this.encounter = encounter;

        getStyleClass().add("encounter-root");

        // 1. Load backgrounds and character sprite maps
        bgWideShotImage = ImageLoader.load("/assets/BACKGROUNDS/bg_bus_wide-shot.png");
        bgCloseUpImage = ImageLoader.load("/assets/BACKGROUNDS/bg_bus_close-up.png");
        Image fallbackBg = ImageLoader.load("/assets/BACKGROUNDS/bg_bus_night_empty.png");

        conductorImage = sprites.getConductor();

        // 2. Stage layer (clipped so camera pans/zooms/shakes never expose blank borders)
        stageLayer = new StackPane();
        Rectangle stageClip = new Rectangle();
        stageClip.widthProperty().bind(widthProperty());
        stageClip.heightProperty().bind(heightProperty());
        stageLayer.setClip(stageClip);

        bgCloseUpTone.setBrightness(-0.10);
        bgCloseUpTone.setContrast(0.08);
        bgCloseUpTone.setSaturation(-0.04);

        bgView = new ImageView(bgCloseUpImage != null ? bgCloseUpImage : fallbackBg);
        bgView.setPreserveRatio(true);
        bgView.setSmooth(false);
        if (bgView.getImage() == bgCloseUpImage) {
            bgView.setEffect(bgCloseUpTone);
        }

        // Subtle vignette behind the sprites so the bright headrests stay soft and characters pop forward
        bgVignetteOverlay = new Region();
        bgVignetteOverlay.setStyle(
                "-fx-background-color: radial-gradient(center 50% 45%, radius 70%, "
                        + "rgba(12, 8, 20, 0.0) 35%, rgba(10, 6, 18, 0.42) 100%);");
        bgVignetteOverlay.setMouseTransparent(true);

        cgCrossfadeView = new ImageView();
        cgCrossfadeView.setPreserveRatio(true);
        cgCrossfadeView.setSmooth(false);
        cgCrossfadeView.setVisible(false);
        cgCrossfadeView.setOpacity(0.0);

        cgView = new ImageView();
        cgView.setPreserveRatio(true);
        cgView.setSmooth(false);
        cgView.setVisible(false);
        cgView.setOpacity(0.0);

        headlightBeamOverlay = new Region();
        headlightBeamOverlay.setStyle(
                "-fx-background-color: radial-gradient(center 62% 56%, radius 46%, "
                        + "rgba(255, 242, 198, 0.70) 0%, "
                        + "rgba(255, 214, 132, 0.38) 36%, "
                        + "rgba(255, 188, 96, 0.12) 64%, "
                        + "rgba(255, 180, 90, 0.0) 88%);");
        headlightBeamOverlay.setScaleX(1.65);
        headlightBeamOverlay.setScaleY(1.35);
        headlightBeamOverlay.setOpacity(0.0);
        headlightBeamOverlay.setVisible(false);
        headlightBeamOverlay.setMouseTransparent(true);

        headlightWashOverlay = new Region();
        headlightWashOverlay.setStyle(
                "-fx-background-color: radial-gradient(center 34% 48%, radius 68%, "
                        + "rgba(255, 232, 174, 0.42) 0%, "
                        + "rgba(255, 206, 128, 0.16) 52%, "
                        + "rgba(255, 186, 98, 0.0) 85%);");
        headlightWashOverlay.setOpacity(0.0);
        headlightWashOverlay.setVisible(false);
        headlightWashOverlay.setMouseTransparent(true);

        // Missing-CG Placeholder overlay (Dev / Testing mode: hides floating sprites and shows expected CG filename)
        cgPlaceholderTagLabel = new Label("MISSING EVENT CG");
        cgPlaceholderTagLabel.getStyleClass().add("cg-placeholder-tag");

        cgPlaceholderFileLabel = new Label("");
        cgPlaceholderFileLabel.getStyleClass().add("cg-placeholder-file");
        cgPlaceholderFileLabel.setWrapText(true);

        cgPlaceholderDescLabel = new Label("");
        cgPlaceholderDescLabel.getStyleClass().add("cg-placeholder-desc");
        cgPlaceholderDescLabel.setWrapText(true);
        cgPlaceholderDescLabel.setAlignment(Pos.CENTER);

        Label cgPlaceholderHint = new Label("Floating sprites hidden to match Event CG framing  •  Toggle in Settings");
        cgPlaceholderHint.getStyleClass().add("cg-placeholder-hint");
        cgPlaceholderHint.setWrapText(true);

        VBox cgPlaceholderCard = new VBox(10,
                cgPlaceholderTagLabel,
                cgPlaceholderFileLabel,
                cgPlaceholderDescLabel,
                cgPlaceholderHint
        );
        cgPlaceholderCard.getStyleClass().add("cg-placeholder-card");
        cgPlaceholderCard.setAlignment(Pos.CENTER);
        cgPlaceholderCard.maxWidthProperty().bind(Bindings.min(widthProperty().multiply(0.56), 680));
        cgPlaceholderCard.setMaxHeight(Region.USE_PREF_SIZE);

        cgPlaceholderOverlay = new StackPane(cgPlaceholderCard);
        cgPlaceholderOverlay.getStyleClass().add("cg-placeholder-backdrop");
        cgPlaceholderOverlay.setAlignment(Pos.CENTER);
        cgPlaceholderOverlay.setMouseTransparent(true);
        cgPlaceholderOverlay.setVisible(false);

        // Aspect-fill binding so 16:9 backgrounds & CGs fill any screen without stretching
        widthProperty().addListener((obs, oldW, newW) -> {
            updateCoverFit();
            updateSpriteSlotPositions();
        });
        heightProperty().addListener((obs, oldH, newH) -> {
            updateCoverFit();
            updateResponsiveMetrics(newH.doubleValue());
        });

        // 3. Character sprites anchored near bottom behind the semi-transparent dialogue box
        conductorSpriteView = createSpriteView(conductorImage);
        conductorSpriteView.fitHeightProperty().bind(heightProperty().multiply(0.82));
        conductorSpriteView.setEffect(conductorTone);

        conductorSlot = new StackPane(conductorSpriteView);
        conductorSlot.setMaxWidth(Region.USE_PREF_SIZE);
        conductorSlot.setMaxHeight(Region.USE_PREF_SIZE);
        conductorSlot.setPickOnBounds(false);
        conductorSlot.setVisible(false);
        StackPane.setAlignment(conductorSpriteView, Pos.BOTTOM_CENTER);

        maleSpriteView = createSpriteView(sprites.getMale("neutral"));
        maleSpriteView.fitHeightProperty().bind(heightProperty().multiply(0.86));
        maleSpriteView.setEffect(maleTone);

        maleSlot = new StackPane(maleSpriteView);
        maleSlot.setMaxWidth(Region.USE_PREF_SIZE);
        maleSlot.setMaxHeight(Region.USE_PREF_SIZE);
        maleSlot.setPickOnBounds(false);
        StackPane.setAlignment(maleSpriteView, Pos.BOTTOM_CENTER);

        femSpriteView = createSpriteView(sprites.getFem("neutral"));
        femSpriteView.fitHeightProperty().bind(heightProperty().multiply(0.76));
        femSpriteView.setEffect(femTone);

        femSlot = new StackPane(femSpriteView);
        femSlot.setMaxWidth(Region.USE_PREF_SIZE);
        femSlot.setMaxHeight(Region.USE_PREF_SIZE);
        femSlot.setPickOnBounds(false);
        StackPane.setAlignment(femSpriteView, Pos.BOTTOM_CENTER);

        spriteLayer = new StackPane(conductorSlot, maleSlot, femSlot);
        spriteLayer.setPickOnBounds(false);
        StackPane.setAlignment(conductorSlot, Pos.BOTTOM_CENTER);
        StackPane.setAlignment(maleSlot, Pos.BOTTOM_CENTER);
        StackPane.setAlignment(femSlot, Pos.BOTTOM_CENTER);

        // Shared camera rig: keeps bus seats and seated sprites locked together during camera zooms/pans
        cameraRig = new StackPane(bgView, bgVignetteOverlay, spriteLayer);
        cameraRig.setPickOnBounds(false);

        // Ambient twilight rain & passing streetlight sweeps
        atmosphereLayer = new BusAtmosphereLayer();
        atmosphereLayer.setAtmosphereEnabled(app.isAtmosphereEnabled());

        // Flash overlay for dramatic impacts (e.g. S03 sudden bus brake)
        flashOverlay = new Region();
        flashOverlay.setStyle("-fx-background-color: rgba(255, 232, 210, 0.85);");
        flashOverlay.setOpacity(0.0);
        flashOverlay.setMouseTransparent(true);

        // Black fade overlay for slow cinematic transitions (starts dark for opening fade-in)
        bgFadeOverlay = new Region();
        bgFadeOverlay.setStyle("-fx-background-color: #000000;");
        bgFadeOverlay.setOpacity(1.0);
        bgFadeOverlay.setMouseTransparent(true);

        stageLayer.getChildren().addAll(
                cameraRig, cgCrossfadeView, cgView, headlightBeamOverlay, headlightWashOverlay,
                cgPlaceholderOverlay, atmosphereLayer, flashOverlay, bgFadeOverlay);

        // 4. Minimal Top HUD Bar (compact Momentum badge left, subtle window controls right)
        topBar = new HBox(12);
        topBar.setPadding(new Insets(16, 24, 0, 24));
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setMaxHeight(Region.USE_PREF_SIZE);
        topBar.setPickOnBounds(false);

        momentumBadge = new Label("MOMENTUM: 0  (1.0x)");
        momentumBadge.getStyleClass().add("hud-momentum-badge");

        Region topSpacer = new Region();
        HBox.setHgrow(topSpacer, Priority.ALWAYS);

        HBox windowControls = app.createWindowControlBar();
        topBar.getChildren().addAll(momentumBadge, topSpacer, windowControls);

        // 5. Centered Choice Stack (DDLC Screenshot 3 layout: centered in upper-middle stage)
        choicesContainer = new VBox(12);
        choicesContainer.setAlignment(Pos.CENTER);
        choicesContainer.prefWidthProperty().bind(
                Bindings.max(320.0, Bindings.min(widthProperty().multiply(0.48), heightProperty().multiply(0.86))));
        choicesContainer.maxWidthProperty().bind(choicesContainer.prefWidthProperty());
        choicesContainer.setMaxHeight(Region.USE_PREF_SIZE);

        choicesOverlay = new StackPane(choicesContainer);
        choicesOverlay.setAlignment(Pos.CENTER);
        choicesOverlay.setPickOnBounds(false);
        choicesOverlay.setVisible(false);
        choicesOverlay.setMouseTransparent(true);

        // 6. DDLC-Proportioned Dialogue Box (compact ~64% width, taller ~24% height, overlapping pill)
        speakerLabel = new Label("");
        speakerLabel.getStyleClass().add("vn-speaker-label");

        nameplate = new HBox(speakerLabel);
        nameplate.getStyleClass().add("vn-nameplate");
        nameplate.setMaxWidth(Region.USE_PREF_SIZE);
        nameplate.setMaxHeight(Region.USE_PREF_SIZE);

        dialogueLabel = new Label("Loading...");
        dialogueLabel.getStyleClass().add("vn-dialogue-text");
        dialogueLabel.setWrapText(true);
        dialogueLabel.setAlignment(Pos.TOP_LEFT);
        dialogueLabel.setMaxWidth(Double.MAX_VALUE);
        dialogueLabel.setMinHeight(Region.USE_PREF_SIZE);
        dialogueLabel.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(dialogueLabel, Priority.ALWAYS);

        // Inline DDLC-style bottom-right control row inside the dialogue box
        backBtn = createInlineControl("Back", this::stepBack);
        backBtn.setDisable(true);
        autoBtn = createInlineControl("Auto", this::toggleAutoMode);
        musicBtn = createInlineControl(app.isMusicMuted() ? "Music: OFF" : "Music", this::handleMusicToggle);
        Button settingsBtn = createInlineControl("Settings", this::openSettingsModal);

        HBox bottomControls = new HBox(18, backBtn, autoBtn, musicBtn, settingsBtn);
        bottomControls.setAlignment(Pos.CENTER_RIGHT);

        nextIndicator = new Label("▶");
        nextIndicator.getStyleClass().add("vn-next-arrow");
        nextIndicator.setVisible(false);

        HBox bottomBarRow = new HBox(16, bottomControls, nextIndicator);
        bottomBarRow.setAlignment(Pos.CENTER_RIGHT);
        bottomBarRow.setPadding(new Insets(4, 2, 0, 2));

        textCard = new VBox(8, dialogueLabel, bottomBarRow);
        textCard.getStyleClass().add("vn-dialogue-box");
        textCard.setPadding(new Insets(26, 36, 12, 36));
        textCard.minHeightProperty().bind(heightProperty().multiply(0.24));
        textCard.setPrefHeight(Region.USE_COMPUTED_SIZE);

        // Wrapper so the overlapping nameplate sits cleanly on the top-left border of textCard
        dialogueWrapper = new StackPane(textCard, nameplate);
        dialogueWrapper.maxWidthProperty().bind(
                Bindings.min(widthProperty().multiply(0.64), heightProperty().multiply(1.16)));
        dialogueWrapper.setMaxHeight(Region.USE_PREF_SIZE);
        dialogueWrapper.setPickOnBounds(false);
        StackPane.setAlignment(textCard, Pos.BOTTOM_CENTER);
        StackPane.setAlignment(nameplate, Pos.TOP_LEFT);
        StackPane.setMargin(nameplate, new Insets(-22, 0, 0, 28));

        // 7. Modal overlay for History & Settings
        modalOverlay = new StackPane();
        modalOverlay.getStyleClass().add("vn-modal-backdrop");
        modalOverlay.setVisible(false);
        modalOverlay.setManaged(false);
        modalOverlay.setOnMouseClicked(e -> {
            if (e.getTarget() == modalOverlay) {
                closeModal();
            }
        });

        // Subtle pulse animation for the ▶ advance triangle
        nextPulseTimeline = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(nextIndicator.opacityProperty(), 1.0),
                        new KeyValue(nextIndicator.translateXProperty(), 0)),
                new KeyFrame(Duration.millis(480),
                        new KeyValue(nextIndicator.opacityProperty(), 0.45),
                        new KeyValue(nextIndicator.translateXProperty(), 4, Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.millis(960),
                        new KeyValue(nextIndicator.opacityProperty(), 1.0),
                        new KeyValue(nextIndicator.translateXProperty(), 0, Interpolator.EASE_BOTH))
        );
        nextPulseTimeline.setCycleCount(Timeline.INDEFINITE);

        // Gentle, independent idle breathing/bus-ride sway in seats (replaces stiff cutouts)
        setupIdleSeatMotion();

        // Input handlers
        setOnMouseClicked(this::handleScreenClick);

        sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (oldScene != null) {
                oldScene.removeEventFilter(KeyEvent.KEY_PRESSED, keyAdvanceHandler);
                cleanupTimers();
                app.stopBusAmbience();
            }
            if (newScene != null) {
                newScene.addEventFilter(KeyEvent.KEY_PRESSED, keyAdvanceHandler);
                atmosphereLayer.start();
                app.startBusAmbience();
                startIdleSeatMotion();
                updateCoverFit();
                updateSpriteSlotPositions();
                updateResponsiveMetrics(getHeight() > 0 ? getHeight() : newScene.getHeight());
                if (!initialFadePlayed) {
                    initialFadePlayed = true;
                    playOpeningFadeIn();
                }
            }
        });

        // Assemble layers from back to front
        getChildren().addAll(stageLayer, choicesOverlay, dialogueWrapper, topBar, modalOverlay);
        StackPane.setAlignment(topBar, Pos.TOP_CENTER);
        StackPane.setAlignment(dialogueWrapper, Pos.BOTTOM_CENTER);
        StackPane.setMargin(dialogueWrapper, new Insets(0, 0, 22, 0));

        // Load initial scene
        loadScene();
    }

    private Button createInlineControl(String label, Runnable action) {
        Button btn = new Button(label);
        btn.getStyleClass().add("vn-inline-btn");
        btn.setFocusTraversable(false);
        btn.setOnMouseEntered(e -> app.playHoverSfx());
        btn.setOnAction(e -> {
            app.playClickSfx();
            action.run();
        });
        return btn;
    }

    // Keeps backgrounds and Event CGs aspect-filled ("cover") across any screen aspect ratio.
    private void updateCoverFit() {
        double w = getWidth();
        double h = getHeight();
        if (w <= 0 || h <= 0) {
            return;
        }
        applyCoverSize(bgView, w, h);
        applyCoverSize(cgCrossfadeView, w, h);
        applyCoverSize(cgView, w, h);
        syncBackgroundBaseTransform();
    }

    // Keeps bgView locked to its resting seat alignment inside cameraRig.
    private void syncBackgroundBaseTransform() {
        boolean isCloseUpBg = (bgView.getImage() == bgCloseUpImage);
        double baseScale = isCloseUpBg ? CLOSEUP_BG_SCALE : 1.02;
        double baseTx = isCloseUpBg ? (getWidth() * CLOSEUP_BG_OFFSET_X_RATIO) : 0.0;
        bgView.setScaleX(baseScale);
        bgView.setScaleY(baseScale);
        bgView.setTranslateX(baseTx);
        bgView.setTranslateY(0.0);
    }

    private void applyCoverSize(ImageView view, double w, double h) {
        Image img = view.getImage();
        if (img == null || img.getWidth() <= 0 || img.getHeight() <= 0) {
            return;
        }
        double imgRatio = img.getWidth() / img.getHeight();
        double screenRatio = w / h;
        if (screenRatio > imgRatio) {
            view.setFitWidth(w);
            view.setFitHeight(0);
        } else {
            view.setFitWidth(0);
            view.setFitHeight(h);
        }
    }

    // Scales dialogue typography, nameplate size, and margins proportionally to screen height.
    private void updateResponsiveMetrics(double h) {
        if (h <= 0) {
            return;
        }
        double dialogueFont = Math.max(20.0, Math.min(30.0, h * 0.0275));
        double speakerFont = Math.max(17.0, Math.min(24.0, h * 0.022));
        double bottomMargin = Math.max(16.0, h * 0.026);
        double choiceBottomPad = h * 0.27;

        dialogueLabel.setStyle("-fx-font-size: " + String.format("%.1f", dialogueFont) + "px;");
        speakerLabel.setStyle("-fx-font-size: " + String.format("%.1f", speakerFont) + "px;");
        StackPane.setMargin(dialogueWrapper, new Insets(0, 0, bottomMargin, 0));
        choicesOverlay.setPadding(new Insets(56, 0, choiceBottomPad, 0));
        cgPlaceholderOverlay.setPadding(new Insets(64, 24, choiceBottomPad, 24));
    }

    // Loads an Event CG if the user has added it to src/main/resources/assets/CG/ (returns null if not yet added).
    private Image loadEventCg(String cgPath) {
        if (cgPath == null) {
            return null;
        }
        if (cgCache.containsKey(cgPath)) {
            return cgCache.get(cgPath);
        }
        Image img = ImageLoader.load(cgPath);
        cgCache.put(cgPath, img);
        return img;
    }

    private ImageView createSpriteView(Image initialImage) {
        ImageView view = new ImageView();
        view.setPreserveRatio(true);
        view.setSmooth(false);
        if (initialImage != null) {
            view.setImage(initialImage);
        }
        return view;
    }

    // -------------------------------------------------------------------------
    // Scene & Beat Flow
    // -------------------------------------------------------------------------

    private void loadScene() {
        Scene current = encounter.getCurrentScene();
        if (current == null) {
            return;
        }

        // Switch BGM between warm acoustic, cold melancholy, and sudden brake tracks
        syncSceneMusic(current.getSceneId(), 0);

        typewriter.stop();
        stopAutoTimer();
        pendingNextSceneId = null;
        hideChoices();

        currentBeats.clear();
        currentBeatCues.clear();
        currentBeatIndex = 0;

        List<String> lines = current.getDialogueLines();
        for (int i = 0; i < lines.size(); i++) {
            currentBeats.add(DialogueBeat.fromLine(lines.get(i), app.getPlayer(), app.getStranger()));
            currentBeatCues.add(StageCue.forSceneBeat(current.getSceneId(), i, momentum, encounter.getS08Variant()));
        }

        updateHUD();
        displayCurrentBeat();
    }

    private void syncSceneMusic(String sceneId, int beatIndex) {
        if ("S03".equals(sceneId)) {
            if (beatIndex == 0 && pendingNextSceneId == null) {
                app.stopBackgroundMusic();
            } else {
                app.switchBackgroundMusic(GameApplication.BGM_SUDDEN_BRAKE_PATH);
            }
            return;
        }
        if ("S05B".equals(sceneId) || "S06B".equals(sceneId)
                || (("S07".equals(sceneId) || "S08".equals(sceneId)) && momentum.getValue() < 0)) {
            app.switchBackgroundMusic(GameApplication.BGM_COLD_PATH);
        } else {
            app.switchBackgroundMusic(GameApplication.BGM_WARM_PATH);
        }
    }

    private void displayCurrentBeat() {
        displayCurrentBeat(false);
    }

    private void displayCurrentBeat(boolean instantRestore) {
        if (currentBeatIndex < 0 || currentBeatIndex >= currentBeats.size()) {
            return;
        }

        stopAutoTimer();
        hideNextIndicator();

        DialogueBeat beat = currentBeats.get(currentBeatIndex);
        StageCue cue = (currentBeatIndex < currentBeatCues.size()) ? currentBeatCues.get(currentBeatIndex) : null;

        // Keep pre-brake silence until the impact flash triggers BGM_SUDDEN_BRAKE_PATH
        if (instantRestore || cue == null || !cue.isPlayBrakeSfx()) {
            syncSceneMusic(encounter.getCurrentSceneId(), currentBeatIndex);
        }

        // Update overlapping speaker nameplate (hidden on Narrator beats like DDLC Screenshot 2)
        updateNameplate(beat.getSpeaker());

        if (!instantRestore && cue != null && cue.isFadeResetTransition() && getScene() != null) {
            playBlackResetBeat(cue, beat);
            return;
        }

        // Apply per-beat CG switch (or Missing-CG placeholder), camera framing, sprite expressions, speaker focus, and animations
        if (cue != null) {
            applyStageCue(cue, beat.getSpeaker(), instantRestore);
        } else {
            applySpeakerFocusFor(beat.getSpeaker());
        }

        if (instantRestore) {
            typewriter.stop();
            dialogueLabel.setText(beat.getText() != null ? beat.getText() : "");
            onBeatTypingComplete();
        } else {
            typewriter.play(dialogueLabel, beat.getText(), this::onBeatTypingComplete);
        }
    }

    private void applySpeakerFocusFor(String speaker) {
        if ("Narrator".equals(speaker)) {
            setBothBalanced();
        } else if ("Conductor".equals(speaker)) {
            setSpeakerFocus(false, false, true);
        } else if (speaker != null && (speaker.equals(app.getPlayerName()) || "You".equals(speaker))) {
            setSpeakerFocus(true, false, false);
        } else {
            setSpeakerFocus(false, true, false);
        }
    }

    // Re-applies the active beat's visual state immediately (used when toggling Dev CG Placeholders in Settings).
    private void refreshCurrentBeatVisuals() {
        if (currentBeatIndex >= 0 && currentBeatIndex < currentBeats.size()
                && currentBeatIndex < currentBeatCues.size()) {
            applyStageCue(currentBeatCues.get(currentBeatIndex), currentBeats.get(currentBeatIndex).getSpeaker());
        }
    }

    private void updateNameplate(String speaker) {
        nameplate.getStyleClass().removeAll("vn-nameplate-player", "vn-nameplate-stranger", "vn-nameplate-conductor");

        if (speaker == null || "Narrator".equals(speaker) || speaker.isBlank()) {
            nameplate.setVisible(false);
            nameplate.setManaged(false);
            return;
        }

        speakerLabel.setText(speaker);
        nameplate.setVisible(true);
        nameplate.setManaged(true);

        if ("Conductor".equals(speaker)) {
            nameplate.getStyleClass().add("vn-nameplate-conductor");
        } else if (speaker.equals(app.getPlayerName()) || "You".equals(speaker)) {
            nameplate.getStyleClass().add("vn-nameplate-player");
        } else {
            nameplate.getStyleClass().add("vn-nameplate-stranger");
        }
    }

    private void onBeatTypingComplete() {
        if (!showingChoices) {
            showNextIndicator();
            scheduleNextAuto();
        }
    }

    private void showNextIndicator() {
        nextIndicator.setVisible(true);
        nextPulseTimeline.playFromStart();
    }

    private void hideNextIndicator() {
        nextPulseTimeline.stop();
        nextIndicator.setVisible(false);
    }

    // -------------------------------------------------------------------------
    // Choices (DDLC Centered Choice Stack with Tone Pills)
    // -------------------------------------------------------------------------

    private void renderChoices(Scene scene) {
        choicesContainer.getChildren().clear();

        // Scramble choices each time so there is no fixed order pattern
        List<DialogueChoice> shuffledChoices = new ArrayList<>(scene.getChoices());
        Collections.shuffle(shuffledChoices);

        for (DialogueChoice choice : shuffledChoices) {
            boolean unlocked = choice.isUnlocked(momentum);
            String formatted = ScriptFormatter.format(choice.getText(), app.getPlayer(), app.getStranger());
            String cleanText = stripTonePrefix(formatted);

            Button btn = new Button();
            btn.getStyleClass().add("vn-choice-button");
            btn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            btn.setWrapText(true);
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setMinHeight(Region.USE_PREF_SIZE);
            btn.setMaxHeight(Region.USE_PREF_SIZE);
            btn.setDisable(!unlocked);

            HBox content = new HBox(12);
            content.setAlignment(Pos.CENTER_LEFT);
            content.prefWidthProperty().bind(choicesContainer.prefWidthProperty().subtract(48));
            content.maxWidthProperty().bind(choicesContainer.prefWidthProperty().subtract(48));
            content.setMinHeight(Region.USE_PREF_SIZE);
            content.setMaxHeight(Region.USE_PREF_SIZE);

            Label textLbl = new Label(cleanText);
            textLbl.getStyleClass().add("vn-choice-label");
            textLbl.setWrapText(true);
            textLbl.setMinWidth(0);
            textLbl.setMinHeight(Region.USE_PREF_SIZE);
            HBox.setHgrow(textLbl, Priority.ALWAYS);
            content.getChildren().add(textLbl);

            if (!unlocked) {
                Label lockBadge = new Label("Momentum +" + choice.getMinMomentum());
                lockBadge.getStyleClass().add("lock-badge");
                lockBadge.setMinWidth(Region.USE_PREF_SIZE);
                content.getChildren().add(lockBadge);
            }

            btn.setGraphic(content);
            btn.setOnMouseEntered(e -> {
                if (unlocked) {
                    app.playHoverSfx();
                }
            });

            DialogueChoice captured = choice;
            btn.setOnAction(e -> {
                app.playClickSfx();
                handleChoiceSelected(captured);
            });

            choicesContainer.getChildren().add(btn);
        }

        choicesOverlay.setVisible(true);
        choicesOverlay.setMouseTransparent(false);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), choicesContainer);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }

    private void hideChoices() {
        choicesContainer.getChildren().clear();
        choicesOverlay.setVisible(false);
        choicesOverlay.setMouseTransparent(true);
        showingChoices = false;
    }

    private void handleChoiceSelected(DialogueChoice choice) {
        pushSnapshot();
        typewriter.stop();
        stopAutoTimer();
        hideChoices();

        choice.applyEffect(stats, momentum);
        updateHUD();

        String rawReaction = choice.getReaction(momentum);
        String reaction = ScriptFormatter.format(rawReaction, app.getPlayer(), app.getStranger());

        pendingNextSceneId = choice.getNextSceneId();
        currentBeats.clear();
        currentBeatCues.clear();
        currentBeatIndex = 0;

        if (!choice.isSpokenChoice()) {
            currentBeats.add(new DialogueBeat("Narrator", reaction));
            currentBeatCues.add(StageCue.forChoiceSpeakBeat(choice));
        } else {
            // Beat 0: Player speaks (focused on MC)
            String playerSpoken = stripOuterQuotes(stripTonePrefix(choice.getText()));
            String formattedPlayerSpoken = ScriptFormatter.format(playerSpoken, app.getPlayer(), app.getStranger());
            currentBeats.add(new DialogueBeat(app.getPlayerName(), formattedPlayerSpoken));
            currentBeatCues.add(StageCue.forChoiceSpeakBeat(choice));

            // Beat 1+: Split spoken dialogue and narrative description into separate beats
            if (reaction != null && !reaction.isBlank()) {
                String strangerName = (app.getStranger() != null && app.getStranger().getName() != null && !app.getStranger().getName().isBlank())
                        ? app.getStranger().getName()
                        : "Amara";
                List<DialogueBeat> reactionBeats = DialogueBeat.splitReaction(reaction, strangerName);
                StageCue reactionCue = StageCue.forChoiceReactionBeat(choice, reaction, momentum);
                boolean strangerHasSpoken = false;
                for (int i = 0; i < reactionBeats.size(); i++) {
                    DialogueBeat rb = reactionBeats.get(i);
                    currentBeats.add(rb);
                    StageCue beatCue = (i == 0) ? reactionCue : reactionCue.withoutEffect();
                    beatCue = StageCue.adjustReactionBeat(beatCue, rb.getSpeaker(), strangerHasSpoken);
                    currentBeatCues.add(beatCue);
                    if (!"Narrator".equals(rb.getSpeaker())) {
                        strangerHasSpoken = true;
                    }
                }
            }
        }

        displayCurrentBeat();
    }

    private String stripTonePrefix(String choiceText) {
        if (choiceText == null) {
            return "...";
        }
        int closingBracket = choiceText.indexOf(']');
        if (closingBracket != -1 && closingBracket + 1 < choiceText.length()) {
            return choiceText.substring(closingBracket + 1).trim();
        }
        return choiceText.trim();
    }

    private String stripOuterQuotes(String text) {
        if (text != null && text.length() >= 2 && text.startsWith("\"") && text.endsWith("\"")) {
            return text.substring(1, text.length() - 1).trim();
        }
        return text;
    }

    // -------------------------------------------------------------------------
    // Input Handling & Auto Mode
    // -------------------------------------------------------------------------

    private void handleScreenClick(MouseEvent e) {
        if (modalOpen || transitioningToEnding || e.getButton() != MouseButton.PRIMARY) {
            return;
        }
        if (isClickOnInteractive(e.getTarget())) {
            return;
        }
        triggerAdvanceAction();
    }

    private void handleKeyAdvance(KeyEvent e) {
        if (e.getCode() == KeyCode.ESCAPE) {
            e.consume();
            if (transitioningToEnding) {
                return;
            }
            if (modalOpen) {
                closeModal();
            } else {
                openSettingsModal();
            }
            return;
        }
        if (modalOpen || transitioningToEnding) {
            return;
        }
        if (e.getCode() == KeyCode.BACK_SPACE || e.getCode() == KeyCode.LEFT) {
            e.consume();
            stepBack();
            return;
        }
        if (e.getCode() != KeyCode.SPACE && e.getCode() != KeyCode.ENTER) {
            return;
        }
        e.consume();
        triggerAdvanceAction();
    }

    private void triggerAdvanceAction() {
        if (transitioningToEnding) {
            return;
        }
        if (awaitingBlackResetReveal) {
            awaitingBlackResetReveal = false;
            stopBgFadeAnimation(true);
            typewriter.finishNow();
            return;
        }
        if (typewriter.isTyping()) {
            typewriter.finishNow();
            return;
        }

        if (showingChoices) {
            return;
        }

        if (currentBeatIndex + 1 < currentBeats.size()) {
            pushSnapshot();
            currentBeatIndex++;
            displayCurrentBeat();
            return;
        }

        if (pendingNextSceneId != null) {
            pushSnapshot();
            String nextId = pendingNextSceneId;
            pendingNextSceneId = null;
            proceedToNextScene(nextId);
            return;
        }

        Scene current = encounter.getCurrentScene();
        if (current != null) {
            if (hasPlayerReplyChoices(current)) {
                pushSnapshot();
                stopAutoTimer();
                hideNextIndicator();
                showingChoices = true;
                renderChoices(current);
                setSpeakerFocus(true, false, false);
            } else {
                String nextId = resolveContinueNextSceneId(current);
                if (nextId != null) {
                    pushSnapshot();
                    proceedToNextScene(nextId);
                }
            }
        }
    }

    public boolean canStepBack() {
        return !historyStack.isEmpty();
    }

    // Debug rewind: pops the last snapshot and restores stats, momentum, scene, beat, or choice menu.
    public void stepBack() {
        if (historyStack.isEmpty() || modalOpen || transitioningToEnding) {
            return;
        }
        StepSnapshot snap = historyStack.remove(historyStack.size() - 1);
        backBtn.setDisable(historyStack.isEmpty());

        typewriter.stop();
        stopAutoTimer();
        stopBgFadeAnimation(false);
        stopBusHeadlightArrival();
        stopSuddenBrakeSequence(false);
        hideChoices();
        hideNextIndicator();

        stats.restore(snap.statsSnapshot);
        momentum.setValue(snap.momentumValue);

        try {
            if ("S08".equals(snap.sceneId)) {
                encounter.prepareFinalScene(stats);
            }
            encounter.advanceTo(snap.sceneId);
        } catch (SceneNotFoundException e) {
            System.err.println("Error rewinding scene: " + e.getMessage());
            return;
        }

        currentBeats.clear();
        currentBeats.addAll(snap.beats);
        currentBeatCues.clear();
        currentBeatCues.addAll(snap.cues);
        currentBeatIndex = snap.beatIndex;
        pendingNextSceneId = snap.pendingNextSceneId;
        warmSmileVariant = snap.warmSmileVariant;
        lastFemMood = snap.lastFemMood;

        syncSceneMusic(snap.sceneId, snap.beatIndex);
        updateHUD();

        if (snap.showingChoices) {
            displayCurrentBeat(true);
            Scene current = encounter.getCurrentScene();
            if (current != null) {
                stopAutoTimer();
                hideNextIndicator();
                showingChoices = true;
                renderChoices(current);
                setSpeakerFocus(true, false, false);
            }
        } else {
            displayCurrentBeat(true);
        }
    }

    // Restores EncounterView cleanly when navigating back from EndingView.
    public void restoreFromEnding() {
        if (endingFadeTimeline != null) {
            endingFadeTimeline.stop();
            endingFadeTimeline = null;
        }
        transitioningToEnding = false;
        dialogueWrapper.setOpacity(1.0);
        dialogueWrapper.setTranslateY(0.0);
        topBar.setOpacity(1.0);
        bgFadeOverlay.setOpacity(0.0);
        stepBack();
    }

    private void pushSnapshot() {
        historyStack.add(new StepSnapshot(
                encounter.getCurrentSceneId(),
                stats.snapshot(),
                momentum.getValue(),
                currentBeats,
                currentBeatCues,
                currentBeatIndex,
                pendingNextSceneId,
                showingChoices,
                warmSmileVariant,
                lastFemMood
        ));
        backBtn.setDisable(false);
    }

    private void toggleAutoMode() {
        autoMode = !autoMode;
        updateInlineButtonState(autoBtn, autoMode);
        if (autoMode && !typewriter.isTyping() && !showingChoices && !modalOpen) {
            scheduleNextAuto();
        } else if (!autoMode) {
            stopAutoTimer();
        }
    }

    private void handleMusicToggle() {
        app.toggleMusicMute();
        refreshMusicButtonLabel();
    }

    private void refreshMusicButtonLabel() {
        musicBtn.setText(app.isMusicMuted() ? "Music: OFF" : "Music");
        updateInlineButtonState(musicBtn, !app.isMusicMuted());
    }

    private void updateInlineButtonState(Button btn, boolean active) {
        if (active) {
            if (!btn.getStyleClass().contains("vn-inline-btn-active")) {
                btn.getStyleClass().add("vn-inline-btn-active");
            }
        } else {
            btn.getStyleClass().remove("vn-inline-btn-active");
        }
    }

    private void scheduleNextAuto() {
        stopAutoTimer();
        if (showingChoices || modalOpen || !autoMode) {
            return;
        }
        int len = dialogueLabel.getText() != null ? dialogueLabel.getText().length() : 0;
        int delay = Math.max(AUTO_MIN_DELAY_MS, Math.min(AUTO_MAX_DELAY_MS, AUTO_BASE_DELAY_MS + len * AUTO_MS_PER_CHAR));
        autoAdvanceTimer = new PauseTransition(Duration.millis(delay));
        autoAdvanceTimer.setOnFinished(e -> triggerAdvanceAction());
        autoAdvanceTimer.play();
    }

    private void stopAutoTimer() {
        if (autoAdvanceTimer != null) {
            autoAdvanceTimer.stop();
            autoAdvanceTimer = null;
        }
    }

    private boolean isClickOnInteractive(EventTarget target) {
        if (!(target instanceof Node)) {
            return false;
        }
        Node current = (Node) target;
        while (current != null && current != this) {
            if (current instanceof ButtonBase || current instanceof Slider || current instanceof CheckBox) {
                return true;
            }
            current = current.getParent();
        }
        return false;
    }

    private String resolveContinueNextSceneId(Scene scene) {
        if (scene.getDefaultNextSceneId() != null) {
            return scene.getDefaultNextSceneId();
        }
        for (DialogueChoice choice : scene.getChoices()) {
            if (!choice.isSpokenChoice()) {
                return choice.getNextSceneId();
            }
        }
        return null;
    }

    private boolean hasPlayerReplyChoices(Scene scene) {
        for (DialogueChoice choice : scene.getChoices()) {
            if (choice.isSpokenChoice()) {
                return true;
            }
        }
        return false;
    }

    private void proceedToNextScene(String nextId) {
        typewriter.stop();
        stopAutoTimer();
        pendingNextSceneId = null;

        if ("ENDING".equals(nextId)) {
            transitionToEndingCinematic();
            return;
        }

        try {
            if ("S04_DONE".equals(nextId) || "S05_DONE".equals(nextId)) {
                encounter.advanceTo(encounter.getMomentumBranch(momentum));
            } else {
                if ("S08".equals(nextId)) {
                    encounter.prepareFinalScene(stats);
                }
                encounter.advanceTo(nextId);
            }
        } catch (SceneNotFoundException e) {
            System.err.println("Error advancing scene: " + e.getMessage());
            cleanupTimers();
            app.showEnding();
            return;
        }

        loadScene();
    }

    private void updateHUD() {
        int m = momentum.getValue();
        String mSign = m > 0 ? "+" + m : String.valueOf(m);
        momentumBadge.setText("MOMENTUM: " + mSign + "  (" + momentum.getMultiplier() + "x)");

        momentumBadge.getStyleClass().removeAll("hud-momentum-warm", "hud-momentum-cold");
        if (m >= 2) {
            momentumBadge.getStyleClass().add("hud-momentum-warm");
        } else if (m <= -2) {
            momentumBadge.getStyleClass().add("hud-momentum-cold");
        }
    }

    // -------------------------------------------------------------------------
    // Settings Modal (DDLC Inline Toolbar)
    // -------------------------------------------------------------------------

    private void openSettingsModal() {
        stopAutoTimer();
        VBox modalCard = new VBox(18);
        modalCard.getStyleClass().add("vn-modal-card");
        modalCard.maxWidthProperty().bind(widthProperty().multiply(0.44));
        modalCard.setMaxHeight(Region.USE_PREF_SIZE);
        modalCard.setPadding(new Insets(28, 34, 26, 34));

        Label title = new Label("Settings & Ride Menu");
        title.getStyleClass().add("vn-modal-title");

        // Music Volume
        Label bgmLbl = new Label("Music Volume");
        bgmLbl.getStyleClass().add("field-label");
        Slider bgmSlider = new Slider(0.0, 1.0, app.getMusicVolume());
        bgmSlider.getStyleClass().add("volume-slider");
        bgmSlider.valueProperty().addListener((obs, o, n) -> {
            app.setMusicVolume(n.doubleValue());
            refreshMusicButtonLabel();
        });

        // SFX Toggle + Volume
        CheckBox sfxToggle = new CheckBox("Enable UI & Bus Sound Effects");
        sfxToggle.setSelected(!app.isSfxMuted());
        sfxToggle.getStyleClass().add("vn-checkbox");
        sfxToggle.selectedProperty().addListener((obs, o, n) -> app.setSfxMuted(!n));

        // Text Speed
        Label speedLbl = new Label("Text Speed (Faster  ←  →  Slower)");
        speedLbl.getStyleClass().add("field-label");
        Slider speedSlider = new Slider(6, 40, app.getTypingDelayMs());
        speedSlider.getStyleClass().add("volume-slider");
        speedSlider.valueProperty().addListener((obs, o, n) -> app.setTypingDelayMs(n.intValue()));

        // Ambient Rain & Streetlights
        CheckBox atmToggle = new CheckBox("Twilight Rain & Passing Streetlight Effects");
        atmToggle.setSelected(app.isAtmosphereEnabled());
        atmToggle.getStyleClass().add("vn-checkbox");
        atmToggle.selectedProperty().addListener((obs, o, n) -> {
            app.setAtmosphereEnabled(n);
            atmosphereLayer.setAtmosphereEnabled(n);
        });

        // Missing-CG Placeholder Overlay (Dev / Testing Mode)
        CheckBox cgPlaceholderToggle = new CheckBox("Show Missing CG Placeholders (Dev)");
        cgPlaceholderToggle.setSelected(app.isCgPlaceholderEnabled());
        cgPlaceholderToggle.getStyleClass().add("vn-checkbox");
        cgPlaceholderToggle.selectedProperty().addListener((obs, o, n) -> {
            app.setCgPlaceholderEnabled(n);
            refreshCurrentBeatVisuals();
        });

        // Quick navigation actions
        Button restartBtn = new Button("Restart Ride");
        restartBtn.getStyleClass().add("menu-button");
        restartBtn.setOnAction(e -> {
            app.playClickSfx();
            closeModal();
            cleanupTimers();
            app.showStart();
        });

        Button skipEndingBtn = new Button("Skip to Ending");
        skipEndingBtn.getStyleClass().add("menu-button");
        skipEndingBtn.setOnAction(e -> {
            app.playClickSfx();
            pushSnapshot();
            closeModal();
            cleanupTimers();
            app.showEnding();
        });

        Button resumeBtn = new Button("Resume");
        resumeBtn.getStyleClass().addAll("menu-button", "commute-menu-btn-active");
        resumeBtn.setOnAction(e -> {
            app.playClickSfx();
            closeModal();
        });

        HBox actions = new HBox(12, restartBtn, skipEndingBtn, resumeBtn);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.setPadding(new Insets(8, 0, 0, 0));

        modalCard.getChildren().addAll(
                title,
                new VBox(6, bgmLbl, bgmSlider),
                sfxToggle,
                new VBox(6, speedLbl, speedSlider),
                atmToggle,
                cgPlaceholderToggle,
                actions
        );
        showModal(modalCard);
    }

    private void showModal(Node content) {
        modalOverlay.getChildren().setAll(content);
        StackPane.setAlignment(content, Pos.CENTER);
        modalOverlay.setVisible(true);
        modalOverlay.setManaged(true);
        modalOpen = true;
    }

    private void closeModal() {
        modalOverlay.setVisible(false);
        modalOverlay.setManaged(false);
        modalOverlay.getChildren().clear();
        modalOpen = false;
        if (autoMode && !typewriter.isTyping() && !showingChoices) {
            scheduleNextAuto();
        }
    }

    private void transitionToEndingCinematic() {
        if (transitioningToEnding) {
            return;
        }
        transitioningToEnding = true;
        typewriter.stop();
        stopAutoTimer();
        stopBgFadeAnimation(false);
        stopBusHeadlightArrival();
        stopSuddenBrakeSequence(true);
        hideChoices();
        hideNextIndicator();

        if (getScene() == null) {
            cleanupTimers();
            app.showEnding();
            return;
        }

        endingFadeTimeline = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(dialogueWrapper.opacityProperty(), dialogueWrapper.getOpacity()),
                        new KeyValue(dialogueWrapper.translateYProperty(), dialogueWrapper.getTranslateY()),
                        new KeyValue(topBar.opacityProperty(), topBar.getOpacity()),
                        new KeyValue(bgFadeOverlay.opacityProperty(), bgFadeOverlay.getOpacity())),
                new KeyFrame(Duration.millis(420),
                        new KeyValue(dialogueWrapper.opacityProperty(), 0.0, Interpolator.EASE_OUT),
                        new KeyValue(dialogueWrapper.translateYProperty(), 14.0, Interpolator.EASE_OUT),
                        new KeyValue(topBar.opacityProperty(), 0.0, Interpolator.EASE_OUT)),
                new KeyFrame(Duration.millis(760),
                        new KeyValue(bgFadeOverlay.opacityProperty(), 1.0, Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.millis(920),
                        new KeyValue(bgFadeOverlay.opacityProperty(), 1.0))
        );
        endingFadeTimeline.setOnFinished(e -> {
            endingFadeTimeline = null;
            cleanupTimers();
            app.showEnding();
        });
        endingFadeTimeline.play();
    }

    private void cleanupTimers() {
        typewriter.stop();
        stopAutoTimer();
        stopBgFadeAnimation(false);
        stopBusHeadlightArrival();
        stopSuddenBrakeSequence(false);
        if (endingFadeTimeline != null) {
            endingFadeTimeline.stop();
            endingFadeTimeline = null;
        }
        stopIdleSeatMotion();
        if (focusFadeTimeline != null) {
            focusFadeTimeline.stop();
            focusFadeTimeline = null;
        }
        if (stageShiftTimeline != null) {
            stageShiftTimeline.stop();
            stageShiftTimeline = null;
        }
        hideNextIndicator();
        resetSpriteTransforms();
        conductorSpriteView.setTranslateX(0);
        conductorSpriteView.setTranslateY(0);
        maleSpriteView.setTranslateX(0);
        maleSpriteView.setTranslateY(0);
        femSpriteView.setTranslateX(0);
        femSpriteView.setTranslateY(0);
        atmosphereLayer.stop();
    }

    // -------------------------------------------------------------------------
    // Stage Choreography: Event CG Switches, Camera Angles & Sprite Effects
    // -------------------------------------------------------------------------

    private static final int FADE_IN_OPENING_MS = 1100;
    private static final int FADE_TO_BLACK_MS = 620;
    private static final int FADE_HOLD_BLACK_MS = 200;
    private static final int FADE_FROM_BLACK_MS = 820;

    private void applyStageCue(StageCue cue, String speaker) {
        applyStageCue(cue, speaker, false);
    }

    private void applyStageCue(StageCue cue, String speaker, boolean instantRestore) {
        resetSpriteTransforms();

        // Commit any interrupted fade, headlight sweep, or pending brake reveal before evaluating the next beat
        stopBgFadeAnimation(!instantRestore);
        stopBusHeadlightArrival();
        stopSuddenBrakeSequence(!instantRestore);

        // 1. Check Event CG first so dramatic brake beats can lead with horn + brake SFX before revealing the CG
        Image cgImage = loadEventCg(cue.getCgPath());

        if (!instantRestore && cue.isPlayBrakeSfx() && getScene() != null) {
            playSuddenBrakeSequence(cue, speaker, cgImage);
            return;
        }

        if (!instantRestore) {
            if (cue.isPlayBrakeSfx()) {
                app.playBusHornSfx();
                app.playBrakeSfx();
                playImpactFlash();
            }
            if (cue.isPlayHeadThudSfx()) {
                app.playHeadThudSfx();
                app.playHeadThudSecondarySfx();
            }
            if (cue.isPlayPhoneNotificationSfx()) {
                app.playPhoneNotificationSfx();
            }
        }

        // 2. Check if this beat has a full-screen Event CG image available in assets/CG/
        if (cgImage != null) {
            String previousCgPath = activeCgPath;
            Image previousCgImage = cgView.isVisible() ? cgView.getImage() : null;
            boolean cgChanged = !cue.getCgPath().equals(activeCgPath);
            if (!instantRestore && cgChanged && getScene() != null && !cue.isPlayBrakeSfx()
                    && !isSameShotCgVariant(activeCgPath, cue.getCgPath())) {
                fadeThroughBlack(() -> {
                    showEventCgImmediate(cue.getCgPath(), cgImage);
                    applyBeatStageVisuals(cue, speaker, true);
                    if (cue.getEffectType() == StageCue.EffectType.BUS_HEADLIGHT_ARRIVAL) {
                        playBusHeadlightArrival(null);
                    }
                });
            } else {
                showEventCgImmediate(cue.getCgPath(), cgImage);
                applyBeatStageVisuals(cue, speaker, true);
                if (!instantRestore && cue.getEffectType() == StageCue.EffectType.BUS_HEADLIGHT_ARRIVAL) {
                    Image crossfadeFrom = isSameShotCgVariant(previousCgPath, cue.getCgPath()) ? previousCgImage : null;
                    playBusHeadlightArrival(crossfadeFrom);
                }
            }
            return;
        }

        // 3. Missing Event CG with Dev Placeholder toggle ON: hide floating sprites and show filename/shot card
        boolean showPlaceholder = cue.getCgPath() != null && app.isCgPlaceholderEnabled();
        Image targetBg = cue.isUseWideBackground() ? bgWideShotImage : bgCloseUpImage;
        boolean wasInCg = (activeCgPath != null);
        boolean wasPlaceholder = cgPlaceholderOverlay.isVisible();
        boolean bgChanged = targetBg != null && bgView.getImage() != targetBg;

        if (showPlaceholder) {
            boolean placeholderChanged = !wasPlaceholder || !cue.getCgPath().equals(activeCgPath);
            if (!instantRestore && (wasInCg || bgChanged || placeholderChanged) && getScene() != null && !cue.isPlayBrakeSfx()) {
                fadeThroughBlack(() -> {
                    if (targetBg != null && bgView.getImage() != targetBg) {
                        bgView.setImage(targetBg);
                        updateCoverFit();
                    }
                    showMissingCgPlaceholderImmediate(cue);
                    applyBeatStageVisuals(cue, speaker, false);
                });
            } else {
                if (bgChanged) {
                    bgView.setImage(targetBg);
                    updateCoverFit();
                }
                showMissingCgPlaceholderImmediate(cue);
                applyBeatStageVisuals(cue, speaker, false);
            }
            return;
        }

        // 4. Normal Sprite Stage (or Missing CG with Dev Placeholder OFF)
        if (!instantRestore && (wasInCg || wasPlaceholder || bgChanged) && getScene() != null) {
            fadeThroughBlack(() -> applySpriteStageImmediate(cue, speaker, targetBg));
        } else {
            applySpriteStageImmediate(cue, speaker, targetBg);
        }
    }

    private void applySpriteStageImmediate(StageCue cue, String speaker, Image targetBg) {
        hideEventCgImmediate();
        if (targetBg != null && bgView.getImage() != targetBg) {
            bgView.setImage(targetBg);
            updateCoverFit();
        }
        applyBeatStageVisuals(cue, speaker, false);
    }

    private void applyBeatStageVisuals(StageCue cue, String speaker, boolean inCgMode) {
        // 3. Sprite visibility, conductor cameo enter/exit, & mood swaps (null mood preserves current expression)
        maleSlot.setVisible(cue.isShowMale());
        femSlot.setVisible(cue.isShowFem());
        applyConductorPresence(cue.isShowConductor(), getScene() != null && !inCgMode);

        if (cue.getMaleMood() != null) {
            Image mImg = sprites.getMale(cue.getMaleMood());
            if (mImg != null) {
                maleSpriteView.setImage(mImg);
            }
        }
        if (cue.getFemMood() != null) {
            String femKey = cue.getFemMood();
            if ("warm_smile".equals(femKey)) {
                // Alternate between warm_smile_1 and warm_smile_2 each time she newly smiles
                if (!"warm_smile".equals(lastFemMood)) {
                    warmSmileVariant = (warmSmileVariant == 1) ? 2 : 1;
                }
                femKey = "warm_smile_" + warmSmileVariant;
            }
            lastFemMood = cue.getFemMood();
            Image fImg = sprites.getFem(femKey);
            if (fImg != null) {
                femSpriteView.setImage(fImg);
            }
        }

        // Apply speaker focus dimming inside the fade midpoint so sprites never flash stale lighting
        applySpeakerFocusFor(speaker);

        // 4. Camera angle framing (smooth zoom/pan on bgView or cgView)
        applyCameraAngle(cue.getCameraAngle(), inCgMode);

        // 5. Beat-specific motion effect
        playEffect(cue.getEffectType());
    }

    private void updateSpriteSlotPositions() {
        double w = getWidth();
        if (w <= 0) {
            return;
        }
        conductorSlot.setTranslateX(w * CONDUCTOR_AISLE_OFFSET_RATIO);
        if (stageShiftTimeline == null) {
            maleSlot.setTranslateX(w * (conductorOnStage ? THREE_SPRITE_MALE_OFFSET_RATIO : -SPRITE_SEAT_OFFSET_RATIO));
            femSlot.setTranslateX(w * (conductorOnStage ? THREE_SPRITE_FEM_OFFSET_RATIO : SPRITE_SEAT_OFFSET_RATIO));
        }
    }

    // Slides the conductor sprite in/out of the left aisle and gently shifts seated sprites to make room.
    private void applyConductorPresence(boolean showConductor, boolean animate) {
        if (showConductor == conductorOnStage) {
            if (showConductor) {
                conductorSlot.setVisible(true);
            }
            return;
        }

        conductorOnStage = showConductor;
        if (conductorAnimTimeline != null) {
            conductorAnimTimeline.stop();
            conductorAnimTimeline = null;
        }
        if (stageShiftTimeline != null) {
            stageShiftTimeline.stop();
            stageShiftTimeline = null;
        }

        double w = getWidth();
        double targetMaleX = w * (showConductor ? THREE_SPRITE_MALE_OFFSET_RATIO : -SPRITE_SEAT_OFFSET_RATIO);
        double targetFemX = w * (showConductor ? THREE_SPRITE_FEM_OFFSET_RATIO : SPRITE_SEAT_OFFSET_RATIO);
        conductorSlot.setTranslateX(w * CONDUCTOR_AISLE_OFFSET_RATIO);

        if (showConductor) {
            conductorSlot.setVisible(true);
            if (animate && w > 0) {
                conductorSpriteView.setTranslateX(-w * 0.25);
                conductorSpriteView.setOpacity(0.0);
                conductorAnimTimeline = new Timeline(
                        new KeyFrame(Duration.millis(380),
                                new KeyValue(conductorSpriteView.translateXProperty(), 0.0, Interpolator.EASE_OUT),
                                new KeyValue(conductorSpriteView.opacityProperty(), 1.0, Interpolator.EASE_OUT))
                );
                conductorAnimTimeline.setOnFinished(e -> conductorAnimTimeline = null);
                conductorAnimTimeline.play();
            } else {
                conductorSpriteView.setTranslateX(0.0);
                conductorSpriteView.setOpacity(1.0);
            }
        } else {
            if (animate && w > 0 && conductorSlot.isVisible()) {
                conductorAnimTimeline = new Timeline(
                        new KeyFrame(Duration.millis(320),
                                new KeyValue(conductorSpriteView.translateXProperty(), -w * 0.25, Interpolator.EASE_IN),
                                new KeyValue(conductorSpriteView.opacityProperty(), 0.0, Interpolator.EASE_IN))
                );
                conductorAnimTimeline.setOnFinished(e -> {
                    conductorAnimTimeline = null;
                    conductorSlot.setVisible(false);
                    conductorSpriteView.setTranslateX(0.0);
                    conductorSpriteView.setOpacity(1.0);
                });
                conductorAnimTimeline.play();
            } else {
                conductorSlot.setVisible(false);
                conductorSpriteView.setTranslateX(0.0);
                conductorSpriteView.setOpacity(1.0);
            }
        }

        if (animate && w > 0) {
            stageShiftTimeline = new Timeline(
                    new KeyFrame(Duration.millis(340),
                            new KeyValue(maleSlot.translateXProperty(), targetMaleX, Interpolator.EASE_BOTH),
                            new KeyValue(femSlot.translateXProperty(), targetFemX, Interpolator.EASE_BOTH))
            );
            stageShiftTimeline.setOnFinished(e -> stageShiftTimeline = null);
            stageShiftTimeline.play();
        } else {
            maleSlot.setTranslateX(targetMaleX);
            femSlot.setTranslateX(targetFemX);
        }
    }

    private void playOpeningFadeIn() {
        stopBgFadeAnimation(false);
        bgFadeOverlay.setOpacity(1.0);
        bgFadeTimeline = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(bgFadeOverlay.opacityProperty(), 1.0)),
                new KeyFrame(Duration.millis(FADE_IN_OPENING_MS),
                        new KeyValue(bgFadeOverlay.opacityProperty(), 0.0, Interpolator.EASE_OUT))
        );
        bgFadeTimeline.setOnFinished(e -> bgFadeTimeline = null);
        bgFadeTimeline.play();
    }

    private void fadeThroughBlack(Runnable onBlackMidpoint) {
        stopBgFadeAnimation(false);
        pendingFadeMidpoint = onBlackMidpoint;

        bgFadeTimeline = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(bgFadeOverlay.opacityProperty(), bgFadeOverlay.getOpacity())),
                new KeyFrame(Duration.millis(FADE_TO_BLACK_MS), e -> {
                    if (pendingFadeMidpoint != null) {
                        Runnable action = pendingFadeMidpoint;
                        pendingFadeMidpoint = null;
                        action.run();
                    }
                }, new KeyValue(bgFadeOverlay.opacityProperty(), 1.0, Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.millis(FADE_TO_BLACK_MS + FADE_HOLD_BLACK_MS),
                        new KeyValue(bgFadeOverlay.opacityProperty(), 1.0)),
                new KeyFrame(Duration.millis(FADE_TO_BLACK_MS + FADE_HOLD_BLACK_MS + FADE_FROM_BLACK_MS),
                        new KeyValue(bgFadeOverlay.opacityProperty(), 0.0, Interpolator.EASE_BOTH))
        );
        bgFadeTimeline.setOnFinished(e -> bgFadeTimeline = null);
        bgFadeTimeline.play();
    }

    // Fades to black, resets sprites behind the curtain, types the narrator line, then fades out to reveal neutral.
    private void playBlackResetBeat(StageCue cue, DialogueBeat beat) {
        resetSpriteTransforms();
        stopBgFadeAnimation(false);
        stopBusHeadlightArrival();
        stopSuddenBrakeSequence(false);
        typewriter.stop();

        dialogueLabel.setText("");
        awaitingBlackResetReveal = true;
        Image targetBg = cue.isUseWideBackground() ? bgWideShotImage : bgCloseUpImage;

        pendingFadeMidpoint = () -> {
            awaitingBlackResetReveal = false;
            bgFadeTimeline = null;
            if (cue.isPlayPhoneNotificationSfx()) {
                app.playPhoneNotificationSfx();
            }
            applySpriteStageImmediate(cue, beat.getSpeaker(), targetBg);
            typewriter.play(dialogueLabel, beat.getText(), () -> {
                fadeOutFromBlack();
                onBeatTypingComplete();
            });
        };

        bgFadeTimeline = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(bgFadeOverlay.opacityProperty(), bgFadeOverlay.getOpacity())),
                new KeyFrame(Duration.millis(FADE_TO_BLACK_MS), e -> {
                    if (pendingFadeMidpoint != null) {
                        Runnable action = pendingFadeMidpoint;
                        pendingFadeMidpoint = null;
                        action.run();
                    }
                }, new KeyValue(bgFadeOverlay.opacityProperty(), 1.0, Interpolator.EASE_BOTH))
        );
        bgFadeTimeline.play();
    }

    private void fadeOutFromBlack() {
        double currentOpacity = bgFadeOverlay.getOpacity();
        if (currentOpacity <= 0.01) {
            bgFadeOverlay.setOpacity(0.0);
            return;
        }
        if (bgFadeTimeline != null) {
            bgFadeTimeline.stop();
            bgFadeTimeline = null;
        }
        bgFadeTimeline = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(bgFadeOverlay.opacityProperty(), currentOpacity)),
                new KeyFrame(Duration.millis(180),
                        new KeyValue(bgFadeOverlay.opacityProperty(), currentOpacity)),
                new KeyFrame(Duration.millis(180 + FADE_FROM_BLACK_MS),
                        new KeyValue(bgFadeOverlay.opacityProperty(), 0.0, Interpolator.EASE_BOTH))
        );
        bgFadeTimeline.setOnFinished(e -> bgFadeTimeline = null);
        bgFadeTimeline.play();
    }

    private void stopBgFadeAnimation(boolean commitPending) {
        awaitingBlackResetReveal = false;
        if (bgFadeTimeline != null) {
            bgFadeTimeline.stop();
            bgFadeTimeline = null;
        }
        if (commitPending && pendingFadeMidpoint != null) {
            Runnable action = pendingFadeMidpoint;
            pendingFadeMidpoint = null;
            action.run();
        } else {
            pendingFadeMidpoint = null;
        }
        bgFadeOverlay.setOpacity(0.0);
    }

    private boolean isSameShotCgVariant(String prevCgPath, String nextCgPath) {
        return (StageCue.CG_BUS_STOP_WAIT.equals(prevCgPath) && StageCue.CG_BUS_STOP_WAIT1.equals(nextCgPath))
                || (StageCue.CG_BUS_STOP_WAIT1.equals(prevCgPath) && StageCue.CG_BUS_STOP_WAIT.equals(nextCgPath));
    }

    private void showEventCgImmediate(String cgPath, Image cgImage) {
        activeCgPath = cgPath;
        cgPlaceholderOverlay.setVisible(false);
        cgView.setImage(cgImage);
        updateCoverFit();
        cgView.setOpacity(1.0);
        cgView.setVisible(true);
        spriteLayer.setVisible(false);
    }

    private void showMissingCgPlaceholderImmediate(StageCue cue) {
        activeCgPath = cue.getCgPath();
        cgView.setOpacity(0.0);
        cgView.setVisible(false);
        spriteLayer.setVisible(false);

        String sceneId = encounter.getCurrentSceneId();
        String beatLabel = (pendingNextSceneId != null) ? "CHOICE REACTION" : ("BEAT " + (currentBeatIndex + 1));
        cgPlaceholderTagLabel.setText("MISSING EVENT CG  •  " + sceneId + "  •  " + beatLabel);
        cgPlaceholderFileLabel.setText("src/main/resources" + cue.getCgPath());
        cgPlaceholderDescLabel.setText(StageCue.getCgDescription(cue.getCgPath()));
        cgPlaceholderOverlay.setVisible(true);
    }

    private void hideEventCgImmediate() {
        stopBusHeadlightArrival();
        activeCgPath = null;
        cgView.setOpacity(0.0);
        cgView.setVisible(false);
        cgPlaceholderOverlay.setVisible(false);
        spriteLayer.setVisible(true);
    }

    // Sweeps warm arriving bus headlights from the street onto MC while crossfading wait0 -> wait1.
    private void playBusHeadlightArrival(Image previousCgImage) {
        stopBusHeadlightArrival();

        double w = getWidth() > 0 ? getWidth() : 1280.0;
        boolean hasCrossfade = (previousCgImage != null);
        if (hasCrossfade) {
            cgCrossfadeView.setImage(previousCgImage);
            updateCoverFit();
            cgCrossfadeView.setScaleX(1.0);
            cgCrossfadeView.setScaleY(1.0);
            cgCrossfadeView.setOpacity(1.0);
            cgCrossfadeView.setVisible(true);
            cgView.setOpacity(0.0);
        } else {
            cgView.setOpacity(1.0);
        }

        cgTone.setBrightness(0.0);
        cgView.setEffect(cgTone);
        cgView.setScaleX(1.0);
        cgView.setScaleY(1.0);

        headlightBeamOverlay.setTranslateX(w * 0.36);
        headlightBeamOverlay.setOpacity(0.0);
        headlightBeamOverlay.setVisible(true);

        headlightWashOverlay.setOpacity(0.0);
        headlightWashOverlay.setVisible(true);

        busHeadlightTimeline = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(headlightBeamOverlay.translateXProperty(), w * 0.36),
                        new KeyValue(headlightBeamOverlay.opacityProperty(), 0.0),
                        new KeyValue(headlightWashOverlay.opacityProperty(), 0.0),
                        new KeyValue(cgView.opacityProperty(), hasCrossfade ? 0.0 : 1.0),
                        new KeyValue(cgView.scaleXProperty(), 1.0),
                        new KeyValue(cgView.scaleYProperty(), 1.0),
                        new KeyValue(cgCrossfadeView.scaleXProperty(), 1.0),
                        new KeyValue(cgCrossfadeView.scaleYProperty(), 1.0),
                        new KeyValue(cgTone.brightnessProperty(), 0.0)),
                new KeyFrame(Duration.millis(220),
                        new KeyValue(headlightBeamOverlay.translateXProperty(), w * 0.06, Interpolator.EASE_OUT),
                        new KeyValue(headlightBeamOverlay.opacityProperty(), 0.92, Interpolator.EASE_OUT),
                        new KeyValue(headlightWashOverlay.opacityProperty(), 0.42, Interpolator.EASE_OUT),
                        new KeyValue(cgView.opacityProperty(), hasCrossfade ? 0.45 : 1.0, Interpolator.EASE_BOTH),
                        new KeyValue(cgTone.brightnessProperty(), 0.14, Interpolator.EASE_OUT)),
                new KeyFrame(Duration.millis(430),
                        new KeyValue(headlightBeamOverlay.translateXProperty(), -w * 0.10, Interpolator.EASE_BOTH),
                        new KeyValue(headlightBeamOverlay.opacityProperty(), 0.84, Interpolator.EASE_BOTH),
                        new KeyValue(headlightWashOverlay.opacityProperty(), 0.56, Interpolator.EASE_BOTH),
                        new KeyValue(cgView.opacityProperty(), 1.0, Interpolator.EASE_OUT),
                        new KeyValue(cgView.scaleXProperty(), 1.016, Interpolator.EASE_OUT),
                        new KeyValue(cgView.scaleYProperty(), 1.016, Interpolator.EASE_OUT),
                        new KeyValue(cgCrossfadeView.scaleXProperty(), 1.016, Interpolator.EASE_OUT),
                        new KeyValue(cgCrossfadeView.scaleYProperty(), 1.016, Interpolator.EASE_OUT),
                        new KeyValue(cgTone.brightnessProperty(), 0.18, Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.millis(940),
                        new KeyValue(headlightBeamOverlay.translateXProperty(), -w * 0.24, Interpolator.EASE_OUT),
                        new KeyValue(headlightBeamOverlay.opacityProperty(), 0.0, Interpolator.EASE_OUT),
                        new KeyValue(headlightWashOverlay.opacityProperty(), 0.0, Interpolator.EASE_OUT),
                        new KeyValue(cgView.opacityProperty(), 1.0),
                        new KeyValue(cgView.scaleXProperty(), 1.022, Interpolator.EASE_OUT),
                        new KeyValue(cgView.scaleYProperty(), 1.022, Interpolator.EASE_OUT),
                        new KeyValue(cgCrossfadeView.scaleXProperty(), 1.022, Interpolator.EASE_OUT),
                        new KeyValue(cgCrossfadeView.scaleYProperty(), 1.022, Interpolator.EASE_OUT),
                        new KeyValue(cgTone.brightnessProperty(), 0.0, Interpolator.EASE_OUT))
        );
        busHeadlightTimeline.setOnFinished(e -> {
            busHeadlightTimeline = null;
            cgCrossfadeView.setVisible(false);
            cgCrossfadeView.setOpacity(0.0);
            cgCrossfadeView.setImage(null);
            headlightBeamOverlay.setVisible(false);
            headlightWashOverlay.setVisible(false);
            cgView.setEffect(null);
        });
        busHeadlightTimeline.play();
    }

    private void stopBusHeadlightArrival() {
        if (busHeadlightTimeline != null) {
            busHeadlightTimeline.stop();
            busHeadlightTimeline = null;
        }
        cgCrossfadeView.setVisible(false);
        cgCrossfadeView.setOpacity(0.0);
        cgCrossfadeView.setImage(null);
        cgCrossfadeView.setScaleX(1.0);
        cgCrossfadeView.setScaleY(1.0);

        headlightBeamOverlay.setVisible(false);
        headlightBeamOverlay.setOpacity(0.0);
        headlightBeamOverlay.setTranslateX(0.0);

        headlightWashOverlay.setVisible(false);
        headlightWashOverlay.setOpacity(0.0);

        cgTone.setBrightness(0.0);
        cgView.setEffect(null);
        if (cgView.isVisible()) {
            cgView.setOpacity(1.0);
        }
    }

    // Plays "BEEP BEEP!" bus horn -> loud brake screech -> impact flash + CG reveal -> double head-bump thuds.
    private void playSuddenBrakeSequence(StageCue cue, String speaker, Image cgImage) {
        stopSuddenBrakeSequence(false);

        // Keep the seated two-shot stage visible during the initial horn + brake build-up
        hideEventCgImmediate();
        Image targetBg = cue.isUseWideBackground() ? bgWideShotImage : bgCloseUpImage;
        if (targetBg != null && bgView.getImage() != targetBg) {
            bgView.setImage(targetBg);
            updateCoverFit();
        }
        maleSlot.setVisible(true);
        femSlot.setVisible(true);
        Image mSurprised = sprites.getMale("surprised");
        if (mSurprised != null) {
            maleSpriteView.setImage(mSurprised);
        }
        Image fSurprised = sprites.getFem("surprised");
        if (fSurprised != null) {
            femSpriteView.setImage(fSurprised);
        }
        setBothBalanced();
        applyCameraAngle(StageCue.CameraAngle.TWO_SEAT, false);

        pendingBrakeReveal = () -> {
            app.switchBackgroundMusic(GameApplication.BGM_SUDDEN_BRAKE_PATH);
            if (cgImage != null) {
                showEventCgImmediate(cue.getCgPath(), cgImage);
                applyBeatStageVisuals(cue, speaker, true);
            } else {
                if (app.isCgPlaceholderEnabled()) {
                    showMissingCgPlaceholderImmediate(cue);
                }
                applyBeatStageVisuals(cue, speaker, false);
            }
        };

        // 0ms: Big double bus horn ("BEEP BEEP!")
        app.playBusHornSfx();

        suddenBrakeTimeline = new Timeline(
                new KeyFrame(Duration.millis(520), e -> {
                    // Loud brake slam + initial bus lurch before cutting to the CG
                    app.playBrakeSfx();
                    playBusSlowdownSway();
                }),
                new KeyFrame(Duration.millis(820), e -> {
                    // Impact peak: flash + reveal cg_sudden_brake + camera jolt
                    playImpactFlash();
                    if (pendingBrakeReveal != null) {
                        Runnable reveal = pendingBrakeReveal;
                        pendingBrakeReveal = null;
                        reveal.run();
                    }
                }),
                new KeyFrame(Duration.millis(930), e -> app.playHeadThudSfx()),
                new KeyFrame(Duration.millis(1120), e -> app.playHeadThudSecondarySfx())
        );
        suddenBrakeTimeline.setOnFinished(e -> suddenBrakeTimeline = null);
        suddenBrakeTimeline.play();
    }

    private void stopSuddenBrakeSequence(boolean commitCg) {
        if (suddenBrakeTimeline != null) {
            suddenBrakeTimeline.stop();
            suddenBrakeTimeline = null;
        }
        if (commitCg && pendingBrakeReveal != null) {
            Runnable reveal = pendingBrakeReveal;
            pendingBrakeReveal = null;
            reveal.run();
        } else {
            pendingBrakeReveal = null;
        }
    }

    private void applyCameraAngle(StageCue.CameraAngle angle, boolean inCgMode) {
        boolean isCloseUpBg = !inCgMode && bgView.getImage() == bgCloseUpImage;

        // Keep close-up seats crisp while subtly deepening contrast and vignette so the sprites pop forward
        bgView.setEffect(isCloseUpBg ? bgCloseUpTone : null);
        bgVignetteOverlay.setVisible(isCloseUpBg);
        syncBackgroundBaseTransform();

        // Keep two-seat camera framing steady and grounded during conversations (no repetitive zoom-in/out)
        cameraRig.setScaleX(1.0);
        cameraRig.setScaleY(1.0);
        cameraRig.setTranslateX(0.0);
        cameraRig.setTranslateY(0.0);

        if (inCgMode) {
            cgView.setScaleX(1.0);
            cgView.setScaleY(1.0);
            cgView.setTranslateX(0.0);
            cgView.setTranslateY(0.0);
        }
    }

    private void playImpactFlash() {
        Timeline flash = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(flashOverlay.opacityProperty(), 0.75)),
                new KeyFrame(Duration.millis(220), new KeyValue(flashOverlay.opacityProperty(), 0.0, Interpolator.EASE_OUT))
        );
        flash.play();
    }

    private void setSpeakerFocus(boolean maleActive, boolean femActive, boolean conductorActive) {
        animateSpeakerLighting(
                maleActive ? ACTIVE_BRIGHTNESS : LISTENING_BRIGHTNESS,
                femActive ? ACTIVE_BRIGHTNESS : LISTENING_BRIGHTNESS,
                conductorActive ? ACTIVE_BRIGHTNESS : LISTENING_BRIGHTNESS
        );
    }

    private void setBothBalanced() {
        animateSpeakerLighting(NARRATION_BRIGHTNESS, NARRATION_BRIGHTNESS, NARRATION_BRIGHTNESS);
    }

    // Smoothly shifts lighting toward the active speaker instead of abruptly snapping or zooming the camera.
    private void animateSpeakerLighting(double targetMale, double targetFem, double targetConductor) {
        if (focusFadeTimeline != null) {
            focusFadeTimeline.stop();
            focusFadeTimeline = null;
        }
        focusFadeTimeline = new Timeline(
                new KeyFrame(Duration.millis(220),
                        new KeyValue(maleTone.brightnessProperty(), targetMale, Interpolator.EASE_OUT),
                        new KeyValue(femTone.brightnessProperty(), targetFem, Interpolator.EASE_OUT),
                        new KeyValue(conductorTone.brightnessProperty(), targetConductor, Interpolator.EASE_OUT))
        );
        focusFadeTimeline.setOnFinished(e -> focusFadeTimeline = null);
        focusFadeTimeline.play();
    }

    private void setupIdleSeatMotion() {
        maleIdleTimeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(maleSlot.translateYProperty(), 0.0)),
                new KeyFrame(Duration.millis(1800), new KeyValue(maleSlot.translateYProperty(), -2.0, Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.millis(3600), new KeyValue(maleSlot.translateYProperty(), 0.0, Interpolator.EASE_BOTH))
        );
        maleIdleTimeline.setCycleCount(Timeline.INDEFINITE);

        femIdleTimeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(femSlot.translateYProperty(), 0.0)),
                new KeyFrame(Duration.millis(2100), new KeyValue(femSlot.translateYProperty(), -2.2, Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.millis(4200), new KeyValue(femSlot.translateYProperty(), 0.0, Interpolator.EASE_BOTH))
        );
        femIdleTimeline.setCycleCount(Timeline.INDEFINITE);
    }

    private void startIdleSeatMotion() {
        if (maleIdleTimeline != null) {
            maleIdleTimeline.play();
        }
        if (femIdleTimeline != null) {
            femIdleTimeline.play();
        }
    }

    private void stopIdleSeatMotion() {
        if (maleIdleTimeline != null) {
            maleIdleTimeline.stop();
        }
        if (femIdleTimeline != null) {
            femIdleTimeline.stop();
        }
        maleSlot.setTranslateY(0.0);
        femSlot.setTranslateY(0.0);
    }

    private void playEffect(StageCue.EffectType type) {
        if (type == null || type == StageCue.EffectType.NONE) {
            easeSpritesToRest();
            return;
        }
        switch (type) {
            case ENTRANCE_SLIDE:
                playSeatArrivalFade();
                break;
            case BRAKE_JOLT:
                easeSpritesToRest();
                playBusBrakeJolt();
                break;
            case GENTLE_NOD:
                playGentleNod();
                break;
            case WARM_LEAN:
                playWarmLean();
                break;
            case KILIG_FLUSTER:
                playKiligFluster();
                break;
            case PLAYFUL_HOP:
                playPlayfulHop();
                break;
            case AWKWARD_SLUMP:
                playAwkwardSlump();
                break;
            case QUIET_LOOK_AWAY:
                playQuietLookAway();
                break;
            case SHAKE_BLUR:
                playQuickStartle();
                break;
            case STOP_SLOWDOWN:
                playBusSlowdownSway();
                break;
            default:
                easeSpritesToRest();
                break;
        }
    }

    private void resetSpriteTransforms() {
        if (conductorAnimTimeline != null) {
            conductorAnimTimeline.stop();
            conductorAnimTimeline = null;
        }
        if (femAnimTimeline != null) {
            femAnimTimeline.stop();
            femAnimTimeline = null;
        }
        if (maleAnimTimeline != null) {
            maleAnimTimeline.stop();
            maleAnimTimeline = null;
        }

        conductorSpriteView.setScaleX(1.0);
        conductorSpriteView.setScaleY(1.0);
        conductorSpriteView.setOpacity(1.0);
        conductorSpriteView.setTranslateX(0.0);
        conductorSlot.setVisible(conductorOnStage);

        femSpriteView.setScaleX(1.0);
        femSpriteView.setScaleY(1.0);
        femSpriteView.setOpacity(1.0);

        maleSpriteView.setScaleX(1.0);
        maleSpriteView.setScaleY(1.0);
        maleSpriteView.setOpacity(1.0);
    }

    // Smoothly returns both characters to their neutral seated posture when a beat has no motion cue.
    private void easeSpritesToRest() {
        if (maleSpriteView.getTranslateX() != 0 || maleSpriteView.getTranslateY() != 0) {
            maleAnimTimeline = new Timeline(
                    new KeyFrame(Duration.millis(240),
                            new KeyValue(maleSpriteView.translateXProperty(), 0.0, Interpolator.EASE_OUT),
                            new KeyValue(maleSpriteView.translateYProperty(), 0.0, Interpolator.EASE_OUT))
            );
            maleAnimTimeline.play();
        }
        if (femSpriteView.getTranslateX() != 0 || femSpriteView.getTranslateY() != 0) {
            femAnimTimeline = new Timeline(
                    new KeyFrame(Duration.millis(240),
                            new KeyValue(femSpriteView.translateXProperty(), 0.0, Interpolator.EASE_OUT),
                            new KeyValue(femSpriteView.translateYProperty(), 0.0, Interpolator.EASE_OUT))
            );
            femAnimTimeline.play();
        }
    }

    // Soft fade-in when MC first settles into the empty seat beside the stranger.
    private void playSeatArrivalFade() {
        maleSpriteView.setTranslateX(0);
        maleSpriteView.setTranslateY(0);
        maleSpriteView.setOpacity(0.0);
        maleAnimTimeline = new Timeline(
                new KeyFrame(Duration.millis(320),
                        new KeyValue(maleSpriteView.opacityProperty(), 1.0, Interpolator.EASE_OUT))
        );
        maleAnimTimeline.play();
    }

    // Very tiny in-seat nod so the character acknowledges without lifting off the seat.
    private void playGentleNod() {
        femAnimTimeline = new Timeline(
                new KeyFrame(Duration.millis(110),
                        new KeyValue(femSpriteView.translateXProperty(), 0.0, Interpolator.EASE_OUT),
                        new KeyValue(femSpriteView.translateYProperty(), 3.5, Interpolator.EASE_OUT)),
                new KeyFrame(Duration.millis(260),
                        new KeyValue(femSpriteView.translateYProperty(), 0.0, Interpolator.EASE_IN))
        );
        femAnimTimeline.play();
    }

    // Subtle inward lean during warm or sincere moments.
    private void playWarmLean() {
        maleAnimTimeline = new Timeline(
                new KeyFrame(Duration.millis(320),
                        new KeyValue(maleSpriteView.translateXProperty(), 6.0, Interpolator.EASE_OUT),
                        new KeyValue(maleSpriteView.translateYProperty(), 1.5, Interpolator.EASE_OUT))
        );
        femAnimTimeline = new Timeline(
                new KeyFrame(Duration.millis(320),
                        new KeyValue(femSpriteView.translateXProperty(), -6.0, Interpolator.EASE_OUT),
                        new KeyValue(femSpriteView.translateYProperty(), 1.5, Interpolator.EASE_OUT))
        );
        maleAnimTimeline.play();
        femAnimTimeline.play();
    }

    // Shy flustered fidget when caught off-guard by a smooth or flirty line.
    private void playKiligFluster() {
        maleAnimTimeline = new Timeline(
                new KeyFrame(Duration.millis(240),
                        new KeyValue(maleSpriteView.translateXProperty(), 0.0, Interpolator.EASE_OUT),
                        new KeyValue(maleSpriteView.translateYProperty(), 0.0, Interpolator.EASE_OUT))
        );
        femAnimTimeline = new Timeline(
                new KeyFrame(Duration.millis(90),
                        new KeyValue(femSpriteView.translateXProperty(), 5.0, Interpolator.EASE_OUT),
                        new KeyValue(femSpriteView.translateYProperty(), 2.0, Interpolator.EASE_OUT)),
                new KeyFrame(Duration.millis(190),
                        new KeyValue(femSpriteView.translateXProperty(), -2.0, Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.millis(300),
                        new KeyValue(femSpriteView.translateXProperty(), 2.5, Interpolator.EASE_OUT))
        );
        maleAnimTimeline.play();
        femAnimTimeline.play();
    }

    // Light upward laugh lift when a joke lands well.
    private void playPlayfulHop() {
        maleAnimTimeline = new Timeline(
                new KeyFrame(Duration.millis(120),
                        new KeyValue(maleSpriteView.translateXProperty(), 0.0, Interpolator.EASE_OUT),
                        new KeyValue(maleSpriteView.translateYProperty(), -4.5, Interpolator.EASE_OUT)),
                new KeyFrame(Duration.millis(270),
                        new KeyValue(maleSpriteView.translateYProperty(), 0.0, Interpolator.EASE_IN))
        );
        femAnimTimeline = new Timeline(
                new KeyFrame(Duration.millis(135),
                        new KeyValue(femSpriteView.translateXProperty(), 0.0, Interpolator.EASE_OUT),
                        new KeyValue(femSpriteView.translateYProperty(), -5.0, Interpolator.EASE_OUT)),
                new KeyFrame(Duration.millis(290),
                        new KeyValue(femSpriteView.translateYProperty(), 0.0, Interpolator.EASE_IN))
        );
        maleAnimTimeline.play();
        femAnimTimeline.play();
    }

    // Subtle slump back into the seat on awkward or blunt moments.
    private void playAwkwardSlump() {
        maleAnimTimeline = new Timeline(
                new KeyFrame(Duration.millis(340),
                        new KeyValue(maleSpriteView.translateXProperty(), -3.0, Interpolator.EASE_OUT),
                        new KeyValue(maleSpriteView.translateYProperty(), 4.5, Interpolator.EASE_OUT))
        );
        femAnimTimeline = new Timeline(
                new KeyFrame(Duration.millis(340),
                        new KeyValue(femSpriteView.translateXProperty(), 4.0, Interpolator.EASE_OUT),
                        new KeyValue(femSpriteView.translateYProperty(), 4.5, Interpolator.EASE_OUT))
        );
        maleAnimTimeline.play();
        femAnimTimeline.play();
    }

    // Gentle drift toward the rainy window during quiet reflection.
    private void playQuietLookAway() {
        maleAnimTimeline = new Timeline(
                new KeyFrame(Duration.millis(320),
                        new KeyValue(maleSpriteView.translateXProperty(), -4.0, Interpolator.EASE_OUT),
                        new KeyValue(maleSpriteView.translateYProperty(), 1.0, Interpolator.EASE_OUT))
        );
        femAnimTimeline = new Timeline(
                new KeyFrame(Duration.millis(380),
                        new KeyValue(femSpriteView.translateXProperty(), 7.0, Interpolator.EASE_OUT),
                        new KeyValue(femSpriteView.translateYProperty(), 0.0, Interpolator.EASE_OUT))
        );
        maleAnimTimeline.play();
        femAnimTimeline.play();
    }

    // Brief startled twitch when a risky joke or remark falls flat.
    private void playQuickStartle() {
        femAnimTimeline = new Timeline(
                new KeyFrame(Duration.millis(65),
                        new KeyValue(femSpriteView.translateXProperty(), 5.0),
                        new KeyValue(femSpriteView.translateYProperty(), -2.0)),
                new KeyFrame(Duration.millis(140),
                        new KeyValue(femSpriteView.translateXProperty(), -3.0),
                        new KeyValue(femSpriteView.translateYProperty(), 0.0)),
                new KeyFrame(Duration.millis(220),
                        new KeyValue(femSpriteView.translateXProperty(), 0.0, Interpolator.EASE_OUT))
        );
        femAnimTimeline.play();
    }

    // Gentle forward inertia sway as the bus slows down toward the final stop.
    private void playBusSlowdownSway() {
        maleAnimTimeline = new Timeline(
                new KeyFrame(Duration.millis(220),
                        new KeyValue(maleSpriteView.translateXProperty(), -6.0, Interpolator.EASE_OUT)),
                new KeyFrame(Duration.millis(520),
                        new KeyValue(maleSpriteView.translateXProperty(), 0.0, Interpolator.EASE_BOTH))
        );
        femAnimTimeline = new Timeline(
                new KeyFrame(Duration.millis(220),
                        new KeyValue(femSpriteView.translateXProperty(), -6.0, Interpolator.EASE_OUT)),
                new KeyFrame(Duration.millis(520),
                        new KeyValue(femSpriteView.translateXProperty(), 0.0, Interpolator.EASE_BOTH))
        );
        maleAnimTimeline.play();
        femAnimTimeline.play();
    }

    // Whole-bus camera shake on sudden brake (moves the entire stage together).
    private void playBusBrakeJolt() {
        Node shakeTarget = cgView.isVisible() ? cgView : stageLayer;
        Timeline shake = new Timeline(
                new KeyFrame(Duration.millis(0), new KeyValue(shakeTarget.translateXProperty(), 0), new KeyValue(shakeTarget.translateYProperty(), 0)),
                new KeyFrame(Duration.millis(45), new KeyValue(shakeTarget.translateXProperty(), -18), new KeyValue(shakeTarget.translateYProperty(), 6)),
                new KeyFrame(Duration.millis(90), new KeyValue(shakeTarget.translateXProperty(), 15), new KeyValue(shakeTarget.translateYProperty(), -5)),
                new KeyFrame(Duration.millis(140), new KeyValue(shakeTarget.translateXProperty(), -11), new KeyValue(shakeTarget.translateYProperty(), 3)),
                new KeyFrame(Duration.millis(195), new KeyValue(shakeTarget.translateXProperty(), 7), new KeyValue(shakeTarget.translateYProperty(), -2)),
                new KeyFrame(Duration.millis(260), new KeyValue(shakeTarget.translateXProperty(), 0), new KeyValue(shakeTarget.translateYProperty(), 0))
        );
        shake.play();
    }
}
