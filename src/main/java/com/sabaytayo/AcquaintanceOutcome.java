package com.sabaytayo;

// Default fallback ending when no specific relationship threshold is met.
public class AcquaintanceOutcome extends Outcome {

    public AcquaintanceOutcome() {
        super(
            "🚏 Just Strangers",
            "You exchanged a polite goodbye before going your separate ways—no numbers exchanged, just a brief, ordinary ride. \"Ingat!\" Sometimes, not every stranger becomes someone you know.",
            "Rule: Default / Fallback"
        );
    }

    @Override
    public boolean matches(RelationshipStats stats) {
        return true;
    }
}
