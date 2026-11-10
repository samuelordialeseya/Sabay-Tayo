// This abstract class serves as the parent class of all possible outcomes.
public abstract class Outcome {

    // These variables store the outcome's title and description.
    private String title;
    private String description;

    // This constructor sets the details of an outcome.
    public Outcome(String title, String description) {
        this.title = title;
        this.description = description;
    }

    // Returns the outcome's title.
    public String getTitle() {
        return title;
    }

    // Returns the outcome's description.
    public String getDescription() {
        return description;
    }

    // Each outcome has its own conditions for being selected.
    public abstract boolean matches(RelationshipStats stats);
}