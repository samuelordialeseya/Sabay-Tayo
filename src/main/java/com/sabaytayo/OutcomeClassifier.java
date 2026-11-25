package com.sabaytayo;

import java.util.ArrayList;
import java.util.List;

// Evaluates final RelationshipStats in priority order and returns the matching Outcome.
public class OutcomeClassifier {

    private final List<Outcome> outcomes;

    public OutcomeClassifier() {
        outcomes = new ArrayList<>();

        // Priority order: Blocked -> Lover -> BestFriend -> Rival -> Acquaintance (default)
        outcomes.add(new BlockedOutcome());
        outcomes.add(new LoverOutcome());
        outcomes.add(new BestFriendOutcome());
        outcomes.add(new RivalOutcome());
        outcomes.add(new AcquaintanceOutcome());
    }

    // Returns the first Outcome whose threshold rules match the given stats.
    public Outcome classify(RelationshipStats stats) {
        if (stats == null) {
            throw new IllegalArgumentException("RelationshipStats cannot be null.");
        }

        for (Outcome outcome : outcomes) {
            if (outcome.matches(stats)) {
                return outcome;
            }
        }

        // AcquaintanceOutcome is always last in the list and matches all stats.
        return outcomes.get(outcomes.size() - 1);
    }
}
