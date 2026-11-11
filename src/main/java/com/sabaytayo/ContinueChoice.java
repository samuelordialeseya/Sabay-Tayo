package com.sabaytayo;

// A transition choice used in narration scenes to move the story forward without altering stats.
public class ContinueChoice extends DialogueChoice {

    public ContinueChoice(String text, String nextSceneId) {
        super(text, nextSceneId, "Continue", -5, "", "");
    }

    public ContinueChoice(String nextSceneId) {
        this("Continue...", nextSceneId);
    }

    @Override
    public void applyEffect(RelationshipStats stats, Momentum momentum) {
        // Narration transition: does not modify relationship stats or momentum
    }

    @Override
    public String getReaction(Momentum momentum) {
        // No reaction text for narration transitions
        return "";
    }
}
