package com.sabaytayo;

// Custom checked exception thrown when a requested scene ID does not exist in the Encounter graph.
public class SceneNotFoundException extends Exception {

    private final String missingId;

    public SceneNotFoundException(String missingId) {
        super("Scene not found: " + missingId);
        this.missingId = missingId;
    }

    public String getMissingId() {
        return missingId;
    }
}
