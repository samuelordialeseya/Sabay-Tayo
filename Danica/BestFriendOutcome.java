// This class checks if the player reaches the Best Friend ending.
public class BestFriendOutcome extends Outcome {

    // Sets the title and description of this ending.
    public BestFriendOutcome() {
        super("Best Friend", "The player and stranger build a strong friendship.");
    }

    // Checks if the Trust and Respect requirements are met.
    @Override
    public boolean matches(RelationshipStats stats) {
        return stats.get(RelationshipStats.TRUST) >= 60
            && stats.get(RelationshipStats.RESPECT) >= 50
            && stats.get(RelationshipStats.ANNOYANCE) < 24;
    }
}