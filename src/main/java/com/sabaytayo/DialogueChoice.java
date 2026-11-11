package com.sabaytayo;

// Abstract base class representing a player choice during a dialogue turn.
public abstract class DialogueChoice {

    private final String text;
    private final String nextSceneId;
    private final String tone;
    private final int minMomentum;
    private final String warmReaction;
    private final String coldReaction;

    public DialogueChoice(String text, String nextSceneId, String tone, int minMomentum, String warmReaction, String coldReaction) {
        this.text = text;
        this.nextSceneId = nextSceneId;
        this.tone = tone;
        this.minMomentum = minMomentum;
        this.warmReaction = warmReaction;
        this.coldReaction = coldReaction;
    }

    public DialogueChoice(String text, String nextSceneId, String tone, int minMomentum) {
        this(text, nextSceneId, tone, minMomentum, "", "");
    }

    // Shared gating check: returns true if current momentum meets the requirement
    public boolean isUnlocked(Momentum momentum) {
        return momentum.getValue() >= this.minMomentum;
    }

    public String getText() {
        return text;
    }

    public String getNextSceneId() {
        return nextSceneId;
    }

    public String getTone() {
        return tone;
    }

    public int getMinMomentum() {
        return minMomentum;
    }

    public String getWarmReaction() {
        return warmReaction;
    }

    public String getColdReaction() {
        return coldReaction;
    }

    // Abstract methods implemented polymorphically by each choice subclass
    public abstract void applyEffect(RelationshipStats stats, Momentum momentum);

    public abstract String getReaction(Momentum momentum);
}
