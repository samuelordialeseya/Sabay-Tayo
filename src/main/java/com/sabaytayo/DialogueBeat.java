package com.sabaytayo;

// Represents a single spoken line or narrative beat in a visual novel scene.
public class DialogueBeat {

    private final String speaker;
    private final String text;

    public DialogueBeat(String speaker, String text) {
        this.speaker = speaker;
        this.text = text;
    }

    public String getSpeaker() {
        return speaker;
    }

    public String getText() {
        return text;
    }

    // Helper to parse a dialogue line into a DialogueBeat based on its prefix.
    public static DialogueBeat fromLine(String rawLine, Player player, Stranger stranger) {
        if (rawLine == null || rawLine.isBlank()) {
            return new DialogueBeat("Narrator", "...");
        }

        String formatted = ScriptFormatter.format(rawLine.trim(), player, stranger);

        // Check if the line is spoken by the player
        if (formatted.startsWith("You: ")) {
            String quote = formatted.substring(5).trim();
            String playerName = (player != null && player.getName() != null && !player.getName().isBlank())
                    ? player.getName()
                    : "You";
            return new DialogueBeat(playerName, quote);
        }

        // Check if the line is spoken by the stranger
        if (formatted.startsWith("Stranger: ")) {
            String quote = formatted.substring(10).trim();
            String strangerName = (stranger != null && stranger.getName() != null && !stranger.getName().isBlank())
                    ? stranger.getName()
                    : "Stranger";
            return new DialogueBeat(strangerName, quote);
        }

        // Otherwise it is narration / ambient description
        return new DialogueBeat("Narrator", formatted);
    }
}
