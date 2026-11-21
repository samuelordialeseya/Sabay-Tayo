package com.sabaytayo;

// Highest-priority ending: triggered when annoyance reaches 34 or higher.
public class BlockedOutcome extends Outcome {

    public BlockedOutcome() {
        super(
            "🚫 Blocked",
            "The conversation grew increasingly uncomfortable, and they stopped trying to engage. \"Excuse me. Dito na ako.\" Sometimes, knowing when to stop is just as important as knowing when to speak.",
            "Rule: Annoyance >= 34"
        );
    }

    @Override
    public boolean matches(RelationshipStats stats) {
        return stats.get(RelationshipStats.ANNOYANCE) >= 34;
    }
}
