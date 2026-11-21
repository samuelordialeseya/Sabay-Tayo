package com.sabaytayo;

// Custom checked exception thrown when a player's entered name is invalid.
public class InvalidNameException extends Exception {

    public InvalidNameException(String message) {
        super(message);
    }
}
