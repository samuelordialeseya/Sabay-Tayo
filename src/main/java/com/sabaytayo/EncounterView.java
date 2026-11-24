package com.sabaytayo;

import java.io.InputStream;
import java.util.ArrayList;
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
    private final ImageView cgView;
    private final StackPane spriteLayer;
    private final StackPane maleSlot;
    private final StackPane femSlot;
    private final ImageView maleSpriteView;
    private final ImageView femSpriteView;
    private final BusAtmosphereLayer atmosphereLayer;
    private final Region bgVignetteOverlay;
    private final Region flashOverlay;
    private final Region bgFadeOverlay;

    // Cached background, CG, and sprite images
    private final Image bgWideShotImage;
    private final Image bgCloseUpImage;
    private final Map<String, Image> maleSprites = new HashMap<>();
    private final Map<String, Image> femSprites = new HashMap<>();
    private final Map<String, Image> cgCache = new HashMap<>();
    private String activeCgPath = null;

    // Background & sprite lighting effects
    private final ColorAdjust bgCloseUpTone = new ColorAdjust();
    private final ColorAdjust maleTone = new ColorAdjust();
    private final ColorAdjust femTone = new ColorAdjust();

    // Dialogue & HUD controls
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
    private final Button autoBtn;
    private final Button musicBtn;

    // Modal overlay (Settings)
    private final StackPane modalOverlay;
    private boolean modalOpen = false;

    // Timelines & VN playback states
    private Timeline typingTimeline;
    private Timeline femAnimTimeline;
    private Timeline maleAnimTimeline;
    private Timeline focusFadeTimeline;
    private Timeline maleIdleTimeline;
    private Timeline femIdleTimeline;
    private Timeline bgFadeTimeline;
    private Timeline nextPulseTimeline;
    private PauseTransition autoAdvanceTimer;
    private Runnable pendingFadeMidpoint;
    private boolean initialFadePlayed = false;

    private Label activeTypingLabel;
    private String activeFullText;
    private Runnable onTypingFinished;
    private String pendingNextSceneId = null;

    private final List<DialogueBeat> currentBeats = new ArrayList<>();
    private final List<StageCue> currentBeatCues = new ArrayList<>();
    private int currentBeatIndex = 0;
    private boolean showingChoices = false;
    private boolean autoMode = false;

    private final EventHandler<KeyEvent> keyAdvanceHandler = this::handleKeyAdvance;

    public EncounterView(GameApplication app, RelationshipStats stats, Momentum momentum, Encounter encounter) {
        this.app = app;
        this.stats = stats;
        this.momentum = momentum;
        this.encounter = encounter;

        getStyleClass().add("encounter-root");

        // 1. Load backgrounds and character sprite maps
        bgWideShotImage = loadSpriteImage("/assets/BACKGROUNDS/bg_bus_wide-shot.png");
        bgCloseUpImage = loadSpriteImage("/assets/BACKGROUNDS/bg_bus_close-up.png");
        Image fallbackBg = loadSpriteImage("/assets/BACKGROUNDS/bg_bus_night_empty.png");

        loadCharacterSprites();

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

        cgView = new ImageView();
        cgView.setPreserveRatio(true);
        cgView.setSmooth(false);
        cgView.setVisible(false);
        cgView.setOpacity(0.0);

        // Aspect-fill binding so 16:9 backgrounds & CGs fill any screen without stretching
        widthProperty().addListener((obs, oldW, newW) -> updateCoverFit());
        heightProperty().addListener((obs, oldH, newH) -> {
            updateCoverFit();
            updateResponsiveMetrics(newH.doubleValue());
        });

        // 3. Character sprites anchored near bottom behind the semi-transparent dialogue box
        maleSpriteView = createSpriteView(maleSprites.get("neutral"));
        maleSpriteView.fitHeightProperty().bind(heightProperty().multiply(0.86));
        maleSpriteView.setEffect(maleTone);

        maleSlot = new StackPane(maleSpriteView);
        maleSlot.setMaxWidth(Region.USE_PREF_SIZE);
        maleSlot.setMaxHeight(Region.USE_PREF_SIZE);
        maleSlot.setPickOnBounds(false);
        StackPane.setAlignment(maleSpriteView, Pos.BOTTOM_CENTER);
        maleSlot.translateXProperty().bind(widthProperty().multiply(-SPRITE_SEAT_OFFSET_RATIO));

        femSpriteView = createSpriteView(femSprites.get("neutral"));
        femSpriteView.fitHeightProperty().bind(heightProperty().multiply(0.76));
        femSpriteView.setEffect(femTone);

        femSlot = new StackPane(femSpriteView);
        femSlot.setMaxWidth(Region.USE_PREF_SIZE);
        femSlot.setMaxHeight(Region.USE_PREF_SIZE);
        femSlot.setPickOnBounds(false);
        StackPane.setAlignment(femSpriteView, Pos.BOTTOM_CENTER);
        femSlot.translateXProperty().bind(widthProperty().multiply(SPRITE_SEAT_OFFSET_RATIO));

        spriteLayer = new StackPane(maleSlot, femSlot);
        spriteLayer.setPickOnBounds(false);
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
                cameraRig, cgView,
                atmosphereLayer, flashOverlay, bgFadeOverlay);

        // 4. Minimal Top HUD Bar (compact Momentum badge left, subtle window controls right)
        HBox topBar = new HBox(12);
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
        autoBtn = createInlineControl("Auto", this::toggleAutoMode);
        musicBtn = createInlineControl(app.isMusicMuted() ? "Music: OFF" : "Music", this::handleMusicToggle);
        Button settingsBtn = createInlineControl("Settings", this::openSettingsModal);

        HBox bottomControls = new HBox(18, autoBtn, musicBtn, settingsBtn);
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
            }
            if (newScene != null) {
                newScene.addEventFilter(KeyEvent.KEY_PRESSED, keyAdvanceHandler);
                atmosphereLayer.start();
                startIdleSeatMotion();
                updateCoverFit();
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

    // Loads all MC and FEM-C expression sprites into lookup maps.
    private void loadCharacterSprites() {
        maleSprites.put("neutral", loadSpriteImage("/assets/MC/male_neutral.png"));
        maleSprites.put("sincere", loadSpriteImage("/assets/MC/male_sincere.png"));
        maleSprites.put("smirk", loadSpriteImage("/assets/MC/male_smirk.png"));
        maleSprites.put("awkward", loadSpriteImage("/assets/MC/male_awkward.png"));
        maleSprites.put("deadpan", loadSpriteImage("/assets/MC/male_deadpan.png"));
        maleSprites.put("flustered", loadSpriteImage("/assets/MC/male_flustered.png"));
        maleSprites.put("laughing", loadSpriteImage("/assets/MC/male_laughing.png"));
        maleSprites.put("relieved_smile", loadSpriteImage("/assets/MC/male_relieved_smile.png"));
        maleSprites.put("surprised", loadSpriteImage("/assets/MC/male_surprised.png"));

        femSprites.put("neutral", loadSpriteImage("/assets/FEM-C/fem_neutral.png"));
        femSprites.put("shy", loadSpriteImage("/assets/FEM-C/fem_shy_apologetic.png"));
        femSprites.put("warm_smile", loadSpriteImage("/assets/FEM-C/fem_warm_smile.png"));
        femSprites.put("flustered", loadSpriteImage("/assets/FEM-C/fem_flustered.png"));
        femSprites.put("laughing", loadSpriteImage("/assets/FEM-C/fem_laughing.png"));
        femSprites.put("annoyed", loadSpriteImage("/assets/FEM-C/fem_annoyed.png"));
        femSprites.put("surprised", loadSpriteImage("/assets/FEM-C/fem_surprised.png"));
        femSprites.put("wince", loadSpriteImage("/assets/FEM-C/fem_wince.png"));
        femSprites.put("phone_looking", loadSpriteImage("/assets/FEM-C/fem_phone_looking.png"));
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
    }

    private Image loadSpriteImage(String resourcePath) {
        if (resourcePath == null) {
            return null;
        }
        try {
            InputStream stream = getClass().getResourceAsStream(resourcePath);
            if (stream != null) {
                return new Image(stream);
            }
        } catch (Exception e) {
            System.err.println("Notice: Could not load image " + resourcePath);
        }
        return null;
    }

    // Loads an Event CG if the user has added it to src/main/resources/assets/CG/ (returns null if not yet added).
    private Image loadEventCg(String cgPath) {
        if (cgPath == null) {
            return null;
        }
        if (cgCache.containsKey(cgPath)) {
            return cgCache.get(cgPath);
        }
        Image img = loadSpriteImage(cgPath);
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

        stopTypingAnimation();
        stopAutoTimer();
        pendingNextSceneId = null;
        hideChoices();

        currentBeats.clear();
        currentBeatCues.clear();
        currentBeatIndex = 0;

        List<String> lines = current.getDialogueLines();
        for (int i = 0; i < lines.size(); i++) {
            currentBeats.add(DialogueBeat.fromLine(lines.get(i), app.getPlayer(), app.getStranger()));
            currentBeatCues.add(StageCue.forSceneBeat(current.getSceneId(), i, momentum));
        }

        updateHUD();
        displayCurrentBeat();
    }

    private void displayCurrentBeat() {
        if (currentBeatIndex < 0 || currentBeatIndex >= currentBeats.size()) {
            return;
        }

        stopAutoTimer();
        hideNextIndicator();

        DialogueBeat beat = currentBeats.get(currentBeatIndex);
        StageCue cue = (currentBeatIndex < currentBeatCues.size()) ? currentBeatCues.get(currentBeatIndex) : null;

        // Update overlapping speaker nameplate (hidden on Narrator beats like DDLC Screenshot 2)
        updateNameplate(beat.getSpeaker());

        // Apply per-beat CG switch, camera framing, sprite expressions, speaker focus, and animations
        if (cue != null) {
            applyStageCue(cue, beat.getSpeaker());
        } else {
            applySpeakerFocusFor(beat.getSpeaker());
        }

        playTypewriter(dialogueLabel, beat.getText(), this::onBeatTypingComplete);
    }

    private void applySpeakerFocusFor(String speaker) {
        if ("Narrator".equals(speaker)) {
            setBothBalanced();
        } else if (speaker != null && (speaker.equals(app.getPlayerName()) || "You".equals(speaker))) {
            setSpeakerFocus(true, false);
        } else {
            setSpeakerFocus(false, true);
        }
    }

    private void updateNameplate(String speaker) {
        nameplate.getStyleClass().removeAll("vn-nameplate-player", "vn-nameplate-stranger");

        if (speaker == null || "Narrator".equals(speaker) || speaker.isBlank()) {
            nameplate.setVisible(false);
            nameplate.setManaged(false);
            return;
        }

        speakerLabel.setText(speaker);
        nameplate.setVisible(true);
        nameplate.setManaged(true);

        if (speaker.equals(app.getPlayerName()) || "You".equals(speaker)) {
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

        for (DialogueChoice choice : scene.getChoices()) {
            boolean unlocked = choice.isUnlocked(momentum);
            String formatted = ScriptFormatter.format(choice.getText(), app.getPlayer(), app.getStranger());

            String toneTag = extractToneTag(formatted);
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

            if (!toneTag.isEmpty()) {
                Label badge = new Label(toneTag.toUpperCase());
                badge.getStyleClass().addAll("tone-badge", "tone-" + toneTag.toLowerCase());
                badge.setMinWidth(Region.USE_PREF_SIZE);
                content.getChildren().add(badge);
            }

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
        stopTypingAnimation();
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
                        : "Fatima";
                List<DialogueBeat> reactionBeats = DialogueBeat.splitReaction(reaction, strangerName);
                StageCue reactionCue = StageCue.forChoiceReactionBeat(choice, reaction, momentum);
                for (int i = 0; i < reactionBeats.size(); i++) {
                    currentBeats.add(reactionBeats.get(i));
                    currentBeatCues.add(i == 0 ? reactionCue : reactionCue.withoutEffect());
                }
            }
        }

        displayCurrentBeat();
    }

    private String extractToneTag(String text) {
        if (text != null && text.startsWith("[")) {
            int close = text.indexOf(']');
            if (close > 1) {
                return text.substring(1, close).trim();
            }
        }
        return "";
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
        if (modalOpen || e.getButton() != MouseButton.PRIMARY) {
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
            if (modalOpen) {
                closeModal();
            } else {
                openSettingsModal();
            }
            return;
        }
        if (modalOpen) {
            return;
        }
        if (e.getCode() != KeyCode.SPACE && e.getCode() != KeyCode.ENTER) {
            return;
        }
        e.consume();
        triggerAdvanceAction();
    }

    private void triggerAdvanceAction() {
        if (typingTimeline != null) {
            finishTypingImmediately();
            return;
        }

        if (showingChoices) {
            return;
        }

        if (currentBeatIndex + 1 < currentBeats.size()) {
            currentBeatIndex++;
            displayCurrentBeat();
            return;
        }

        if (pendingNextSceneId != null) {
            String nextId = pendingNextSceneId;
            pendingNextSceneId = null;
            proceedToNextScene(nextId);
            return;
        }

        Scene current = encounter.getCurrentScene();
        if (current != null) {
            if (hasPlayerReplyChoices(current)) {
                stopAutoTimer();
                hideNextIndicator();
                showingChoices = true;
                renderChoices(current);
                setSpeakerFocus(true, false);
            } else {
                String nextId = resolveContinueNextSceneId(current);
                if (nextId != null) {
                    proceedToNextScene(nextId);
                }
            }
        }
    }

    private void toggleAutoMode() {
        autoMode = !autoMode;
        updateInlineButtonState(autoBtn, autoMode);
        if (autoMode && typingTimeline == null && !showingChoices && !modalOpen) {
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
        stopTypingAnimation();
        stopAutoTimer();
        pendingNextSceneId = null;

        if ("ENDING".equals(nextId)) {
            cleanupTimers();
            app.showEnding();
            return;
        }

        try {
            if ("S04_DONE".equals(nextId) || "S05_DONE".equals(nextId)) {
                encounter.advanceTo(encounter.getMomentumBranch(momentum));
            } else {
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

        // Quick navigation actions
        Button restartBtn = new Button("Restart Ride");
        restartBtn.getStyleClass().add("menu-button");
        restartBtn.setOnAction(e -> {
            app.playClickSfx();
            closeModal();
            cleanupTimers();
            app.showStart();
        });

        Button resumeBtn = new Button("Resume");
        resumeBtn.getStyleClass().addAll("menu-button", "commute-menu-btn-active");
        resumeBtn.setOnAction(e -> {
            app.playClickSfx();
            closeModal();
        });

        HBox actions = new HBox(12, restartBtn, resumeBtn);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.setPadding(new Insets(8, 0, 0, 0));

        modalCard.getChildren().addAll(
                title,
                new VBox(6, bgmLbl, bgmSlider),
                sfxToggle,
                new VBox(6, speedLbl, speedSlider),
                atmToggle,
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
        if (autoMode && typingTimeline == null && !showingChoices) {
            scheduleNextAuto();
        }
    }

    // -------------------------------------------------------------------------
    // Typewriter Effect
    // -------------------------------------------------------------------------

    private void playTypewriter(Label targetLabel, String fullText, Runnable afterDone) {
        stopTypingAnimation();

        if (fullText == null || fullText.isEmpty()) {
            targetLabel.setText("");
            if (afterDone != null) {
                afterDone.run();
            }
            return;
        }

        activeTypingLabel = targetLabel;
        activeFullText = fullText;
        onTypingFinished = afterDone;
        targetLabel.setText("");

        final int[] charIndex = { 0 };
        typingTimeline = new Timeline(new KeyFrame(Duration.millis(app.getTypingDelayMs()), e -> {
            charIndex[0]++;
            targetLabel.setText(fullText.substring(0, charIndex[0]));
            if (charIndex[0] % 3 == 1 && !java.lang.Character.isWhitespace(fullText.charAt(charIndex[0] - 1))) {
                app.playTextBlip();
            }
        }));
        typingTimeline.setCycleCount(fullText.length());
        typingTimeline.setOnFinished(e -> {
            typingTimeline = null;
            activeTypingLabel = null;
            activeFullText = null;
            if (onTypingFinished != null) {
                Runnable done = onTypingFinished;
                onTypingFinished = null;
                done.run();
            }
        });
        typingTimeline.play();
    }

    private void finishTypingImmediately() {
        if (typingTimeline == null) {
            return;
        }
        typingTimeline.stop();
        typingTimeline = null;

        if (activeTypingLabel != null && activeFullText != null) {
            activeTypingLabel.setText(activeFullText);
        }
        activeTypingLabel = null;
        activeFullText = null;

        if (onTypingFinished != null) {
            Runnable done = onTypingFinished;
            onTypingFinished = null;
            done.run();
        }
    }

    private void stopTypingAnimation() {
        if (typingTimeline != null) {
            typingTimeline.stop();
            typingTimeline = null;
        }
        activeTypingLabel = null;
        activeFullText = null;
        onTypingFinished = null;
    }

    private void cleanupTimers() {
        stopTypingAnimation();
        stopAutoTimer();
        stopBgFadeAnimation(false);
        stopIdleSeatMotion();
        if (focusFadeTimeline != null) {
            focusFadeTimeline.stop();
            focusFadeTimeline = null;
        }
        hideNextIndicator();
        resetSpriteTransforms();
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
        resetSpriteTransforms();

        // Commit any interrupted fade before evaluating the next beat
        stopBgFadeAnimation(true);

        // 1. Play brake SFX & flash on dramatic impact beats
        if (cue.isPlayBrakeSfx()) {
            app.playBrakeSfx();
            playImpactFlash();
        }

        // 2. Check if this beat has a full-screen Event CG image available in assets/CG/
        Image cgImage = loadEventCg(cue.getCgPath());
        if (cgImage != null) {
            boolean cgChanged = !cue.getCgPath().equals(activeCgPath);
            if (cgChanged && getScene() != null && !cue.isPlayBrakeSfx()) {
                fadeThroughBlack(() -> {
                    showEventCgImmediate(cue.getCgPath(), cgImage);
                    applyBeatStageVisuals(cue, speaker, true);
                });
            } else {
                showEventCgImmediate(cue.getCgPath(), cgImage);
                applyBeatStageVisuals(cue, speaker, true);
            }
            return;
        }

        // 3. Normal Sprite Stage (or fallback when CG artwork is not present)
        Image targetBg = cue.isUseWideBackground() ? bgWideShotImage : bgCloseUpImage;
        boolean wasInCg = (activeCgPath != null);
        boolean bgChanged = targetBg != null && bgView.getImage() != targetBg;

        if ((wasInCg || bgChanged) && getScene() != null) {
            fadeThroughBlack(() -> {
                hideEventCgImmediate();
                if (targetBg != null && bgView.getImage() != targetBg) {
                    bgView.setImage(targetBg);
                    updateCoverFit();
                }
                applyBeatStageVisuals(cue, speaker, false);
            });
        } else {
            hideEventCgImmediate();
            if (bgChanged) {
                bgView.setImage(targetBg);
                updateCoverFit();
            }
            applyBeatStageVisuals(cue, speaker, false);
        }
    }

    private void applyBeatStageVisuals(StageCue cue, String speaker, boolean inCgMode) {
        // 3. Sprite visibility & mood swaps (null mood preserves current sprite expression)
        maleSlot.setVisible(cue.isShowMale());
        femSlot.setVisible(cue.isShowFem());

        if (cue.getMaleMood() != null) {
            Image mImg = maleSprites.get(cue.getMaleMood());
            if (mImg != null) {
                maleSpriteView.setImage(mImg);
            }
        }
        if (cue.getFemMood() != null) {
            Image fImg = femSprites.get(cue.getFemMood());
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

    private void stopBgFadeAnimation(boolean commitPending) {
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

    private void showEventCgImmediate(String cgPath, Image cgImage) {
        activeCgPath = cgPath;
        cgView.setImage(cgImage);
        updateCoverFit();
        cgView.setOpacity(1.0);
        cgView.setVisible(true);
        spriteLayer.setVisible(false);
    }

    private void hideEventCgImmediate() {
        activeCgPath = null;
        cgView.setOpacity(0.0);
        cgView.setVisible(false);
        spriteLayer.setVisible(true);
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

    private void setSpeakerFocus(boolean maleActive, boolean femActive) {
        animateSpeakerLighting(
                maleActive ? ACTIVE_BRIGHTNESS : LISTENING_BRIGHTNESS,
                femActive ? ACTIVE_BRIGHTNESS : LISTENING_BRIGHTNESS
        );
    }

    private void setBothBalanced() {
        animateSpeakerLighting(NARRATION_BRIGHTNESS, NARRATION_BRIGHTNESS);
    }

    // Smoothly shifts lighting toward the active speaker instead of abruptly snapping or zooming the camera.
    private void animateSpeakerLighting(double targetMale, double targetFem) {
        if (focusFadeTimeline != null) {
            focusFadeTimeline.stop();
            focusFadeTimeline = null;
        }
        focusFadeTimeline = new Timeline(
                new KeyFrame(Duration.millis(220),
                        new KeyValue(maleTone.brightnessProperty(), targetMale, Interpolator.EASE_OUT),
                        new KeyValue(femTone.brightnessProperty(), targetFem, Interpolator.EASE_OUT))
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
        if (femAnimTimeline != null) {
            femAnimTimeline.stop();
            femAnimTimeline = null;
        }
        if (maleAnimTimeline != null) {
            maleAnimTimeline.stop();
            maleAnimTimeline = null;
        }

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
