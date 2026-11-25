package com.sabaytayo;

// Replaces pronoun placeholders in dialogue and narration text.
public class TextFormatter {

    // Replaces player pronoun placeholders ({they}, {them}, {their} and capitalized forms).
    public static String formatText(String text, Pronouns pronouns) {
        if (text == null) {
            return null;
        }

        if (pronouns == null) {
            throw new IllegalArgumentException("Pronouns cannot be null.");
        }

        String subject = pronouns.getSubject();
        String object = pronouns.getObject();
        String possessive = pronouns.getPossessive();

        return text
            .replace("{They}", capitalize(subject))
            .replace("{they}", subject)
            .replace("{Them}", capitalize(object))
            .replace("{them}", object)
            .replace("{Their}", capitalize(possessive))
            .replace("{their}", possessive);
    }

    // Replaces stranger/seatmate pronoun placeholders ({s_they}, {s_them}, {s_their} and capitalized forms).
    public static String formatStrangerText(String text, Pronouns strangerPronouns) {
        if (text == null) {
            return null;
        }

        if (strangerPronouns == null) {
            throw new IllegalArgumentException("Stranger pronouns cannot be null.");
        }

        String subject = strangerPronouns.getSubject();
        String object = strangerPronouns.getObject();
        String possessive = strangerPronouns.getPossessive();

        return text
            .replace("{S_they}", capitalize(subject))
            .replace("{s_they}", subject)
            .replace("{S_them}", capitalize(object))
            .replace("{s_them}", object)
            .replace("{S_their}", capitalize(possessive))
            .replace("{s_their}", possessive);
    }

    // Capitalizes the first letter of a word.
    private static String capitalize(String word) {
        if (word == null || word.isEmpty()) {
            return word;
        }
        return word.substring(0, 1).toUpperCase() + word.substring(1);
    }
}
