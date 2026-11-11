package com.sabaytayo;

// A witty or teasing line. If momentum is high, it lands as playful banter; otherwise, it comes off as rude.
public class SarcasticChoice extends DialogueChoice {

    public SarcasticChoice(String text, String nextSceneId, String warmReaction, String coldReaction) {
        super(text, nextSceneId, "Sarcastic", -5, warmReaction, coldReaction);
    }

    public SarcasticChoice(String text, String nextSceneId) {
        this(text, nextSceneId, "They let out a genuine chuckle.", "They roll their eyes and look out the window.");
    }

    @Override
    public void applyEffect(RelationshipStats stats, Momentum momentum) {
        // If momentum is at least 2, the banter lands well
        if (momentum.getValue() >= 2) {
            stats.adjustRespect(momentum.scaleGain(3));
            stats.adjustAttraction(momentum.scaleGain(3));
            momentum.adjust(1);
        } else {
            // Otherwise, it increases annoyance and lowers momentum
            stats.adjustAnnoyance(6);
            momentum.adjust(-1);
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
