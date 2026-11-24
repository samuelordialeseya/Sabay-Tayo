package com.sabaytayo;

import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

// Ending screen: reveals final relationship stats, progress bars, momentum, matched outcome, and Play Again.
public class EndingView extends StackPane {

    private final BusAtmosphereLayer atmosphereLayer;

    public EndingView(GameApplication app, RelationshipStats stats, Momentum momentum) {

        // Background image
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

        atmosphereLayer = new BusAtmosphereLayer();
        atmosphereLayer.setAtmosphereEnabled(app.isAtmosphereEnabled());

        // Center result card
        VBox card = new VBox(18);
        card.maxWidthProperty().bind(Bindings.min(widthProperty().multiply(0.56), 740));
        card.setMaxHeight(Region.USE_PREF_SIZE);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(40, 52, 40, 52));
        card.getStyleClass().add("dialogue-box");

        int trust = stats.getTrust();
        int attraction = stats.getAttraction();
        int respect = stats.getRespect();
        int annoyance = stats.getAnnoyance();

        OutcomeClassifier classifier = new OutcomeClassifier();
        Outcome outcome = classifier.classify(stats);

        Label title = new Label(outcome.getTitle());
        title.getStyleClass().add("title-header");

        Label desc = new Label(outcome.getDescription());
        desc.getStyleClass().add("sub-header");
        desc.setWrapText(true);
        desc.setAlignment(Pos.CENTER);

        Label ruleLabel = new Label(outcome.getRuleSummary());
        ruleLabel.getStyleClass().add("rule-text");

        // Stats grid with visual bars
        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(20);
        statsGrid.setVgap(12);
        statsGrid.setAlignment(Pos.CENTER);
        statsGrid.setPadding(new Insets(6, 0, 6, 0));

        addStatBarRow(statsGrid, 0, "Trust", trust, false);
        addStatBarRow(statsGrid, 1, "Attraction", attraction, false);
        addStatBarRow(statsGrid, 2, "Respect", respect, false);
        addStatBarRow(statsGrid, 3, "Annoyance", annoyance, true);

        Label momentumName = new Label("Final Momentum:");
        momentumName.getStyleClass().add("stat-name");
        Label momentumVal = new Label((momentum.getValue() >= 0 ? "+" : "") + momentum.getValue()
                + "  (" + momentum.getMultiplier() + "x)");
        momentumVal.getStyleClass().add("stat-value");
        statsGrid.add(momentumName, 0, 4);
        statsGrid.add(momentumVal, 1, 4, 2, 1);

        Button playAgainButton = new Button("Ride Again ↺");
        playAgainButton.getStyleClass().addAll("menu-button", "commute-menu-btn-active");
        playAgainButton.setPrefWidth(260);
        playAgainButton.setOnMouseEntered(e -> app.playHoverSfx());
        playAgainButton.setOnAction(e -> {
            app.playClickSfx();
            app.showStart();
        });

        card.getChildren().addAll(title, desc, ruleLabel, statsGrid, playAgainButton);

        HBox windowControls = app.createWindowControlBar();

        sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (oldScene != null) {
                atmosphereLayer.stop();
            }
            if (newScene != null) {
                atmosphereLayer.start();
            }
        });

        getChildren().addAll(bgView, atmosphereLayer, card, windowControls);
        StackPane.setAlignment(windowControls, Pos.TOP_RIGHT);
        StackPane.setMargin(windowControls, new Insets(20, 28, 0, 0));
    }

    private void addStatBarRow(GridPane grid, int row, String name, int value, boolean isAnnoyance) {
        Label nameLbl = new Label(name);
        nameLbl.getStyleClass().add("stat-name");

        ProgressBar bar = new ProgressBar(value / 100.0);
        bar.setPrefWidth(240);
        bar.getStyleClass().add("stat-bar");
        if (isAnnoyance) {
            bar.getStyleClass().add("stat-bar-annoyance");
        }

        Label valLbl = new Label(value + " / 100");
        valLbl.getStyleClass().add("stat-value");

        grid.add(nameLbl, 0, row);
        grid.add(bar, 1, row);
        grid.add(valLbl, 2, row);
    }
}
