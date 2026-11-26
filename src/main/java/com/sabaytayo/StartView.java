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
import javafx.scene.layout.Priority;
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
    private final Label previewLabel;
    private final Label errorLabel;

    // Menu button rows for active highlight switching
    private final List<Button> menuButtons = new ArrayList<>();
    private final List<Label> leftArrows = new ArrayList<>();
    private final List<Label> rightArrows = new ArrayList<>();

    public StartView(GameApplication app) {
        this.app = app;

        // 1. Rainy twilight bus stop background image
        ImageView bgView = new ImageView();
        Image bg = ImageLoader.load("/assets/BACKGROUNDS/bg_main_menu.png");
        if (bg != null) {
            bgView.setImage(bg);
            bgView.fitWidthProperty().bind(widthProperty());
            bgView.fitHeightProperty().bind(heightProperty());
            bgView.setPreserveRatio(false);
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

        previewLabel = new Label("");
        previewLabel.setWrapText(true);

        nameField.textProperty().addListener((obs, oldVal, newVal) -> updatePreview());
        playerPronounsBox.valueProperty().addListener((obs, oldVal, newVal) -> updatePreview());
        strangerPronounsBox.valueProperty().addListener((obs, oldVal, newVal) -> updatePreview());
        updatePreview();

        errorLabel = new Label("");
        errorLabel.getStyleClass().add("error-text");

        // Display the opening menu
        showMainMenu();

        HBox topControls = app.createWindowControlBar();
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
        Image logo = ImageLoader.load("/assets/BACKGROUNDS/logo_sabay_tayo2.png");
        if (logo != null) {
            logoView.setImage(logo);
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

        VBox card = new VBox(18);
        card.maxWidthProperty().bind(Bindings.min(widthProperty().multiply(0.46), 600));
        card.setMaxHeight(Region.USE_PREF_SIZE);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPadding(new Insets(34, 46, 34, 46));
        card.getStyleClass().add("dialogue-box");

        Label titleLabel = new Label("Boarding Pass");
        titleLabel.getStyleClass().add("title-header");

        Label subtitleLabel = new Label("Set up your commuter profile before boarding the bus");
        subtitleLabel.getStyleClass().add("sub-header");

        // thin dashed line under the header, like a ticket tear-off
        Region divider = new Region();
        divider.getStyleClass().add("pass-divider");
        divider.setPrefHeight(1);
        divider.setMinHeight(1);
        divider.setMaxHeight(1);

        Label nameLabel = new Label("YOUR NAME");
        nameLabel.getStyleClass().add("field-label");
        nameField.setMaxWidth(Double.MAX_VALUE);
        VBox nameBox = new VBox(7, nameLabel, nameField);

        Label playerPronounLabel = new Label("YOUR PRONOUNS");
        playerPronounLabel.getStyleClass().add("field-label");

        Label strangerPronounLabel = new Label("SEATMATE'S PRONOUNS");
        strangerPronounLabel.getStyleClass().add("field-label");

        playerPronounsBox.getStyleClass().add("dark-combo");
        strangerPronounsBox.getStyleClass().add("dark-combo");
        playerPronounsBox.setMaxWidth(Double.MAX_VALUE);
        strangerPronounsBox.setMaxWidth(Double.MAX_VALUE);

        VBox playerPronounBox = new VBox(7, playerPronounLabel, playerPronounsBox);
        VBox strangerPronounBox = new VBox(7, strangerPronounLabel, strangerPronounsBox);
        HBox.setHgrow(playerPronounBox, Priority.ALWAYS);
        HBox.setHgrow(strangerPronounBox, Priority.ALWAYS);
        playerPronounBox.setMaxWidth(Double.MAX_VALUE);
        strangerPronounBox.setMaxWidth(Double.MAX_VALUE);
        HBox pronounsRow = new HBox(20, playerPronounBox, strangerPronounBox);

        // preview sits in its own recessed box so it reads like a line from the story
        Label previewCaption = new Label("PREVIEW");
        previewCaption.getStyleClass().add("preview-caption");
        previewLabel.getStyleClass().add("preview-text");
        VBox previewBox = new VBox(5, previewCaption, previewLabel);
        previewBox.getStyleClass().add("preview-box");
        previewBox.setAlignment(Pos.CENTER_LEFT);

        HBox buttonRow = new HBox(16);
        buttonRow.setAlignment(Pos.CENTER);
        buttonRow.setPadding(new Insets(8, 0, 0, 0));

        Button backButton = new Button("← Back");
        backButton.getStyleClass().addAll("menu-button", "ghost-button");
        backButton.setPrefWidth(145);
        backButton.setOnAction(e -> {
            app.playClickSfx();
            showMainMenu();
        });

        Button boardButton = new Button("Board the Bus ➔");
        boardButton.getStyleClass().addAll("menu-button", "primary-button");
        boardButton.setPrefWidth(220);
        boardButton.setOnAction(e -> {
            app.playClickSfx();
            handleBoarding();
        });

        buttonRow.getChildren().addAll(backButton, boardButton);

        card.getChildren().addAll(
                titleLabel,
                subtitleLabel,
                divider,
                nameBox,
                pronounsRow,
                previewBox,
                errorLabel,
                buttonRow
        );

        contentLayer.getChildren().add(card);
        StackPane.setAlignment(card, Pos.CENTER);
    }

    private void updatePreview() {
        String rawName = nameField.getText();
        String previewName = (rawName == null || rawName.trim().isEmpty()) ? "Sam" : rawName.trim();
        Pronouns playerPronouns = playerPronounsBox.getValue() != null ? playerPronounsBox.getValue() : Pronouns.THEY_THEM;
        Pronouns strangerPronouns = strangerPronounsBox.getValue() != null ? strangerPronounsBox.getValue() : Pronouns.SHE_HER;

        String sample = "{player} took {their} seat beside {seatmate} as {s_they} shifted {s_their} bag.";
        String formatted = ScriptFormatter.format(sample, playerPronouns, previewName, "Amara");
        previewLabel.setText(TextFormatter.formatStrangerText(formatted, strangerPronouns));
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
