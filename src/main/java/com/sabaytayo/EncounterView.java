package com.sabaytayo;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.List;

// Visual Novel Encounter screen: drives the player through the 12-scene dialogue graph.
public class EncounterView extends StackPane {

    private final GameApplication app;
    private final RelationshipStats stats;
    private final Momentum momentum;
    private final Encounter encounter;

    private final Label speakerLabel;
    private final Label dialogueLabel;
    private final Label reactionLabel;
    private final Label momentumBadge;
    private final Label expressionBadge;
    private final VBox choicesContainer;

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

        momentumBadge = new Label("⚡ MOMENTUM: 0 (1.0x GAIN)");
        momentumBadge.getStyleClass().add("p5-hud-badge");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button skipButton = new Button("Skip to Ending ➔");
        skipButton.getStyleClass().add("choice-button");
        skipButton.setOnAction(e -> app.showEnding());

        topBar.getChildren().addAll(momentumBadge, spacer, skipButton);

        // 3. Character Sprite / Expression Badge
        VBox spriteContainer = new VBox(8);
        spriteContainer.setAlignment(Pos.BOTTOM_CENTER);
        spriteContainer.setMaxWidth(340);
        spriteContainer.setMaxHeight(Region.USE_PREF_SIZE);
        spriteContainer.setPadding(new Insets(0, 0, 160, 0));

        expressionBadge = new Label("Stranger • Listening");
        expressionBadge.getStyleClass().add("sprite-frame");
        spriteContainer.getChildren().add(expressionBadge);

        // 4. Choices Container
        choicesContainer = new VBox(8);
        choicesContainer.setAlignment(Pos.CENTER);
        choicesContainer.setMaxWidth(620);
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

        // Assemble layers
        getChildren().addAll(bgView, spriteContainer, dialogueCard, choicesContainer, topBar);

        StackPane.setAlignment(topBar, Pos.TOP_CENTER);
        StackPane.setAlignment(choicesContainer, Pos.TOP_CENTER);
        StackPane.setMargin(choicesContainer, new Insets(68, 0, 0, 0));
        StackPane.setAlignment(spriteContainer, Pos.CENTER);
        StackPane.setMargin(spriteContainer, new Insets(80, 0, 0, 240));
        StackPane.setAlignment(dialogueCard, Pos.BOTTOM_CENTER);
        StackPane.setMargin(dialogueCard, new Insets(0, 90, 16, 90));

        // Load the first scene
        loadScene();
    }

    // Reads the current scene from Encounter and displays its dialogue + choices.
    private void loadScene() {
        Scene current = encounter.getCurrentScene();
        if (current == null) return;

        // Show all dialogue lines joined together
        List<String> lines = current.getDialogueLines();
        if (!lines.isEmpty()) {
            dialogueLabel.setText(String.join("\n\n", lines));
        } else {
            dialogueLabel.setText("...");
        }

        // Clear reaction text when entering a new scene
        reactionLabel.setText("");

        // Update speaker label based on scene content
        speakerLabel.setText("Stranger");

        // Render choices
        renderChoices(current);

        // Update HUD
        updateHUD();
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
            btn.setMaxWidth(620);
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

        // Otherwise show the reaction and a Continue button so the player can read it
        reactionLabel.setText("✦ Stranger: \"" + reaction + "\"");
        choicesContainer.getChildren().clear();

        String nextId = choice.getNextSceneId();
        String nextButtonText = "ENDING".equals(nextId) ? "See Ending Outcome ➔" : "Continue ➔";
        Button nextBtn = new Button(nextButtonText);
        nextBtn.getStyleClass().add("choice-button");
        nextBtn.setMaxWidth(620);
        nextBtn.setOnAction(e -> proceedToNextScene(nextId));
        choicesContainer.getChildren().add(nextBtn);
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
