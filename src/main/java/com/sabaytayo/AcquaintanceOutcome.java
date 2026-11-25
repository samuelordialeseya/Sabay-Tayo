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

    @Override
    public String getEndingCgPath() {
        return "/assets/CG/cg_ending_strangers.png";
    }

    @Override
    public String getBadgePath() {
        return "/assets/ui/endings/badge_ending_strangers.png";
    }

    @Override
    public String getEndingCgDescription() {
        return "MC standing alone under the rainy bus stop shelter watching the red taillights disappear down the wet street.";
    }
}
