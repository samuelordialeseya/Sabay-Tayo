package com.sabaytayo;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

// Ending screen: cinematically reveals the outcome's 16:9 Ending CG first, then animates in the stats card and badge.
public class EndingView extends StackPane {

    private static final double LETTERBOX_HEIGHT = 42.0;

    private final BusAtmosphereLayer atmosphereLayer;
    private final ImageView bgView;
    private final Region toneOverlay;
    private final Region topLetterbox;
    private final Region bottomLetterbox;
    private final Region dimOverlay;
    private final Region introCurtain;
    private final StackPane cardWrapper;
    private final VBox card;
    private final Label cgPromptLabel;
    private final StackPane cgPlaceholderOverlay;
    private final Button toggleCardBtn;
    private final boolean hasEndingCg;
    private final boolean showMissingPlaceholder;
    private final Outcome outcome;
    private boolean showingResults = false;

    private Timeline revealTimeline;
    private Timeline cameraDriftTimeline;
    private Timeline promptPulseTimeline;
    private Timeline cardAnimTimeline;

    public EndingView(GameApplication app, RelationshipStats stats, Momentum momentum) {
        OutcomeClassifier classifier = new OutcomeClassifier();
        this.outcome = classifier.classify(stats);

        // Clip root so subtle Ken Burns camera scaling never leaks outside bounds
        Rectangle rootClip = new Rectangle();
        rootClip.widthProperty().bind(widthProperty());
        rootClip.heightProperty().bind(heightProperty());
        setClip(rootClip);

        app.stopBusAmbience();
        app.switchBackgroundMusic(outcome.isColdTone()
                ? GameApplication.BGM_COLD_PATH
                : GameApplication.BGM_WARM_PATH);
        if (outcome.playsChimeOnReveal()) {
            app.playPhoneNotificationSfx();
        }

        // 1. Outcome-specific 16:9 Ending CG (falls back to empty night bus background if missing)
        Image endingCg = ImageLoader.load(outcome.getEndingCgPath());
        this.hasEndingCg = (endingCg != null);
        Image bgImage = hasEndingCg ? endingCg : ImageLoader.load("/assets/BACKGROUNDS/bg_bus_night_empty.png");

        bgView = new ImageView();
        bgView.setPreserveRatio(true);
        bgView.setSmooth(false);
        if (bgImage != null) {
            bgView.setImage(bgImage);
        }

        widthProperty().addListener((obs, oldW, newW) -> updateCoverFit());
        heightProperty().addListener((obs, oldH, newH) -> updateCoverFit());

        atmosphereLayer = new BusAtmosphereLayer();
        atmosphereLayer.setAtmosphereEnabled(app.isAtmosphereEnabled());

        // Subtle warm/cool emotional wash that eases out as the Ending CG settles
        toneOverlay = new Region();
        if (outcome.playsChimeOnReveal()) {
            toneOverlay.setStyle("-fx-background-color: radial-gradient(center 50% 45%, radius 75%, "
                    + "rgba(255, 214, 174, 0.28) 0%, rgba(255, 168, 138, 0.14) 65%, rgba(20, 12, 24, 0.0) 100%);");
        } else if (outcome.isColdTone()) {
            toneOverlay.setStyle("-fx-background-color: radial-gradient(center 50% 50%, radius 75%, "
                    + "rgba(74, 98, 146, 0.18) 0%, rgba(24, 34, 62, 0.28) 100%);");
        } else {
            toneOverlay.setStyle("-fx-background-color: radial-gradient(center 50% 45%, radius 75%, "
                    + "rgba(248, 206, 168, 0.18) 0%, rgba(18, 12, 26, 0.10) 100%);");
        }
        toneOverlay.setOpacity(0.0);
        toneOverlay.setMouseTransparent(true);

        // Cinematic letterbox bars framing Phase 1 (Ending CG showcase)
        topLetterbox = createLetterboxBar();
        bottomLetterbox = createLetterboxBar();

        dimOverlay = new Region();
        dimOverlay.setStyle("-fx-background-color: rgba(12, 9, 20, 0.56);");
        dimOverlay.setOpacity(0.0);
        dimOverlay.setMouseTransparent(true);

        // Black intro curtain that lifts smoothly once EndingView mounts on stage
        introCurtain = new Region();
        introCurtain.setStyle("-fx-background-color: #000000;");
        introCurtain.setOpacity(0.0);
        introCurtain.setVisible(false);
        introCurtain.setMouseTransparent(true);

        // Optional Dev placeholder if the Ending CG is not yet drawn and Dev placeholders are ON
        cgPlaceholderOverlay = buildMissingCgPlaceholder(outcome);
        this.showMissingPlaceholder = !hasEndingCg && app.isCgPlaceholderEnabled();
        cgPlaceholderOverlay.setVisible(showMissingPlaceholder);

        // Subtle prompt during Phase 1 (full-screen Ending CG reveal)
        cgPromptLabel = new Label("Click or press Space to view results  ▶");
        cgPromptLabel.getStyleClass().add("cg-continue-pill");
        cgPromptLabel.setMouseTransparent(true);

        // 2. Center result card with commemorative badge overlapping the top border, stats, momentum, and Ride Again
        Image badgeImg = ImageLoader.load(outcome.getBadgePath());
        ImageView badgeView = new ImageView();
        badgeView.setFitWidth(96);
        badgeView.setFitHeight(96);
        badgeView.setPreserveRatio(true);
        badgeView.setSmooth(true);
        badgeView.setMouseTransparent(true);
        if (badgeImg != null) {
            badgeView.setImage(badgeImg);
            badgeView.setTranslateY(-48);
        } else {
            badgeView.setVisible(false);
            badgeView.setManaged(false);
        }

        double topPadding = (badgeImg != null) ? 64 : 36;
        card = new VBox(16);
        card.setMaxWidth(Double.MAX_VALUE);
        card.setMaxHeight(Region.USE_PREF_SIZE);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(topPadding, 52, 36, 52));
        card.getStyleClass().add("dialogue-box");

        Label title = new Label(outcome.getTitle());
        title.getStyleClass().add("title-header");

        Label desc = new Label(outcome.getDescription());
        desc.getStyleClass().add("sub-header");
        desc.setWrapText(true);
        desc.setAlignment(Pos.CENTER);

        Label ruleLabel = new Label(outcome.getRuleSummary());
        ruleLabel.getStyleClass().add("rule-text");

        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(20);
        statsGrid.setVgap(12);
        statsGrid.setAlignment(Pos.CENTER);
        statsGrid.setPadding(new Insets(6, 0, 6, 0));

        addStatBarRow(statsGrid, 0, "Trust", stats.getTrust(), false);
        addStatBarRow(statsGrid, 1, "Attraction", stats.getAttraction(), false);
        addStatBarRow(statsGrid, 2, "Respect", stats.getRespect(), false);
        addStatBarRow(statsGrid, 3, "Annoyance", stats.getAnnoyance(), true);

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

        cardWrapper = new StackPane(card, badgeView);
        cardWrapper.maxWidthProperty().bind(Bindings.min(widthProperty().multiply(0.56), 740));
        cardWrapper.setMaxHeight(Region.USE_PREF_SIZE);
        StackPane.setAlignment(badgeView, Pos.TOP_CENTER);

        // Top-right toolbar: Debug Back + View CG / Results toggle + Window controls
        Button backToRideBtn = new Button("← Back (Debug)");
        backToRideBtn.getStyleClass().add("vn-sub-button");
        backToRideBtn.setFocusTraversable(false);
        boolean canGoBack = app.canReturnToEncounter();
        backToRideBtn.setVisible(canGoBack);
        backToRideBtn.setManaged(canGoBack);
        backToRideBtn.setOnMouseEntered(e -> app.playHoverSfx());
        backToRideBtn.setOnAction(e -> {
            app.playClickSfx();
            app.returnToEncounter();
        });

        toggleCardBtn = new Button("View CG");
        toggleCardBtn.getStyleClass().add("vn-sub-button");
        toggleCardBtn.setFocusTraversable(false);
        toggleCardBtn.setVisible(hasEndingCg || showMissingPlaceholder);
        toggleCardBtn.setManaged(hasEndingCg || showMissingPlaceholder);
        toggleCardBtn.setOnAction(e -> {
            app.playClickSfx();
            setResultsVisible(!showingResults, true);
        });

        HBox topControls = new HBox(12, backToRideBtn, toggleCardBtn, app.createWindowControlBar());
        topControls.setAlignment(Pos.CENTER_RIGHT);
        topControls.setMaxWidth(Region.USE_PREF_SIZE);
        topControls.setMaxHeight(Region.USE_PREF_SIZE);

        // Start in Phase 1 (CG-first) when an Ending CG or Dev placeholder is active; else show card immediately
        boolean startInCgPhase = hasEndingCg || showMissingPlaceholder;
        setResultsVisible(!startInCgPhase, false);

        setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY && !showingResults && !isInteractiveControl(e.getTarget())) {
                app.playClickSfx();
                setResultsVisible(true, true);
            }
        });

        EventHandler<KeyEvent> keyHandler = e -> {
            if ((e.getCode() == KeyCode.BACK_SPACE || e.getCode() == KeyCode.LEFT) && app.canReturnToEncounter()) {
                e.consume();
                app.playClickSfx();
                app.returnToEncounter();
            } else if ((e.getCode() == KeyCode.SPACE || e.getCode() == KeyCode.ENTER) && !showingResults) {
                e.consume();
                app.playClickSfx();
                setResultsVisible(true, true);
            }
        };

        sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (oldScene != null) {
                oldScene.removeEventFilter(KeyEvent.KEY_PRESSED, keyHandler);
                atmosphereLayer.stop();
                stopEndingAnimations();
            }
            if (newScene != null) {
                newScene.addEventFilter(KeyEvent.KEY_PRESSED, keyHandler);
                atmosphereLayer.start();
                updateCoverFit();
                if (startInCgPhase) {
                    playEndingCgReveal();
                }
            }
        });

        getChildren().addAll(
                bgView, atmosphereLayer, toneOverlay,
                topLetterbox, bottomLetterbox,
                cgPlaceholderOverlay, dimOverlay, cardWrapper,
                cgPromptLabel, introCurtain, topControls
        );
        StackPane.setAlignment(topLetterbox, Pos.TOP_CENTER);
        StackPane.setAlignment(bottomLetterbox, Pos.BOTTOM_CENTER);
        StackPane.setAlignment(cgPromptLabel, Pos.BOTTOM_CENTER);
        StackPane.setMargin(cgPromptLabel, new Insets(0, 0, 56, 0));
        StackPane.setAlignment(topControls, Pos.TOP_RIGHT);
        StackPane.setMargin(topControls, new Insets(20, 28, 0, 0));
    }

    private Region createLetterboxBar() {
        Region bar = new Region();
        bar.setStyle("-fx-background-color: rgba(8, 5, 14, 0.94);");
        bar.setMaxWidth(Double.MAX_VALUE);
        bar.setPrefHeight(0.0);
        bar.setMinHeight(0.0);
        bar.setMaxHeight(Region.USE_PREF_SIZE);
        bar.setMouseTransparent(true);
        return bar;
    }

    // Plays the initial curtain lift, warm/cold bloom, letterbox framing, and Ken Burns camera drift.
    private void playEndingCgReveal() {
        stopEndingAnimations();

        introCurtain.setOpacity(1.0);
        introCurtain.setVisible(true);

        topLetterbox.setPrefHeight(LETTERBOX_HEIGHT);
        bottomLetterbox.setPrefHeight(LETTERBOX_HEIGHT);

        toneOverlay.setOpacity(1.0);
        cgPromptLabel.setOpacity(0.0);

        double startScale = hasEndingCg ? 1.09 : 1.02;
        double startY = outcome.isColdTone() ? 8.0 : -8.0;
        bgView.setScaleX(startScale);
        bgView.setScaleY(startScale);
        bgView.setTranslateY(startY);

        int curtainLiftMs = outcome.isColdTone() ? 1450 : 1200;

        revealTimeline = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(introCurtain.opacityProperty(), 1.0),
                        new KeyValue(toneOverlay.opacityProperty(), 1.0),
                        new KeyValue(cgPromptLabel.opacityProperty(), 0.0)),
                new KeyFrame(Duration.millis(curtainLiftMs),
                        new KeyValue(introCurtain.opacityProperty(), 0.0, Interpolator.EASE_OUT),
                        new KeyValue(toneOverlay.opacityProperty(), 0.0, Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.millis(curtainLiftMs + 420),
                        new KeyValue(cgPromptLabel.opacityProperty(), 1.0, Interpolator.EASE_OUT))
        );
        revealTimeline.setOnFinished(e -> {
            revealTimeline = null;
            introCurtain.setVisible(false);
            startPromptPulse();
        });
        revealTimeline.play();

        cameraDriftTimeline = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(bgView.scaleXProperty(), startScale),
                        new KeyValue(bgView.scaleYProperty(), startScale),
                        new KeyValue(bgView.translateYProperty(), startY)),
                new KeyFrame(Duration.millis(4200),
                        new KeyValue(bgView.scaleXProperty(), 1.01, Interpolator.EASE_OUT),
                        new KeyValue(bgView.scaleYProperty(), 1.01, Interpolator.EASE_OUT),
                        new KeyValue(bgView.translateYProperty(), 0.0, Interpolator.EASE_OUT))
        );
        cameraDriftTimeline.setOnFinished(e -> cameraDriftTimeline = null);
        cameraDriftTimeline.play();
    }

    private void startPromptPulse() {
        if (promptPulseTimeline != null) {
            promptPulseTimeline.stop();
        }
        if (showingResults) {
            return;
        }
        promptPulseTimeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(cgPromptLabel.opacityProperty(), 1.0)),
                new KeyFrame(Duration.millis(1100), new KeyValue(cgPromptLabel.opacityProperty(), 0.52, Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.millis(2200), new KeyValue(cgPromptLabel.opacityProperty(), 1.0, Interpolator.EASE_BOTH))
        );
        promptPulseTimeline.setCycleCount(Timeline.INDEFINITE);
        promptPulseTimeline.play();
    }

    private void stopEndingAnimations() {
        if (revealTimeline != null) {
            revealTimeline.stop();
            revealTimeline = null;
        }
        if (cameraDriftTimeline != null) {
            cameraDriftTimeline.stop();
            cameraDriftTimeline = null;
        }
        if (promptPulseTimeline != null) {
            promptPulseTimeline.stop();
            promptPulseTimeline = null;
        }
        if (cardAnimTimeline != null) {
            cardAnimTimeline.stop();
            cardAnimTimeline = null;
        }
        introCurtain.setVisible(false);
        introCurtain.setOpacity(0.0);
    }

    private void setResultsVisible(boolean visible, boolean animate) {
        showingResults = visible;
        toggleCardBtn.setText(visible ? "View CG" : "Show Results");
        cgPlaceholderOverlay.setVisible(!visible && showMissingPlaceholder);

        if (cardAnimTimeline != null) {
            cardAnimTimeline.stop();
            cardAnimTimeline = null;
        }

        if (!animate) {
            cardWrapper.setVisible(visible);
            cardWrapper.setOpacity(visible ? 1.0 : 0.0);
            cardWrapper.setTranslateY(0.0);
            cardWrapper.setScaleX(1.0);
            cardWrapper.setScaleY(1.0);
            dimOverlay.setOpacity(visible ? 1.0 : 0.0);
            topLetterbox.setPrefHeight(visible ? 0.0 : LETTERBOX_HEIGHT);
            bottomLetterbox.setPrefHeight(visible ? 0.0 : LETTERBOX_HEIGHT);
            cgPromptLabel.setVisible(!visible);
            return;
        }

        // If user clicks early during the opening curtain lift, complete the curtain lift smoothly
        if (visible && revealTimeline != null) {
            revealTimeline.stop();
            revealTimeline = null;
            introCurtain.setVisible(false);
            introCurtain.setOpacity(0.0);
            toneOverlay.setOpacity(0.0);
        }

        if (visible) {
            if (promptPulseTimeline != null) {
                promptPulseTimeline.stop();
                promptPulseTimeline = null;
            }
            cgPromptLabel.setVisible(false);
            cardWrapper.setVisible(true);
            cardWrapper.setOpacity(0.0);
            cardWrapper.setTranslateY(18.0);
            cardWrapper.setScaleX(0.97);
            cardWrapper.setScaleY(0.97);

            cardAnimTimeline = new Timeline(
                    new KeyFrame(Duration.ZERO,
                            new KeyValue(cardWrapper.opacityProperty(), 0.0),
                            new KeyValue(cardWrapper.translateYProperty(), 18.0),
                            new KeyValue(cardWrapper.scaleXProperty(), 0.97),
                            new KeyValue(cardWrapper.scaleYProperty(), 0.97),
                            new KeyValue(dimOverlay.opacityProperty(), dimOverlay.getOpacity()),
                            new KeyValue(topLetterbox.prefHeightProperty(), topLetterbox.getPrefHeight()),
                            new KeyValue(bottomLetterbox.prefHeightProperty(), bottomLetterbox.getPrefHeight())),
                    new KeyFrame(Duration.millis(320),
                            new KeyValue(cardWrapper.opacityProperty(), 1.0, Interpolator.EASE_OUT),
                            new KeyValue(cardWrapper.translateYProperty(), 0.0, Interpolator.EASE_OUT),
                            new KeyValue(cardWrapper.scaleXProperty(), 1.0, Interpolator.EASE_OUT),
                            new KeyValue(cardWrapper.scaleYProperty(), 1.0, Interpolator.EASE_OUT),
                            new KeyValue(dimOverlay.opacityProperty(), 1.0, Interpolator.EASE_OUT),
                            new KeyValue(topLetterbox.prefHeightProperty(), 0.0, Interpolator.EASE_BOTH),
                            new KeyValue(bottomLetterbox.prefHeightProperty(), 0.0, Interpolator.EASE_BOTH))
            );
            cardAnimTimeline.setOnFinished(e -> cardAnimTimeline = null);
            cardAnimTimeline.play();
        } else {
            cgPromptLabel.setOpacity(1.0);
            cgPromptLabel.setVisible(true);
            startPromptPulse();

            cardAnimTimeline = new Timeline(
                    new KeyFrame(Duration.ZERO,
                            new KeyValue(cardWrapper.opacityProperty(), cardWrapper.getOpacity()),
                            new KeyValue(cardWrapper.translateYProperty(), cardWrapper.getTranslateY()),
                            new KeyValue(dimOverlay.opacityProperty(), dimOverlay.getOpacity()),
                            new KeyValue(topLetterbox.prefHeightProperty(), topLetterbox.getPrefHeight()),
                            new KeyValue(bottomLetterbox.prefHeightProperty(), bottomLetterbox.getPrefHeight())),
                    new KeyFrame(Duration.millis(260),
                            new KeyValue(cardWrapper.opacityProperty(), 0.0, Interpolator.EASE_IN),
                            new KeyValue(cardWrapper.translateYProperty(), 14.0, Interpolator.EASE_IN),
                            new KeyValue(dimOverlay.opacityProperty(), 0.0, Interpolator.EASE_OUT),
                            new KeyValue(topLetterbox.prefHeightProperty(), LETTERBOX_HEIGHT, Interpolator.EASE_BOTH),
                            new KeyValue(bottomLetterbox.prefHeightProperty(), LETTERBOX_HEIGHT, Interpolator.EASE_BOTH))
            );
            cardAnimTimeline.setOnFinished(e -> {
                cardAnimTimeline = null;
                cardWrapper.setVisible(false);
                cardWrapper.setTranslateY(0.0);
            });
            cardAnimTimeline.play();
        }
    }

    private StackPane buildMissingCgPlaceholder(Outcome outcome) {
        Label tag = new Label("MISSING ENDING CG  •  " + outcome.getTitle().toUpperCase());
        tag.getStyleClass().add("cg-placeholder-tag");

        Label file = new Label("src/main/resources" + outcome.getEndingCgPath());
        file.getStyleClass().add("cg-placeholder-file");
        file.setWrapText(true);

        Label desc = new Label(outcome.getEndingCgDescription());
        desc.getStyleClass().add("cg-placeholder-desc");
        desc.setWrapText(true);
        desc.setAlignment(Pos.CENTER);

        Label hint = new Label("Click or press Space to open Results & Badge  •  Drop 16:9 PNG to auto-activate");
        hint.getStyleClass().add("cg-placeholder-hint");
        hint.setWrapText(true);

        VBox box = new VBox(10, tag, file, desc, hint);
        box.getStyleClass().add("cg-placeholder-card");
        box.setAlignment(Pos.CENTER);
        box.maxWidthProperty().bind(Bindings.min(widthProperty().multiply(0.56), 680));
        box.setMaxHeight(Region.USE_PREF_SIZE);

        StackPane overlay = new StackPane(box);
        overlay.getStyleClass().add("cg-placeholder-backdrop");
        overlay.setAlignment(Pos.CENTER);
        overlay.setMouseTransparent(true);
        return overlay;
    }

    private void updateCoverFit() {
        double w = getWidth();
        double h = getHeight();
        Image img = bgView.getImage();
        if (w <= 0 || h <= 0 || img == null || img.getWidth() <= 0 || img.getHeight() <= 0) {
            return;
        }
        double imgRatio = img.getWidth() / img.getHeight();
        double screenRatio = w / h;
        if (screenRatio > imgRatio) {
            bgView.setFitWidth(w);
            bgView.setFitHeight(0);
        } else {
            bgView.setFitWidth(0);
            bgView.setFitHeight(h);
        }
    }

    private boolean isInteractiveControl(Object target) {
        if (!(target instanceof Node)) {
            return false;
        }
        Node cur = (Node) target;
        while (cur != null && cur != this) {
            if (cur instanceof ButtonBase) {
                return true;
            }
            cur = cur.getParent();
        }
        return false;
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
