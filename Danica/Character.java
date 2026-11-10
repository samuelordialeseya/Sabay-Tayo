// This is the parent class of Player and Stranger.
public class Character {

    // These are the common details of every character.
    private String name;
    private Pronouns pronouns;

    // This constructor sets the character's name and pronouns.
    public Character(String name, Pronouns pronouns) {
        this.name = name;
        this.pronouns = pronouns;
    }

    // Returns the character's name.
    public String getName() {
        return name;
    }

    // Returns the character's pronouns.
    public Pronouns getPronouns() {
        return pronouns;
    }
}