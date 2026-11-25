package com.sabaytayo;

// Romantic ending: requires high trust and attraction with low annoyance.
public class LoverOutcome extends Outcome {

    public LoverOutcome() {
        super(
            "💕 Lovers",
            "What started as an ordinary rainy bus ride turned into something unexpected. Instead of stepping off alone, you asked \"Sabay tayo?\" and stayed seated beside them toward Ayala.",
            "Rule: Trust >= 58, Attraction >= 40, Annoyance < 24"
        );
    }

    @Override
    public boolean matches(RelationshipStats stats) {
        return stats.get(RelationshipStats.TRUST) >= 58
            && stats.get(RelationshipStats.ATTRACTION) >= 40
            && stats.get(RelationshipStats.ANNOYANCE) < 24;
    }

    @Override
    public String getEndingCgPath() {
        return "/assets/CG/cg_ending_lovers.png";
    }

    @Override
    public String getBadgePath() {
        return "/assets/ui/endings/badge_ending_lovers.png";
    }

    @Override
    public String getEndingCgDescription() {
        return "Both commuters seated side-by-side by the warm rainy bus window on the way to Ayala, smiling softly.";
    }

    @Override
    public boolean playsChimeOnReveal() {
        return true;
    }
}
