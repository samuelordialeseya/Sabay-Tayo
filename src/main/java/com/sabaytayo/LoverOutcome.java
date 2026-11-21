package com.sabaytayo;

// Romantic ending: requires high trust and attraction with low annoyance.
public class LoverOutcome extends Outcome {

    public LoverOutcome() {
        super(
            "💕 Lovers",
            "What started as an ordinary rainy bus ride turned into something unexpected. Before parting ways, you exchanged contacts and agreed to meet again. \"Message mo na lang ako kapag free ka.\"",
            "Rule: Trust >= 58, Attraction >= 40, Annoyance < 24"
        );
    }

    @Override
    public boolean matches(RelationshipStats stats) {
        return stats.get(RelationshipStats.TRUST) >= 58
            && stats.get(RelationshipStats.ATTRACTION) >= 40
            && stats.get(RelationshipStats.ANNOYANCE) < 24;
    }
}
