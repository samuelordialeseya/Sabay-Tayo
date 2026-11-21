package com.sabaytayo;

// Tests RelationshipStats clamping bounds (0..100) and invalid stat index handling.
public class RelationshipStatsTest {

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

    public static void main(String[] args) {
        RelationshipStats stats = new RelationshipStats();

        check("Default Trust is 40", stats.getTrust() == 40);
        check("Default Attraction is 30", stats.getAttraction() == 30);
        check("Default Respect is 40", stats.getRespect() == 40);
        check("Default Annoyance is 10", stats.getAnnoyance() == 10);

        stats.adjustTrust(1000);
        check("Trust clamps at 100", stats.getTrust() == 100);

        stats.adjustTrust(-1000);
        check("Trust clamps at 0", stats.getTrust() == 0);

        stats.adjustAttraction(1000);
        stats.adjustRespect(-1000);
        stats.adjustAnnoyance(1000);

        check("Attraction clamps at 100", stats.getAttraction() == 100);
        check("Respect clamps at 0", stats.getRespect() == 0);
        check("Annoyance clamps at 100", stats.getAnnoyance() == 100);

        boolean threwInvalidIndex = false;
        try {
            stats.get(99);
        } catch (IllegalArgumentException e) {
            threwInvalidIndex = true;
        }
        check("Invalid stat index throws IllegalArgumentException", threwInvalidIndex);

        System.out.println("\n===== RELATIONSHIP STATS TEST SUMMARY =====");
        System.out.println("Passed: " + passed + " | Failed: " + failed);
        if (failed > 0) {
            throw new AssertionError("RelationshipStatsTest had " + failed + " failure(s).");
        }
    }
}
