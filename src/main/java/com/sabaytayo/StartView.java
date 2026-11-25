package com.sabaytayo;

import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
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

// Opening game menu view with proportional scaling and animated twilight bus atmosphere.
public class StartView extends StackPane {

    private final GameApplication app;
    private final StackPane contentLayer;
    private final BusAtmosphereLayer atmosphereLayer;

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

        // 2. Ambient rain & passing streetlight layer
        atmosphereLayer = new BusAtmosphereLayer();
        atmosphereLayer.setAtmosphereEnabled(app.isAtmosphereEnabled());

        // 3. Dynamic content layer
        contentLayer = new StackPane();

        // Initialize reusable input components
        nameField = new TextField();
        nameField.setPromptText("Enter your name (e.g. Sam)...");
        nameField.setMaxWidth(440);
        nameField.getStyleClass().add("input-field");

        playerPronounsBox = new ComboBox<>();
        playerPronounsBox.getItems().addAll(Pronouns.THEY_THEM, Pronouns.HE_HIM, Pronouns.SHE_HER);
        playerPronounsBox.setValue(Pronouns.THEY_THEM);
        playerPronounsBox.setPrefWidth(210);

        strangerPronounsBox = new ComboBox<>();
        strangerPronounsBox.getItems().addAll(Pronouns.SHE_HER, Pronouns.HE_HIM, Pronouns.THEY_THEM);
        strangerPronounsBox.setValue(Pronouns.SHE_HER);
        strangerPronounsBox.setPrefWidth(210);

        errorLabel = new Label("");
        errorLabel.getStyleClass().add("error-text");

        // Display the opening menu
        showMainMenu();

        HBox musicBar = app.createMusicControlBar();
        musicBar.getStyleClass().add("music-pill");

        HBox topControls = new HBox(12, musicBar, app.createWindowControlBar());
        topControls.setAlignment(Pos.CENTER_RIGHT);
        topControls.setMaxWidth(Region.USE_PREF_SIZE);
        topControls.setMaxHeight(Region.USE_PREF_SIZE);

        sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (oldScene != null) {
                atmosphereLayer.stop();
            }
            if (newScene != null) {
                atmosphereLayer.start();
            }
        });

        getChildren().addAll(bgView, atmosphereLayer, contentLayer, topControls);
        StackPane.setAlignment(topControls, Pos.TOP_RIGHT);
        StackPane.setMargin(topControls, new Insets(20, 28, 0, 0));
    }

    // 1. Main Start Menu: Signboard logo up top, commute menu buttons centered below
    public void showMainMenu() {
        contentLayer.getChildren().clear();
        menuButtons.clear();
        leftArrows.clear();
        rightArrows.clear();

        ImageView logoView = createLogoView();

        VBox buttonsBox = new VBox(16);
        buttonsBox.setAlignment(Pos.CENTER);

        HBox startRow = createMenuButtonRow("START COMMUTE", 0, this::showCharacterSetup);
        HBox testRow = createMenuButtonRow("QUICK START (SAM)", 1, () -> {
            try {
                app.setPlayer(new Player("Sam", Pronouns.THEY_THEM));
            } catch (InvalidNameException ignored) {
            }
            app.setStranger(new Stranger(Pronouns.SHE_HER));
            app.showEncounter();
        });
        HBox exitRow = createMenuButtonRow("EXIT", 2, () -> Platform.exit());

        buttonsBox.getChildren().addAll(startRow, testRow, exitRow);
        setActiveMenuButton(0);

        VBox centerColumn = new VBox(28, logoView, buttonsBox);
        centerColumn.setAlignment(Pos.CENTER);
        centerColumn.setMaxWidth(Region.USE_PREF_SIZE);
        centerColumn.setMaxHeight(Region.USE_PREF_SIZE);

        contentLayer.getChildren().add(centerColumn);
        StackPane.setAlignment(centerColumn, Pos.CENTER);
    }

    // Loads the pixel-art "Sabay Tayo" signboard logo scaled proportionally to screen width.
    private ImageView createLogoView() {
        ImageView logoView = new ImageView();
        logoView.setPreserveRatio(true);
        logoView.fitWidthProperty().bind(Bindings.min(widthProperty().multiply(0.45), 680));
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
        btn.setPrefWidth(320);
        btn.setPrefHeight(52);
        btn.setOnAction(e -> {
            app.playClickSfx();
            action.run();
        });

        Label rightArrow = new Label("◀");
        rightArrow.getStyleClass().add("menu-arrow");

        menuButtons.add(btn);
        leftArrows.add(leftArrow);
        rightArrows.add(rightArrow);

        btn.setOnMouseEntered(e -> {
            app.playHoverSfx();
            setActiveMenuButton(index);
        });

        HBox row = new HBox(14, leftArrow, btn, rightArrow);
        row.setAlignment(Pos.CENTER);
        row.setMaxWidth(Region.USE_PREF_SIZE);
        return row;
    }

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

    // 2. Character Setup ("Boarding Pass")
    public void showCharacterSetup() {
        contentLayer.getChildren().clear();

        VBox card = new VBox(16);
        card.maxWidthProperty().bind(Bindings.min(widthProperty().multiply(0.46), 600));
        card.setMaxHeight(Region.USE_PREF_SIZE);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(36, 46, 36, 46));
        card.getStyleClass().add("dialogue-box");

        Label titleLabel = new Label("Boarding Pass");
        titleLabel.getStyleClass().add("title-header");

        Label subtitleLabel = new Label("Set up your commuter profile before boarding the bus");
        subtitleLabel.getStyleClass().add("sub-header");

        Label nameLabel = new Label("Your Name:");
        nameLabel.getStyleClass().add("field-label");

        Label playerPronounLabel = new Label("Your Pronouns:");
        playerPronounLabel.getStyleClass().add("field-label");

        Label strangerPronounLabel = new Label("Seatmate's Pronouns:");
        strangerPronounLabel.getStyleClass().add("field-label");

        HBox pronounsRow = new HBox(20,
                new VBox(6, playerPronounLabel, playerPronounsBox),
                new VBox(6, strangerPronounLabel, strangerPronounsBox)
        );
        pronounsRow.setAlignment(Pos.CENTER);

        HBox buttonRow = new HBox(16);
        buttonRow.setAlignment(Pos.CENTER);
        buttonRow.setPadding(new Insets(10, 0, 0, 0));

        Button backButton = new Button("← Back");
        backButton.getStyleClass().add("menu-button");
        backButton.setPrefWidth(145);
        backButton.setOnAction(e -> {
            app.playClickSfx();
            showMainMenu();
        });

        Button boardButton = new Button("Board the Bus ➔");
        boardButton.getStyleClass().addAll("menu-button", "commute-menu-btn-active");
        boardButton.setPrefWidth(220);
        boardButton.setOnAction(e -> {
            app.playClickSfx();
            handleBoarding();
        });

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
