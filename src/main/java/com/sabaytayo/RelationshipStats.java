package com.sabaytayo;

import java.util.Arrays;

// This class manages and clamps the four relationship stats between the player and stranger.
public class RelationshipStats {

    // Constants for the index of each stat
    public static final int TRUST = 0;
    public static final int ATTRACTION = 1;
    public static final int RESPECT = 2;
    public static final int ANNOYANCE = 3;

    // Stat labels used for the ending screen and debugging
    private static final String[] LABELS = {
        "Trust", "Attraction", "Respect", "Annoyance"
    };

    // Array storing the current values: [Trust, Attraction, Respect, Annoyance]
    private int[] values;

    // Constructor setting the starting values: Trust 40, Attraction 30, Respect 40, Annoyance 10
    public RelationshipStats() {
        this.values = new int[] { 40, 30, 40, 10 };
    }

    // Clamps any stat to the allowed range of 0 to 100
    private int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }

    // Bounded adjusters
    public void adjustTrust(int amount) {
        values[TRUST] = clamp(values[TRUST] + amount);
    }

    public void adjustAttraction(int amount) {
        values[ATTRACTION] = clamp(values[ATTRACTION] + amount);
    }

    public void adjustRespect(int amount) {
        values[RESPECT] = clamp(values[RESPECT] + amount);
    }

    public void adjustAnnoyance(int amount) {
        values[ANNOYANCE] = clamp(values[ANNOYANCE] + amount);
    }

    // Returns the value of a specific stat
    public int get(int stat) {
        if (stat < 0 || stat >= values.length) {
            throw new IllegalArgumentException("Invalid stat index: " + stat);
        }
        return values[stat];
    }

    public int getTrust() {
        return values[TRUST];
    }

    public int getAttraction() {
        return values[ATTRACTION];
    }

    public int getRespect() {
        return values[RESPECT];
    }

    public int getAnnoyance() {
        return values[ANNOYANCE];
    }

    // Returns a copy of the current stats array
    public int[] snapshot() {
        return Arrays.copyOf(values, values.length);
    }

    public static String[] getLabels() {
        return Arrays.copyOf(LABELS, LABELS.length);
    }

    // Prints stats to console for testing
    public void displayStats() {
        for (int i = 0; i < values.length; i++) {
            System.out.println(LABELS[i] + ": " + values[i]);
        }
    }
}
