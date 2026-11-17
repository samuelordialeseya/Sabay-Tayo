package com.sabaytayo;

import java.util.ArrayList;
import java.util.List;

// Holds the content and player choices for one scene in the dialogue graph.
public class Scene {

    private String sceneId;
    private List<String> dialogueLines;
    private List<DialogueChoice> choices;
    private String defaultNextSceneId;

    // Sets up an empty scene with the given ID.
    public Scene(String sceneId) {
        this.sceneId = sceneId;
        this.dialogueLines = new ArrayList<>();
        this.choices = new ArrayList<>();
        this.defaultNextSceneId = null;
    }

    // Adds a line of narration or spoken dialogue to this scene.
    public void addDialogueLine(String line) {
        dialogueLines.add(line);
    }

    // Adds a choice the player can pick in this scene.
    public void addChoice(DialogueChoice choice) {
        choices.add(choice);
    }

    // Returns the scene's unique ID.
    public String getSceneId() {
        return sceneId;
    }

    // Returns all the dialogue or narration lines for this scene.
    public List<String> getDialogueLines() {
        return dialogueLines;
    }

    // Returns the list of choices available in this scene.
    public List<DialogueChoice> getChoices() {
        return choices;
    }

    // Returns the default next scene ID (used for narration/continue scenes).
    public String getDefaultNextSceneId() {
        return defaultNextSceneId;
    }

    // Sets the default next scene to advance to.
    public void setDefaultNextSceneId(String id) {
        this.defaultNextSceneId = id;
    }

    // Returns true if this scene has player choices.
    public boolean hasChoices() {
        return !choices.isEmpty();
    }
}
