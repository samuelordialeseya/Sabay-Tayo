// This class represents the default outcome of the game.
public class AcquaintanceOutcome extends Outcome {

    // Sets the title and description of this ending.
    public AcquaintanceOutcome() {
        super("Acquaintance", "The player and stranger remain casual acquaintances.");
    }

    // Returns true because this is the default outcome.
    @Override
    public boolean matches(RelationshipStats stats) {
        return true;
    }
}