// This enum stores the pronoun choices of the characters.
public enum Pronouns {

    // These are the available pronoun choices.
    HE_HIM("He", "Him", "His"),
    SHE_HER("She", "Her", "Her"),
    THEY_THEM("They", "Them", "Their");

    // These variables store the different forms of pronouns.
    private String subject;
    private String object;
    private String possessive;

    // This constructor assigns the pronoun values.
    Pronouns(String subject, String object, String possessive) {
        this.subject = subject;
        this.object = object;
        this.possessive = possessive;
    }

    // Returns the subject pronoun.
    public String getSubject() {
        return subject;
    }

    // Returns the object pronoun.
    public String getObject() {
        return object;
    }

    // Returns the possessive pronoun.
    public String getPossessive() {
        return possessive;
    }
}