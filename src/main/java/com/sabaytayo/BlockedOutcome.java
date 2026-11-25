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

    @Override
    public String getEndingCgPath() {
        return "/assets/CG/cg_ending_blocked.png";
    }

    @Override
    public String getBadgePath() {
        return "/assets/ui/endings/badge_ending_blocked.png";
    }

    @Override
    public String getEndingCgDescription() {
        return "View past the empty pink window seat through rain-streaked glass as the Stranger walks away down the wet street without looking back.";
    }

    @Override
    public boolean isColdTone() {
        return true;
    }
}
