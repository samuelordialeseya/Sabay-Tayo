package com.sabaytayo;

// A charming or playful remark that unlocks only when conversation momentum is high (>= 2).
public class FlirtyChoice extends DialogueChoice {

    public FlirtyChoice(String text, String nextSceneId, String warmReaction, String coldReaction) {
        super(text, nextSceneId, "Flirty", 2, warmReaction, coldReaction);
    }

    public FlirtyChoice(String text, String nextSceneId) {
        this(text, nextSceneId, "They blush slightly and look away with a soft grin.", "They look taken aback and cross their arms.");
    }

    @Override
    public void applyEffect(RelationshipStats stats, Momentum momentum) {
        stats.adjustAttraction(momentum.scaleGain(10));
        stats.adjustTrust(momentum.scaleGain(2));

        // If respect is too low (< 30), the flirty line comes off as too forward or creepy
        if (stats.get(RelationshipStats.RESPECT) < 30) {
            stats.adjustAnnoyance(6);
            momentum.adjust(-2);
        } else {
            momentum.adjust(1);
        }
    }

    @Override
    public String getReaction(Momentum momentum) {
        if (momentum.getValue() >= 0) {
            return getWarmReaction();
        }
        return getColdReaction();
    }
}
