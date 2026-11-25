package com.sabaytayo;

import java.util.Random;
import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;

// Lightweight ambient overlay rendering diagonal twilight raindrops and passing streetlight sweeps.
public class BusAtmosphereLayer extends Pane {

    private static final int DROP_COUNT = 34;

    private final Canvas canvas;
    private final double[] dropX = new double[DROP_COUNT];
    private final double[] dropY = new double[DROP_COUNT];
    private final double[] dropLen = new double[DROP_COUNT];
    private final double[] dropSpeed = new double[DROP_COUNT];
    private final double[] dropAlpha = new double[DROP_COUNT];
    private final Random random = new Random();

    private double lightSweepPos = -0.35;
    private long lastNanos = 0;
    private boolean enabled = true;
    private boolean initialized = false;

    private final AnimationTimer timer;

    public BusAtmosphereLayer() {
        setMouseTransparent(true);
        setPickOnBounds(false);

        canvas = new Canvas();
        canvas.widthProperty().bind(widthProperty());
        canvas.heightProperty().bind(heightProperty());
        getChildren().add(canvas);

        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (!enabled) {
                    return;
                }
                if (lastNanos == 0) {
                    lastNanos = now;
                    return;
                }
                double dt = (now - lastNanos) / 1_000_000_000.0;
                lastNanos = now;
                if (dt > 0.1) {
                    dt = 0.033;
                }
                updateAndRender(dt);
            }
        };
    }

    public void start() {
        lastNanos = 0;
        if (enabled) {
            timer.start();
        }
    }

    public void stop() {
        timer.stop();
        lastNanos = 0;
    }

    public void setAtmosphereEnabled(boolean active) {
        this.enabled = active;
        setVisible(active);
        if (active) {
            start();
        } else {
            stop();
            GraphicsContext gc = canvas.getGraphicsContext2D();
            gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        }
    }

    public boolean isAtmosphereEnabled() {
        return enabled;
    }

    private void initDrops(double w, double h) {
        for (int i = 0; i < DROP_COUNT; i++) {
            dropX[i] = random.nextDouble() * w;
            dropY[i] = random.nextDouble() * h;
            dropLen[i] = 14 + random.nextDouble() * 24;
            dropSpeed[i] = 220 + random.nextDouble() * 210;
            dropAlpha[i] = 0.06 + random.nextDouble() * 0.12;
        }
        initialized = true;
    }

    private void updateAndRender(double dt) {
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        if (w <= 0 || h <= 0) {
            return;
        }

        if (!initialized) {
            initDrops(w, h);
        }

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, w, h);

        // 1. Subtle passing warm amber streetlight glow sweeping across the bus cabin
        lightSweepPos += dt * 0.14;
        if (lightSweepPos > 1.45) {
            lightSweepPos = -0.45;
        }
        double bandCenter = lightSweepPos * w;
        double bandHalf = w * 0.16;
        if (bandCenter + bandHalf > 0 && bandCenter - bandHalf < w) {
            LinearGradient glow = new LinearGradient(
                    bandCenter - bandHalf, 0,
                    bandCenter + bandHalf, h * 0.45,
                    false, CycleMethod.NO_CYCLE,
                    new Stop(0.0, Color.color(0.98, 0.72, 0.52, 0.0)),
                    new Stop(0.5, Color.color(0.98, 0.72, 0.52, 0.045)),
                    new Stop(1.0, Color.color(0.98, 0.72, 0.52, 0.0))
            );
            gc.setFill(glow);
            gc.fillRect(0, 0, w, h);
        }

        // 2. Soft diagonal rain streaks drifting across the twilight view
        gc.setLineWidth(1.25);
        for (int i = 0; i < DROP_COUNT; i++) {
            dropY[i] += dropSpeed[i] * dt;
            dropX[i] -= dropSpeed[i] * 0.24 * dt;

            if (dropY[i] > h + 30 || dropX[i] < -30) {
                dropY[i] = -random.nextDouble() * 90;
                dropX[i] = random.nextDouble() * (w + 180);
            }

            gc.setStroke(Color.color(0.95, 0.88, 0.96, dropAlpha[i]));
            gc.strokeLine(dropX[i], dropY[i], dropX[i] - dropLen[i] * 0.24, dropY[i] + dropLen[i]);
        }
    }
}
