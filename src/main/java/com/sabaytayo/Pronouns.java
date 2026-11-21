package com.sabaytayo;

// Enum containing the supported pronoun configurations for characters.
public enum Pronouns {

    HE_HIM("He/Him", "he", "him", "his"),
    SHE_HER("She/Her", "she", "her", "her"),
    THEY_THEM("They/Them", "they", "them", "their");

    private final String displayLabel;
    private final String subject;
    private final String object;
    private final String possessive;

    Pronouns(String displayLabel, String subject, String object, String possessive) {
        this.displayLabel = displayLabel;
        this.subject = subject;
        this.object = object;
        this.possessive = possessive;
    }

    // Returns the UI-friendly label (e.g., "He/Him", "She/Her", "They/Them").
    public String getDisplayLabel() {
        return displayLabel;
    }

    // Returns the subject pronoun (he, she, they).
    public String getSubject() {
        return subject;
    }

    // Returns the object pronoun (him, her, them).
    public String getObject() {
        return object;
    }

    // Returns the possessive pronoun (his, her, their).
    public String getPossessive() {
        return possessive;
    }

    // Finds a Pronouns constant from its display label; defaults to THEY_THEM if unmatched.
    public static Pronouns fromLabel(String label) {
        if (label != null) {
            for (Pronouns p : values()) {
                if (p.displayLabel.equalsIgnoreCase(label.trim())) {
                    return p;
                }
            }
        }
        return THEY_THEM;
    }

    @Override
    public String toString() {
        return displayLabel;
    }
}
