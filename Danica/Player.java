
public class Player extends Character {

    public Player(String name, Pronouns pronouns)
            throws InvalidNameException {

        super(validateName(name), pronouns);
    }

    public static String validateName(String name)
            throws InvalidNameException {

        if (name == null || name.trim().isEmpty()) {
            throw new InvalidNameException(
                "Name cannot be empty."
            );
        }

        name = name.trim();

        if (name.length() > 20) {
            throw new InvalidNameException(
                "Name cannot exceed 20 characters."
            );
        }

        if (name.matches(".*\\d.*")) {
            throw new InvalidNameException(
                "Name cannot contain numbers."
            );
        }

        return name;
    }
}
