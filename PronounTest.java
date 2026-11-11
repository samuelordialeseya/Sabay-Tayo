
public class PronounTest {

    public static void main(String[] args) {

        Pronouns[] pronouns = Pronouns.values();

        String[][] placeholders = {
            {"{they}", "{them}", "{their}"},
            {"{They}", "{Them}", "{Their}"}
        };

        String[][] expected = {
            {"he", "him", "his"},
            {"she", "her", "her"},
            {"they", "them", "their"}
        };

        int passed = 0;
        int failed = 0;

        // Test all three pronoun configurations.
        for (int i = 0; i < pronouns.length; i++) {

            System.out.println("\nTesting: " + pronouns[i]);

            for (int j = 0; j < 3; j++) {

                // Test lowercase placeholders.
                String actual = TextFormatter.formatText(
                    placeholders[0][j], pronouns[i]
                );

                if (actual.equals(expected[i][j])) {
                    System.out.println("PASS: " + actual);
                    passed++;
                } else {
                    System.out.println("FAIL: " + actual);
                    failed++;
                }

                // Test capitalized placeholders.
                String capitalized = TextFormatter.formatText(
                    placeholders[1][j], pronouns[i]
                );

                String expectedCapitalized =
                    Character.toUpperCase(expected[i][j].charAt(0))
                    + expected[i][j].substring(1);

                if (capitalized.equals(expectedCapitalized)) {
                    System.out.println("PASS: " + capitalized);
                    passed++;
                } else {
                    System.out.println("FAIL: " + capitalized);
                    failed++;
                }
            }
        }

        // Display the test summary.
        System.out.println("\n===== TEST SUMMARY =====");
        System.out.println("Passed: " + passed);
        System.out.println("Failed: " + failed);
        System.out.println("Total: " + (passed + failed));

        // Test null text.
        if (TextFormatter.formatText(null, Pronouns.HE_HIM) == null) {
            System.out.println("PASS: Null text handling");
        } else {
            System.out.println("FAIL: Null text handling");
        }

        // Test null pronouns.
        try {
            TextFormatter.formatText("Hello", null);
            System.out.println("FAIL: Null pronoun handling");
        } catch (IllegalArgumentException e) {
            System.out.println("PASS: Null pronoun handling");
        }
    }
}
