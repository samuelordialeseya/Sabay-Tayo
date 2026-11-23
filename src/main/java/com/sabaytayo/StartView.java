package com.sabaytayo;

import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
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

// Opening game menu view recreated from the Sabay Tayo bus signboard & ticket concept
public class StartView extends StackPane {

    private final GameApplication app;
    private final StackPane contentLayer;

    // Persisted setup inputs
    private final TextField nameField;
    private final ComboBox<Pronouns> playerPronounsBox;
    private final ComboBox<Pronouns> strangerPronounsBox;
    private final Label errorLabel;

    // Menu button rows for active highlight switching
    private final List<Button> menuButtons = new ArrayList<>();
    private final List<Label> leftArrows = new ArrayList<>();
    private final List<Label> rightArrows = new ArrayList<>();

    public StartView(GameApplication app) {
        this.app = app;

        // 1. Bus interior twilight background image
        ImageView bgView = new ImageView();
        try {
            Image bg = new Image(getClass().getResourceAsStream("/com/sabaytayo/images/bg.jpg"));
            bgView.setImage(bg);
            bgView.fitWidthProperty().bind(widthProperty());
            bgView.fitHeightProperty().bind(heightProperty());
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
        playerPronounsBox.getItems().addAll(Pronouns.THEY_THEM, Pronouns.HE_HIM, Pronouns.SHE_HER);
        playerPronounsBox.setValue(Pronouns.THEY_THEM);

        strangerPronounsBox = new ComboBox<>();
        strangerPronounsBox.getItems().addAll(Pronouns.SHE_HER, Pronouns.HE_HIM, Pronouns.THEY_THEM);
        strangerPronounsBox.setValue(Pronouns.SHE_HER);

        errorLabel = new Label("");
        errorLabel.getStyleClass().add("error-text");

        // Display the opening menu
        showMainMenu();

        HBox musicBar = app.createMusicControlBar();
        musicBar.getStyleClass().add("music-pill");

        HBox topControls = new HBox(10, musicBar, app.createWindowControlBar());
        topControls.setAlignment(Pos.CENTER_RIGHT);
        topControls.setMaxWidth(Region.USE_PREF_SIZE);
        topControls.setMaxHeight(Region.USE_PREF_SIZE);

        getChildren().addAll(bgView, contentLayer, topControls);
        StackPane.setAlignment(topControls, Pos.TOP_RIGHT);
        StackPane.setMargin(topControls, new Insets(16, 20, 0, 0));
    }

    // 1. Recreated Start Menu: Signboard Plaque up top, Commute Buttons in center
    public void showMainMenu() {
        contentLayer.getChildren().clear();
        menuButtons.clear();
        leftArrows.clear();
        rightArrows.clear();

        // Top: Pixel-art "Sabay Tayo" signboard logo asset
        ImageView logoView = createLogoView();

        // Center: Menu Buttons
        VBox buttonsBox = new VBox(14);
        buttonsBox.setAlignment(Pos.CENTER);

        HBox startRow = createMenuButtonRow("START COMMUTE", 0, () -> showCharacterSetup());
        HBox exitRow = createMenuButtonRow("EXIT", 1, () -> Platform.exit());

        buttonsBox.getChildren().addAll(startRow, exitRow);

        // Set START COMMUTE active by default
        setActiveMenuButton(0);

        // Main center column combining Signboard Logo + Buttons
        VBox centerColumn = new VBox(24, logoView, buttonsBox);
        centerColumn.setAlignment(Pos.CENTER);
        centerColumn.setMaxWidth(Region.USE_PREF_SIZE);
        centerColumn.setMaxHeight(Region.USE_PREF_SIZE);

        contentLayer.getChildren().add(centerColumn);
        StackPane.setAlignment(centerColumn, Pos.CENTER);
    }

    // Loads the pixel-art "Sabay Tayo" signboard logo image.
    private ImageView createLogoView() {
        ImageView logoView = new ImageView();
        logoView.setPreserveRatio(true);
        logoView.setFitWidth(560);
        logoView.setSmooth(false);
        try {
            Image logo = new Image(getClass().getResourceAsStream("/assets/BACKGROUNDS/logo_sabay_tayo2.png"));
            logoView.setImage(logo);
        } catch (Exception e) {
            System.err.println("Notice: Could not load logo_sabay_tayo2.png.");
        }
        return logoView;
    }

    // Creates one menu button row flanked by left (▶) and right (◀) pointer arrows
    private HBox createMenuButtonRow(String text, int index, Runnable action) {
        Label leftArrow = new Label("▶");
        leftArrow.getStyleClass().add("menu-arrow");

        Button btn = new Button(text);
        btn.getStyleClass().add("commute-menu-btn");
        btn.setPrefWidth(280);
        btn.setPrefHeight(46);
        btn.setOnAction(e -> action.run());

        Label rightArrow = new Label("◀");
        rightArrow.getStyleClass().add("menu-arrow");

        menuButtons.add(btn);
        leftArrows.add(leftArrow);
        rightArrows.add(rightArrow);

        btn.setOnMouseEntered(e -> setActiveMenuButton(index));

        HBox row = new HBox(12, leftArrow, btn, rightArrow);
        row.setAlignment(Pos.CENTER);
        row.setMaxWidth(Region.USE_PREF_SIZE);
        return row;
    }

    // Highlights the active button and shows its flanking ▶ / ◀ pointer arrows
    private void setActiveMenuButton(int activeIndex) {
        for (int i = 0; i < menuButtons.size(); i++) {
            Button btn = menuButtons.get(i);
            Label left = leftArrows.get(i);
            Label right = rightArrows.get(i);

            if (i == activeIndex) {
                if (!btn.getStyleClass().contains("commute-menu-btn-active")) {
                    btn.getStyleClass().add("commute-menu-btn-active");
                }
                left.setVisible(true);
                right.setVisible(true);
            } else {
                btn.getStyleClass().remove("commute-menu-btn-active");
                left.setVisible(false);
                right.setVisible(false);
            }
        }
    }

    // 2. Character Setup (Shown after clicking START COMMUTE)
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

        Label subtitleLabel = new Label("Setup your commuter profile before boarding the bus");
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
        backButton.getStyleClass().add("menu-button");
        backButton.setPrefWidth(130);
        backButton.setOnAction(e -> showMainMenu());

        Button boardButton = new Button("Board the Bus ➔");
        boardButton.getStyleClass().add("menu-button");
        boardButton.setPrefWidth(200);
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



    private void handleBoarding() {
        try {
            Player player = new Player(nameField.getText(), playerPronounsBox.getValue());
            Stranger stranger = new Stranger(strangerPronounsBox.getValue());

            errorLabel.setText("");
            app.setPlayer(player);
            app.setStranger(stranger);
            app.showEncounter();
        } catch (InvalidNameException e) {
            errorLabel.setText(e.getMessage());
        }
    }
}
