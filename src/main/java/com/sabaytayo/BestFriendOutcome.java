package com.sabaytayo;

// Platonic close-bond ending: requires high trust and respect with low annoyance.
public class BestFriendOutcome extends Outcome {

    public BestFriendOutcome() {
        super(
            "🤝 Best Friends",
            "Although the conversation began awkwardly, you found genuine common ground and swapped Instagrams before hopping off. \"Follow mo na lang ako—ingat ka pauwi!\"",
            "Rule: Trust >= 60, Respect >= 50, Annoyance < 24"
        );
    }

    @Override
    public boolean matches(RelationshipStats stats) {
        return stats.get(RelationshipStats.TRUST) >= 60
            && stats.get(RelationshipStats.RESPECT) >= 50
            && stats.get(RelationshipStats.ANNOYANCE) < 24;
    }

    @Override
    public String getEndingCgPath() {
        return "/assets/CG/cg_ending_bestfriend.png";
    }

    @Override
    public String getBadgePath() {
        return "/assets/ui/endings/badge_ending_bestfriend.png";
    }

    @Override
    public String getEndingCgDescription() {
        return "MC stepping onto the rainy curb as the Stranger smiles and waves from the bus window holding her phone.";
    }
}
