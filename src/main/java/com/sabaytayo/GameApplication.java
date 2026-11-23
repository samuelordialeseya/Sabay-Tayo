package com.sabaytayo;

import java.net.URL;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

// Main JavaFX Application class managing the window stage and transitions across views.
public class GameApplication extends Application {

    private static final String BGM_RESOURCE_PATH = "/assets/AUDIO/closer_acoustic.mp3";
    private static final double DEFAULT_BGM_VOLUME = 0.14;

    private Stage primaryStage;
    private Scene activeScene;
    private MediaPlayer bgmPlayer;
    private boolean musicMuted = false;
    private double musicVolume = DEFAULT_BGM_VOLUME;

    private RelationshipStats stats;
    private Momentum momentum;

    private Player player;
    private Stranger stranger;

    public GameApplication() {
        try {
            this.player = new Player("Sam", Pronouns.THEY_THEM);
        } catch (InvalidNameException e) {
            // Default name is always valid
        }
        this.stranger = new Stranger(Pronouns.SHE_HER);
    }

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        stage.setTitle("Sabay Tayo? — a branching bus-ride dialogue game");
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setResizable(true);

        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        stage.setX(bounds.getMinX());
        stage.setY(bounds.getMinY());
        stage.setWidth(bounds.getWidth());
        stage.setHeight(bounds.getHeight());

        startBackgroundMusic();
        showStart();
        stage.show();
    }

    // Loads and loops the background instrumental track throughout the game.
    private void startBackgroundMusic() {
        try {
            URL audioUrl = getClass().getResource(BGM_RESOURCE_PATH);
            if (audioUrl == null) {
                System.err.println("Notice: Could not find background music at " + BGM_RESOURCE_PATH);
                return;
            }
            Media media = new Media(audioUrl.toExternalForm());
            bgmPlayer = new MediaPlayer(media);
            bgmPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            bgmPlayer.setVolume(musicVolume);
            bgmPlayer.setMute(musicMuted);
            bgmPlayer.play();
        } catch (Exception e) {
            System.err.println("Notice: Could not play background music: " + e.getMessage());
        }
    }

    // Toggles mute on/off for the background music.
    public void toggleMusicMute() {
        musicMuted = !musicMuted;
        if (bgmPlayer != null) {
            bgmPlayer.setMute(musicMuted);
        }
    }

    public boolean isMusicMuted() {
        return musicMuted;
    }

    // Updates background music volume (0.0 to 1.0) and unmutes if raised above 0.
    public void setMusicVolume(double volume) {
        if (volume < 0.0) {
            volume = 0.0;
        } else if (volume > 1.0) {
            volume = 1.0;
        }
        this.musicVolume = volume;
        if (volume > 0.0 && musicMuted) {
            musicMuted = false;
        }
        if (bgmPlayer != null) {
            bgmPlayer.setVolume(musicVolume);
            bgmPlayer.setMute(musicMuted);
        }
    }

    public double getMusicVolume() {
        return musicVolume;
    }

    // Creates a compact Music mute toggle + volume slider synced to the background music player.
    public HBox createMusicControlBar() {
        Button musicBtn = new Button();
        musicBtn.getStyleClass().add("vn-sub-button");
        musicBtn.setFocusTraversable(false);

        Slider volumeSlider = new Slider(0.0, 1.0, musicVolume);
        volumeSlider.getStyleClass().add("volume-slider");
        volumeSlider.setPrefWidth(88);
        volumeSlider.setFocusTraversable(false);

        Runnable refreshLabel = () -> {
            musicBtn.setText(musicMuted ? "Music: OFF" : "Music: ON");
            if (!musicMuted) {
                if (!musicBtn.getStyleClass().contains("vn-sub-button-active")) {
                    musicBtn.getStyleClass().add("vn-sub-button-active");
                }
            } else {
                musicBtn.getStyleClass().remove("vn-sub-button-active");
            }
        };
        refreshLabel.run();

        musicBtn.setOnAction(e -> {
            toggleMusicMute();
            refreshLabel.run();
        });

        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            setMusicVolume(newVal.doubleValue());
            refreshLabel.run();
        });

        HBox bar = new HBox(6, musicBtn, volumeSlider);
        bar.setAlignment(Pos.CENTER);
        bar.setMaxWidth(Region.USE_PREF_SIZE);
        bar.setMaxHeight(Region.USE_PREF_SIZE);
        return bar;
    }

    // Creates a compact window control bar (Minimize — and Close ✕) for the top right
    public HBox createWindowControlBar() {
        Button minBtn = new Button("—");
        minBtn.getStyleClass().add("window-control-btn");
        minBtn.setFocusTraversable(false);
        minBtn.setOnAction(e -> {
            if (primaryStage != null) {
                primaryStage.setIconified(true);
            }
        });

        Button closeBtn = new Button("✕");
        closeBtn.getStyleClass().addAll("window-control-btn", "window-close-btn");
        closeBtn.setFocusTraversable(false);
        closeBtn.setOnAction(e -> {
            stop();
            Platform.exit();
        });

        HBox bar = new HBox(6, minBtn, closeBtn);
        bar.setAlignment(Pos.CENTER_RIGHT);
        bar.setMaxWidth(Region.USE_PREF_SIZE);
        bar.setMaxHeight(Region.USE_PREF_SIZE);
        return bar;
    }

    @Override
    public void stop() {
        if (bgmPlayer != null) {
            bgmPlayer.stop();
            bgmPlayer.dispose();
            bgmPlayer = null;
        }
    }

    // Switches the screen to the character setup / start view
    public void showStart() {
        this.stats = new RelationshipStats();
        this.momentum = new Momentum();

        StartView startView = new StartView(this);

        if (activeScene == null) {
            Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
            activeScene = new Scene(startView, bounds.getWidth(), bounds.getHeight());
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
        Encounter encounter = new Encounter();
        EncounterView encounterView = new EncounterView(this, stats, momentum, encounter);
        activeScene.setRoot(encounterView);
    }

    // Switches the screen to the final stats & outcome breakdown view
    public void showEnding() {
        EndingView endingView = new EndingView(this, stats, momentum);
        activeScene.setRoot(endingView);
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Stranger getStranger() {
        return stranger;
    }

    public void setStranger(Stranger stranger) {
        this.stranger = stranger;
    }

    public String getPlayerName() {
        return player.getName();
    }

    public Pronouns getPlayerPronouns() {
        return player.getPronouns();
    }

    public Pronouns getStrangerPronouns() {
        return stranger.getPronouns();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
