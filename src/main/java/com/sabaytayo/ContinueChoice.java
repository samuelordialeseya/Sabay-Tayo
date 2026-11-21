package com.sabaytayo;

// A transition or "Stay Quiet" choice that moves the story forward without altering stats.
public class ContinueChoice extends DialogueChoice {

    public ContinueChoice(String text, String nextSceneId, String reaction) {
        super(text, nextSceneId, "Continue", -5, reaction, reaction);
    }

    public ContinueChoice(String text, String nextSceneId) {
        this(text, nextSceneId, "");
    }

    public ContinueChoice(String nextSceneId) {
        this("Continue...", nextSceneId, "");
    }

    @Override
    public void applyEffect(RelationshipStats stats, Momentum momentum) {
        // Narration transition or staying quiet: does not modify relationship stats or momentum
    }

    @Override
    public String getReaction(Momentum momentum) {
        // Returns empty string for standard Continue transitions, or narrator text for Stay Quiet choices
        return getWarmReaction();
    }
}
