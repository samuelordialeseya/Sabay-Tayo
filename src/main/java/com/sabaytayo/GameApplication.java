package com.sabaytayo;

import java.net.URL;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

// Main JavaFX Application class managing the window stage and transitions across views.
public class GameApplication extends Application {

    public static final String BGM_MENU_PATH = "/assets/AUDIO/bgm_main_menu.mp3";
    public static final String BGM_WARM_PATH = "/assets/AUDIO/closer_acoustic.mp3";
    public static final String BGM_COLD_PATH = "/assets/AUDIO/bgm_cold_melancholy.mp3";
    public static final String BGM_SUDDEN_BRAKE_PATH = "/assets/AUDIO/bgm_sudden_brake.mp3";
    private static final String AMBIENT_RAIN_BUS_PATH = "/assets/AUDIO/sfx_rain_bus_loop.mp3";
    private static final double DEFAULT_BGM_VOLUME = 0.14;
    private static final double DEFAULT_SFX_VOLUME = 0.45;
    private static final double AMBIENT_RAIN_SCALE = 0.25;

    private Stage primaryStage;
    private Scene activeScene;
    private MediaPlayer bgmPlayer;
    private String currentBgmPath = null;
    private boolean musicMuted = false;
    private double musicVolume = DEFAULT_BGM_VOLUME;

    // Continuous bus/rain ambient loop (active during S01-S08)
    private MediaPlayer ambientPlayer;
    private boolean ambientActive = false;

    // UI & story sound effects
    private AudioClip textBlipClip;
    private AudioClip clickClip;
    private AudioClip hoverClip;
    private AudioClip busHornClip;
    private AudioClip brakeClip;
    private AudioClip headThudClip;
    private AudioClip headThudSecondaryClip;
    private AudioClip phoneNotificationClip;
    private boolean sfxMuted = false;
    private double sfxVolume = DEFAULT_SFX_VOLUME;

    // Shared VN presentation settings
    private int typingDelayMs = 18;
    private boolean atmosphereEnabled = true;
    private boolean cgPlaceholderEnabled = true;

    private RelationshipStats stats;
    private Momentum momentum;
    private EncounterView activeEncounterView;

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
        Image appIcon = ImageLoader.load("/assets/ui/app_icon.png");
        if (appIcon != null) {
            stage.getIcons().add(appIcon);
        }
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setResizable(true);

        Rectangle2D bounds = Screen.getPrimary().getBounds();
        stage.setX(bounds.getMinX());
        stage.setY(bounds.getMinY());
        stage.setWidth(bounds.getWidth());
        stage.setHeight(bounds.getHeight());
        stage.setFullScreen(true);
        stage.setFullScreenExitHint("");
        stage.setFullScreenExitKeyCombination(javafx.scene.input.KeyCombination.NO_MATCH);

        // Keep fullscreen when restored from taskbar
        stage.iconifiedProperty().addListener((obs, wasIconified, isIconified) -> {
            if (!isIconified) {
                stage.setFullScreen(true);
            }
        });

        startBackgroundMusic();
        loadSoundEffects();
        showStart();
        stage.show();
    }

    // Loads lightweight sound effects (prefers .mp3 if team adds custom files, falls back to .wav).
    private void loadSoundEffects() {
        textBlipClip = loadAudioClip("/assets/AUDIO/sfx_text_blip.mp3", "/assets/AUDIO/sfx_text_blip.wav");
        clickClip = loadAudioClip("/assets/AUDIO/sfx_choice_click.mp3", "/assets/AUDIO/sfx_choice_click.wav");
        hoverClip = loadAudioClip("/assets/AUDIO/sfx_hover.mp3", "/assets/AUDIO/sfx_hover.wav");
        busHornClip = loadAudioClip("/assets/AUDIO/sfx_bus_horn.mp3", null);
        brakeClip = loadAudioClip("/assets/AUDIO/sfx_bus_brake.mp3", "/assets/AUDIO/sfx_bus_brake.wav");
        headThudClip = loadAudioClip("/assets/AUDIO/sfx_head_thud.mp3", null);
        headThudSecondaryClip = loadAudioClip("/assets/AUDIO/sfx_head_thud2.mp3", null);
        phoneNotificationClip = loadAudioClip("/assets/AUDIO/sfx_phone_notification.mp3", null);
    }

    private AudioClip loadAudioClip(String primaryPath, String fallbackPath) {
        try {
            URL url = getClass().getResource(primaryPath);
            if (url == null && fallbackPath != null) {
                url = getClass().getResource(fallbackPath);
            }
            if (url != null) {
                return new AudioClip(url.toExternalForm());
            }
        } catch (Exception e) {
            System.err.println("Notice: Could not load SFX " + primaryPath + ": " + e.getMessage());
        }
        return null;
    }

    public void playTextBlip() {
        if (!sfxMuted && textBlipClip != null) {
            textBlipClip.play(sfxVolume * 0.35);
        }
    }

    public void playClickSfx() {
        if (!sfxMuted && clickClip != null) {
            clickClip.play(sfxVolume * 0.75);
        }
    }

    public void playHoverSfx() {
        if (!sfxMuted && hoverClip != null) {
            hoverClip.play(sfxVolume * 0.40);
        }
    }

    public void playBusHornSfx() {
        if (!sfxMuted && busHornClip != null) {
            busHornClip.play(Math.min(1.0, sfxVolume * 1.45));
        }
    }

    public void playBrakeSfx() {
        if (!sfxMuted && brakeClip != null) {
            brakeClip.play(Math.min(1.0, sfxVolume * 2.0));
        }
    }

    public void playHeadThudSfx() {
        if (!sfxMuted && headThudClip != null) {
            headThudClip.play(Math.min(1.0, sfxVolume * 1.65));
        }
    }

    public void playHeadThudSecondarySfx() {
        if (!sfxMuted && headThudSecondaryClip != null) {
            headThudSecondaryClip.play(Math.min(1.0, sfxVolume * 1.45));
        }
    }

    public void playPhoneNotificationSfx() {
        if (!sfxMuted && phoneNotificationClip != null) {
            phoneNotificationClip.play(sfxVolume * 0.70);
        }
    }

    public boolean isSfxMuted() {
        return sfxMuted;
    }

    public void setSfxMuted(boolean muted) {
        this.sfxMuted = muted;
        syncAmbientAudioState();
    }

    public double getSfxVolume() {
        return sfxVolume;
    }

    public void setSfxVolume(double volume) {
        this.sfxVolume = Math.max(0.0, Math.min(1.0, volume));
        if (this.sfxVolume > 0.0 && sfxMuted) {
            sfxMuted = false;
        }
        syncAmbientAudioState();
    }

    public int getTypingDelayMs() {
        return typingDelayMs;
    }

    public void setTypingDelayMs(int delayMs) {
        this.typingDelayMs = Math.max(6, Math.min(45, delayMs));
    }

    public boolean isAtmosphereEnabled() {
        return atmosphereEnabled;
    }

    public void setAtmosphereEnabled(boolean enabled) {
        this.atmosphereEnabled = enabled;
        syncAmbientAudioState();
    }

    public boolean isCgPlaceholderEnabled() {
        return cgPlaceholderEnabled;
    }

    public void setCgPlaceholderEnabled(boolean enabled) {
        this.cgPlaceholderEnabled = enabled;
    }

    // Loads and loops the main menu background track on startup.
    private void startBackgroundMusic() {
        switchBackgroundMusic(BGM_MENU_PATH);
    }

    // Stops and disposes the active background music track (e.g. tense silence before the sudden brake).
    public void stopBackgroundMusic() {
        if (bgmPlayer != null) {
            bgmPlayer.stop();
            bgmPlayer.dispose();
            bgmPlayer = null;
        }
        currentBgmPath = null;
    }

    // Switches the looping background music track across menus, dialogue lanes, sudden brake, and endings.
    public void switchBackgroundMusic(String resourcePath) {
        if (resourcePath == null) {
            return;
        }
        if (bgmPlayer != null && resourcePath.equals(currentBgmPath)) {
            return;
        }
        stopBackgroundMusic();
        currentBgmPath = resourcePath;
        try {
            URL audioUrl = getClass().getResource(resourcePath);
            if (audioUrl == null) {
                System.err.println("Notice: Could not find background music at " + resourcePath);
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

    // Starts the continuous rain + bus engine ambient loop for scenes S01-S08.
    public void startBusAmbience() {
        ambientActive = true;
        try {
            if (ambientPlayer == null) {
                URL audioUrl = getClass().getResource(AMBIENT_RAIN_BUS_PATH);
                if (audioUrl == null) {
                    System.err.println("Notice: Could not find ambient loop at " + AMBIENT_RAIN_BUS_PATH);
                    return;
                }
                Media media = new Media(audioUrl.toExternalForm());
                ambientPlayer = new MediaPlayer(media);
                ambientPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            }
            syncAmbientAudioState();
            ambientPlayer.play();
        } catch (Exception e) {
            System.err.println("Notice: Could not play bus ambience: " + e.getMessage());
        }
    }

    // Pauses the bus/rain ambient loop outside of the encounter scenes.
    public void stopBusAmbience() {
        ambientActive = false;
        if (ambientPlayer != null) {
            ambientPlayer.pause();
        }
    }

    private void syncAmbientAudioState() {
        if (ambientPlayer != null) {
            ambientPlayer.setVolume(sfxVolume * AMBIENT_RAIN_SCALE);
            ambientPlayer.setMute(!ambientActive || sfxMuted || !atmosphereEnabled);
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
        stopBackgroundMusic();
        if (ambientPlayer != null) {
            ambientPlayer.stop();
            ambientPlayer.dispose();
            ambientPlayer = null;
        }
    }

    // Switches the screen to the character setup / start view
    public void showStart() {
        stopBusAmbience();
        switchBackgroundMusic(BGM_MENU_PATH);
        this.stats = new RelationshipStats();
        this.momentum = new Momentum();
        this.activeEncounterView = null;

        StartView startView = new StartView(this);

        if (activeScene == null) {
            Rectangle2D bounds = Screen.getPrimary().getBounds();
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
        activeEncounterView = new EncounterView(this, stats, momentum, encounter);
        activeScene.setRoot(activeEncounterView);
    }

    // Returns whether the active encounter has saved steps to rewind to from EndingView
    public boolean canReturnToEncounter() {
        return activeEncounterView != null && activeEncounterView.canStepBack();
    }

    // Rewinds from EndingView back to the last encounter beat/choice for debug testing
    public void returnToEncounter() {
        if (activeEncounterView == null || activeScene == null) {
            showStart();
            return;
        }
        activeEncounterView.restoreFromEnding();
        activeScene.setRoot(activeEncounterView);
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
