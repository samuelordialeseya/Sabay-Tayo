package com.sabaytayo;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

// Main JavaFX Application class managing the window stage and transitions across views.
public class GameApplication extends Application {

    private Stage primaryStage;
    private Scene activeScene;

    private RelationshipStats stats;
    private Momentum momentum;

    private String playerName = "Sam";
    private String playerPronouns = "They/Them";
    private String strangerPronouns = "She/Her";

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        stage.setTitle("Sabay Tayo? — a branching bus-ride dialogue game");
        stage.setResizable(false);

        showStart();
        stage.show();
    }

    // Switches the screen to the character setup / start view
    public void showStart() {
        this.stats = new RelationshipStats();
        this.momentum = new Momentum();

        StartView startView = new StartView(this);

        if (activeScene == null) {
            activeScene = new Scene(startView, 1280, 720);
            try {
                String css = getClass().getResource("/com/sabaytayo/style.css").toExternalForm();
                activeScene.getStylesheets().add(css);
            } catch (Exception e) {
                System.err.println("Notice: Could not load style.css: " + e.getMessage());
            }
            primaryStage.setScene(activeScene);
        } else {
            activeScene.setRoot(startView);
        }
    }

    // Switches the screen to the bus encounter dialogue view
    public void showEncounter() {
        EncounterView encounterView = new EncounterView(this, stats, momentum);
        activeScene.setRoot(encounterView);
    }

    // Switches the screen to the final stats & outcome breakdown view
    public void showEnding() {
        EndingView endingView = new EndingView(this, stats, momentum);
        activeScene.setRoot(endingView);
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getPlayerPronouns() {
        return playerPronouns;
    }

    public void setPlayerPronouns(String playerPronouns) {
        this.playerPronouns = playerPronouns;
    }

    public String getStrangerPronouns() {
        return strangerPronouns;
    }

    public void setStrangerPronouns(String strangerPronouns) {
        this.strangerPronouns = strangerPronouns;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
