package com.sabaytayo;

// Tests StageCue beat-by-beat camera cuts, CG hooks, sprite visibility, and choice reactions.
public class StageCueTest {

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
        Momentum warmM = new Momentum();
        warmM.adjust(3);

        // S01 Beat 0: Stranger seated in background CG before MC asks to sit (no floating sprites)
        StageCue s01b0 = StageCue.forSceneBeat("S01", 0, warmM);
        check("S01 Beat 0 maps to boarding CG", StageCue.CG_BOARDING.equals(s01b0.getCgPath()));
        check("S01 Beat 0 hides floating sprites so she stays seated in BG", !s01b0.isShowMale() && !s01b0.isShowFem());

        // S01 Beat 1: Ask-seat CG ("Excuse me, may nakaupo ba rito?")
        StageCue s01b1 = StageCue.forSceneBeat("S01", 1, warmM);
        check("S01 Beat 1 maps to ask-seat CG", StageCue.CG_ASK_SEAT.equals(s01b1.getCgPath()));

        // S01 Beat 2: Returns to two-seat close-up sprites as Sam sits down
        StageCue s01b2 = StageCue.forSceneBeat("S01", 2, warmM);
        check("S01 Beat 2 returns to seated sprites", s01b2.getCgPath() == null && s01b2.isShowMale() && s01b2.isShowFem());

        // S02 Beat 0: Traffic built up window CG
        StageCue s02b0 = StageCue.forSceneBeat("S02", 0, warmM);
        check("S02 Beat 0 maps to traffic window CG", StageCue.CG_TRAFFIC_WINDOW.equals(s02b0.getCgPath()));

        // S03 Beat 0: Sudden brake CG + SFX + jolt
        StageCue s03b0 = StageCue.forSceneBeat("S03", 0, warmM);
        check("S03 Beat 0 maps to sudden brake CG", StageCue.CG_SUDDEN_BRAKE.equals(s03b0.getCgPath()));
        check("S03 Beat 0 triggers brake SFX and jolt", s03b0.isPlayBrakeSfx() && s03b0.getEffectType() == StageCue.EffectType.BRAKE_JOLT);

        // S04A Beat 0: Returns to normal sprites
        StageCue s04a = StageCue.forSceneBeat("S04A", 0, warmM);
        check("S04A returns to sprites after S03 CG", s04a.getCgPath() == null && "warm_smile".equals(s04a.getFemMood()));

        // S06A Warm earphone CG & S08 Exit lookback CG
        StageCue s06a = StageCue.forSceneBeat("S06A", 0, warmM);
        check("S06A maps to warm earphone smile CG", StageCue.CG_WARM_SMILE.equals(s06a.getCgPath()));

        StageCue s08b0 = StageCue.forSceneBeat("S08", 0, warmM);
        check("S08 Beat 0 maps to exit lookback CG", StageCue.CG_EXIT_LOOKBACK.equals(s08b0.getCgPath()));
        check("S08 Beat 0 hides close-up sprites on wide fallback", !s08b0.isShowMale() && !s08b0.isShowFem());
        check("S01 Beat 1 hides MC sprite until seated on fallback", !s01b1.isShowMale() && s01b1.isShowFem());

        // Choice reaction beats
        FlirtyChoice flirty = new FlirtyChoice("[Flirty] Test", "S07", "She smiled softly.", "Okay.");
        StageCue speakCue = StageCue.forChoiceSpeakBeat(flirty);
        StageCue reactCue = StageCue.forChoiceReactionBeat(flirty, "She smiled softly.", warmM);
        check("Flirty speak beat stays in steady two-seat framing with smirk", speakCue.getCameraAngle() == StageCue.CameraAngle.TWO_SEAT && "smirk".equals(speakCue.getMaleMood()));
        check("Flirty warm reaction focuses on stranger with kilig fluster", reactCue.getCameraAngle() == StageCue.CameraAngle.FOCUS_RIGHT && "flustered".equals(reactCue.getFemMood()) && reactCue.getEffectType() == StageCue.EffectType.KILIG_FLUSTER);

        BluntChoice blunt = new BluntChoice("[Blunt] Test", "S07", "Okay.", "Okay.");
        StageCue bluntReact = StageCue.forChoiceReactionBeat(blunt, "Okay.", warmM);
        check("Blunt reaction keeps steady two-seat framing without zoom", bluntReact.getCameraAngle() == StageCue.CameraAngle.TWO_SEAT);

        ContinueChoice quiet = new ContinueChoice("[Quiet] Look outside.", "S03", "Quiet rain.");
        StageCue quietCue = StageCue.forChoiceSpeakBeat(quiet);
        check("Quiet choice switches to rainy window CG", StageCue.CG_QUIET_WINDOW.equals(quietCue.getCgPath()));

        System.out.println("\n===== STAGE CUE TEST SUMMARY =====");
        System.out.println("Passed: " + passed + " | Failed: " + failed);
        if (failed > 0) {
            throw new AssertionError("StageCueTest had " + failed + " failure(s).");
        }
    }
}
