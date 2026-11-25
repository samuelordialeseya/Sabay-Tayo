package com.sabaytayo;

// Banter-driven rivalry ending: high respect and tension, lower trust.
public class RivalOutcome extends Outcome {

    public RivalOutcome() {
        super(
            "⚡ Rivals",
            "Your ride was filled with teasing remarks, disagreements, and sharp banter—neither of you willing to back down. \"At least hindi boring ang biyahe... Whatever.\"",
            "Rule: Respect >= 50, Annoyance >= 22, Trust < 50"
        );
    }

    @Override
    public boolean matches(RelationshipStats stats) {
        return stats.get(RelationshipStats.RESPECT) >= 50
            && stats.get(RelationshipStats.ANNOYANCE) >= 22
            && stats.get(RelationshipStats.TRUST) < 50;
    }

    @Override
    public String getEndingCgPath() {
        return "/assets/CG/cg_ending_rivals.png";
    }

    @Override
    public String getBadgePath() {
        return "/assets/ui/endings/badge_ending_rivals.png";
    }

    @Override
    public String getEndingCgDescription() {
        return "Eye-level POV from the bus aisle as the Stranger crosses her arms with a playful, unimpressed \"whatever\" side-glance.";
    }
}
