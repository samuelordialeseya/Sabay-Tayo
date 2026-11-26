package com.sabaytayo;

import java.util.List;

// Tests Encounter scene graph integrity, SceneNotFoundException, Momentum gating, and token formatting.
public class EncounterGraphTest {

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
        Encounter encounter = new Encounter();
        check("Encounter has 12 scenes", encounter.getScenes().size() == 12);
        check("Starts at S01", "S01".equals(encounter.getCurrentSceneId()));

        // Verify valid advanceTo
        encounter.advanceTo("S05A");
        check("advanceTo S05A succeeds", "S05A".equals(encounter.getCurrentSceneId()));

        // Verify SceneNotFoundException on invalid scene ID
        boolean threwMissing = false;
        try {
            encounter.advanceTo("S99_INVALID");
        } catch (SceneNotFoundException e) {
            threwMissing = "S99_INVALID".equals(e.getMissingId());
        }
        check("Invalid scene ID throws SceneNotFoundException", threwMissing);

        // Verify FlirtyChoice gating in S05A via Scene.getAvailableChoices(Momentum)
        Scene s05a = encounter.getScene("S05A");
        Momentum lowM = new Momentum();
        List<DialogueChoice> atZero = s05a.getAvailableChoices(lowM);
        check("S05A unlocks 4 choices (Sincere/Sarcastic/Blunt/Quiet) at Momentum 0", atZero.size() == 4);

        Momentum highM = new Momentum();
        highM.adjust(2);
        List<DialogueChoice> atTwo = s05a.getAvailableChoices(highM);
        check("S05A unlocks 5 choices (including Flirty) at Momentum +2", atTwo.size() == 5);

        // Verify Two-Lane Momentum branching
        Momentum coldM = new Momentum();
        coldM.adjust(-1);
        check("Momentum >= 0 branches to S05A (Warm Lane)", "S05A".equals(encounter.getMomentumBranch(lowM)));
        check("Momentum < 0 branches to S05B (Cold Lane)", "S05B".equals(encounter.getMomentumBranch(coldM)));

        // Verify all scenes and choice reactions format cleanly with zero unresolved tokens
        for (Pronouns playerP : Pronouns.values()) {
            Player player = new Player("Sam", playerP);
            for (Pronouns strangerP : Pronouns.values()) {
                Stranger stranger = new Stranger(strangerP);
                for (Scene scene : encounter.getScenes().values()) {
                    for (String line : scene.getDialogueLines()) {
                        String out = ScriptFormatter.format(line, player, stranger);
                        if (ScriptFormatter.hasUnresolvedTokens(out)) {
                            check("Unresolved token in " + scene.getSceneId() + ": " + out, false);
                        }
                    }
                    for (DialogueChoice choice : scene.getChoices()) {
                        String cOut = ScriptFormatter.format(choice.getText(), player, stranger);
                        String wOut = ScriptFormatter.format(choice.getWarmReaction(), player, stranger);
                        String kOut = ScriptFormatter.format(choice.getColdReaction(), player, stranger);
                        if (ScriptFormatter.hasUnresolvedTokens(cOut)
                                || ScriptFormatter.hasUnresolvedTokens(wOut)
                                || ScriptFormatter.hasUnresolvedTokens(kOut)) {
                            check("Unresolved token in choice of " + scene.getSceneId(), false);
                        }
                    }
                }
            }
        }
        check("All 12 scenes & choices have zero unresolved tokens across all 9 pronoun pairs", true);

        // Verify S08 branch preparation (Lovers -> "Sabay tayo?" only; Blocked -> Option B exit; Standard -> 4 choices)
        RelationshipStats loverStats = new RelationshipStats();
        loverStats.adjustTrust(20);
        loverStats.adjustAttraction(15);
        encounter.prepareFinalScene(loverStats);
        Scene loversS08 = encounter.getScene("S08");
        check("Lovers S08 configures LOVERS variant", "LOVERS".equals(encounter.getS08Variant()));
        check("Lovers S08 has only Sabay tayo? choice",
                loversS08.getChoices().size() == 1 && loversS08.getChoices().get(0).getText().contains("Sabay tayo?"));

        RelationshipStats blockedStats = new RelationshipStats();
        blockedStats.adjustAnnoyance(25);
        encounter.prepareFinalScene(blockedStats);
        Scene blockedS08 = encounter.getScene("S08");
        check("Blocked S08 configures BLOCKED variant", "BLOCKED".equals(encounter.getS08Variant()));
        check("Blocked S08 uses Option B line Excuse me. Dito na ako.",
                blockedS08.getDialogueLines().stream().anyMatch(l -> l.contains("Excuse me. Dito na ako.")));

        encounter.prepareFinalScene(new RelationshipStats());
        check("Default stats restore STANDARD S08 with 4 choices",
                "STANDARD".equals(encounter.getS08Variant()) && encounter.getScene("S08").getChoices().size() == 4);

        // Verify IG choice in STANDARD S08 is locked when momentum is cold, while Sarcastic/Blunt/Quiet stay available
        Momentum allBluntM = new Momentum();
        allBluntM.setValue(-5);
        List<DialogueChoice> coldS08Choices = encounter.getScene("S08").getAvailableChoices(allBluntM);
        check("STANDARD S08 locks IG choice at Momentum -5 (3 choices unlocked)",
                coldS08Choices.size() == 3
                        && coldS08Choices.stream().noneMatch(c -> c.getText().contains("IG mo")));
        check("STANDARD S08 unlocks all 4 choices (including IG) at Momentum +2",
                encounter.getScene("S08").getAvailableChoices(highM).size() == 4);

        System.out.println("\n===== ENCOUNTER GRAPH TEST SUMMARY =====");
        System.out.println("Passed: " + passed + " | Failed: " + failed);
        if (failed > 0) {
            throw new AssertionError("EncounterGraphTest had " + failed + " failure(s).");
        }
    }
}
