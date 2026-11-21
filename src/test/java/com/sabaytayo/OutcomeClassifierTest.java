package com.sabaytayo;

// Tests all five Outcome subclasses and OutcomeClassifier priority rules.
public class OutcomeClassifierTest {

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
        OutcomeClassifier classifier = new OutcomeClassifier();

        // 1. Default stats -> AcquaintanceOutcome
        RelationshipStats defaultStats = new RelationshipStats();
        check("Default stats -> AcquaintanceOutcome",
                classifier.classify(defaultStats) instanceof AcquaintanceOutcome);

        // 2. Blocked ending (Annoyance >= 34)
        RelationshipStats blockedStats = new RelationshipStats();
        blockedStats.adjustAnnoyance(24); // 10 + 24 = 34
        check("Annoyance 34 -> BlockedOutcome",
                classifier.classify(blockedStats) instanceof BlockedOutcome);

        // 3. Priority check: Blocked beats Rival even if Respect >= 50 and Trust < 50
        blockedStats.adjustRespect(20); // 60
        check("Blocked takes priority over Rival",
                classifier.classify(blockedStats) instanceof BlockedOutcome);

        // 4. Lover ending (Trust >= 58, Attraction >= 40, Annoyance < 24)
        RelationshipStats loverStats = new RelationshipStats();
        loverStats.adjustTrust(20);      // 60
        loverStats.adjustAttraction(15); // 45
        loverStats.adjustRespect(15);    // 55 (also qualifies for BestFriend, Lover must win)
        check("Lover takes priority over BestFriend",
                classifier.classify(loverStats) instanceof LoverOutcome);

        // 5. Best Friend ending (Trust >= 60, Respect >= 50, Annoyance < 24, Attraction < 40)
        RelationshipStats friendStats = new RelationshipStats();
        friendStats.adjustTrust(20);   // 60
        friendStats.adjustRespect(10); // 50
        check("Trust 60 + Respect 50 -> BestFriendOutcome",
                classifier.classify(friendStats) instanceof BestFriendOutcome);

        // 6. Rival ending (Respect >= 50, Annoyance >= 22, Trust < 50)
        RelationshipStats rivalStats = new RelationshipStats();
        rivalStats.adjustRespect(10);   // 50
        rivalStats.adjustAnnoyance(12); // 22
        check("Respect 50 + Annoyance 22 + Trust 40 -> RivalOutcome",
                classifier.classify(rivalStats) instanceof RivalOutcome);

        // 7. Null guard
        boolean threwNull = false;
        try {
            classifier.classify(null);
        } catch (IllegalArgumentException e) {
            threwNull = true;
        }
        check("Null stats throws IllegalArgumentException", threwNull);

        System.out.println("\n===== OUTCOME CLASSIFIER TEST SUMMARY =====");
        System.out.println("Passed: " + passed + " | Failed: " + failed);
        if (failed > 0) {
            throw new AssertionError("OutcomeClassifierTest had " + failed + " failure(s).");
        }
    }
}
