package com.sabaytayo;

// Formats dialogue lines with character names and pronoun placeholders.
// Wraps TextFormatter to keep pronoun and name token replacement cleanly separated.
public class ScriptFormatter {

    // Replaces player pronouns along with {player} and {seatmate} name tokens.
    public static String format(String text, Pronouns pronouns,
                                String playerName, String seatmateName) {
        if (text == null) {
            return null;
        }

        if (playerName == null || seatmateName == null) {
            throw new IllegalArgumentException("Player and seatmate names cannot be null.");
        }

        return TextFormatter.formatText(text, pronouns)
            .replace("{player}", playerName)
            .replace("{seatmate}", seatmateName);
    }

    // Formats both player and stranger pronouns along with their names.
    public static String format(String text, Player player, Stranger stranger) {
        if (text == null) {
            return null;
        }

        if (player == null || stranger == null) {
            throw new IllegalArgumentException("Player and stranger cannot be null.");
        }

        String result = TextFormatter.formatText(text, player.getPronouns());
        result = TextFormatter.formatStrangerText(result, stranger.getPronouns());
        return result
            .replace("{player}", player.getName())
            .replace("{seatmate}", stranger.getName());
    }

    // Returns true if any unresolved {token} remains in the formatted text.
    public static boolean hasUnresolvedTokens(String text) {
        return text != null && text.matches("(?s).*\\{[^{}]*\\}.*");
    }
}
