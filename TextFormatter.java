
public class TextFormatter {

    // Replaces pronoun placeholders with the selected pronouns.
    public static String formatText(String text, Pronouns pronouns) {

        // Return null if the given text is null.
        if (text == null) {
            return null;
        }

        // Make sure a pronoun selection is provided.
        if (pronouns == null) {
            throw new IllegalArgumentException(
                "Pronouns cannot be null."
            );
        }

        // Get the selected pronoun forms.
        String subject = pronouns.getSubject();
        String object = pronouns.getObject();
        String possessive = pronouns.getPossessive();

        // Replace lowercase and capitalized placeholders.
        return text
            .replace("{They}", capitalize(subject))
            .replace("{they}", subject)
            .replace("{Them}", capitalize(object))
            .replace("{them}", object)
            .replace("{Their}", capitalize(possessive))
            .replace("{their}", possessive);
    }

    // Capitalizes the first letter of a word.
    private static String capitalize(String word) {
        if (word == null || word.isEmpty()) {
            return word;
        }

        return word.substring(0, 1).toUpperCase()
                + word.substring(1);
    }
}
