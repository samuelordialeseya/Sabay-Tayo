// Formats script lines: name tokens + pronouns.
// Wraps the existing TextFormatter (which is left unchanged).
public class ScriptFormatter {

    // Replaces {player} / {seatmate} and all pronoun placeholders.
    public static String format(String text, Pronouns pronouns,
                                String playerName, String seatmateName) {

        // Return null if the given text is null.
        if (text == null) {
            return null;
        }

        if (playerName == null || seatmateName == null) {
            throw new IllegalArgumentException(
                "Player and seatmate names cannot be null."
            );
        }

        // Pronouns first (handled by TextFormatter), then names.
        return TextFormatter.formatText(text, pronouns)
            .replace("{player}", playerName)
            .replace("{seatmate}", seatmateName);
    }

    // Returns true if any {token} is still left in the text.
    // Used by tests to catch typos such as {thye} or an unsupported token.
    public static boolean hasUnresolvedTokens(String text) {
        return text != null && text.matches("(?s).*\\{[^{}]*\\}.*");
    }
}
