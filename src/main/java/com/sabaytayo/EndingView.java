package com.sabaytayo;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

// Ending screen: reveals final relationship stats, momentum, matched outcome, and a Play Again option.
public class EndingView extends StackPane {

    public EndingView(GameApplication app, RelationshipStats stats, Momentum momentum) {

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

        // Center result card
        VBox card = new VBox(16);
        card.setMaxWidth(600);
        card.setMaxHeight(Region.USE_PREF_SIZE);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(32, 40, 32, 40));
        card.getStyleClass().add("dialogue-box");

        // Classify the ending polymorphically using OutcomeClassifier
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

        Label ruleLabel = new Label(outcome.getRuleSummary());
        ruleLabel.getStyleClass().add("rule-text");

        // Stats grid
        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(30);
        statsGrid.setVgap(8);
        statsGrid.setAlignment(Pos.CENTER);

        addStatRow(statsGrid, 0, "Trust", trust + " / 100");
        addStatRow(statsGrid, 1, "Attraction", attraction + " / 100");
        addStatRow(statsGrid, 2, "Respect", respect + " / 100");
        addStatRow(statsGrid, 3, "Annoyance", annoyance + " / 100");
        addStatRow(statsGrid, 4, "Final Momentum", (momentum.getValue() >= 0 ? "+" : "") + momentum.getValue());

        // Play Again button
        Button playAgainButton = new Button("Play Again ↺");
        playAgainButton.getStyleClass().add("choice-button");
        playAgainButton.setPrefWidth(220);
        playAgainButton.setAlignment(Pos.CENTER);
        playAgainButton.setOnAction(e -> app.showStart());

        card.getChildren().addAll(title, desc, ruleLabel, statsGrid, playAgainButton);

        getChildren().addAll(bgView, card);
    }

    private void addStatRow(GridPane grid, int row, String name, String value) {
        Label nameLbl = new Label(name + ":");
        nameLbl.getStyleClass().add("stat-name");

        Label valLbl = new Label(value);
        valLbl.getStyleClass().add("stat-value");

        grid.add(nameLbl, 0, row);
        grid.add(valLbl, 1, row);
    }
}
