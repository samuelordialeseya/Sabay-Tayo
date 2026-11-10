// This class checks if the player reaches the Lover ending.
public class LoverOutcome extends Outcome {

    // Sets the title and description of this ending.
    public LoverOutcome() {
        super("Lover", "The player and stranger develop a romantic connection.");
    }

    // Checks if all the requirements for the Lover ending are met.
    @Override
    public boolean matches(RelationshipStats stats) {
        return stats.get(RelationshipStats.TRUST) >= 58
            && stats.get(RelationshipStats.ATTRACTION) >= 40
            && stats.get(RelationshipStats.ANNOYANCE) < 24;
    }
}