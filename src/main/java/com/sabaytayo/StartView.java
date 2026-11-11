package com.sabaytayo;

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
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

// Opening game menu view: title directly on top, two buttons below, free-floating without a container box.
public class StartView extends StackPane {

    private final GameApplication app;
    private final StackPane contentLayer;

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

        // 2. Dynamic content layer
        contentLayer = new StackPane();

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

        // Display the opening menu (Title up top, two buttons below)
        showMainMenu();

        getChildren().addAll(bgView, contentLayer);
    }

    // 1. Free-floating Opening Screen: Title up top, two buttons below it (No enclosing box)
    public void showMainMenu() {
        contentLayer.getChildren().clear();

        // Title up top
        VBox titleBox = new VBox(8);
        titleBox.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Sabay Tayo?");
        titleLabel.getStyleClass().add("main-title");

        Label subtitleLabel = new Label("a branching bus-ride dialogue game");
        subtitleLabel.getStyleClass().add("main-subtitle");

        titleBox.getChildren().addAll(titleLabel, subtitleLabel);

        // Two buttons directly below it
        VBox buttonsBox = new VBox(16);
        buttonsBox.setAlignment(Pos.CENTER);

        Button startButton = new Button("Start Game");
        startButton.getStyleClass().add("menu-button");
        startButton.setPrefWidth(260);
        startButton.setOnAction(e -> showCharacterSetup());

        Button settingsButton = new Button("Settings");
        settingsButton.getStyleClass().add("menu-button");
        settingsButton.setPrefWidth(260);
        settingsButton.setOnAction(e -> showSettings());

        buttonsBox.getChildren().addAll(startButton, settingsButton);

        // Vertical layout: Title up top, two buttons below
        VBox menuLayout = new VBox(42, titleBox, buttonsBox);
        menuLayout.setAlignment(Pos.CENTER);

        contentLayer.getChildren().add(menuLayout);
        StackPane.setAlignment(menuLayout, Pos.CENTER);
    }

    // 2. Character Setup (Shown after clicking Start Game)
    public void showCharacterSetup() {
        contentLayer.getChildren().clear();

        VBox card = new VBox(12);
        card.setMaxWidth(520);
        card.setMaxHeight(Region.USE_PREF_SIZE);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(30, 40, 30, 40));
        card.getStyleClass().add("dialogue-box");

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

        contentLayer.getChildren().add(card);
        StackPane.setAlignment(card, Pos.CENTER);
    }

    // 3. Settings Screen (Shown after clicking Settings)
    public void showSettings() {
        contentLayer.getChildren().clear();

        VBox card = new VBox(14);
        card.setMaxWidth(520);
        card.setMaxHeight(Region.USE_PREF_SIZE);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(30, 40, 30, 40));
        card.getStyleClass().add("dialogue-box");

        Label titleLabel = new Label("Settings");
        titleLabel.getStyleClass().add("title-header");

        Label subtitleLabel = new Label("Preferences & Audio");
        subtitleLabel.getStyleClass().add("sub-header");

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

        Label textSpeedLabel = new Label("Text Speed:");
        textSpeedLabel.getStyleClass().add("field-label");

        ComboBox<String> textSpeedBox = new ComboBox<>();
        textSpeedBox.getItems().addAll("Normal", "Fast", "Instant");
        textSpeedBox.setValue("Normal");

        HBox speedRow = new HBox(12, textSpeedLabel, textSpeedBox);
        speedRow.setAlignment(Pos.CENTER);

        Label displayLabel = new Label("Display: Windowed (1280 x 720)");
        displayLabel.getStyleClass().add("sub-header");

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
            backButton
        );

        contentLayer.getChildren().add(card);
        StackPane.setAlignment(card, Pos.CENTER);
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
