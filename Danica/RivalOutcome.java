// This class checks if the player reaches the Rival ending.
public class RivalOutcome extends Outcome {

    // Sets the title and description of this ending.
    public RivalOutcome() {
        super("Rival", "The player and stranger develop a competitive relationship.");
    }

    // Checks if the requirements for the Rival ending are met.
    @Override
    public boolean matches(RelationshipStats stats) {
        return stats.get(RelationshipStats.RESPECT) >= 50
            && stats.get(RelationshipStats.ANNOYANCE) >= 22
            && stats.get(RelationshipStats.TRUST) < 50;
    }
}