// This custom exception is used when a requested scene cannot be found.
public class SceneNotFoundException extends Exception {

    // Stores the ID of the scene that is missing.
    private String missingId;

    // Receives the missing scene ID and creates an error message.
    public SceneNotFoundException(String missingId) {
        super("Scene not found: " + missingId);
        this.missingId = missingId;
    }

    // Returns the ID of the missing scene.
    public String getMissingId() {
        return missingId;
    }
}