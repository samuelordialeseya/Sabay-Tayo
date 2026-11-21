package com.sabaytayo;

// Represents the seatmate stranger on the bus.
public class Stranger extends Character {

    public Stranger(Pronouns pronouns) {
        super("Stranger", pronouns);
    }

    public Stranger(String name, Pronouns pronouns) {
        super((name == null || name.trim().isEmpty()) ? "Stranger" : name.trim(), pronouns);
    }
}
