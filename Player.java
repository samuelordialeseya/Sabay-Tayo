public class Player extends Character {

    // This constructor will receive the player's name and pronouns.
    public Player(String name, Pronouns pronouns)
            throws InvalidNameException {

        // I will pass the name and pronouns to the Character class.
        super(name, pronouns);

        // I will check if the player's name follows the rules.
        validateName(name);
    }

    // I will use this method to check if the player's name is valid.
    public static void validateName(String name)
            throws InvalidNameException {

        // I will check if the name is empty or only contains spaces.

        // I will check if the name is longer than 20 characters.

        // I will check if the name contains any numbers.

        // If the name is invalid, I will throw an InvalidNameException.
    }
}
