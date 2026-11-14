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
import javafx.scene.layout.BorderPane;
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
    private final ComboBox<String> playerPronounsBox;
    private final ComboBox<String> strangerPronounsBox;
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

        // Display the opening menu
        showMainMenu();

        getChildren().addAll(bgView, contentLayer);
    }

    // 1. Recreated Start Menu: Signboard Plaque up top, Commute Buttons in center
    public void showMainMenu() {
        contentLayer.getChildren().clear();
        menuButtons.clear();
        leftArrows.clear();
        rightArrows.clear();

        // Top: Bus Signboard Plaque ("Sabay Tayo")
        VBox signboard = createSignboard();

        // Center: Menu Buttons
        VBox buttonsBox = new VBox(14);
        buttonsBox.setAlignment(Pos.CENTER);

        HBox startRow = createMenuButtonRow("START COMMUTE", 0, () -> showCharacterSetup());
        HBox exitRow = createMenuButtonRow("EXIT", 1, () -> Platform.exit());

        buttonsBox.getChildren().addAll(startRow, exitRow);

        // Set START COMMUTE active by default
        setActiveMenuButton(0);

        // Main center column combining Signboard + Buttons
        VBox centerColumn = new VBox(28, signboard, buttonsBox);
        centerColumn.setAlignment(Pos.CENTER);
        centerColumn.setMaxWidth(Region.USE_PREF_SIZE);
        centerColumn.setMaxHeight(Region.USE_PREF_SIZE);

        contentLayer.getChildren().add(centerColumn);
        StackPane.setAlignment(centerColumn, Pos.CENTER);
    }


    // Builds the double-bordered "Sabay Tayo" signboard plaque
    private VBox createSignboard() {
        VBox outerPlaque = new VBox();
        outerPlaque.getStyleClass().add("signboard-outer");
        outerPlaque.setMaxWidth(Region.USE_PREF_SIZE);
        outerPlaque.setMaxHeight(Region.USE_PREF_SIZE);

        VBox innerPlaque = new VBox(2);
        innerPlaque.getStyleClass().add("signboard-inner");
        innerPlaque.setAlignment(Pos.CENTER);

        // Top ornament line: sparkles + transit arrows + bus icon
        BorderPane topOrnament = new BorderPane();
        Label topLeftStar = new Label("✦");
        topLeftStar.getStyleClass().add("signboard-star");
        Label topCenterRoute = new Label("───────►   🚌   ───────►");
        topCenterRoute.getStyleClass().add("signboard-ornament");
        Label topRightStar = new Label("✦ ✧");
        topRightStar.getStyleClass().add("signboard-star");
        topOrnament.setLeft(topLeftStar);
        topOrnament.setCenter(topCenterRoute);
        topOrnament.setRight(topRightStar);

        // Center cursive script title
        Label titleLabel = new Label("Sabay Tayo");
        titleLabel.getStyleClass().add("signboard-title");

        // Bottom ornament line: reverse transit arrows + bus icon
        BorderPane bottomOrnament = new BorderPane();
        Label bottomLeftStar = new Label("·");
        bottomLeftStar.getStyleClass().add("signboard-ornament");
        Label bottomCenterRoute = new Label("◄───────   ◄───────");
        bottomCenterRoute.getStyleClass().add("signboard-ornament");
        Label bottomRightBus = new Label("🚌 ✦");
        bottomRightBus.getStyleClass().add("signboard-ornament");
        bottomOrnament.setLeft(bottomLeftStar);
        bottomOrnament.setCenter(bottomCenterRoute);
        bottomOrnament.setRight(bottomRightBus);

        innerPlaque.getChildren().addAll(topOrnament, titleLabel, bottomOrnament);
        outerPlaque.getChildren().add(innerPlaque);
        return outerPlaque;
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
