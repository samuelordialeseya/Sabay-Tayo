import java.util.ArrayList;
import java.util.List;

// This class determines which ending the player will receive.
public class OutcomeClassifier {

    // This list stores all the possible outcomes.
    private List<Outcome> outcomes;

    // Adds all outcomes in the order they should be checked.
    public OutcomeClassifier() {
        outcomes = new ArrayList<>();

        // Blocked is checked first because it has the highest priority.
        outcomes.add(new BlockedOutcome());

        // The other outcomes follow based on the project rules.
        outcomes.add(new LoverOutcome());
        outcomes.add(new BestFriendOutcome());
        outcomes.add(new RivalOutcome());

        // Acquaintance is the default outcome.
        outcomes.add(new AcquaintanceOutcome());
    }

    // Checks the player's stats and returns the first matching outcome.
    public Outcome classify(RelationshipStats stats) {

        for (Outcome outcome : outcomes) {

            // Returns the outcome if its conditions are satisfied.
            if (outcome.matches(stats)) {
                return outcome;
            }
        }

        // Returns Acquaintance if no other outcome matches.
        return new AcquaintanceOutcome();
    }
}