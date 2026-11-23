package com.sabaytayo;

// Tests DialogueBeat parsing for Player, Stranger, and Narrator beats.
public class DialogueBeatTest {

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

    public static void main(String[] args) throws Exception {
        Player player = new Player("Sam", Pronouns.THEY_THEM);
        Stranger stranger = new Stranger(Pronouns.SHE_HER);

        // 1. Narrator beat
        String narratorLine = "It was a rainy afternoon. The bus was almost full.";
        DialogueBeat b1 = DialogueBeat.fromLine(narratorLine, player, stranger);
        check("Narrator beat speaker is Narrator", "Narrator".equals(b1.getSpeaker()));
        check("Narrator beat text matches", narratorLine.equals(b1.getText()));

        // 2. Player beat
        String playerLine = "You: \"Excuse me, may nakaupo ba rito?\"";
        DialogueBeat b2 = DialogueBeat.fromLine(playerLine, player, stranger);
        check("Player beat speaker is Sam", "Sam".equals(b2.getSpeaker()));
        check("Player beat text strips You: prefix", "\"Excuse me, may nakaupo ba rito?\"".equals(b2.getText()));

        // 3. Stranger beat
        String strangerLine = "Stranger: \"Aray! Grabe naman 'yon!\"";
        DialogueBeat b3 = DialogueBeat.fromLine(strangerLine, player, stranger);
        check("Stranger beat speaker is Stranger", "Stranger".equals(b3.getSpeaker()));
        check("Stranger beat text strips Stranger: prefix", "\"Aray! Grabe naman 'yon!\"".equals(b3.getText()));

        // 4. Token formatting inside beat
        String tokenLine = "{S_they} glanced briefly at the empty space beside {s_them}.";
        DialogueBeat b4 = DialogueBeat.fromLine(tokenLine, player, stranger);
        check("Tokens formatted in narrator beat", "She glanced briefly at the empty space beside her.".equals(b4.getText()));

        System.out.println("\n===== DIALOGUE BEAT TEST SUMMARY =====");
        System.out.println("Passed: " + passed + " | Failed: " + failed);
        if (failed > 0) {
            System.exit(1);
        }
    }
}
