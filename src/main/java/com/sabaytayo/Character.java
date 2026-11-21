package com.sabaytayo;

// Abstract parent class for characters in the encounter (Player and Stranger).
public abstract class Character {

    private final String name;
    private final Pronouns pronouns;

    public Character(String name, Pronouns pronouns) {
        if (pronouns == null) {
            throw new IllegalArgumentException("Pronouns cannot be null.");
        }
        this.name = name;
        this.pronouns = pronouns;
    }

    // Returns the character's name.
    public String getName() {
        return name;
    }

    // Returns the character's selected pronouns.
    public Pronouns getPronouns() {
        return pronouns;
    }
}
