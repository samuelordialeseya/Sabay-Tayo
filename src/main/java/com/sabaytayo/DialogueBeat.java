package com.sabaytayo;

import java.util.ArrayList;
import java.util.List;

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
                    : "Fatima";
            return new DialogueBeat(strangerName, quote);
        }

        // Otherwise it is narration / ambient description
        return new DialogueBeat("Narrator", formatted);
    }

    // Splits a formatted choice reaction into separate spoken dialogue and narration beats.
    public static List<DialogueBeat> splitReaction(String formattedReaction, String strangerName) {
        List<DialogueBeat> beats = new ArrayList<>();
        if (formattedReaction == null || formattedReaction.isBlank()) {
            return beats;
        }

        String speakerName = (strangerName != null && !strangerName.isBlank())
                ? strangerName
                : "Fatima";
        String text = formattedReaction.trim();

        // Plain unquoted reaction: either pure description or pure spoken line
        if (!text.contains("\"")) {
            if (isNarrationOnly(text)) {
                beats.add(new DialogueBeat("Narrator", stripOuterParens(text)));
            } else {
                beats.add(new DialogueBeat(speakerName, text));
            }
            return beats;
        }

        // Mixed reaction: alternate between narration outside quotes and spoken lines inside quotes
        int pos = 0;
        int len = text.length();
        while (pos < len) {
            int openQuote = text.indexOf('"', pos);
            if (openQuote == -1) {
                String tail = stripOuterParens(text.substring(pos).trim());
                if (!tail.isEmpty()) {
                    beats.add(new DialogueBeat("Narrator", tail));
                }
                break;
            }

            if (openQuote > pos) {
                String before = stripOuterParens(text.substring(pos, openQuote).trim());
                if (!before.isEmpty()) {
                    beats.add(new DialogueBeat("Narrator", before));
                }
            }

            int closeQuote = text.indexOf('"', openQuote + 1);
            if (closeQuote == -1) {
                String rest = text.substring(openQuote + 1).trim();
                if (!rest.isEmpty()) {
                    beats.add(new DialogueBeat(speakerName, rest));
                }
                break;
            }

            String spoken = text.substring(openQuote + 1, closeQuote).trim();
            if (!spoken.isEmpty()) {
                beats.add(new DialogueBeat(speakerName, spoken));
            }
            pos = closeQuote + 1;
        }

        return beats;
    }

    private static boolean isNarrationOnly(String text) {
        return text.startsWith("(")
                || text.startsWith("You ")
                || text.startsWith("She ")
                || text.startsWith("He ")
                || text.startsWith("They ");
    }

    private static String stripOuterParens(String text) {
        if (text != null && text.length() >= 2 && text.startsWith("(") && text.endsWith(")")) {
            int depth = 0;
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                if (c == '(') {
                    depth++;
                } else if (c == ')') {
                    depth--;
                    if (depth == 0 && i < text.length() - 1) {
                        return text;
                    }
                }
            }
            return text.substring(1, text.length() - 1).trim();
        }
        return text;
    }
}
