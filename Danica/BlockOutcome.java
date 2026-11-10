// This class checks if the player reaches the Blocked ending.
public class BlockedOutcome extends Outcome {

    // Sets the title and description of this ending.
    public BlockedOutcome() {
        super("Blocked", "The stranger no longer wants to continue the conversation.");
    }

    // Checks if the Annoyance points reach 34 or higher.
    @Override
    public boolean matches(RelationshipStats stats) {
        return stats.get(RelationshipStats.ANNOYANCE) >= 34;
    }
}