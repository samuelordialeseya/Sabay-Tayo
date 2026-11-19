package com.sabaytayo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Stores the dialogue and choices for one conversation scene.
public class Scene {

    private String sceneId;
    private String dialogue;
    private List<DialogueChoice> choices;

    // Creates a new scene.
    public Scene(String sceneId, String dialogue) {
        this.sceneId = sceneId;
        this.dialogue = dialogue;
        this.choices = new ArrayList<>();
    }

    // Gets the scene ID.
    public String getSceneId() {
        return sceneId;
    }

    // Gets the dialogue.
    public String getDialogue() {
        return dialogue;
    }

    // Gets all choices in the scene.
    public List<DialogueChoice> getChoices() {
        return Collections.unmodifiableList(choices);
    }

    // Adds a choice to the scene.
    public void addChoice(DialogueChoice choice) {
        if (choice != null) {
            choices.add(choice);
        }
    }

    // Gets choices that are unlocked based on Momentum.
    public List<DialogueChoice> getAvailableChoices(Momentum momentum) {
        List<DialogueChoice> availableChoices = new ArrayList<>();

        for (DialogueChoice choice : choices) {
            if (choice.isUnlocked(momentum)) {
                availableChoices.add(choice);
            }
        }

        return availableChoices;
    }
}
