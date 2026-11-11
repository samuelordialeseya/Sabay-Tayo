
// Enum containing the three supported pronoun configurations.
public enum Pronouns {

    // Each configuration stores the subject, object, and possessive pronouns.
    HE_HIM("he", "him", "his"),
    SHE_HER("she", "her", "her"),
    THEY_THEM("they", "them", "their");

    // Private variables store the corresponding pronoun forms.
    private final String subject;
    private final String object;
    private final String possessive;

    // Constructor initializes the pronoun values for each enum constant.
    Pronouns(String subject, String object, String possessive) {
        this.subject = subject;
        this.object = object;
        this.possessive = possessive;
    }

    // Returns the subject pronoun (he, she, they).
    public String getSubject() {
        return subject;
    }

    // Returns the object pronoun (him, her, them).
    public String getObject() {
        return object;
    }

    // Returns the possessive pronoun (his, her, their).
    public String getPossessive() {
        return possessive;
    }
}
