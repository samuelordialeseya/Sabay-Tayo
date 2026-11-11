package com.sabaytayo;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

// Start screen: displays the bus background, character setup (name and pronouns), and start button.
public class StartView extends StackPane {

    private final GameApplication app;
    private final TextField nameField;
    private final ComboBox<String> playerPronounsBox;
    private final ComboBox<String> strangerPronounsBox;
    private final Label errorLabel;

    public StartView(GameApplication app) {
        this.app = app;

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

        // Center card with anime-styled dialogue box class
        VBox card = new VBox(14);
        card.setMaxWidth(520);
        card.setMaxHeight(Region.USE_PREF_SIZE);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(28, 36, 28, 36));
        card.getStyleClass().add("dialogue-box");

        // Game title & subtitle (Branded typography)
        Label titleLabel = new Label("Sabay Tayo?");
        titleLabel.getStyleClass().add("title-header");

        Label subtitleLabel = new Label("a branching bus-ride dialogue game");
        subtitleLabel.getStyleClass().add("sub-header");

        // Player Name Input
        Label nameLabel = new Label("Your Name:");
        nameLabel.getStyleClass().add("field-label");

        nameField = new TextField();
        nameField.setPromptText("Enter your name (e.g. Sam)...");
        nameField.setMaxWidth(360);
        nameField.getStyleClass().add("input-field");

        // Pronoun selections
        Label playerPronounLabel = new Label("Your Pronouns:");
        playerPronounLabel.getStyleClass().add("field-label");

        playerPronounsBox = new ComboBox<>();
        playerPronounsBox.getItems().addAll("They/Them", "He/Him", "She/Her");
        playerPronounsBox.setValue("They/Them");

        Label strangerPronounLabel = new Label("Stranger's Pronouns:");
        strangerPronounLabel.getStyleClass().add("field-label");

        strangerPronounsBox = new ComboBox<>();
        strangerPronounsBox.getItems().addAll("She/Her", "He/Him", "They/Them");
        strangerPronounsBox.setValue("She/Her");

        HBox pronounsRow = new HBox(20,
            new VBox(4, playerPronounLabel, playerPronounsBox),
            new VBox(4, strangerPronounLabel, strangerPronounsBox)
        );
        pronounsRow.setAlignment(Pos.CENTER);

        errorLabel = new Label("");
        errorLabel.getStyleClass().add("error-text");

        // Start Button
        Button startButton = new Button("Board the Bus");
        startButton.getStyleClass().add("choice-button");
        startButton.setPrefWidth(240);
        startButton.setAlignment(Pos.CENTER);
        startButton.setOnAction(e -> handleStart());

        card.getChildren().addAll(
            titleLabel,
            subtitleLabel,
            nameLabel,
            nameField,
            pronounsRow,
            errorLabel,
            startButton
        );

        getChildren().addAll(bgView, card);
    }

    private void handleStart() {
        String enteredName = nameField.getText().trim();
        if (enteredName.isEmpty()) {
            errorLabel.setText("Please enter your name before boarding.");
            return;
        }
        if (enteredName.length() > 20) {
            errorLabel.setText("Name cannot be longer than 20 characters.");
            return;
        }

        app.setPlayerName(enteredName);
        app.setPlayerPronouns(playerPronounsBox.getValue());
        app.setStrangerPronouns(strangerPronounsBox.getValue());
        app.showEncounter();
    }
}
