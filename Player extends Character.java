
public class Player extends Character {

    // Constructor for the player's name and pronouns
    public Player(String name, Pronouns pronouns)
            throws InvalidNameException {
        super(name, pronouns);
        validateName(name);
    }

    // Checks if the player's name follows our rules
    public static void validateName(String name)
            throws InvalidNameException {

        if (name == null || name.trim().isEmpty()) {
            // I will throw an error if the name is empty.
        }

        if (name.trim().length() > 20) {
            // I will throw an error if the name is too long.
        }

        if (name.matches(".*\\d.*")) {
            // I will throw an error if the name contains numbers.
        }
    }
}
