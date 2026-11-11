package com.sabaytayo;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

// Opening game menu & character setup view: inspired by visual novel main menus (DDLC & Persona 5).
public class StartView extends StackPane {

    private final GameApplication app;
    private final VBox card;

    // Persisted setup inputs
    private final TextField nameField;
    private final ComboBox<String> playerPronounsBox;
    private final ComboBox<String> strangerPronounsBox;
    private final Label errorLabel;

    public StartView(GameApplication app) {
        this.app = app;

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

        // 2. Central interactive card
        card = new VBox();
        card.setMaxWidth(520);
        card.setMaxHeight(Region.USE_PREF_SIZE);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(32, 40, 32, 40));
        card.getStyleClass().add("dialogue-box");

        // Initialize reusable input components
        nameField = new TextField();
        nameField.setPromptText("Enter your name (e.g. Sam)...");
        nameField.setMaxWidth(340);
        nameField.getStyleClass().add("input-field");

        playerPronounsBox = new ComboBox<>();
        playerPronounsBox.getItems().addAll("They/Them", "He/Him", "She/Her");
        playerPronounsBox.setValue("They/Them");

        strangerPronounsBox = new ComboBox<>();
        strangerPronounsBox.getItems().addAll("She/Her", "He/Him", "They/Them");
        strangerPronounsBox.setValue("She/Her");

        errorLabel = new Label("");
        errorLabel.getStyleClass().add("error-text");

        // Display the opening main menu by default
        showMainMenu();

        getChildren().addAll(bgView, card);
        StackPane.setAlignment(card, Pos.CENTER);
    }

    // 1. Clean Opening Game Menu (Title, Start Game, Settings, Exit)
    public void showMainMenu() {
        card.getChildren().clear();
        card.setSpacing(14);

        Label titleLabel = new Label("Sabay Tayo?");
        titleLabel.getStyleClass().add("title-header");

        Label subtitleLabel = new Label("a branching bus-ride dialogue game");
        subtitleLabel.getStyleClass().add("sub-header");

        Region spacer = new Region();
        spacer.setPrefHeight(16);

        Button startButton = new Button("Start Game");
        startButton.getStyleClass().add("menu-button");
        startButton.setPrefWidth(280);
        startButton.setOnAction(e -> showCharacterSetup());

        Button settingsButton = new Button("Settings");
        settingsButton.getStyleClass().add("menu-button");
        settingsButton.setPrefWidth(280);
        settingsButton.setOnAction(e -> showSettings());

        Button exitButton = new Button("Exit");
        exitButton.getStyleClass().add("menu-button");
        exitButton.setPrefWidth(280);
        exitButton.setOnAction(e -> Platform.exit());

        card.getChildren().addAll(
            titleLabel,
            subtitleLabel,
            spacer,
            startButton,
            settingsButton,
            exitButton
        );
    }

    // 2. Character Setup Screen (Entered after clicking Start Game)
    public void showCharacterSetup() {
        card.getChildren().clear();
        card.setSpacing(12);

        Label titleLabel = new Label("Boarding Pass");
        titleLabel.getStyleClass().add("title-header");

        Label subtitleLabel = new Label("Setup your character before boarding the bus");
        subtitleLabel.getStyleClass().add("sub-header");

        Label nameLabel = new Label("Your Name:");
        nameLabel.getStyleClass().add("field-label");

        Label playerPronounLabel = new Label("Your Pronouns:");
        playerPronounLabel.getStyleClass().add("field-label");

        Label strangerPronounLabel = new Label("Stranger's Pronouns:");
        strangerPronounLabel.getStyleClass().add("field-label");

        HBox pronounsRow = new HBox(16,
            new VBox(4, playerPronounLabel, playerPronounsBox),
            new VBox(4, strangerPronounLabel, strangerPronounsBox)
        );
        pronounsRow.setAlignment(Pos.CENTER);

        HBox buttonRow = new HBox(14);
        buttonRow.setAlignment(Pos.CENTER);
        buttonRow.setPadding(new Insets(10, 0, 0, 0));

        Button backButton = new Button("← Back");
        backButton.getStyleClass().add("choice-button");
        backButton.setPrefWidth(120);
        backButton.setAlignment(Pos.CENTER);
        backButton.setOnAction(e -> showMainMenu());

        Button boardButton = new Button("Board the Bus ➔");
        boardButton.getStyleClass().add("choice-button");
        boardButton.setPrefWidth(200);
        boardButton.setAlignment(Pos.CENTER);
        boardButton.setOnAction(e -> handleBoarding());

        buttonRow.getChildren().addAll(backButton, boardButton);

        card.getChildren().addAll(
            titleLabel,
            subtitleLabel,
            nameLabel,
            nameField,
            pronounsRow,
            errorLabel,
            buttonRow
        );
    }

    // 3. Settings Screen (Audio & Visual Novel Preferences)
    public void showSettings() {
        card.getChildren().clear();
        card.setSpacing(14);

        Label titleLabel = new Label("Settings");
        titleLabel.getStyleClass().add("title-header");

        Label subtitleLabel = new Label("Preferences & Audio");
        subtitleLabel.getStyleClass().add("sub-header");

        // Master BGM Volume Slider
        Label volumeLabel = new Label("BGM Volume:");
        volumeLabel.getStyleClass().add("field-label");

        Slider volumeSlider = new Slider(0, 100, 80);
        volumeSlider.setMaxWidth(260);

        Label volumeValueLabel = new Label("80%");
        volumeValueLabel.getStyleClass().add("stat-value");
        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            volumeValueLabel.setText(newVal.intValue() + "%");
        });

        HBox volumeRow = new HBox(12, volumeSlider, volumeValueLabel);
        volumeRow.setAlignment(Pos.CENTER);

        // Text Speed Preference
        Label textSpeedLabel = new Label("Text Speed:");
        textSpeedLabel.getStyleClass().add("field-label");

        ComboBox<String> textSpeedBox = new ComboBox<>();
        textSpeedBox.getItems().addAll("Normal", "Fast", "Instant");
        textSpeedBox.setValue("Normal");

        HBox speedRow = new HBox(12, textSpeedLabel, textSpeedBox);
        speedRow.setAlignment(Pos.CENTER);

        // Display Mode
        Label displayLabel = new Label("Display: Windowed (1280 x 720)");
        displayLabel.getStyleClass().add("sub-header");

        Region spacer = new Region();
        spacer.setPrefHeight(10);

        Button backButton = new Button("← Back to Menu");
        backButton.getStyleClass().add("menu-button");
        backButton.setPrefWidth(240);
        backButton.setOnAction(e -> showMainMenu());

        card.getChildren().addAll(
            titleLabel,
            subtitleLabel,
            volumeLabel,
            volumeRow,
            speedRow,
            displayLabel,
            spacer,
            backButton
        );
    }

    private void handleBoarding() {
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
