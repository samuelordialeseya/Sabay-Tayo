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

// Visual Novel Encounter screen: inspired by Doki Doki Literature Club & Persona 5.
public class EncounterView extends StackPane {

    private final GameApplication app;
    private final RelationshipStats stats;
    private final Momentum momentum;

    private final Label speakerLabel;
    private final Label dialogueLabel;
    private final Label reactionLabel;
    private final Label momentumBadge;
    private final Label expressionBadge;
    private final VBox choicesContainer;

    public EncounterView(GameApplication app, RelationshipStats stats, Momentum momentum) {
        this.app = app;
        this.stats = stats;
        this.momentum = momentum;
        setPrefSize(1280, 720);

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

        // 2. Top HUD Bar (Persona 5 style Momentum tracker)
        HBox topBar = new HBox(14);
        topBar.setPadding(new Insets(16, 28, 0, 28));
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setMaxHeight(Region.USE_PREF_SIZE);

        momentumBadge = new Label("⚡ MOMENTUM: 0 (1.0x GAIN)");
        momentumBadge.getStyleClass().add("p5-hud-badge");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button viewEndingButton = new Button("End Ride & See Outcome ➔");
        viewEndingButton.getStyleClass().add("choice-button");
        viewEndingButton.setStyle("-fx-font-size: 13px; -fx-padding: 6px 14px; -fx-background-radius: 12px;");
        viewEndingButton.setOnAction(e -> app.showEnding());

        topBar.getChildren().addAll(momentumBadge, spacer, viewEndingButton);

        // 3. Character Sprite Layer (placed in the passenger seat area)
        VBox spriteContainer = new VBox(8);
        spriteContainer.setAlignment(Pos.BOTTOM_CENTER);
        spriteContainer.setMaxWidth(340);
        spriteContainer.setMaxHeight(Region.USE_PREF_SIZE);
        spriteContainer.setPadding(new Insets(0, 0, 160, 0)); // Sits directly above bottom dialogue box

        expressionBadge = new Label("Seatmate • Listening");
        expressionBadge.getStyleClass().add("sprite-frame");
        expressionBadge.setStyle("-fx-text-fill: #ff94c2; -fx-font-size: 13px; -fx-font-weight: bold;");

        spriteContainer.getChildren().add(expressionBadge);

        // 4. Choices Container (Upper screen, Persona 5 response cards)
        choicesContainer = new VBox(8);
        choicesContainer.setAlignment(Pos.CENTER);
        choicesContainer.setMaxWidth(620);
        choicesContainer.setMaxHeight(Region.USE_PREF_SIZE);

        // 5. DDLC Anime Dialogue Box (Pinned at bottom)
        VBox dialogueCard = new VBox(0);
        dialogueCard.setMaxWidth(1100);
        dialogueCard.setMaxHeight(Region.USE_PREF_SIZE);

        // Speaker Nameplate perched on top of dialogue box
        speakerLabel = new Label("Seatmate");
        speakerLabel.getStyleClass().add("speaker-label");

        HBox nameplate = new HBox(speakerLabel);
        nameplate.getStyleClass().add("speaker-tag");
        nameplate.setMaxWidth(Region.USE_PREF_SIZE);

        // Main text box
        VBox textCard = new VBox(8);
        textCard.getStyleClass().add("dialogue-box");
        textCard.setPadding(new Insets(16, 26, 12, 26));

        dialogueLabel = new Label("Pasensya ka na ah... medyo masikip dito sa tabi ko, okay ka lang ba?");
        dialogueLabel.getStyleClass().add("dialogue-text");
        dialogueLabel.setWrapText(true);

        reactionLabel = new Label("✦ Tip: Choose how you want to respond above.");
        reactionLabel.setStyle("-fx-text-fill: #ffd166; -fx-font-style: italic; -fx-font-size: 14px;");
        reactionLabel.setWrapText(true);

        // DDLC Visual Novel sub-bar (matching page 9 mockup)
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

        // Load turn choices
        loadChoices();

        // Assemble layering: background -> sprite -> dialogue box -> choices -> top HUD
        getChildren().addAll(bgView, spriteContainer, dialogueCard, choicesContainer, topBar);

        StackPane.setAlignment(topBar, Pos.TOP_CENTER);
        StackPane.setAlignment(choicesContainer, Pos.TOP_CENTER);
        StackPane.setMargin(choicesContainer, new Insets(68, 0, 0, 0));

        StackPane.setAlignment(spriteContainer, Pos.CENTER);
        StackPane.setMargin(spriteContainer, new Insets(80, 0, 0, 240)); // Positioned toward the right seat

        StackPane.setAlignment(dialogueCard, Pos.BOTTOM_CENTER);
        StackPane.setMargin(dialogueCard, new Insets(0, 90, 16, 90));
    }

    private void loadChoices() {
        choicesContainer.getChildren().clear();

        List<DialogueChoice> currentChoices = new ArrayList<>();
        currentChoices.add(new SincereChoice(
            "[Sincere] \"Oo naman, ok lang ako. Masikip talaga rito.\"",
            "scene_turn2A",
            "They give you a warm, relieved smile.",
            "They nod politely."
        ));
        currentChoices.add(new FlirtyChoice(
            "[Flirty] \"Ok lang naman, mas gusto ko nga 'tong tabi mo.\"",
            "scene_turn2A",
            "They blush slightly and look away with a soft grin.",
            "They look caught off guard and shift slightly."
        ));
        currentChoices.add(new SarcasticChoice(
            "[Sarcastic] \"Sanay na, parang MRT pag uwian.\"",
            "scene_turn2B",
            "They let out a genuine chuckle at your joke.",
            "They sigh and stare quietly at the floor."
        ));
        currentChoices.add(new BluntChoice(
            "[Blunt] \"Medyo masikip nga.\"",
            "scene_turn2C",
            "They nod, appreciating the straightforward answer.",
            "They frown slightly and shift closer to the window."
        ));

        for (DialogueChoice choice : currentChoices) {
            boolean unlocked = choice.isUnlocked(momentum);
            String buttonText = unlocked
                ? choice.getText()
                : choice.getText() + "  [🔒 Locked: Momentum >= " + choice.getMinMomentum() + "]";

            Button btn = new Button(buttonText);
            btn.getStyleClass().add("choice-button");
            btn.setMaxWidth(620);
            btn.setWrapText(true);
            btn.setDisable(!unlocked);

            btn.setOnAction(e -> handleChoiceSelected(choice));
            choicesContainer.getChildren().add(btn);
        }

        updateHUD();
    }

    private void handleChoiceSelected(DialogueChoice choice) {
        // Polymorphic call
        choice.applyEffect(stats, momentum);
        String reaction = choice.getReaction(momentum);

        reactionLabel.setText("✦ Seatmate: \"" + reaction + "\"");

        // Dynamically update character expression badge to reflect reaction
        if (choice instanceof FlirtyChoice) {
            expressionBadge.setText("Seatmate • Blushing (Soft Grin)");
        } else if (choice instanceof SincereChoice) {
            expressionBadge.setText("Seatmate • Smiling Warmly");
        } else if (choice instanceof SarcasticChoice) {
            expressionBadge.setText(momentum.getValue() >= 2 ? "Seatmate • Laughing Playfully" : "Seatmate • Rolling Eyes");
        } else if (choice instanceof BluntChoice) {
            expressionBadge.setText("Seatmate • Reserved / Quiet");
        }

        updateHUD();
        loadChoices();
    }

    private void updateHUD() {
        int m = momentum.getValue();
        String mSign = m > 0 ? "+" + m : String.valueOf(m);
        momentumBadge.setText("⚡ MOMENTUM: " + mSign + " (" + momentum.getMultiplier() + "x GAINS)");
    }
}
