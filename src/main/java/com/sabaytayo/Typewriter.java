package com.sabaytayo;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Label;
import javafx.util.Duration;

// Types dialogue text into a label one character at a time (with text blip sound).
public class Typewriter {

    private final GameApplication app;
    private Timeline timeline;
    private Label activeLabel;
    private String activeText;
    private Runnable onFinished;

    public Typewriter(GameApplication app) {
        this.app = app;
    }

    public boolean isTyping() {
        return timeline != null;
    }

    public void play(Label targetLabel, String fullText, Runnable afterDone) {
        stop();

        if (fullText == null || fullText.isEmpty()) {
            targetLabel.setText("");
            if (afterDone != null) {
                afterDone.run();
            }
            return;
        }

        activeLabel = targetLabel;
        activeText = fullText;
        onFinished = afterDone;
        targetLabel.setText("");

        final int[] charIndex = { 0 };
        timeline = new Timeline(new KeyFrame(Duration.millis(app.getTypingDelayMs()), e -> {
            charIndex[0]++;
            targetLabel.setText(fullText.substring(0, charIndex[0]));
            if (charIndex[0] % 3 == 1 && !java.lang.Character.isWhitespace(fullText.charAt(charIndex[0] - 1))) {
                app.playTextBlip();
            }
        }));
        timeline.setCycleCount(fullText.length());
        timeline.setOnFinished(e -> {
            timeline = null;
            activeLabel = null;
            activeText = null;
            runCallback();
        });
        timeline.play();
    }

    // Skips to the full text right away and runs the finish callback.
    public void finishNow() {
        if (timeline == null) {
            return;
        }
        timeline.stop();
        timeline = null;

        if (activeLabel != null && activeText != null) {
            activeLabel.setText(activeText);
        }
        activeLabel = null;
        activeText = null;
        runCallback();
    }

    // Cancels typing without running the finish callback.
    public void stop() {
        if (timeline != null) {
            timeline.stop();
            timeline = null;
        }
        activeLabel = null;
        activeText = null;
        onFinished = null;
    }

    private void runCallback() {
        if (onFinished != null) {
            Runnable done = onFinished;
            onFinished = null;
            done.run();
        }
    }
}
