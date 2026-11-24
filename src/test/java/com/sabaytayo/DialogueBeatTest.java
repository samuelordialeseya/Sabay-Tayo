package com.sabaytayo;

import java.util.List;

// Tests DialogueBeat parsing and reaction splitting for Player, Stranger, and Narrator beats.
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
        String strangerLine = "Stranger: Aray! Grabe naman 'yon!";
        DialogueBeat b3 = DialogueBeat.fromLine(strangerLine, player, stranger);
        check("Stranger beat speaker is Fatima", "Fatima".equals(b3.getSpeaker()));
        check("Stranger beat text strips Stranger: prefix", "Aray! Grabe naman 'yon!".equals(b3.getText()));

        // 4. Token formatting inside beat
        String tokenLine = "{S_they} glanced briefly at the empty space beside {s_them}.";
        DialogueBeat b4 = DialogueBeat.fromLine(tokenLine, player, stranger);
        check("Tokens formatted in narrator beat", "She glanced briefly at the empty space beside her.".equals(b4.getText()));

        // 5. Mixed reaction split (dialogue -> description -> dialogue)
        String mixed = "\"Oo nga eh.\" She glanced briefly at you before looking outside again. \"Sana nga bumilis na. Medyo mahaba pa yata ang biyahe.\"";
        List<DialogueBeat> split1 = DialogueBeat.splitReaction(mixed, "Fatima");
        check("Mixed reaction splits into 3 beats", split1.size() == 3);
        check("Beat 0 is Fatima spoken dialogue", "Fatima".equals(split1.get(0).getSpeaker()) && "Oo nga eh.".equals(split1.get(0).getText()));
        check("Beat 1 is Narrator description", "Narrator".equals(split1.get(1).getSpeaker()) && "She glanced briefly at you before looking outside again.".equals(split1.get(1).getText()));
        check("Beat 2 is Fatima follow-up dialogue", "Fatima".equals(split1.get(2).getSpeaker()) && "Sana nga bumilis na. Medyo mahaba pa yata ang biyahe.".equals(split1.get(2).getText()));

        // 6. Parenthetical description before dialogue
        String parenFirst = "(Napatingin sa iyo and smiled slightly.) \"Haha, baka nga. Ang bagal talaga ng usad.\"";
        List<DialogueBeat> split2 = DialogueBeat.splitReaction(parenFirst, "Fatima");
        check("Parenthetical + dialogue splits into 2 beats", split2.size() == 2);
        check("Parenthetical becomes clean Narrator beat", "Narrator".equals(split2.get(0).getSpeaker()) && "Napatingin sa iyo and smiled slightly.".equals(split2.get(0).getText()));
        check("Following quote becomes Fatima beat", "Fatima".equals(split2.get(1).getSpeaker()) && "Haha, baka nga. Ang bagal talaga ng usad.".equals(split2.get(1).getText()));

        // 7. Ellipsis dialogue + parenthetical description
        String ellipsisReaction = "\"...\" (She glanced at you briefly, then looked right back down at her phone.)";
        List<DialogueBeat> split3 = DialogueBeat.splitReaction(ellipsisReaction, "Fatima");
        check("Ellipsis + parenthetical splits into 2 beats", split3.size() == 2);
        check("Ellipsis is spoken by Fatima", "Fatima".equals(split3.get(0).getSpeaker()) && "...".equals(split3.get(0).getText()));
        check("Parenthetical action is Narrator", "Narrator".equals(split3.get(1).getSpeaker()) && "She glanced at you briefly, then looked right back down at her phone.".equals(split3.get(1).getText()));

        // 8. Pure unquoted description vs pure unquoted dialogue
        List<DialogueBeat> pureDesc = DialogueBeat.splitReaction("She nodded slightly before looking back down at her phone.", "Fatima");
        check("Pure unquoted narration becomes Narrator beat", pureDesc.size() == 1 && "Narrator".equals(pureDesc.get(0).getSpeaker()));

        List<DialogueBeat> pureSpoken = DialogueBeat.splitReaction("Oo, minsan kapag may kailangan akong puntahan.", "Fatima");
        check("Pure unquoted dialogue stays Fatima beat", pureSpoken.size() == 1 && "Fatima".equals(pureSpoken.get(0).getSpeaker()));

        // 9. Verify every choice reaction in Encounter splits without leftover quotes
        Encounter encounter = new Encounter();
        boolean allClean = true;
        for (Scene scene : encounter.getScenes().values()) {
            for (DialogueChoice choice : scene.getChoices()) {
                for (String raw : new String[]{choice.getWarmReaction(), choice.getColdReaction()}) {
                    if (raw == null || raw.isBlank()) {
                        continue;
                    }
                    String formatted = ScriptFormatter.format(raw, player, stranger);
                    List<DialogueBeat> beats = DialogueBeat.splitReaction(formatted, stranger.getName());
                    if (beats.isEmpty()) {
                        allClean = false;
                    }
                    for (DialogueBeat beat : beats) {
                        if (beat.getText().isBlank() || beat.getText().contains("\"")) {
                            allClean = false;
                        }
                    }
                }
            }
        }
        check("All Encounter choice reactions split cleanly without mixed quotes", allClean);

        System.out.println("\n===== DIALOGUE BEAT TEST SUMMARY =====");
        System.out.println("Passed: " + passed + " | Failed: " + failed);
        if (failed > 0) {
            System.exit(1);
        }
    }
}
