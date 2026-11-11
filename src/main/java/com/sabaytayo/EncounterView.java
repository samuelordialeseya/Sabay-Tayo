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

import java.util.ArrayList;
import java.util.List;

// Encounter screen: displays the bus encounter dialogue, choice options, and reacts to player input.
public class EncounterView extends StackPane {

    private final GameApplication app;
    private final RelationshipStats stats;
    private final Momentum momentum;

    private final Label speakerLabel;
    private final Label dialogueLabel;
    private final Label reactionLabel;
    private final Label momentumBadge;
    private final VBox choicesContainer;

    public EncounterView(GameApplication app, RelationshipStats stats, Momentum momentum) {
        this.app = app;
        this.stats = stats;
        this.momentum = momentum;
        setPrefSize(1280, 720);

        // Background image
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

        // Top HUD bar for quick status & skip-to-ending
        HBox topBar = new HBox(12);
        topBar.setPadding(new Insets(16, 24, 0, 24));
        topBar.setAlignment(Pos.CENTER_LEFT);

        momentumBadge = new Label("Momentum: 0");
        momentumBadge.setStyle("-fx-background-color: rgba(30, 15, 40, 0.85); -fx-text-fill: #ff94c2; "
                + "-fx-font-weight: bold; -fx-padding: 6px 14px; -fx-background-radius: 12px; "
                + "-fx-border-color: #d1568d; -fx-border-radius: 12px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button viewEndingButton = new Button("End Ride & See Outcome ➔");
        viewEndingButton.setStyle("-fx-background-color: rgba(30, 15, 40, 0.85); -fx-text-fill: #f5e9f2; "
                + "-fx-padding: 6px 14px; -fx-background-radius: 12px; -fx-border-color: #d1568d; -fx-border-radius: 12px; -fx-cursor: hand;");
        viewEndingButton.setOnAction(e -> app.showEnding());

        topBar.getChildren().addAll(momentumBadge, spacer, viewEndingButton);

        // Bottom Dialogue & Choices Container
        VBox bottomContainer = new VBox(10);
        bottomContainer.setAlignment(Pos.BOTTOM_CENTER);
        bottomContainer.setPadding(new Insets(0, 120, 30, 120));

        // Choices Box
        choicesContainer = new VBox(8);
        choicesContainer.setAlignment(Pos.CENTER);
        choicesContainer.setMaxWidth(800);

        // Dialogue Box
        VBox dialogueBox = new VBox(6);
        dialogueBox.getStyleClass().add("dialogue-box");
        dialogueBox.setPadding(new Insets(18, 24, 18, 24));
        dialogueBox.setMaxWidth(1000);

        speakerLabel = new Label("Seatmate");
        speakerLabel.getStyleClass().add("speaker-label");

        dialogueLabel = new Label("Pasensya ka na ah... medyo masikip dito sa tabi ko, okay ka lang ba?");
        dialogueLabel.getStyleClass().add("dialogue-text");
        dialogueLabel.setWrapText(true);

        reactionLabel = new Label("");
        reactionLabel.setStyle("-fx-text-fill: #ffd166; -fx-font-style: italic; -fx-font-size: 14px;");
        reactionLabel.setWrapText(true);

        dialogueBox.getChildren().addAll(speakerLabel, dialogueLabel, reactionLabel);

        bottomContainer.getChildren().addAll(choicesContainer, dialogueBox);

        // Load turn 1 choices
        loadChoices();

        getChildren().addAll(bgView, topBar, bottomContainer);
        StackPane.setAlignment(topBar, Pos.TOP_CENTER);
        StackPane.setAlignment(bottomContainer, Pos.BOTTOM_CENTER);
    }

    private void loadChoices() {
        choicesContainer.getChildren().clear();

        // Sample choices demonstrating our DialogueChoice subclasses
        List<DialogueChoice> currentChoices = new ArrayList<>();
        currentChoices.add(new SincereChoice(
            "[Sincere] Oo naman, ok lang ako. Masikip talaga rito.",
            "scene_turn2A",
            "They give you a warm, relieved smile.",
            "They nod politely."
        ));
        currentChoices.add(new FlirtyChoice(
            "[Flirty] Ok lang naman, mas gusto ko nga 'tong tabi mo.",
            "scene_turn2A",
            "They blush slightly and look away with a soft grin.",
            "They look caught off guard and stiffen slightly."
        ));
        currentChoices.add(new SarcasticChoice(
            "[Sarcastic] Sanay na, parang MRT pag uwian.",
            "scene_turn2B",
            "They let out a genuine chuckle at your joke.",
            "They sigh and stare at their shoes."
        ));
        currentChoices.add(new BluntChoice(
            "[Blunt] Medyo masikip nga.",
            "scene_turn2C",
            "They nod quietly, appreciating the straightforward answer.",
            "They frown slightly and shift closer to the window."
        ));

        for (DialogueChoice choice : currentChoices) {
            boolean unlocked = choice.isUnlocked(momentum);
            String buttonText = unlocked ? choice.getText() : choice.getText() + " (Locked: Momentum >= " + choice.getMinMomentum() + ")";

            Button btn = new Button(buttonText);
            btn.getStyleClass().add("choice-button");
            btn.setMaxWidth(800);
            btn.setDisable(!unlocked);

            btn.setOnAction(e -> handleChoiceSelected(choice));
            choicesContainer.getChildren().add(btn);
        }

        updateHUD();
    }

    private void handleChoiceSelected(DialogueChoice choice) {
        // Polymorphic call: applies choice effect and gets the reaction
        choice.applyEffect(stats, momentum);
        String reaction = choice.getReaction(momentum);

        reactionLabel.setText("Seatmate's reaction: \"" + reaction + "\"");
        updateHUD();

        // Re-render choices to reflect updated unlock states (e.g. Flirty unlocking)
        loadChoices();
    }

    private void updateHUD() {
        int m = momentum.getValue();
        String mSign = m > 0 ? "+" + m : String.valueOf(m);
        momentumBadge.setText("Momentum: " + mSign + " (" + momentum.getMultiplier() + "x gains)");
    }
}
