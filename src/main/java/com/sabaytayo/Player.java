package com.sabaytayo;

// Represents the user-controlled commuter, enforcing name validation rules on creation.
public class Player extends Character {

    public Player(String name, Pronouns pronouns) throws InvalidNameException {
        super(validateName(name), pronouns);
    }

    // Validates and trims the player's name before creating a Player instance.
    public static String validateName(String name) throws InvalidNameException {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidNameException("Name cannot be empty.");
        }

        String trimmed = name.trim();

        if (trimmed.length() > 20) {
            throw new InvalidNameException("Name cannot exceed 20 characters.");
        }

        if (trimmed.matches(".*\\d.*")) {
            throw new InvalidNameException("Name cannot contain numbers.");
        }

        if (trimmed.contains("{") || trimmed.contains("}")) {
            throw new InvalidNameException("Name cannot contain curly braces.");
        }

        if (!trimmed.matches("[a-zA-Z]+")) {
            throw new InvalidNameException("Name can only contain letters.");
        }

        return trimmed;
    }
}
