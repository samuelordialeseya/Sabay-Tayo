package com.sabaytayo;

// Abstract parent class for all possible relationship endings.
public abstract class Outcome {

    private final String title;
    private final String description;
    private final String ruleSummary;

    public Outcome(String title, String description, String ruleSummary) {
        this.title = title;
        this.description = description;
        this.ruleSummary = ruleSummary;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getRuleSummary() {
        return ruleSummary;
    }

    // Each concrete outcome defines its own stat threshold rules.
    public abstract boolean matches(RelationshipStats stats);

    // Resource path to the 16:9 Ending CG summarizing this outcome.
    public abstract String getEndingCgPath();

    // Resource path to the commemorative badge icon for this outcome.
    public abstract String getBadgePath();

    // Short shot description displayed when Dev CG placeholders are enabled and the PNG is missing.
    public abstract String getEndingCgDescription();

    // Resource path to the background music track played on this ending screen.
    public abstract String getEndingBgmPath();

    // Cold outcomes switch the ending visual tone and curtain pacing.
    public boolean isColdTone() {
        return false;
    }

    // Romantic outcome triggers a soft chime on reveal.
    public boolean playsChimeOnReveal() {
        return false;
    }
}
