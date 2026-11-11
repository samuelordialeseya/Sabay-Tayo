package com.sabaytayo;

// A direct, no-nonsense statement. Earns respect for honesty, but slightly strains trust and increases annoyance.
public class BluntChoice extends DialogueChoice {

    public BluntChoice(String text, String nextSceneId, String warmReaction, String coldReaction) {
        super(text, nextSceneId, "Blunt", -5, warmReaction, coldReaction);
    }

    public BluntChoice(String text, String nextSceneId) {
        this(text, nextSceneId, "They nod slowly, respecting the straightforward answer.", "They frown slightly at the abrupt response.");
    }

    @Override
    public void applyEffect(RelationshipStats stats, Momentum momentum) {
        stats.adjustRespect(momentum.scaleGain(6));
        stats.adjustTrust(-2);
        stats.adjustAnnoyance(4);
        momentum.adjust(-1);
    }

    @Override
    public String getReaction(Momentum momentum) {
        if (momentum.getValue() >= 0) {
            return getWarmReaction();
        }
        return getColdReaction();
    }
}
