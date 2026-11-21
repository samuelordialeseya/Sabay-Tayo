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
}
