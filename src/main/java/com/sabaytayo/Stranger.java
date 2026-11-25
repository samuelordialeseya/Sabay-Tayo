package com.sabaytayo;

// Represents the seatmate stranger on the bus.
public class Stranger extends Character {

    public Stranger(Pronouns pronouns) {
        super("Fatima", pronouns);
    }

    public Stranger(String name, Pronouns pronouns) {
        super((name == null || name.trim().isEmpty()) ? "Fatima" : name.trim(), pronouns);
    }
}
