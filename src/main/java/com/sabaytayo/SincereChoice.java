package com.sabaytayo;

// A genuine, friendly response that builds trust and respect.
public class SincereChoice extends DialogueChoice {

    public SincereChoice(String text, String nextSceneId, String warmReaction, String coldReaction) {
        super(text, nextSceneId, "Sincere", -5, warmReaction, coldReaction);
    }

    public SincereChoice(String text, String nextSceneId) {
        this(text, nextSceneId, "They give you a warm, gentle smile.", "They nod politely.");
    }

    @Override
    public void applyEffect(RelationshipStats stats, Momentum momentum) {
        stats.adjustTrust(momentum.scaleGain(8));
        stats.adjustRespect(momentum.scaleGain(3));
        momentum.adjust(1);
    }

    @Override
    public String getReaction(Momentum momentum) {
        if (momentum.getValue() >= 0) {
            return getWarmReaction();
        }
        return getColdReaction();
    }
}
