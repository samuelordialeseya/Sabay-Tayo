package com.sabaytayo;

// Platonic close-bond ending: requires high trust and respect with low annoyance.
public class BestFriendOutcome extends Outcome {

    public BestFriendOutcome() {
        super(
            "🤝 Best Friends",
            "Although the conversation began awkwardly, you eventually found common ground and agreed to keep in touch. \"It was nice talking to you. Sana magkita ulit tayo.\"",
            "Rule: Trust >= 60, Respect >= 50, Annoyance < 24"
        );
    }

    @Override
    public boolean matches(RelationshipStats stats) {
        return stats.get(RelationshipStats.TRUST) >= 60
            && stats.get(RelationshipStats.RESPECT) >= 50
            && stats.get(RelationshipStats.ANNOYANCE) < 24;
    }
}
