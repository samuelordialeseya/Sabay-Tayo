// This custom exception is used when the player's name is invalid.
public class InvalidNameException extends Exception {

    // Receives the error message explaining why the name is invalid.
    public InvalidNameException(String message) {
        super(message);
    }
}