package com.sabaytayo;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.MotionBlur;
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
import javafx.util.Duration;

import java.io.InputStream;
import java.util.List;

// Visual Novel Encounter screen: drives the player through the 12-scene dialogue graph.
public class EncounterView extends StackPane {

    private static final double ACTIVE_BRIGHTNESS = 0.0;
    private static final double LISTENING_BRIGHTNESS = -0.28;
    private static final double NARRATION_BRIGHTNESS = -0.10;
    private static final int TYPING_DELAY_MS = 18;
    private static final int AUTO_BASE_DELAY_MS = 1200;
    private static final int AUTO_MS_PER_CHAR = 18;
    private static final int AUTO_MIN_DELAY_MS = 1500;
    private static final int AUTO_MAX_DELAY_MS = 4000;

    private final GameApplication app;
    private final RelationshipStats stats;
    private final Momentum momentum;
    private final Encounter encounter;

    private final ImageView bgView;
    private final Image bgWideShotImage;
    private final Image bgCloseUpImage;
    private final ImageView maleSpriteView;
    private final ImageView femSpriteView;
    private final Image maleNeutralImage;
    private final Image maleSincereImage;
    private final Image maleSmirkImage;
    private final Image maleAwkwardImage;
    private final Image maleDeadpanImage;
    private final Image femNeutralImage;
    private final Image femShyImage;
    private final Image femWarmSmileImage;
    private final Image femFlusteredImage;
    private final Image femLaughingImage;
    private final Image femAnnoyedImage;
    private final Image femSurprisedImage;
    private final ColorAdjust maleTone = new ColorAdjust();
    private final ColorAdjust femTone = new ColorAdjust();
    private final MotionBlur maleBlur = new MotionBlur(0, 0);
    private final MotionBlur femBlur = new MotionBlur(0, 0);
    private final Label speakerLabel;
    private final Label dialogueLabel;
    private final Label momentumBadge;
    private final VBox choicesContainer;

    private Timeline typingTimeline;
    private Timeline femAnimTimeline;
    private Timeline maleAnimTimeline;
    private Timeline bgAnimTimeline;
    private Label activeTypingLabel;
    private String activeFullText;
    private Runnable onTypingFinished;
    private String pendingNextSceneId = null;
    private final EventHandler<KeyEvent> keyAdvanceHandler = this::handleKeyAdvance;

    private final List<DialogueBeat> currentBeats = new java.util.ArrayList<>();
    private int currentBeatIndex = 0;
    private boolean showingChoices = false;

    public EncounterView(GameApplication app, RelationshipStats stats, Momentum momentum, Encounter encounter) {
        this.app = app;
        this.stats = stats;
        this.momentum = momentum;
        this.encounter = encounter;

        // 1. Bus background images (wide-shot for boarding approach, close-up for talking)
        bgWideShotImage = loadSpriteImage("/assets/BACKGROUNDS/bg_bus_wide-shot.png");
        bgCloseUpImage = loadSpriteImage("/assets/BACKGROUNDS/bg_bus_close-up.png");
        bgView = new ImageView();
        bgView.fitWidthProperty().bind(widthProperty());
        bgView.fitHeightProperty().bind(heightProperty());
        bgView.setPreserveRatio(false);
        setBackground(bgWideShotImage != null ? bgWideShotImage : loadSpriteImage("/assets/BACKGROUNDS/bg_bus_night_empty.png"));

        // 2. Top HUD Bar
        HBox topBar = new HBox(14);
        topBar.setPadding(new Insets(16, 28, 0, 28));
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setMaxHeight(Region.USE_PREF_SIZE);
        topBar.setPickOnBounds(false);

        momentumBadge = new Label("⚡ MOMENTUM: 0 (1.0x GAIN)");
        momentumBadge.getStyleClass().add("p5-hud-badge");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button skipButton = new Button("Skip to Ending ➔");
        skipButton.getStyleClass().add("choice-button");
        skipButton.setFocusTraversable(false);
        skipButton.setOnAction(e -> {
            stopTypingAnimation();
            app.showEnding();
        });

        HBox windowControls = app.createWindowControlBar();
        topBar.getChildren().addAll(momentumBadge, spacer, skipButton, windowControls);

        // 3. Character Sprites (Left: MC, Right: FEM-C)
        maleNeutralImage = loadSpriteImage("/assets/MC/male_neutral.png");
        maleSincereImage = loadSpriteImage("/assets/MC/male_sincere.png");
        maleSmirkImage = loadSpriteImage("/assets/MC/male_smirk.png");
        maleAwkwardImage = loadSpriteImage("/assets/MC/male_awkward.png");
        maleDeadpanImage = loadSpriteImage("/assets/MC/male_deadpan.png");

        // Chain MotionBlur on top of ColorAdjust so speaker dimming and motion blur work together
        maleBlur.setInput(maleTone);
        maleSpriteView = createSpriteView("/assets/MC/male_neutral.png", 435);
        maleSpriteView.fitHeightProperty().bind(heightProperty().multiply(0.60));
        maleSpriteView.setEffect(maleBlur);

        femNeutralImage = loadSpriteImage("/assets/FEM-C/fem_neutral.png");
        femShyImage = loadSpriteImage("/assets/FEM-C/fem_shy_apologetic.png");
        femWarmSmileImage = loadSpriteImage("/assets/FEM-C/fem_warm_smile.png");
        femFlusteredImage = loadSpriteImage("/assets/FEM-C/fem_flustered.png");
        femLaughingImage = loadSpriteImage("/assets/FEM-C/fem_laughing.png");
        femAnnoyedImage = loadSpriteImage("/assets/FEM-C/fem_annoyed.png");
        femSurprisedImage = loadSpriteImage("/assets/FEM-C/fem_surprised.png");

        femBlur.setInput(femTone);
        femSpriteView = createSpriteView("/assets/FEM-C/fem_neutral.png", 460);
        femSpriteView.fitHeightProperty().bind(heightProperty().multiply(0.64));
        femSpriteView.setEffect(femBlur);

        StackPane femaleSpriteContainer = new StackPane(femSpriteView);
        femaleSpriteContainer.setMaxWidth(Region.USE_PREF_SIZE);
        femaleSpriteContainer.setMaxHeight(Region.USE_PREF_SIZE);
        StackPane.setAlignment(femSpriteView, Pos.BOTTOM_CENTER);

        // 4. Choices Container (centered between the two character sprites)
        choicesContainer = new VBox(6);
        choicesContainer.setAlignment(Pos.CENTER);
        choicesContainer.setMaxWidth(420);
        choicesContainer.setMaxHeight(Region.USE_PREF_SIZE);

        // 5. Dialogue Box (bottom)
        VBox dialogueCard = new VBox(0);
        dialogueCard.maxWidthProperty().bind(widthProperty().multiply(0.86));
        dialogueCard.setMaxHeight(Region.USE_PREF_SIZE);

        speakerLabel = new Label("Stranger");
        speakerLabel.getStyleClass().add("speaker-label");

        HBox nameplate = new HBox(speakerLabel);
        nameplate.getStyleClass().add("speaker-tag");
        nameplate.setMaxWidth(Region.USE_PREF_SIZE);

        VBox textCard = new VBox(8);
        textCard.getStyleClass().add("dialogue-box");
        textCard.setPadding(new Insets(16, 26, 12, 26));
        textCard.setMinHeight(125);

        dialogueLabel = new Label("Loading...");
        dialogueLabel.getStyleClass().add("dialogue-text");
        dialogueLabel.setWrapText(true);

        // VN sub-bar: only Music control bar on the right
        Region subBarSpacer = new Region();
        HBox.setHgrow(subBarSpacer, Priority.ALWAYS);

        HBox vnSubBar = new HBox(12);
        vnSubBar.setAlignment(Pos.CENTER_RIGHT);
        vnSubBar.setPadding(new Insets(4, 0, 0, 0));
        vnSubBar.getChildren().addAll(subBarSpacer, app.createMusicControlBar());

        textCard.getChildren().addAll(dialogueLabel, vnSubBar);
        dialogueCard.getChildren().addAll(nameplate, textCard);

        // Left-clicking anywhere on the screen finishes typing or advances when ready
        setOnMouseClicked(this::handleScreenClick);

        // Support Space and Enter to finish typing or advance when this view is active
        sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (oldScene != null) {
                oldScene.removeEventFilter(KeyEvent.KEY_PRESSED, keyAdvanceHandler);
                stopTypingAnimation();
            }
            if (newScene != null) {
                newScene.addEventFilter(KeyEvent.KEY_PRESSED, keyAdvanceHandler);
            }
        });

        // Assemble layers (sprites sit behind dialogueCard; choices sit above topBar's empty center)
        getChildren().addAll(bgView, maleSpriteView, femaleSpriteContainer, dialogueCard, topBar, choicesContainer);

        StackPane.setAlignment(topBar, Pos.TOP_CENTER);
        StackPane.setAlignment(choicesContainer, Pos.TOP_CENTER);
        StackPane.setMargin(choicesContainer, new Insets(24, 0, 0, 0));
        StackPane.setAlignment(maleSpriteView, Pos.BOTTOM_LEFT);
        StackPane.setMargin(maleSpriteView, new Insets(0, 0, 138, 150));
        StackPane.setAlignment(femaleSpriteContainer, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(femaleSpriteContainer, new Insets(0, 135, 138, 0));
        StackPane.setAlignment(dialogueCard, Pos.BOTTOM_CENTER);
        StackPane.setMargin(dialogueCard, new Insets(0, 90, 16, 90));

        // Load the first scene
        loadScene();
    }

    // Helper to load a sprite image safely from resources.
    private Image loadSpriteImage(String resourcePath) {
        try {
            InputStream stream = getClass().getResourceAsStream(resourcePath);
            if (stream != null) {
                return new Image(stream);
            } else {
                System.err.println("Notice: Could not find sprite " + resourcePath);
            }
        } catch (Exception e) {
            System.err.println("Notice: Could not load sprite " + resourcePath);
        }
        return null;
    }

    // Loads a character sprite image cleanly with pixel-art smoothing disabled.
    private ImageView createSpriteView(String resourcePath, double fitHeight) {
        ImageView view = new ImageView();
        view.setPreserveRatio(true);
        view.setFitHeight(fitHeight);
        view.setSmooth(false);
        Image img = loadSpriteImage(resourcePath);
        if (img != null) {
            view.setImage(img);
        }
        return view;
    }

    // Reads the current scene from Encounter and displays its sequential dialogue beats.
    private void loadScene() {
        Scene current = encounter.getCurrentScene();
        if (current == null) return;

        stopTypingAnimation();
        pendingNextSceneId = null;
        choicesContainer.getChildren().clear();
        showingChoices = false;

        currentBeats.clear();
        currentBeatIndex = 0;
        for (String line : current.getDialogueLines()) {
            currentBeats.add(DialogueBeat.fromLine(line, app.getPlayer(), app.getStranger()));
        }

        // Play scene-specific visual novel sprite effects based on the current beat
        playSceneEntranceEffect(current.getSceneId());

        // Update HUD
        updateHUD();

        // Display the first beat of the scene
        displayCurrentBeat();
    }

    // Displays the current dialogue beat with matching speaker nameplate and character lighting.
    private void displayCurrentBeat() {
        if (currentBeatIndex < 0 || currentBeatIndex >= currentBeats.size()) {
            return;
        }

        DialogueBeat beat = currentBeats.get(currentBeatIndex);
        speakerLabel.setText(beat.getSpeaker());

        // Highlight speaker
        if ("Narrator".equals(beat.getSpeaker())) {
            setBothBalanced();
        } else if (beat.getSpeaker().equals(app.getPlayerName()) || "You".equals(beat.getSpeaker())) {
            setSpeakerFocus(true, false);
        } else {
            setSpeakerFocus(false, true);
        }

        playTypewriter(dialogueLabel, beat.getText(), null);
    }

    // Renders the choice buttons for the current scene.
    private void renderChoices(Scene scene) {
        choicesContainer.getChildren().clear();

        for (DialogueChoice choice : scene.getChoices()) {
            boolean unlocked = choice.isUnlocked(momentum);
            String formattedChoiceText = ScriptFormatter.format(choice.getText(), app.getPlayer(), app.getStranger());

            String buttonText = unlocked
                    ? formattedChoiceText
                    : formattedChoiceText + "  [🔒 Locked: Momentum >= " + choice.getMinMomentum() + "]";

            Button btn = new Button(buttonText);
            btn.getStyleClass().add("choice-button");
            btn.setMaxWidth(420);
            btn.setWrapText(true);
            btn.setDisable(!unlocked);

            DialogueChoice captured = choice;
            btn.setOnAction(e -> handleChoiceSelected(captured));

            choicesContainer.getChildren().add(btn);
        }
    }

    // Called when the player clicks a choice.
    private void handleChoiceSelected(DialogueChoice choice) {
        stopTypingAnimation();
        choicesContainer.getChildren().clear();
        showingChoices = false;

        // Apply the choice's effect on stats and momentum
        choice.applyEffect(stats, momentum);

        // Update HUD
        updateHUD();

        String rawReaction = choice.getReaction(momentum);
        String reaction = ScriptFormatter.format(rawReaction, app.getPlayer(), app.getStranger());

        pendingNextSceneId = choice.getNextSceneId();
        currentBeats.clear();
        currentBeatIndex = 0;

        playChoiceReactionEffect(choice, reaction);

        if (choice instanceof ContinueChoice) {
            // Stay quiet / continue transition: narrator describes the quiet moment
            currentBeats.add(new DialogueBeat("Narrator", reaction));
        } else {
            // Spoken choice: Beat 1 is Player speaking, Beat 2 is Stranger reacting
            String playerSpoken = extractPlayerDialogue(choice.getText());
            String formattedPlayerSpoken = ScriptFormatter.format(playerSpoken, app.getPlayer(), app.getStranger());

            currentBeats.add(new DialogueBeat(app.getPlayerName(), formattedPlayerSpoken));

            if (reaction != null && !reaction.isBlank()) {
                currentBeats.add(new DialogueBeat("Stranger", reaction));
            }
        }

        displayCurrentBeat();
    }

    // Strips [Tone] bracket prefix for spoken player dialogue line.
    private String extractPlayerDialogue(String choiceText) {
        if (choiceText == null) return "...";
        int closingBracket = choiceText.indexOf(']');
        if (closingBracket != -1 && closingBracket + 1 < choiceText.length()) {
            return choiceText.substring(closingBracket + 1).trim();
        }
        return choiceText.trim();
    }

    // Handles left-clicking anywhere on the encounter screen.
    private void handleScreenClick(MouseEvent e) {
        if (e.getButton() != MouseButton.PRIMARY) {
            return;
        }
        if (isClickOnButton(e.getTarget())) {
            return;
        }
        triggerAdvanceAction();
    }

    // Handles Space or Enter key presses to finish typing or advance to the next beat.
    private void handleKeyAdvance(KeyEvent e) {
        if (e.getCode() != KeyCode.SPACE && e.getCode() != KeyCode.ENTER) {
            return;
        }
        e.consume();
        triggerAdvanceAction();
    }

    // Finishes typing if active, otherwise advances to the next beat or scene.
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

        // All current beats are finished
        if (pendingNextSceneId != null) {
            String nextId = pendingNextSceneId;
            pendingNextSceneId = null;
            proceedToNextScene(nextId);
            return;
        }

        Scene current = encounter.getCurrentScene();
        if (current != null) {
            if (hasPlayerReplyChoices(current)) {
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

    // Returns true if the mouse click originated on an interactive button or slider control.
    private boolean isClickOnButton(EventTarget target) {
        if (!(target instanceof Node)) {
            return false;
        }
        Node current = (Node) target;
        while (current != null && current != this) {
            if (current instanceof ButtonBase || current instanceof Slider) {
                return true;
            }
            current = current.getParent();
        }
        return false;
    }

    // Resolves the next scene ID for narration/continue beats without showing a Continue button.
    private String resolveContinueNextSceneId(Scene scene) {
        if (scene.getDefaultNextSceneId() != null) {
            return scene.getDefaultNextSceneId();
        }
        for (DialogueChoice choice : scene.getChoices()) {
            if (choice instanceof ContinueChoice) {
                return choice.getNextSceneId();
            }
        }
        return null;
    }

    // Checks if a scene has actual dialogue choices (not just Continue).
    private boolean hasPlayerReplyChoices(Scene scene) {
        for (DialogueChoice choice : scene.getChoices()) {
            if (!(choice instanceof ContinueChoice)) {
                return true;
            }
        }
        return false;
    }

    // Applies subtle dimming cue to show who is currently speaking or replying.
    private void setSpeakerFocus(boolean maleActive, boolean femActive) {
        maleTone.setBrightness(maleActive ? ACTIVE_BRIGHTNESS : LISTENING_BRIGHTNESS);
        femTone.setBrightness(femActive ? ACTIVE_BRIGHTNESS : LISTENING_BRIGHTNESS);
    }

    // Keeps both sprites evenly lit during pure narration beats.
    private void setBothBalanced() {
        maleTone.setBrightness(NARRATION_BRIGHTNESS);
        femTone.setBrightness(NARRATION_BRIGHTNESS);
    }

    // Plays a character-by-character typing animation on the given label.
    private void playTypewriter(Label targetLabel, String fullText, Runnable afterDone) {
        stopTypingAnimation();

        if (fullText == null || fullText.isEmpty()) {
            targetLabel.setText("");
            if (afterDone != null) afterDone.run();
            return;
        }

        activeTypingLabel = targetLabel;
        activeFullText = fullText;
        onTypingFinished = afterDone;
        targetLabel.setText("");

        final int[] charIndex = { 0 };
        typingTimeline = new Timeline(new KeyFrame(Duration.millis(TYPING_DELAY_MS), e -> {
            charIndex[0]++;
            targetLabel.setText(fullText.substring(0, charIndex[0]));
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

    // Completes the current typing animation immediately when the user clicks the dialogue box.
    private void finishTypingImmediately() {
        if (typingTimeline == null) return;

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

    // Stops any active typing animation without triggering its completion callback.
    private void stopTypingAnimation() {
        if (typingTimeline != null) {
            typingTimeline.stop();
            typingTimeline = null;
        }
        activeTypingLabel = null;
        activeFullText = null;
        onTypingFinished = null;
    }

    // Advances the dialogue graph to the next scene or ending.
    private void proceedToNextScene(String nextId) {
        stopTypingAnimation();
        pendingNextSceneId = null;

        if ("ENDING".equals(nextId)) {
            // S08 choice was made — transition to EndingView
            app.showEnding();
            return;
        }

        // After post-brake beats (S04A/B/C), apply momentum branching to enter Warm (S05A) or Cold (S05B) lane
        try {
            if ("S04_DONE".equals(nextId) || "S05_DONE".equals(nextId)) {
                String branch = encounter.getMomentumBranch(momentum);
                encounter.advanceTo(branch);
            } else {
                encounter.advanceTo(nextId);
            }
        } catch (SceneNotFoundException e) {
            System.err.println("Error advancing scene: " + e.getMessage());
            app.showEnding();
            return;
        }

        // Reload the view for the new scene
        loadScene();
    }

    // Updates the top HUD momentum display.
    private void updateHUD() {
        int m = momentum.getValue();
        String mSign = m > 0 ? "+" + m : String.valueOf(m);
        momentumBadge.setText("⚡ MOMENTUM: " + mSign + " (" + momentum.getMultiplier() + "x GAINS)");
    }

    // -------------------------------------------------------------------------
    // Visual Novel Sprite Reaction Effects (Shakes, Motion Blur, Kilig & Moods)
    // -------------------------------------------------------------------------

    // Triggers scene-specific character sprites, backgrounds, and animations aligned to each beat.
    private void playSceneEntranceEffect(String sceneId) {
        resetSpriteTransforms();

        // Wide-shot while approaching in S01; close-up two-seat view for all talking scenes (S02+)
        if ("S01".equals(sceneId)) {
            setBackground(bgWideShotImage);
        } else {
            setBackground(bgCloseUpImage);
        }

        switch (sceneId) {
            case "S01":
                // Boarding in the rain: MC approaches & sits; Stranger shifts bag & gives small nod
                setMaleSprite(maleNeutralImage);
                setFemSprite(femNeutralImage);
                playEntranceSlide();
                break;
            case "S02":
                // Slow afternoon traffic: gentle bus sway while MC wonders whether to speak
                setMaleSprite(maleDeadpanImage);
                setFemSprite(femNeutralImage);
                playTrafficSway();
                break;
            case "S03":
                // Sudden bus brake: shake the whole bus + jolt both sprites with motion blur
                setMaleSprite(maleAwkwardImage);
                setFemSprite(femSurprisedImage);
                playBusBrakeJolt();
                break;
            case "S04A":
                // After the Incident (Sincere): Stranger glances over gratefully
                setMaleSprite(maleSincereImage);
                setFemSprite(femWarmSmileImage);
                playWarmLeanIn();
                break;
            case "S04B":
                // After the Incident (Sarcastic): Stranger steadies herself on the seat
                setMaleSprite(maleAwkwardImage);
                setFemSprite(femShyImage);
                playGentleNod();
                break;
            case "S04C":
                // After the Incident (Blunt / Quiet): quiet distance looking at the rainy window
                setMaleSprite(maleDeadpanImage);
                setFemSprite(femAnnoyedImage);
                playAwkwardSlump();
                break;
            case "S05A":
                // Warm Lane Part 1 (Proper Introduction): both nod & lean in warmly exchanging names
                setMaleSprite(maleSincereImage);
                setFemSprite(femWarmSmileImage);
                playWarmGreeting();
                break;
            case "S05B":
                // Cold Lane Part 1 (Unspoken Distance): Stranger keeps earphones in and shifts away
                setMaleSprite(maleAwkwardImage);
                setFemSprite(femNeutralImage);
                playColdShiftAway();
                break;
            case "S06A":
                // Warm Lane Part 2 (Warmer Conversation): removes earphone and faces MC
                setMaleSprite(maleSincereImage);
                setFemSprite(femWarmSmileImage);
                playWarmLeanIn();
                break;
            case "S06B":
                // Cold Lane Part 2 (Still-Awkward Conversation): quiet, guarded posture
                setMaleSprite(maleAwkwardImage);
                setFemSprite(femAnnoyedImage);
                playAwkwardSlump();
                break;
            case "S07":
                // Merge (Almost There): bus slows down as passengers prepare for their stops
                setMaleSprite(maleNeutralImage);
                setFemSprite(momentum.getValue() >= 2 ? femWarmSmileImage : femSurprisedImage);
                playStopSlowdown();
                break;
            case "S08":
                // Final Interaction (Before the Ride Ends): MC stands up and looks back one last time
                setMaleSprite(momentum.getValue() >= 0 ? maleSincereImage : maleNeutralImage);
                setFemSprite(momentum.getValue() >= 0 ? femShyImage : femNeutralImage);
                playStandUpPrepare();
                break;
            default:
                setMaleSprite(maleNeutralImage);
                setFemSprite(femNeutralImage);
                break;
        }
    }

    // Triggers both MC and Stranger sprite swaps & animations when the player picks a choice.
    private void playChoiceReactionEffect(DialogueChoice choice, String reaction) {
        resetSpriteTransforms();
        boolean isWarm = momentum.getValue() >= 0;
        boolean isHighMomentum = momentum.getValue() >= 2;
        String lower = reaction.toLowerCase();

        if (choice instanceof FlirtyChoice) {
            // Kilig / flustered reaction: MC smirks, Stranger flusters (or looks surprised if low momentum)
            if (isHighMomentum || lower.contains("nagulat") || lower.contains("smile") || lower.contains("sure") || lower.contains("ohh")) {
                setMaleSprite(maleSmirkImage);
                setFemSprite(femFlusteredImage);
                playKiligFluster();
            } else {
                setMaleSprite(maleAwkwardImage);
                setFemSprite(femSurprisedImage);
                playFemShakeWithBlur(12);
            }
        } else if (choice instanceof SincereChoice) {
            setMaleSprite(maleSincereImage);
            if (isWarm) {
                boolean extraWarm = lower.contains("smile") || lower.contains("thank you")
                        || lower.contains("salamat") || lower.contains("nice meeting")
                        || lower.contains("napangiti") || lower.contains("masaya") || lower.contains("oo nga");
                setFemSprite(extraWarm ? femWarmSmileImage : femShyImage);
                playWarmLeanIn();
            } else {
                setFemSprite(femShyImage);
                playGentleNod();
            }
        } else if (choice instanceof SarcasticChoice) {
            if (isHighMomentum || lower.contains("natawa") || lower.contains("napangiti")
                    || lower.contains("haha") || lower.contains("laugh") || lower.contains("smirk")) {
                setMaleSprite(maleSmirkImage);
                setFemSprite(femLaughingImage);
                playPlayfulHop();
            } else {
                setMaleSprite(maleAwkwardImage);
                setFemSprite(femAnnoyedImage);
                playFemShakeWithBlur(9);
            }
        } else if (choice instanceof BluntChoice) {
            setMaleSprite(maleDeadpanImage);
            setFemSprite(femAnnoyedImage);
            playAwkwardSlump();
        } else if (choice instanceof ContinueChoice) {
            // Stay Quiet choice: calm, reserved settle as both look out the window
            setMaleSprite(maleDeadpanImage);
            setFemSprite(femNeutralImage);
            playQuietLookAway();
        }
    }

    // Safely swaps the bus background image.
    private void setBackground(Image img) {
        if (img != null) {
            bgView.setImage(img);
        }
    }

    // Safely swaps the male protagonist's sprite image.
    private void setMaleSprite(Image img) {
        if (img != null) {
            maleSpriteView.setImage(img);
        }
    }

    // Safely swaps the female character's sprite image.
    private void setFemSprite(Image img) {
        if (img != null) {
            femSpriteView.setImage(img);
        }
    }

    // Stops any running sprite animations and resets positions/blurs so nothing drifts.
    private void resetSpriteTransforms() {
        if (femAnimTimeline != null) {
            femAnimTimeline.stop();
            femAnimTimeline = null;
        }
        if (maleAnimTimeline != null) {
            maleAnimTimeline.stop();
            maleAnimTimeline = null;
        }
        if (bgAnimTimeline != null) {
            bgAnimTimeline.stop();
            bgAnimTimeline = null;
        }

        femSpriteView.setTranslateX(0);
        femSpriteView.setTranslateY(0);
        femSpriteView.setScaleX(1.0);
        femSpriteView.setScaleY(1.0);
        femBlur.setRadius(0);

        maleSpriteView.setTranslateX(0);
        maleSpriteView.setTranslateY(0);
        maleSpriteView.setScaleX(1.0);
        maleSpriteView.setScaleY(1.0);
        maleBlur.setRadius(0);

        bgView.setTranslateX(0);
        bgView.setTranslateY(0);
        bgView.setScaleX(1.0);
        bgView.setScaleY(1.0);
    }

    // Smooth ease-out slide-in when boarding the bus in Scene 1.
    private void playEntranceSlide() {
        maleSpriteView.setTranslateX(-45);
        maleBlur.setAngle(0);
        maleBlur.setRadius(12);

        femSpriteView.setTranslateX(45);
        femBlur.setAngle(0);
        femBlur.setRadius(12);

        maleAnimTimeline = new Timeline(
            new KeyFrame(Duration.millis(360),
                new KeyValue(maleSpriteView.translateXProperty(), 0, Interpolator.EASE_OUT),
                new KeyValue(maleBlur.radiusProperty(), 0, Interpolator.EASE_OUT))
        );
        femAnimTimeline = new Timeline(
            new KeyFrame(Duration.millis(380),
                new KeyValue(femSpriteView.translateXProperty(), 0, Interpolator.EASE_OUT),
                new KeyValue(femBlur.radiusProperty(), 0, Interpolator.EASE_OUT))
        );
        maleAnimTimeline.play();
        femAnimTimeline.play();
    }

    // Kilig / flustered reaction: quick startled hop, soft blush-shake with motion blur, and a close lean-in.
    private void playKiligFluster() {
        femBlur.setAngle(0);

        femAnimTimeline = new Timeline(
            new KeyFrame(Duration.millis(0),
                new KeyValue(femSpriteView.translateXProperty(), 0),
                new KeyValue(femSpriteView.translateYProperty(), 0),
                new KeyValue(femSpriteView.scaleXProperty(), 1.0),
                new KeyValue(femSpriteView.scaleYProperty(), 1.0),
                new KeyValue(femBlur.radiusProperty(), 0)),
            new KeyFrame(Duration.millis(70),
                new KeyValue(femSpriteView.translateYProperty(), -16, Interpolator.EASE_OUT),
                new KeyValue(femSpriteView.translateXProperty(), -9),
                new KeyValue(femBlur.radiusProperty(), 11)),
            new KeyFrame(Duration.millis(130),
                new KeyValue(femSpriteView.translateYProperty(), -4),
                new KeyValue(femSpriteView.translateXProperty(), 9),
                new KeyValue(femBlur.radiusProperty(), 12)),
            new KeyFrame(Duration.millis(190),
                new KeyValue(femSpriteView.translateYProperty(), 0),
                new KeyValue(femSpriteView.translateXProperty(), -6),
                new KeyValue(femBlur.radiusProperty(), 7)),
            new KeyFrame(Duration.millis(250),
                new KeyValue(femSpriteView.translateXProperty(), 6),
                new KeyValue(femBlur.radiusProperty(), 5)),
            new KeyFrame(Duration.millis(310),
                new KeyValue(femSpriteView.translateXProperty(), -3),
                new KeyValue(femBlur.radiusProperty(), 2)),
            new KeyFrame(Duration.millis(420),
                new KeyValue(femSpriteView.translateXProperty(), 0, Interpolator.EASE_OUT),
                new KeyValue(femSpriteView.translateYProperty(), -4, Interpolator.EASE_OUT),
                new KeyValue(femSpriteView.scaleXProperty(), 1.045, Interpolator.EASE_OUT),
                new KeyValue(femSpriteView.scaleYProperty(), 1.045, Interpolator.EASE_OUT),
                new KeyValue(femBlur.radiusProperty(), 0))
        );
        femAnimTimeline.play();
    }

    // Romantic / heartwarming reaction: gentle heart-flutter bounce + soft lean-in closer.
    private void playWarmLeanIn() {
        femBlur.setAngle(90);

        femAnimTimeline = new Timeline(
            new KeyFrame(Duration.millis(0),
                new KeyValue(femSpriteView.translateYProperty(), 0),
                new KeyValue(femSpriteView.scaleXProperty(), 1.0),
                new KeyValue(femSpriteView.scaleYProperty(), 1.0),
                new KeyValue(femBlur.radiusProperty(), 0)),
            new KeyFrame(Duration.millis(120),
                new KeyValue(femSpriteView.translateYProperty(), -10, Interpolator.EASE_OUT),
                new KeyValue(femSpriteView.scaleXProperty(), 1.03, Interpolator.EASE_OUT),
                new KeyValue(femSpriteView.scaleYProperty(), 1.03, Interpolator.EASE_OUT),
                new KeyValue(femBlur.radiusProperty(), 4)),
            new KeyFrame(Duration.millis(260),
                new KeyValue(femSpriteView.translateYProperty(), -2, Interpolator.EASE_IN),
                new KeyValue(femSpriteView.scaleXProperty(), 1.045, Interpolator.EASE_OUT),
                new KeyValue(femSpriteView.scaleYProperty(), 1.045, Interpolator.EASE_OUT),
                new KeyValue(femBlur.radiusProperty(), 0))
        );
        femAnimTimeline.play();
    }

    // Playful laughing double-hop with vertical motion blur.
    private void playPlayfulHop() {
        femBlur.setAngle(90);

        femAnimTimeline = new Timeline(
            new KeyFrame(Duration.millis(0),
                new KeyValue(femSpriteView.translateYProperty(), 0),
                new KeyValue(femBlur.radiusProperty(), 0)),
            new KeyFrame(Duration.millis(90),
                new KeyValue(femSpriteView.translateYProperty(), -22, Interpolator.EASE_OUT),
                new KeyValue(femBlur.radiusProperty(), 9)),
            new KeyFrame(Duration.millis(180),
                new KeyValue(femSpriteView.translateYProperty(), 0, Interpolator.EASE_IN),
                new KeyValue(femBlur.radiusProperty(), 5)),
            new KeyFrame(Duration.millis(250),
                new KeyValue(femSpriteView.translateYProperty(), -10, Interpolator.EASE_OUT),
                new KeyValue(femBlur.radiusProperty(), 4)),
            new KeyFrame(Duration.millis(330),
                new KeyValue(femSpriteView.translateYProperty(), 0, Interpolator.EASE_IN),
                new KeyValue(femBlur.radiusProperty(), 0))
        );
        femAnimTimeline.play();
    }

    // Horizontal startled shake with directional motion blur.
    private void playFemShakeWithBlur(double intensity) {
        femBlur.setAngle(0);

        femAnimTimeline = new Timeline(
            new KeyFrame(Duration.millis(0),
                new KeyValue(femSpriteView.translateXProperty(), 0),
                new KeyValue(femBlur.radiusProperty(), 0)),
            new KeyFrame(Duration.millis(45),
                new KeyValue(femSpriteView.translateXProperty(), -intensity),
                new KeyValue(femBlur.radiusProperty(), 14)),
            new KeyFrame(Duration.millis(95),
                new KeyValue(femSpriteView.translateXProperty(), intensity),
                new KeyValue(femBlur.radiusProperty(), 16)),
            new KeyFrame(Duration.millis(145),
                new KeyValue(femSpriteView.translateXProperty(), -intensity * 0.7),
                new KeyValue(femBlur.radiusProperty(), 10)),
            new KeyFrame(Duration.millis(195),
                new KeyValue(femSpriteView.translateXProperty(), intensity * 0.6),
                new KeyValue(femBlur.radiusProperty(), 8)),
            new KeyFrame(Duration.millis(245),
                new KeyValue(femSpriteView.translateXProperty(), -intensity * 0.3),
                new KeyValue(femBlur.radiusProperty(), 4)),
            new KeyFrame(Duration.millis(300),
                new KeyValue(femSpriteView.translateXProperty(), 0),
                new KeyValue(femBlur.radiusProperty(), 0))
        );
        femAnimTimeline.play();
    }

    // Subtle polite nod for calm neutral replies.
    private void playGentleNod() {
        femAnimTimeline = new Timeline(
            new KeyFrame(Duration.millis(0), new KeyValue(femSpriteView.translateYProperty(), 0)),
            new KeyFrame(Duration.millis(120), new KeyValue(femSpriteView.translateYProperty(), 9, Interpolator.EASE_OUT)),
            new KeyFrame(Duration.millis(260), new KeyValue(femSpriteView.translateYProperty(), 0, Interpolator.EASE_IN))
        );
        femAnimTimeline.play();
    }

    // Awkward / reserved slump for blunt or cold moments.
    private void playAwkwardSlump() {
        femAnimTimeline = new Timeline(
            new KeyFrame(Duration.millis(0),
                new KeyValue(femSpriteView.translateYProperty(), 0),
                new KeyValue(femSpriteView.scaleXProperty(), 1.0),
                new KeyValue(femSpriteView.scaleYProperty(), 1.0)),
            new KeyFrame(Duration.millis(320),
                new KeyValue(femSpriteView.translateYProperty(), 14, Interpolator.EASE_OUT),
                new KeyValue(femSpriteView.scaleXProperty(), 0.97, Interpolator.EASE_OUT),
                new KeyValue(femSpriteView.scaleYProperty(), 0.97, Interpolator.EASE_OUT))
        );
        femAnimTimeline.play();
    }

    // Scene 3 Sudden Bus Brake: shakes the bus background and jolts both characters with motion blur.
    private void playBusBrakeJolt() {
        // Scale background slightly during the shake so edges never expose blank gaps
        bgView.setScaleX(1.04);
        bgView.setScaleY(1.04);

        bgAnimTimeline = new Timeline(
            new KeyFrame(Duration.millis(0),   new KeyValue(bgView.translateXProperty(), 0),   new KeyValue(bgView.translateYProperty(), 0)),
            new KeyFrame(Duration.millis(45),  new KeyValue(bgView.translateXProperty(), -16), new KeyValue(bgView.translateYProperty(), 5)),
            new KeyFrame(Duration.millis(90),  new KeyValue(bgView.translateXProperty(), 14),  new KeyValue(bgView.translateYProperty(), -4)),
            new KeyFrame(Duration.millis(140), new KeyValue(bgView.translateXProperty(), -10), new KeyValue(bgView.translateYProperty(), 3)),
            new KeyFrame(Duration.millis(190), new KeyValue(bgView.translateXProperty(), 7),   new KeyValue(bgView.translateYProperty(), -2)),
            new KeyFrame(Duration.millis(240), new KeyValue(bgView.translateXProperty(), -4),  new KeyValue(bgView.translateYProperty(), 0)),
            new KeyFrame(Duration.millis(310),
                new KeyValue(bgView.translateXProperty(), 0),
                new KeyValue(bgView.translateYProperty(), 0),
                new KeyValue(bgView.scaleXProperty(), 1.0, Interpolator.EASE_OUT),
                new KeyValue(bgView.scaleYProperty(), 1.0, Interpolator.EASE_OUT))
        );

        // MC jolts sideways with brief motion blur
        maleBlur.setAngle(0);
        maleAnimTimeline = new Timeline(
            new KeyFrame(Duration.millis(0),   new KeyValue(maleSpriteView.translateXProperty(), 0),   new KeyValue(maleBlur.radiusProperty(), 0)),
            new KeyFrame(Duration.millis(55),  new KeyValue(maleSpriteView.translateXProperty(), 18),  new KeyValue(maleBlur.radiusProperty(), 16)),
            new KeyFrame(Duration.millis(120), new KeyValue(maleSpriteView.translateXProperty(), -12), new KeyValue(maleBlur.radiusProperty(), 12)),
            new KeyFrame(Duration.millis(190), new KeyValue(maleSpriteView.translateXProperty(), 7),   new KeyValue(maleBlur.radiusProperty(), 6)),
            new KeyFrame(Duration.millis(270), new KeyValue(maleSpriteView.translateXProperty(), 0),   new KeyValue(maleBlur.radiusProperty(), 0))
        );

        // Stranger lurches harder as she bumps her head ("Aray! Grabe naman 'yon!")
        femBlur.setAngle(0);
        femAnimTimeline = new Timeline(
            new KeyFrame(Duration.millis(0),
                new KeyValue(femSpriteView.translateXProperty(), 0),
                new KeyValue(femSpriteView.translateYProperty(), 0),
                new KeyValue(femBlur.radiusProperty(), 0)),
            new KeyFrame(Duration.millis(55),
                new KeyValue(femSpriteView.translateXProperty(), -22),
                new KeyValue(femSpriteView.translateYProperty(), 10),
                new KeyValue(femBlur.radiusProperty(), 20)),
            new KeyFrame(Duration.millis(115),
                new KeyValue(femSpriteView.translateXProperty(), 16),
                new KeyValue(femSpriteView.translateYProperty(), -12),
                new KeyValue(femBlur.radiusProperty(), 18)),
            new KeyFrame(Duration.millis(175),
                new KeyValue(femSpriteView.translateXProperty(), -12),
                new KeyValue(femSpriteView.translateYProperty(), 4),
                new KeyValue(femBlur.radiusProperty(), 12)),
            new KeyFrame(Duration.millis(235),
                new KeyValue(femSpriteView.translateXProperty(), 7),
                new KeyValue(femSpriteView.translateYProperty(), -2),
                new KeyValue(femBlur.radiusProperty(), 6)),
            new KeyFrame(Duration.millis(320),
                new KeyValue(femSpriteView.translateXProperty(), 0),
                new KeyValue(femSpriteView.translateYProperty(), 0),
                new KeyValue(femBlur.radiusProperty(), 0))
        );

        bgAnimTimeline.play();
        maleAnimTimeline.play();
        femAnimTimeline.play();
    }

    // Scene 2 Traffic: gentle slow bus sway while MC wonders whether to speak.
    private void playTrafficSway() {
        bgView.setScaleX(1.015);
        bgView.setScaleY(1.015);

        bgAnimTimeline = new Timeline(
            new KeyFrame(Duration.millis(0),   new KeyValue(bgView.translateXProperty(), 0)),
            new KeyFrame(Duration.millis(420), new KeyValue(bgView.translateXProperty(), -4, Interpolator.EASE_BOTH)),
            new KeyFrame(Duration.millis(840), new KeyValue(bgView.translateXProperty(), 3, Interpolator.EASE_BOTH)),
            new KeyFrame(Duration.millis(1200),
                new KeyValue(bgView.translateXProperty(), 0, Interpolator.EASE_OUT),
                new KeyValue(bgView.scaleXProperty(), 1.0, Interpolator.EASE_OUT),
                new KeyValue(bgView.scaleYProperty(), 1.0, Interpolator.EASE_OUT))
        );

        // MC glances slightly toward the seatmate
        maleAnimTimeline = new Timeline(
            new KeyFrame(Duration.millis(0),   new KeyValue(maleSpriteView.translateXProperty(), 0)),
            new KeyFrame(Duration.millis(320), new KeyValue(maleSpriteView.translateXProperty(), 8, Interpolator.EASE_OUT)),
            new KeyFrame(Duration.millis(700), new KeyValue(maleSpriteView.translateXProperty(), 0, Interpolator.EASE_BOTH))
        );

        bgAnimTimeline.play();
        maleAnimTimeline.play();
    }

    // Scene 5A Warm Lane (Proper Introduction): both characters lean in slightly with a friendly nod.
    private void playWarmGreeting() {
        maleAnimTimeline = new Timeline(
            new KeyFrame(Duration.millis(0),   new KeyValue(maleSpriteView.translateXProperty(), 0), new KeyValue(maleSpriteView.translateYProperty(), 0)),
            new KeyFrame(Duration.millis(180), new KeyValue(maleSpriteView.translateXProperty(), 12, Interpolator.EASE_OUT), new KeyValue(maleSpriteView.translateYProperty(), 5, Interpolator.EASE_OUT)),
            new KeyFrame(Duration.millis(380), new KeyValue(maleSpriteView.translateXProperty(), 4, Interpolator.EASE_BOTH), new KeyValue(maleSpriteView.translateYProperty(), 0, Interpolator.EASE_IN))
        );

        femAnimTimeline = new Timeline(
            new KeyFrame(Duration.millis(0),   new KeyValue(femSpriteView.translateXProperty(), 0), new KeyValue(femSpriteView.translateYProperty(), 0), new KeyValue(femSpriteView.scaleXProperty(), 1.0), new KeyValue(femSpriteView.scaleYProperty(), 1.0)),
            new KeyFrame(Duration.millis(200), new KeyValue(femSpriteView.translateXProperty(), -10, Interpolator.EASE_OUT), new KeyValue(femSpriteView.translateYProperty(), 6, Interpolator.EASE_OUT), new KeyValue(femSpriteView.scaleXProperty(), 1.03, Interpolator.EASE_OUT), new KeyValue(femSpriteView.scaleYProperty(), 1.03, Interpolator.EASE_OUT)),
            new KeyFrame(Duration.millis(420), new KeyValue(femSpriteView.translateXProperty(), -3, Interpolator.EASE_BOTH), new KeyValue(femSpriteView.translateYProperty(), 0, Interpolator.EASE_IN), new KeyValue(femSpriteView.scaleXProperty(), 1.03, Interpolator.EASE_OUT), new KeyValue(femSpriteView.scaleYProperty(), 1.03, Interpolator.EASE_OUT))
        );

        maleAnimTimeline.play();
        femAnimTimeline.play();
    }

    // Scene 5B Cold Lane (Unspoken Distance): Stranger subtly shifts away toward the window.
    private void playColdShiftAway() {
        femAnimTimeline = new Timeline(
            new KeyFrame(Duration.millis(0),
                new KeyValue(femSpriteView.translateXProperty(), 0),
                new KeyValue(femSpriteView.translateYProperty(), 0)),
            new KeyFrame(Duration.millis(360),
                new KeyValue(femSpriteView.translateXProperty(), 16, Interpolator.EASE_OUT),
                new KeyValue(femSpriteView.translateYProperty(), 6, Interpolator.EASE_OUT))
        );
        femAnimTimeline.play();
    }

    // Scene 7 Merge (Almost There): gentle bus deceleration sway as stops approach.
    private void playStopSlowdown() {
        bgView.setScaleX(1.02);
        bgView.setScaleY(1.02);

        bgAnimTimeline = new Timeline(
            new KeyFrame(Duration.millis(0),   new KeyValue(bgView.translateXProperty(), 0)),
            new KeyFrame(Duration.millis(180), new KeyValue(bgView.translateXProperty(), -7, Interpolator.EASE_OUT)),
            new KeyFrame(Duration.millis(420),
                new KeyValue(bgView.translateXProperty(), 0, Interpolator.EASE_BOTH),
                new KeyValue(bgView.scaleXProperty(), 1.0, Interpolator.EASE_OUT),
                new KeyValue(bgView.scaleYProperty(), 1.0, Interpolator.EASE_OUT))
        );

        femAnimTimeline = new Timeline(
            new KeyFrame(Duration.millis(0),   new KeyValue(femSpriteView.translateXProperty(), 0)),
            new KeyFrame(Duration.millis(200), new KeyValue(femSpriteView.translateXProperty(), -6, Interpolator.EASE_OUT)),
            new KeyFrame(Duration.millis(420), new KeyValue(femSpriteView.translateXProperty(), 0, Interpolator.EASE_BOTH))
        );

        bgAnimTimeline.play();
        femAnimTimeline.play();
    }

    // Scene 8 Final Interaction (Before the Ride Ends): MC rises slightly preparing to step off.
    private void playStandUpPrepare() {
        maleAnimTimeline = new Timeline(
            new KeyFrame(Duration.millis(0),
                new KeyValue(maleSpriteView.translateYProperty(), 0),
                new KeyValue(maleSpriteView.translateXProperty(), 0)),
            new KeyFrame(Duration.millis(280),
                new KeyValue(maleSpriteView.translateYProperty(), -16, Interpolator.EASE_OUT),
                new KeyValue(maleSpriteView.translateXProperty(), 8, Interpolator.EASE_OUT))
        );

        femAnimTimeline = new Timeline(
            new KeyFrame(Duration.millis(0),   new KeyValue(femSpriteView.translateYProperty(), 0)),
            new KeyFrame(Duration.millis(220), new KeyValue(femSpriteView.translateYProperty(), -5, Interpolator.EASE_OUT)),
            new KeyFrame(Duration.millis(400), new KeyValue(femSpriteView.translateYProperty(), 0, Interpolator.EASE_IN))
        );

        maleAnimTimeline.play();
        femAnimTimeline.play();
    }

    // Stay Quiet reaction: both characters settle calmly back into their seats looking outside.
    private void playQuietLookAway() {
        maleAnimTimeline = new Timeline(
            new KeyFrame(Duration.millis(0),   new KeyValue(maleSpriteView.translateXProperty(), 0), new KeyValue(maleSpriteView.translateYProperty(), 0)),
            new KeyFrame(Duration.millis(320), new KeyValue(maleSpriteView.translateXProperty(), -8, Interpolator.EASE_OUT), new KeyValue(maleSpriteView.translateYProperty(), 5, Interpolator.EASE_OUT))
        );
        femAnimTimeline = new Timeline(
            new KeyFrame(Duration.millis(0),   new KeyValue(femSpriteView.translateXProperty(), 0), new KeyValue(femSpriteView.translateYProperty(), 0)),
            new KeyFrame(Duration.millis(320), new KeyValue(femSpriteView.translateXProperty(), 8, Interpolator.EASE_OUT), new KeyValue(femSpriteView.translateYProperty(), 4, Interpolator.EASE_OUT))
        );
        maleAnimTimeline.play();
        femAnimTimeline.play();
    }
}
