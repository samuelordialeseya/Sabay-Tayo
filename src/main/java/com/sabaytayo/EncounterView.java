package com.sabaytayo;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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

    private final GameApplication app;
    private final RelationshipStats stats;
    private final Momentum momentum;
    private final Encounter encounter;

    private final ImageView maleSpriteView;
    private final ImageView femSpriteView;
    private final ColorAdjust maleTone = new ColorAdjust();
    private final ColorAdjust femTone = new ColorAdjust();
    private final Label speakerLabel;
    private final Label dialogueLabel;
    private final Label reactionLabel;
    private final Label momentumBadge;
    private final Label expressionBadge;
    private final VBox choicesContainer;

    private Timeline typingTimeline;
    private Label activeTypingLabel;
    private String activeFullText;
    private Runnable onTypingFinished;

    public EncounterView(GameApplication app, RelationshipStats stats, Momentum momentum, Encounter encounter) {
        this.app = app;
        this.stats = stats;
        this.momentum = momentum;
        this.encounter = encounter;

        // 1. Bus background image
        ImageView bgView = new ImageView();
        try {
            Image bg = new Image(getClass().getResourceAsStream("/com/sabaytayo/images/bg.jpg"));
            bgView.setImage(bg);
            bgView.setFitWidth(1280);
            bgView.setFitHeight(720);
            bgView.setPreserveRatio(false);
        } catch (Exception e) {
            System.err.println("Notice: Could not load bg.jpg, using default styling.");
        }

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
        skipButton.setOnAction(e -> {
            stopTypingAnimation();
            app.showEnding();
        });

        topBar.getChildren().addAll(momentumBadge, spacer, skipButton);

        // 3. Character Sprite Placeholders (Left: MC, Right: FEM-C)
        maleSpriteView = createSpriteView("/assets/MC/male_neutral.png", 380);
        maleSpriteView.setEffect(maleTone);

        femSpriteView = createSpriteView("/assets/FEM-C/fem_neutral.png", 405);
        femSpriteView.setEffect(femTone);
        expressionBadge = new Label("Stranger • Listening");
        expressionBadge.getStyleClass().add("sprite-frame");

        StackPane femaleSpriteContainer = new StackPane(femSpriteView, expressionBadge);
        femaleSpriteContainer.setMaxWidth(Region.USE_PREF_SIZE);
        femaleSpriteContainer.setMaxHeight(Region.USE_PREF_SIZE);
        StackPane.setAlignment(femSpriteView, Pos.BOTTOM_CENTER);
        StackPane.setAlignment(expressionBadge, Pos.BOTTOM_CENTER);
        StackPane.setMargin(expressionBadge, new Insets(0, 0, 42, 0));

        // 4. Choices Container (centered between the two character sprites)
        choicesContainer = new VBox(6);
        choicesContainer.setAlignment(Pos.CENTER);
        choicesContainer.setMaxWidth(435);
        choicesContainer.setMaxHeight(Region.USE_PREF_SIZE);

        // 5. Dialogue Box (bottom)
        VBox dialogueCard = new VBox(0);
        dialogueCard.setMaxWidth(1100);
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

        reactionLabel = new Label("");
        reactionLabel.getStyleClass().add("reaction-text");
        reactionLabel.setWrapText(true);

        // VN sub-bar
        HBox vnSubBar = new HBox(12);
        vnSubBar.setAlignment(Pos.CENTER_RIGHT);
        vnSubBar.setPadding(new Insets(4, 0, 0, 0));

        String[] subActions = { "History", "Skip", "Auto", "Save", "Load", "Settings" };
        for (String action : subActions) {
            Button subBtn = new Button(action);
            subBtn.getStyleClass().add("vn-sub-button");
            vnSubBar.getChildren().add(subBtn);
        }

        textCard.getChildren().addAll(dialogueLabel, reactionLabel, vnSubBar);
        dialogueCard.getChildren().addAll(nameplate, textCard);

        // Clicking the dialogue box completes the current typing animation immediately
        dialogueCard.setOnMouseClicked(e -> finishTypingImmediately());

        // Assemble layers (sprites sit behind dialogueCard; choices sit above topBar's empty center)
        getChildren().addAll(bgView, maleSpriteView, femaleSpriteContainer, dialogueCard, topBar, choicesContainer);

        StackPane.setAlignment(topBar, Pos.TOP_CENTER);
        StackPane.setAlignment(choicesContainer, Pos.TOP_CENTER);
        StackPane.setMargin(choicesContainer, new Insets(24, 0, 0, 0));
        StackPane.setAlignment(maleSpriteView, Pos.BOTTOM_LEFT);
        StackPane.setMargin(maleSpriteView, new Insets(0, 0, 138, 185));
        StackPane.setAlignment(femaleSpriteContainer, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(femaleSpriteContainer, new Insets(0, 170, 138, 0));
        StackPane.setAlignment(dialogueCard, Pos.BOTTOM_CENTER);
        StackPane.setMargin(dialogueCard, new Insets(0, 90, 16, 90));

        // Load the first scene
        loadScene();
    }

    // Loads a character sprite image cleanly with pixel-art smoothing disabled.
    private ImageView createSpriteView(String resourcePath, double fitHeight) {
        ImageView view = new ImageView();
        view.setPreserveRatio(true);
        view.setFitHeight(fitHeight);
        view.setSmooth(false);
        try {
            InputStream stream = getClass().getResourceAsStream(resourcePath);
            if (stream != null) {
                view.setImage(new Image(stream));
            } else {
                System.err.println("Notice: Could not find sprite " + resourcePath);
            }
        } catch (Exception e) {
            System.err.println("Notice: Could not load sprite " + resourcePath);
        }
        return view;
    }

    // Reads the current scene from Encounter and displays its dialogue + choices.
    private void loadScene() {
        Scene current = encounter.getCurrentScene();
        if (current == null) return;

        List<String> lines = current.getDialogueLines();
        String fullDialogue = !lines.isEmpty() ? String.join("\n\n", lines) : "...";
        boolean strangerSpeaks = fullDialogue.contains("Stranger:");

        // Clear reaction and choices while the new scene line types in
        reactionLabel.setText("");
        choicesContainer.getChildren().clear();
        speakerLabel.setText(strangerSpeaks ? "Stranger" : "Narrator");

        // Highlight Stranger if speaking, or keep both balanced during narration
        if (strangerSpeaks) {
            setSpeakerFocus(false, true);
        } else {
            setBothBalanced();
        }

        // Update HUD
        updateHUD();

        // Type out the scene text, then reveal the player's choices
        playTypewriter(dialogueLabel, fullDialogue, () -> {
            renderChoices(current);
            if (hasPlayerReplyChoices(current)) {
                setSpeakerFocus(true, false);
            }
        });
    }

    // Renders the choice buttons for the current scene.
    private void renderChoices(Scene scene) {
        choicesContainer.getChildren().clear();

        for (DialogueChoice choice : scene.getChoices()) {
            boolean unlocked = choice.isUnlocked(momentum);

            String buttonText = unlocked
                    ? choice.getText()
                    : choice.getText() + "  [🔒 Locked: Momentum >= " + choice.getMinMomentum() + "]";

            Button btn = new Button(buttonText);
            btn.getStyleClass().add("choice-button");
            btn.setMaxWidth(435);
            btn.setWrapText(true);
            btn.setDisable(!unlocked);

            // Capture the choice for the lambda
            DialogueChoice captured = choice;
            btn.setOnAction(e -> handleChoiceSelected(captured));

            choicesContainer.getChildren().add(btn);
        }
    }

    // Called when the player clicks a choice.
    private void handleChoiceSelected(DialogueChoice choice) {
        stopTypingAnimation();

        // Apply the choice's effect on stats and momentum
        choice.applyEffect(stats, momentum);

        // Show the stranger's reaction in the dialogue box
        String reaction = choice.getReaction(momentum);

        // Update the expression badge based on choice type
        updateExpressionBadge(choice);

        // Update HUD
        updateHUD();

        // If it's a continue choice or has no reaction, advance immediately
        if (reaction.isEmpty()) {
            proceedToNextScene(choice.getNextSceneId());
            return;
        }

        // Shift subtle focus to Stranger while she reacts
        speakerLabel.setText("Stranger");
        setSpeakerFocus(false, true);
        choicesContainer.getChildren().clear();

        String reactionText = "✦ Stranger: \"" + reaction + "\"";
        String nextId = choice.getNextSceneId();
        String nextButtonText = "ENDING".equals(nextId) ? "See Ending Outcome ➔" : "Continue ➔";

        playTypewriter(reactionLabel, reactionText, () -> {
            Button nextBtn = new Button(nextButtonText);
            nextBtn.getStyleClass().add("choice-button");
            nextBtn.setMaxWidth(435);
            nextBtn.setOnAction(e -> proceedToNextScene(nextId));
            choicesContainer.getChildren().add(nextBtn);
        });
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
        if ("ENDING".equals(nextId)) {
            // S08 choice was made — transition to EndingView
            app.showEnding();
            return;
        }

        // After S05 choices, apply momentum branching to decide S06A or S06B
        if ("S05_DONE".equals(nextId)) {
            String branch = encounter.getMomentumBranch(momentum);
            encounter.advanceTo(branch);
        } else {
            encounter.advanceTo(nextId);
        }

        // Reload the view for the new scene
        loadScene();
    }

    // Updates the expression badge to reflect the stranger's current mood.
    private void updateExpressionBadge(DialogueChoice choice) {
        if (choice instanceof FlirtyChoice) {
            expressionBadge.setText(momentum.getValue() >= 2
                    ? "Stranger • Flustered (Soft Grin)"
                    : "Stranger • Caught Off Guard");
        } else if (choice instanceof SincereChoice) {
            expressionBadge.setText(momentum.getValue() >= 0
                    ? "Stranger • Smiling Warmly"
                    : "Stranger • Politely Nodding");
        } else if (choice instanceof SarcasticChoice) {
            expressionBadge.setText(momentum.getValue() >= 2
                    ? "Stranger • Laughing Playfully"
                    : "Stranger • Rolling Eyes");
        } else if (choice instanceof BluntChoice) {
            expressionBadge.setText("Stranger • Reserved / Quiet");
        } else if (choice instanceof ContinueChoice) {
            expressionBadge.setText("Stranger • Listening");
        }
    }

    // Updates the top HUD momentum display.
    private void updateHUD() {
        int m = momentum.getValue();
        String mSign = m > 0 ? "+" + m : String.valueOf(m);
        momentumBadge.setText("⚡ MOMENTUM: " + mSign + " (" + momentum.getMultiplier() + "x GAINS)");
    }
}
