// This class manages the relationship points between the player and stranger.
import java.util.Arrays;

public class RelationshipStats {

    // These constants represent the positions of each relationship stat.
    public static final int TRUST = 0;
    public static final int ATTRACTION = 1;
    public static final int RESPECT = 2;
    public static final int ANNOYANCE = 3;

    // These labels are used when displaying the relationship stats.
    private static final String[] LABELS = {
        "Trust", "Attraction", "Respect", "Annoyance"
    };

    // This array stores the current values of the relationship stats.
    private int[] values;

    // Sets the starting values of the relationship stats.
    public RelationshipStats() {
        values = new int[] {40, 30, 40, 10};
    }

    // Keeps each stat within the allowed range of 0 to 100.
    private int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }

    // Adds or subtracts points from Trust.
    public void adjustTrust(int amount) {
        values[TRUST] = clamp(values[TRUST] + amount);
    }

    // Adds or subtracts points from Attraction.
    public void adjustAttraction(int amount) {
        values[ATTRACTION] = clamp(values[ATTRACTION] + amount);
    }

    // Adds or subtracts points from Respect.
    public void adjustRespect(int amount) {
        values[RESPECT] = clamp(values[RESPECT] + amount);
    }

    // Adds or subtracts points from Annoyance.
    public void adjustAnnoyance(int amount) {
        values[ANNOYANCE] = clamp(values[ANNOYANCE] + amount);
    }

    // Returns the value of the selected stat.
    public int get(int stat) {

        // Checks if the given stat position is valid.
        if (stat < 0 || stat >= values.length) {
            throw new IllegalArgumentException("Invalid stat index.");
        }

        return values[stat];
    }

    // Creates a copy of the current stats.
    public int[] snapshot() {
        return Arrays.copyOf(values, values.length);
    }

    // Displays all the current relationship stats.
    public void displayStats() {
        for (int i = 0; i < values.length; i++) {
            System.out.println(LABELS[i] + ": " + values[i]);
        }
    }
}