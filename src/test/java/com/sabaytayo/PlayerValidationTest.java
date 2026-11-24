package com.sabaytayo;

// Tests Character, Player name validation, InvalidNameException, and Stranger defaults.
public class PlayerValidationTest {

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

    private static void expectInvalidName(String label, String badName) {
        try {
            new Player(badName, Pronouns.THEY_THEM);
            check(label, false);
        } catch (InvalidNameException e) {
            check(label + " (" + e.getMessage() + ")", true);
        }
    }

    public static void main(String[] args) throws InvalidNameException {
        Player valid = new Player("  Sam  ", Pronouns.HE_HIM);
        check("Trims valid name", valid.getName().equals("Sam"));
        check("Stores player pronouns", valid.getPronouns() == Pronouns.HE_HIM);

        expectInvalidName("Rejects null name", null);
        expectInvalidName("Rejects blank name", "   ");
        expectInvalidName("Rejects name over 20 chars", "Abcdefghijklmnopqrstu");
        expectInvalidName("Rejects name with digits", "Sam123");
        expectInvalidName("Rejects name with curly braces", "{player}");

        Stranger stranger = new Stranger(Pronouns.SHE_HER);
        check("Stranger default name", stranger.getName().equals("Fatima"));
        check("Stranger pronouns", stranger.getPronouns() == Pronouns.SHE_HER);

        boolean threwNullPronouns = false;
        try {
            new Stranger(null);
        } catch (IllegalArgumentException e) {
            threwNullPronouns = true;
        }
        check("Character rejects null pronouns", threwNullPronouns);

        System.out.println("\n===== PLAYER VALIDATION TEST SUMMARY =====");
        System.out.println("Passed: " + passed + " | Failed: " + failed);
        if (failed > 0) {
            throw new AssertionError("PlayerValidationTest had " + failed + " failure(s).");
        }
    }
}
