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

        // S01 Beat 0–1: MC waiting at the bus stop -> eyes light up ("Oh, here's the bus!")
        StageCue s01b0 = StageCue.forSceneBeat("S01", 0, warmM);
        check("S01 Beat 0 maps to bus stop wait CG", StageCue.CG_BUS_STOP_WAIT.equals(s01b0.getCgPath()));
        check("S01 Beat 0 hides floating sprites during bus stop CG", !s01b0.isShowMale() && !s01b0.isShowFem());

        StageCue s01b1 = StageCue.forSceneBeat("S01", 1, warmM);
        check("S01 Beat 1 switches to arrival smile bus stop CG", StageCue.CG_BUS_STOP_WAIT1.equals(s01b1.getCgPath()));
        check("S01 Beat 1 triggers bus headlight arrival effect",
                s01b1.getEffectType() == StageCue.EffectType.BUS_HEADLIGHT_ARRIVAL);

        // S01 Beat 2: Stranger seated in bus aisle CG before MC asks to sit (no floating sprites)
        StageCue s01b2 = StageCue.forSceneBeat("S01", 2, warmM);
        check("S01 Beat 2 maps to boarding CG", StageCue.CG_BOARDING.equals(s01b2.getCgPath()));
        check("S01 Beat 2 hides floating sprites so she stays seated in BG", !s01b2.isShowMale() && !s01b2.isShowFem());

        // S01 Beat 3: Ask-seat CG ("Excuse me, may nakaupo ba rito?")
        StageCue s01b3 = StageCue.forSceneBeat("S01", 3, warmM);
        check("S01 Beat 3 maps to ask-seat CG", StageCue.CG_ASK_SEAT.equals(s01b3.getCgPath()));

        // S01 Beat 4: Keeps ask-seat CG until advancing past this line into S02
        StageCue s01b4 = StageCue.forSceneBeat("S01", 4, warmM);
        check("S01 Beat 4 keeps ask-seat CG until advancing", StageCue.CG_ASK_SEAT.equals(s01b4.getCgPath()));

        // S02 Beat 0: Traffic built up window CG
        StageCue s02b0 = StageCue.forSceneBeat("S02", 0, warmM);
        check("S02 Beat 0 maps to traffic window CG", StageCue.CG_TRAFFIC_WINDOW.equals(s02b0.getCgPath()));

        // S03 Beat 0: Neutral silence pause ("...."); Beat 1: Sudden brake CG + SFX + jolt; Beat 2: Head thud SFX ("Aray!")
        StageCue s03b0 = StageCue.forSceneBeat("S03", 0, warmM);
        check("S03 Beat 0 resets both sprites to neutral before brake",
                s03b0.getCgPath() == null && "neutral".equals(s03b0.getMaleMood())
                        && "neutral".equals(s03b0.getFemMood()) && !s03b0.isPlayBrakeSfx());
        StageCue s03b1 = StageCue.forSceneBeat("S03", 1, warmM);
        check("S03 Beat 1 maps to sudden brake CG", StageCue.CG_SUDDEN_BRAKE.equals(s03b1.getCgPath()));
        check("S03 Beat 1 triggers brake SFX and jolt", s03b1.isPlayBrakeSfx() && s03b1.getEffectType() == StageCue.EffectType.BRAKE_JOLT);
        StageCue s03b2 = StageCue.forSceneBeat("S03", 2, warmM);
        check("S03 Beat 2 triggers head thud SFX", s03b2.isPlayHeadThudSfx() && !s03b2.isPlayBrakeSfx());

        // S05A Beat 0: Phone notification SFX; S07 Beats 0-2: Conductor enters (3 sprites) -> calls stop -> leaves
        StageCue s05ab0 = StageCue.forSceneBeat("S05A", 0, warmM);
        check("S05A Beat 0 triggers phone notification SFX", s05ab0.isPlayPhoneNotificationSfx());
        StageCue s07b0 = StageCue.forSceneBeat("S07", 0, warmM);
        StageCue s07b1 = StageCue.forSceneBeat("S07", 1, warmM);
        StageCue s07b2 = StageCue.forSceneBeat("S07", 2, warmM);
        check("S07 Beat 0 brings conductor on stage with MC and Stranger (3 sprites)",
                s07b0.isShowMale() && s07b0.isShowFem() && s07b0.isShowConductor());
        check("S07 Beat 1 keeps all 3 sprites visible while conductor calls stop",
                s07b1.isShowMale() && s07b1.isShowFem() && s07b1.isShowConductor());
        check("S07 Beat 2 hides conductor when he moves down the aisle",
                s07b2.isShowMale() && s07b2.isShowFem() && !s07b2.isShowConductor());

        // S04A Beat 0: Returns to normal sprites
        StageCue s04a = StageCue.forSceneBeat("S04A", 0, warmM);
        check("S04A returns to sprites after S03 CG", s04a.getCgPath() == null && "warm_smile".equals(s04a.getFemMood()));

        // S04B: Warm sarcastic path vs. Cold unamused path
        Momentum coldM = new Momentum();
        coldM.adjust(-1);
        StageCue s04bWarm = StageCue.forSceneBeat("S04B", 0, warmM);
        StageCue s04bCold = StageCue.forSceneBeat("S04B", 0, coldM);
        check("S04B warm path uses warm_smile and gentle nod",
                s04bWarm.getCgPath() == null && "warm_smile".equals(s04bWarm.getFemMood())
                        && s04bWarm.getEffectType() == StageCue.EffectType.GENTLE_NOD);
        check("S04B cold path keeps annoyed mood instead of shy/apologetic",
                s04bCold.getCgPath() == null && "annoyed".equals(s04bCold.getFemMood())
                        && "awkward".equals(s04bCold.getMaleMood())
                        && s04bCold.getEffectType() == StageCue.EffectType.AWKWARD_SLUMP);

        // S06A Warm earphone CG & S08 Exit lookback CG
        StageCue s06a = StageCue.forSceneBeat("S06A", 0, warmM);
        check("S06A maps to warm earphone smile CG", StageCue.CG_WARM_SMILE.equals(s06a.getCgPath()));

        StageCue s08b0 = StageCue.forSceneBeat("S08", 0, warmM, "STANDARD");
        check("Standard S08 Beat 0 shows conductor cameo on two-seat stage",
                s08b0.getCgPath() == null && s08b0.isShowConductor() && s08b0.isShowMale() && s08b0.isShowFem());
        StageCue s08b1 = StageCue.forSceneBeat("S08", 1, warmM, "STANDARD");
        check("Standard S08 Beat 1 uses sprite stage before Ending CG",
                s08b1.getCgPath() == null && s08b1.isShowMale() && s08b1.isShowFem());

        StageCue s08Lovers2 = StageCue.forSceneBeat("S08", 2, warmM, "LOVERS");
        check("Lovers S08 Beat 2 stays seated with no CG",
                s08Lovers2.getCgPath() == null && s08Lovers2.isShowMale() && s08Lovers2.isShowFem());

        StageCue s08Blocked3 = StageCue.forSceneBeat("S08", 3, warmM, "BLOCKED");
        check("Blocked S08 Beat 3 hides Stranger after walking past MC",
                s08Blocked3.isShowMale() && !s08Blocked3.isShowFem());
        check("S01 Beat 3 hides MC sprite until seated on fallback", !s01b3.isShowMale() && s01b3.isShowFem());
        check("CG description returns shot text for bus stop CG", !StageCue.getCgDescription(StageCue.CG_BUS_STOP_WAIT).isEmpty());
        check("CG description returns shot text for missing placeholder", !StageCue.getCgDescription(StageCue.CG_SUDDEN_BRAKE).isEmpty());

        // Choice reaction beats
        FlirtyChoice flirty = new FlirtyChoice("[Flirty] Test", "S07", "She smiled softly.", "Okay.");
        StageCue speakCue = StageCue.forChoiceSpeakBeat(flirty);
        StageCue reactCue = StageCue.forChoiceReactionBeat(flirty, "She smiled softly.", warmM);
        check("Flirty speak beat stays in steady two-seat framing with smirk", speakCue.getCameraAngle() == StageCue.CameraAngle.TWO_SEAT && "smirk".equals(speakCue.getMaleMood()));
        check("Flirty warm reaction focuses on stranger with kilig fluster", reactCue.getCameraAngle() == StageCue.CameraAngle.FOCUS_RIGHT && "flustered".equals(reactCue.getFemMood()) && reactCue.getEffectType() == StageCue.EffectType.KILIG_FLUSTER);

        // S02 traffic line uses neutral talking; narration beat drops back to neutral
        SincereChoice traffic = new SincereChoice("[Sincere] \"Ang traffic ngayon, no? Mukhang matatagalan pa tayo rito.\"",
                "S03", "\"Oo nga eh.\" She glanced at you.", "\"Oo nga eh.\"");
        StageCue trafficCue = StageCue.forChoiceReactionBeat(traffic, "\"Oo nga eh.\" She glanced at you.", warmM);
        check("S02 traffic reply uses neutral_talking", "neutral_talking".equals(trafficCue.getFemMood()));
        check("Narration beat of traffic reply uses neutral",
                "neutral".equals(StageCue.adjustReactionBeat(trafficCue, "Narrator", true).getFemMood()));

        // After the flustered reaction, her spoken lines switch to flustered_laugh
        StageCue flusteredCue = StageCue.forChoiceReactionBeat(flirty, "She smiled softly.", warmM);
        check("Flirty narration beat keeps flustered",
                "flustered".equals(StageCue.adjustReactionBeat(flusteredCue, "Narrator", false).getFemMood()));
        check("Flirty spoken beat switches to flustered_laugh",
                "flustered_laugh".equals(StageCue.adjustReactionBeat(flusteredCue, "Fatima", false).getFemMood()));
        check("Narration after she spoke stays flustered_laugh",
                "flustered_laugh".equals(StageCue.adjustReactionBeat(flusteredCue, "Narrator", true).getFemMood()));

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
