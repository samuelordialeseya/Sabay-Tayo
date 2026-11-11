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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.File;

// Start Menu view implementing the visual novel commute sim opening layout.
public class StartView extends StackPane {

    private final GameApplication app;
    private final StackPane contentLayer;

    // Palette constants from the specification
    private static final String BTN_IDLE_STYLE =
        "-fx-background-color: rgba(46, 38, 64, 0.88);" +
        "-fx-text-fill: #f5f0fa;" +
        "-fx-font-family: 'SansSerif';" +
        "-fx-font-size: 15px;" +
        "-fx-font-weight: bold;" +
        "-fx-border-color: #f2a6bd;" +
        "-fx-border-width: 2px;" +
        "-fx-border-radius: 6px;" +
        "-fx-background-radius: 6px;" +
        "-fx-cursor: hand;";

    private static final String BTN_HOVER_STYLE =
        "-fx-background-color: rgba(64, 52, 86, 0.95);" +
        "-fx-text-fill: #f4d38a;" +
        "-fx-font-family: 'SansSerif';" +
        "-fx-font-size: 15px;" +
        "-fx-font-weight: bold;" +
        "-fx-border-color: #f4d38a;" +
        "-fx-border-width: 2px;" +
        "-fx-border-radius: 6px;" +
        "-fx-background-radius: 6px;" +
        "-fx-cursor: hand;";

    // Persisted setup inputs
    private final TextField nameField;
    private final ComboBox<String> playerPronounsBox;
    private final ComboBox<String> strangerPronounsBox;
    private final Label errorLabel;

    @SuppressWarnings("this-escape")
    public StartView(GameApplication app) {
        this.app = app;

        // 1. Background Image Layer
        ImageView backgroundView = new ImageView();
        try {
            File bgFile = new File("start_menu_bg.png");
            if (bgFile.exists()) {
                backgroundView.setImage(new Image(bgFile.toURI().toString()));
            } else {
                // Fallback to existing bus backdrop
                Image bg = new Image(getClass().getResourceAsStream("/com/sabaytayo/images/bg.jpg"));
                backgroundView.setImage(bg);
            }
        } catch (Exception e) {
            System.err.println("Could not load background: " + e.getMessage());
        }
        backgroundView.setPreserveRatio(false);

        // Bind background size to window size
        backgroundView.fitWidthProperty().bind(widthProperty());
        backgroundView.fitHeightProperty().bind(heightProperty());

        // Ambient dark overlay
        Region darkOverlay = new Region();
        darkOverlay.setStyle("-fx-background-color: rgba(30, 24, 42, 0.60);");

        // 2. Foreground content layer
        contentLayer = new StackPane();

        // Initialize reusable input components
        nameField = new TextField();
        nameField.setPromptText("Enter your character's name...");
        nameField.setMaxWidth(340);
        nameField.setStyle(
            "-fx-background-color: rgba(32, 26, 44, 0.95);" +
            "-fx-text-fill: #ffffff;" +
            "-fx-font-size: 14px;" +
            "-fx-padding: 8px 12px;" +
            "-fx-background-radius: 6px;" +
            "-fx-border-color: #f2a6bd;" +
            "-fx-border-radius: 6px;" +
            "-fx-border-width: 1.5px;"
        );

        playerPronounsBox = new ComboBox<>();
        playerPronounsBox.getItems().addAll("They/Them", "He/Him", "She/Her");
        playerPronounsBox.setValue("They/Them");

        strangerPronounsBox = new ComboBox<>();
        strangerPronounsBox.getItems().addAll("She/Her", "He/Him", "They/Them");
        strangerPronounsBox.setValue("She/Her");

        errorLabel = new Label("");
        errorLabel.setStyle("-fx-text-fill: #e57373; -fx-font-size: 13px; -fx-font-weight: bold;");

        // Show opening main menu
        showMainMenu();

        getChildren().addAll(backgroundView, darkOverlay, contentLayer);
    }

    // 1. Opening Menu: Logo/Title, Vertical Menu Buttons, and Bottom Route Bar
    public void showMainMenu() {
        contentLayer.getChildren().clear();

        BorderPane uiLayout = new BorderPane();
        uiLayout.setPadding(new Insets(30, 30, 20, 30));

        // Top: Logo / Title Display
        VBox topBox = new VBox(8);
        topBox.setAlignment(Pos.CENTER);
        topBox.setPadding(new Insets(20, 0, 10, 0));

        boolean logoLoaded = false;
        try {
            File logoFile = new File("logo.png");
            if (logoFile.exists()) {
                ImageView logoView = new ImageView(new Image(logoFile.toURI().toString()));
                logoView.setFitWidth(380);
                logoView.setPreserveRatio(true);
                topBox.getChildren().add(logoView);
                logoLoaded = true;
            }
        } catch (Exception e) {
            System.err.println("Logo load error: " + e.getMessage());
        }

        if (!logoLoaded) {
            Label titleFallback = new Label("SABAY TAYO");
            titleFallback.setStyle("-fx-text-fill: #f4d38a; -fx-font-size: 42px; -fx-font-weight: bold; -fx-font-family: 'SansSerif';");

            Label subtitleFallback = new Label("— A BUS COMMUTE SIMULATION —");
            subtitleFallback.setStyle("-fx-text-fill: #f5f0fa; -fx-font-size: 13px; -fx-font-family: 'Monospaced'; -fx-letter-spacing: 2px;");

            topBox.getChildren().addAll(titleFallback, subtitleFallback);
        }
        uiLayout.setTop(topBox);

        // Center: Vertical Menu Buttons
        VBox menuBox = new VBox(12);
        menuBox.setAlignment(Pos.CENTER);

        Button btnStart    = createMenuButton("START COMMUTE");
        Button btnContinue = createMenuButton("CONTINUE");
        Button btnGallery  = createMenuButton("GALLERY");
        Button btnSettings = createMenuButton("SETTINGS");
        Button btnExit     = createMenuButton("EXIT");

        btnStart.setOnAction(e -> showCharacterSetup());
        btnSettings.setOnAction(e -> showSettings());
        btnExit.setOnAction(e -> Platform.exit());

        menuBox.getChildren().addAll(btnStart, btnContinue, btnGallery, btnSettings, btnExit);
        uiLayout.setCenter(menuBox);

        // Bottom: Route & Version Bar
        HBox bottomBar = new HBox();
        bottomBar.setAlignment(Pos.CENTER_LEFT);

        Label routeTag = new Label("ROUTE: BUENDIA ➔ ALABANG (SLEX EXPRESS)");
        routeTag.setStyle("-fx-text-fill: #f4d38a; -fx-font-family: 'Monospaced'; -fx-font-size: 13px; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label versionTag = new Label("v1.0.0 | OOP Final Project");
        versionTag.setStyle("-fx-text-fill: #f5f0fa; -fx-font-family: 'Monospaced'; -fx-font-size: 12px;");

        bottomBar.getChildren().addAll(routeTag, spacer, versionTag);
        uiLayout.setBottom(bottomBar);

        contentLayer.getChildren().add(uiLayout);
    }

    // 2. Character Setup Screen
    public void showCharacterSetup() {
        contentLayer.getChildren().clear();

        VBox card = new VBox(14);
        card.setMaxWidth(520);
        card.setMaxHeight(Region.USE_PREF_SIZE);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(28, 36, 28, 36));
        card.setStyle(
            "-fx-background-color: rgba(36, 30, 50, 0.94);" +
            "-fx-border-color: #f2a6bd;" +
            "-fx-border-width: 2px;" +
            "-fx-border-radius: 8px;" +
            "-fx-background-radius: 8px;" +
            "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.7), 16, 0, 0, 4);"
        );

        Label titleLabel = new Label("PASSENGER SETUP");
        titleLabel.setStyle("-fx-text-fill: #f4d38a; -fx-font-size: 24px; -fx-font-weight: bold; -fx-font-family: 'SansSerif';");

        Label subtitleLabel = new Label("Set up your character before boarding");
        subtitleLabel.setStyle("-fx-text-fill: #f5f0fa; -fx-font-size: 13px; -fx-font-style: italic;");

        Label nameLabel = new Label("Your Name:");
        nameLabel.setStyle("-fx-text-fill: #f5f0fa; -fx-font-size: 13px; -fx-font-weight: bold;");

        Label playerPronounLabel = new Label("Your Pronouns:");
        playerPronounLabel.setStyle("-fx-text-fill: #f5f0fa; -fx-font-size: 13px; -fx-font-weight: bold;");

        Label strangerPronounLabel = new Label("Stranger's Pronouns:");
        strangerPronounLabel.setStyle("-fx-text-fill: #f5f0fa; -fx-font-size: 13px; -fx-font-weight: bold;");

        HBox pronounsRow = new HBox(16,
            new VBox(4, playerPronounLabel, playerPronounsBox),
            new VBox(4, strangerPronounLabel, strangerPronounsBox)
        );
        pronounsRow.setAlignment(Pos.CENTER);

        HBox buttonRow = new HBox(14);
        buttonRow.setAlignment(Pos.CENTER);
        buttonRow.setPadding(new Insets(10, 0, 0, 0));

        Button backButton = createSmallButton("← BACK");
        backButton.setOnAction(e -> showMainMenu());

        Button boardButton = createSmallButton("BOARD THE BUS ➔");
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

    // 3. Settings Screen
    public void showSettings() {
        contentLayer.getChildren().clear();

        VBox card = new VBox(14);
        card.setMaxWidth(520);
        card.setMaxHeight(Region.USE_PREF_SIZE);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(28, 36, 28, 36));
        card.setStyle(
            "-fx-background-color: rgba(36, 30, 50, 0.94);" +
            "-fx-border-color: #f2a6bd;" +
            "-fx-border-width: 2px;" +
            "-fx-border-radius: 8px;" +
            "-fx-background-radius: 8px;" +
            "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.7), 16, 0, 0, 4);"
        );

        Label titleLabel = new Label("SETTINGS");
        titleLabel.setStyle("-fx-text-fill: #f4d38a; -fx-font-size: 24px; -fx-font-weight: bold; -fx-font-family: 'SansSerif';");

        Label subtitleLabel = new Label("Commute Preferences & Audio");
        subtitleLabel.setStyle("-fx-text-fill: #f5f0fa; -fx-font-size: 13px; -fx-font-style: italic;");

        Label volumeLabel = new Label("BGM Volume:");
        volumeLabel.setStyle("-fx-text-fill: #f5f0fa; -fx-font-size: 13px; -fx-font-weight: bold;");

        Slider volumeSlider = new Slider(0, 100, 80);
        volumeSlider.setMaxWidth(260);

        Label volumeValueLabel = new Label("80%");
        volumeValueLabel.setStyle("-fx-text-fill: #f4d38a; -fx-font-size: 14px; -fx-font-weight: bold;");
        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            volumeValueLabel.setText(newVal.intValue() + "%");
        });

        HBox volumeRow = new HBox(12, volumeSlider, volumeValueLabel);
        volumeRow.setAlignment(Pos.CENTER);

        Label textSpeedLabel = new Label("Text Speed:");
        textSpeedLabel.setStyle("-fx-text-fill: #f5f0fa; -fx-font-size: 13px; -fx-font-weight: bold;");

        ComboBox<String> textSpeedBox = new ComboBox<>();
        textSpeedBox.getItems().addAll("Normal", "Fast", "Instant");
        textSpeedBox.setValue("Normal");

        HBox speedRow = new HBox(12, textSpeedLabel, textSpeedBox);
        speedRow.setAlignment(Pos.CENTER);

        Label displayLabel = new Label("Display Mode: Windowed (1280 x 720)");
        displayLabel.setStyle("-fx-text-fill: #a2a8ba; -fx-font-size: 13px;");

        Button backButton = createSmallButton("← BACK TO MENU");
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

    private Button createMenuButton(String text) {
        Button btn = new Button(text);
        btn.setStyle(BTN_IDLE_STYLE);
        btn.setPrefWidth(280);
        btn.setPrefHeight(46);

        // Hover events from specification
        btn.setOnMouseEntered(e -> btn.setStyle(BTN_HOVER_STYLE));
        btn.setOnMouseExited(e -> btn.setStyle(BTN_IDLE_STYLE));

        return btn;
    }

    private Button createSmallButton(String text) {
        Button btn = new Button(text);
        btn.setStyle(BTN_IDLE_STYLE);
        btn.setPrefWidth(180);
        btn.setPrefHeight(40);

        btn.setOnMouseEntered(e -> btn.setStyle(BTN_HOVER_STYLE));
        btn.setOnMouseExited(e -> btn.setStyle(BTN_IDLE_STYLE));

        return btn;
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
