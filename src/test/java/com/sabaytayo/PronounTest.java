package com.sabaytayo;

// Tests Pronouns, TextFormatter, and ScriptFormatter across all pronoun configurations.
public class PronounTest {

    private static int passed = 0;
    private static int failed = 0;

    private static void check(String label, boolean condition) {
        if (condition) {
            System.out.println("PASS: " + label);
            passed++;
        } else {
            System.out.println("FAIL: " + label);
            failed++;
        }
    }

    public static void main(String[] args) throws InvalidNameException {
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

        // Test all three pronoun configurations in TextFormatter.
        for (int i = 0; i < pronouns.length; i++) {
            for (int j = 0; j < 3; j++) {
                String actual = TextFormatter.formatText(placeholders[0][j], pronouns[i]);
                check(pronouns[i] + " lowercase " + placeholders[0][j], actual.equals(expected[i][j]));

                String capitalized = TextFormatter.formatText(placeholders[1][j], pronouns[i]);
                String expectedCap = expected[i][j].substring(0, 1).toUpperCase()
                        + expected[i][j].substring(1);
                check(pronouns[i] + " capitalized " + placeholders[1][j], capitalized.equals(expectedCap));
            }
        }

        // Test null text and null pronouns handling.
        check("Null text returns null", TextFormatter.formatText(null, Pronouns.HE_HIM) == null);

        boolean threwNullPronouns = false;
        try {
            TextFormatter.formatText("Hello", null);
        } catch (IllegalArgumentException e) {
            threwNullPronouns = true;
        }
        check("Null pronouns throws IllegalArgumentException", threwNullPronouns);

        // Test ScriptFormatter with both Player and Stranger objects.
        Player player = new Player("Sam", Pronouns.HE_HIM);
        Stranger stranger = new Stranger(Pronouns.SHE_HER);

        String formatted = ScriptFormatter.format(
            "{player} sat beside {seatmate}. {S_they} put {s_their} phone away, and {they} smiled.",
            player,
            stranger
        );
        check(
            "ScriptFormatter formats player & stranger tokens",
            formatted.equals("Sam sat beside Fatima. She put her phone away, and he smiled.")
        );
        check("No unresolved tokens remain", !ScriptFormatter.hasUnresolvedTokens(formatted));
        check("Detects unresolved typo token", ScriptFormatter.hasUnresolvedTokens("Hi {thye}"));

        // Test Pronouns.fromLabel helper.
        check("fromLabel She/Her", Pronouns.fromLabel("She/Her") == Pronouns.SHE_HER);
        check("fromLabel unknown falls back to THEY_THEM", Pronouns.fromLabel("unknown") == Pronouns.THEY_THEM);

        System.out.println("\n===== PRONOUN & FORMATTER TEST SUMMARY =====");
        System.out.println("Passed: " + passed + " | Failed: " + failed);
        if (failed > 0) {
            throw new AssertionError("PronounTest had " + failed + " failure(s).");
        }
    }
}
