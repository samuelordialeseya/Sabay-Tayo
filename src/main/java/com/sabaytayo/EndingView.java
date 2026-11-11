package com.sabaytayo;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

// Ending screen: reveals final relationship stats, momentum, matched outcome, and a Play Again option.
public class EndingView extends StackPane {

    public EndingView(GameApplication app, RelationshipStats stats, Momentum momentum) {
        setPrefSize(1280, 720);

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
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(32, 40, 32, 40));
        card.getStyleClass().add("dialogue-box");

        // Determine ending based on project proposal thresholds
        int trust = stats.get(RelationshipStats.TRUST);
        int attraction = stats.get(RelationshipStats.ATTRACTION);
        int respect = stats.get(RelationshipStats.RESPECT);
        int annoyance = stats.get(RelationshipStats.ANNOYANCE);

        String outcomeTitle;
        String outcomeDesc;
        String matchedRule;

        if (annoyance >= 34) {
            outcomeTitle = "🚫 Blocked";
            outcomeDesc = "The conversation grew too frustrating. The stranger moved to another seat.";
            matchedRule = "Rule: Annoyance >= 34";
        } else if (trust >= 58 && attraction >= 40 && annoyance < 24) {
            outcomeTitle = "💕 Lovers";
            outcomeDesc = "Sparks flew in the crowded bus. You exchanged contacts before getting off.";
            matchedRule = "Rule: Trust >= 58, Attraction >= 40, Annoyance < 24";
        } else if (trust >= 60 && respect >= 50 && annoyance < 24) {
            outcomeTitle = "🤝 Best Friends";
            outcomeDesc = "An effortless bond formed. You talked like you've known each other for years.";
            matchedRule = "Rule: Trust >= 60, Respect >= 50, Annoyance < 24";
        } else if (respect >= 50 && annoyance >= 22 && trust < 50) {
            outcomeTitle = "⚡ Rivals";
            outcomeDesc = "A sharp, bantering dynamic. You challenged each other the whole ride.";
            matchedRule = "Rule: Respect >= 50, Annoyance >= 22, Trust < 50";
        } else {
            outcomeTitle = "🚏 Just Strangers";
            outcomeDesc = "A pleasant, quiet ride. You parted ways as normal fellow passengers.";
            matchedRule = "Rule: Default / Fallback";
        }

        Label title = new Label(outcomeTitle);
        title.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #ff94c2;");

        Label desc = new Label(outcomeDesc);
        desc.setStyle("-fx-font-size: 15px; -fx-text-fill: #ffffff; -fx-text-alignment: center;");
        desc.setWrapText(true);

        Label ruleLabel = new Label(matchedRule);
        ruleLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #ffd166; -fx-font-style: italic;");

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
        nameLbl.setStyle("-fx-text-fill: #e0d0e6; -fx-font-size: 14px;");

        Label valLbl = new Label(value);
        valLbl.setStyle("-fx-text-fill: #ff94c2; -fx-font-size: 14px; -fx-font-weight: bold;");

        grid.add(nameLbl, 0, row);
        grid.add(valLbl, 1, row);
    }
}
