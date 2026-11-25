package com.sabaytayo;

// Encapsulates visual stage direction for a single dialogue beat:
// optional full-screen Event CG angle cut, camera framing, character sprites, and motion effect.
public class StageCue {

    public enum CameraAngle {
        WIDE,
        TWO_SEAT,
        FOCUS_LEFT,
        FOCUS_RIGHT,
        WINDOW_PAN,
        DRAMATIC_ZOOM
    }

    public enum EffectType {
        NONE,
        ENTRANCE_SLIDE,
        TRAFFIC_SWAY,
        BRAKE_JOLT,
        WARM_LEAN,
        GENTLE_NOD,
        AWKWARD_SLUMP,
        STOP_SLOWDOWN,
        STAND_UP,
        KILIG_FLUSTER,
        PLAYFUL_HOP,
        SHAKE_BLUR,
        QUIET_LOOK_AWAY,
        BUS_HEADLIGHT_ARRIVAL
    }

    public static final String CG_BUS_STOP_WAIT = "/assets/CG/cg_bus_stop_wait0.png";
    public static final String CG_BUS_STOP_WAIT1 = "/assets/CG/cg_bus_stop_wait1.png";
    public static final String CG_BOARDING = "/assets/CG/cg_boarding_seat.png";
    public static final String CG_ASK_SEAT = "/assets/CG/cg_ask_seat.png";
    public static final String CG_TRAFFIC_WINDOW = "/assets/CG/cg_traffic_window.png";
    public static final String CG_SUDDEN_BRAKE = "/assets/CG/cg_sudden_brake.png";
    public static final String CG_QUIET_WINDOW = "/assets/CG/cg_rainy_window_quiet.png";
    public static final String CG_WARM_SMILE = "/assets/CG/cg_warm_earphone_smile.png";
    public static final String CG_EXIT_LOOKBACK = "/assets/CG/cg_bus_exit_lookback.png";

    // Returns a short shot description for the in-game Missing-CG placeholder card.
    public static String getCgDescription(String cgPath) {
        if (cgPath == null) {
            return "";
        }
        switch (cgPath) {
            case CG_BUS_STOP_WAIT:
                return "Close-up of MC waiting at the rainy twilight bus stop, watching the wet street.";
            case CG_BUS_STOP_WAIT1:
                return "MC's eyes light up with a hopeful smile as the bus arrives.";
            case CG_BOARDING:
                return "Rainy bus aisle POV — walking down the crowded aisle and spotting the stranger by the window with one free seat left.";
            case CG_ASK_SEAT:
                return "MC standing by the aisle asking \"Excuse me, may nakaupo ba rito?\" while stranger glances up.";
            case CG_TRAFFIC_WINDOW:
                return "Rain-streaked bus window with heavy twilight traffic; stranger adjusts one earphone looking outside.";
            case CG_SUDDEN_BRAKE:
                return "Sudden brake jolt — stranger lurches forward and lightly bumps head on the seat in front.";
            case CG_QUIET_WINDOW:
                return "Quiet distance — raindrops on glass, blurred streetlights, unspoken silence between seats.";
            case CG_WARM_SMILE:
                return "Warm Lane highlight — stranger removes one earphone and turns toward MC with a warm smile.";
            case CG_EXIT_LOOKBACK:
                return "Bus nearing the stop — MC stands up in the aisle and looks back one last time at the stranger.";
            default:
                return "Event CG artwork slot (16:9 PNG).";
        }
    }

    private final String cgPath;
    private final boolean useWideBackground;
    private final CameraAngle cameraAngle;
    private final String maleMood;
    private final String femMood;
    private final boolean showMale;
    private final boolean showFem;
    private final boolean showConductor;
    private final EffectType effectType;
    private final boolean playBrakeSfx;
    private final boolean playHeadThudSfx;
    private final boolean playPhoneNotificationSfx;

    public StageCue(String cgPath, boolean useWideBackground, CameraAngle cameraAngle,
                    String maleMood, String femMood, boolean showMale, boolean showFem,
                    EffectType effectType, boolean playBrakeSfx) {
        this(cgPath, useWideBackground, cameraAngle, maleMood, femMood,
                showMale, showFem, effectType, playBrakeSfx, false, false, false);
    }

    public StageCue(String cgPath, boolean useWideBackground, CameraAngle cameraAngle,
                    String maleMood, String femMood, boolean showMale, boolean showFem,
                    EffectType effectType, boolean playBrakeSfx,
                    boolean playHeadThudSfx, boolean playPhoneNotificationSfx) {
        this(cgPath, useWideBackground, cameraAngle, maleMood, femMood,
                showMale, showFem, effectType, playBrakeSfx, playHeadThudSfx, playPhoneNotificationSfx, false);
    }

    public StageCue(String cgPath, boolean useWideBackground, CameraAngle cameraAngle,
                    String maleMood, String femMood, boolean showMale, boolean showFem,
                    EffectType effectType, boolean playBrakeSfx,
                    boolean playHeadThudSfx, boolean playPhoneNotificationSfx, boolean showConductor) {
        this.cgPath = cgPath;
        this.useWideBackground = useWideBackground;
        this.cameraAngle = cameraAngle;
        this.maleMood = maleMood;
        this.femMood = femMood;
        this.showMale = showMale;
        this.showFem = showFem;
        this.showConductor = showConductor;
        this.effectType = effectType;
        this.playBrakeSfx = playBrakeSfx;
        this.playHeadThudSfx = playHeadThudSfx;
        this.playPhoneNotificationSfx = playPhoneNotificationSfx;
    }

    public String getCgPath() {
        return cgPath;
    }

    public boolean isUseWideBackground() {
        return useWideBackground;
    }

    public CameraAngle getCameraAngle() {
        return cameraAngle;
    }

    public String getMaleMood() {
        return maleMood;
    }

    public String getFemMood() {
        return femMood;
    }

    public boolean isShowMale() {
        return showMale;
    }

    public boolean isShowFem() {
        return showFem;
    }

    public boolean isShowConductor() {
        return showConductor;
    }

    public EffectType getEffectType() {
        return effectType;
    }

    public boolean isPlayBrakeSfx() {
        return playBrakeSfx;
    }

    public boolean isPlayHeadThudSfx() {
        return playHeadThudSfx;
    }

    public boolean isPlayPhoneNotificationSfx() {
        return playPhoneNotificationSfx;
    }

    // Returns a copy of this cue without replaying motion effects or SFX (for split follow-up beats).
    public StageCue withoutEffect() {
        return new StageCue(cgPath, useWideBackground, cameraAngle,
                maleMood, femMood, showMale, showFem, EffectType.NONE, false, false, false, showConductor);
    }

    // Returns the visual stage direction for a specific scene and dialogue beat index.
    public static StageCue forSceneBeat(String sceneId, int beatIndex, Momentum momentum) {
        return forSceneBeat(sceneId, beatIndex, momentum, "STANDARD");
    }

    // Returns the visual stage direction for a specific scene, dialogue beat index, and S08 branch.
    public static StageCue forSceneBeat(String sceneId, int beatIndex, Momentum momentum, String s08Variant) {
        int m = (momentum != null) ? momentum.getValue() : 0;

        switch (sceneId) {
            case "S01":
                if (beatIndex == 0) {
                    // Beat 0: Close-up of MC waiting at the rainy bus stop
                    return new StageCue(CG_BUS_STOP_WAIT, true, CameraAngle.TWO_SEAT,
                            "deadpan", "neutral", false, false, EffectType.NONE, false);
                } else if (beatIndex == 1) {
                    // Beat 1: MC's eyes light up — "Oh, here's the bus!"
                    return new StageCue(CG_BUS_STOP_WAIT1, true, CameraAngle.FOCUS_LEFT,
                            "surprised", "neutral", false, false, EffectType.BUS_HEADLIGHT_ARRIVAL, false);
                } else if (beatIndex == 2) {
                    // Beat 2: Rainy bus aisle POV — spotting the girl and the last empty seat
                    return new StageCue(CG_BOARDING, true, CameraAngle.WIDE,
                            "neutral", "neutral", false, false, EffectType.TRAFFIC_SWAY, false);
                } else if (beatIndex == 3) {
                    // Beat 3: MC asks "Excuse me, may nakaupo ba rito?" (CG of MC standing; fallback hides MC until seated)
                    return new StageCue(CG_ASK_SEAT, false, CameraAngle.FOCUS_RIGHT,
                            "neutral", "neutral", false, true, EffectType.NONE, false);
                } else {
                    // Beat 4: Stranger shifts bag and nods while MC settles in — keep ask-seat CG until advancing to S02
                    return new StageCue(CG_ASK_SEAT, false, CameraAngle.FOCUS_RIGHT,
                            "neutral", "shy", false, true, EffectType.NONE, false);
                }

            case "S02":
                if (beatIndex == 0) {
                    return new StageCue(CG_TRAFFIC_WINDOW, false, CameraAngle.WINDOW_PAN,
                            "neutral", "phone_looking", true, true, EffectType.TRAFFIC_SWAY, false);
                } else {
                    return new StageCue(null, false, CameraAngle.TWO_SEAT,
                            "awkward", "phone_looking", true, true, EffectType.NONE, false);
                }

            case "S03":
                if (beatIndex == 0) {
                    // Calm beat before the brake: both return to neutral expressions in quiet silence
                    return new StageCue(null, false, CameraAngle.TWO_SEAT,
                            "neutral", "neutral", true, true, EffectType.NONE, false);
                }
                // Notable action: Sudden brake cuts to full-screen CG angle (or dramatic zoom/jolt fallback)
                boolean firstImpact = (beatIndex == 1);
                boolean headBumpBeat = (beatIndex == 2);
                EffectType brakeEffect = firstImpact ? EffectType.BRAKE_JOLT : EffectType.NONE;
                CameraAngle s03Angle = headBumpBeat ? CameraAngle.FOCUS_RIGHT
                        : (firstImpact ? CameraAngle.DRAMATIC_ZOOM : CameraAngle.TWO_SEAT);
                String s03Male = firstImpact ? "surprised" : "awkward";
                String s03Fem = firstImpact ? "surprised" : "wince";
                return new StageCue(CG_SUDDEN_BRAKE, false, s03Angle,
                        s03Male, s03Fem, true, true, brakeEffect, firstImpact, headBumpBeat, false);

            case "S04A":
                return new StageCue(null, false, beatIndex == 1 ? CameraAngle.FOCUS_RIGHT : CameraAngle.TWO_SEAT,
                        "relieved_smile", "warm_smile", true, true,
                        beatIndex == 0 ? EffectType.WARM_LEAN : EffectType.NONE, false);

            case "S04B":
                boolean warmS04B = (m >= 0);
                return new StageCue(null, false, CameraAngle.TWO_SEAT,
                        warmS04B ? "relieved_smile" : "awkward",
                        warmS04B ? "warm_smile" : "annoyed",
                        true, true,
                        beatIndex == 0 ? (warmS04B ? EffectType.GENTLE_NOD : EffectType.AWKWARD_SLUMP) : EffectType.NONE,
                        false);

            case "S04C":
                return new StageCue(CG_QUIET_WINDOW, false, CameraAngle.WINDOW_PAN,
                        "deadpan", "annoyed", true, true, EffectType.AWKWARD_SLUMP, false);

            case "S05A":
                if (beatIndex == 0) {
                    return new StageCue(null, false, CameraAngle.TWO_SEAT,
                            "neutral", "neutral", true, true, EffectType.TRAFFIC_SWAY, false, false, true);
                } else if (beatIndex == 1) {
                    return new StageCue(null, false, CameraAngle.TWO_SEAT,
                            "sincere", "neutral", true, true, EffectType.WARM_LEAN, false);
                } else if (beatIndex == 2) {
                    // Key moment: she shares her name with a warm smile
                    return new StageCue(null, false, CameraAngle.FOCUS_RIGHT,
                            "sincere", "warm_smile", true, true, EffectType.GENTLE_NOD, false);
                } else {
                    return new StageCue(null, false, CameraAngle.TWO_SEAT,
                            "relieved_smile", "warm_smile", true, true, EffectType.NONE, false);
                }

            case "S05B":
                if (beatIndex == 0) {
                    return new StageCue(CG_QUIET_WINDOW, false, CameraAngle.WINDOW_PAN,
                            "awkward", "phone_looking", true, true, EffectType.QUIET_LOOK_AWAY, false);
                } else {
                    return new StageCue(null, false, CameraAngle.TWO_SEAT,
                            "awkward", "phone_looking", true, true, EffectType.NONE, false);
                }

            case "S06A":
                // Warm Lane highlight: she removes one earphone and smiles warmly
                return new StageCue(CG_WARM_SMILE, false, CameraAngle.FOCUS_RIGHT,
                        "sincere", "warm_smile", true, true,
                        beatIndex == 0 ? EffectType.WARM_LEAN : EffectType.NONE, false);

            case "S06B":
                if (beatIndex == 0) {
                    return new StageCue(CG_QUIET_WINDOW, false, CameraAngle.WINDOW_PAN,
                            "awkward", "phone_looking", true, true, EffectType.AWKWARD_SLUMP, false);
                } else if (beatIndex == 1) {
                    return new StageCue(null, false, CameraAngle.TWO_SEAT,
                            "awkward", "phone_looking", true, true, EffectType.NONE, false);
                } else {
                    return new StageCue(null, false, CameraAngle.TWO_SEAT,
                            "awkward", "neutral", true, true, EffectType.GENTLE_NOD, false);
                }

            case "S07":
                String maleMoodS07 = (m >= 2) ? "relieved_smile" : "neutral";
                String femMoodS07 = (m >= 2) ? "warm_smile" : "surprised";
                boolean conductorVisible = (beatIndex == 0 || beatIndex == 1);
                EffectType s07Effect = (beatIndex == 0) ? EffectType.STOP_SLOWDOWN : EffectType.NONE;
                return new StageCue(null, false, CameraAngle.TWO_SEAT,
                        maleMoodS07, femMoodS07, true, true,
                        s07Effect, false, false, false, conductorVisible);

            case "S08":
                String variant = (s08Variant != null) ? s08Variant : "STANDARD";
                if ("LOVERS".equals(variant)) {
                    // Stay seated dialogue-only climax (no CG, no exit lookback)
                    if (beatIndex == 0) {
                        return new StageCue(null, false, CameraAngle.TWO_SEAT,
                                "neutral", "neutral", true, true, EffectType.NONE, false, false, false, true);
                    } else if (beatIndex == 1) {
                        return new StageCue(null, false, CameraAngle.TWO_SEAT,
                                "sincere", "neutral", true, true, EffectType.NONE, false);
                    } else if (beatIndex == 2) {
                        return new StageCue(null, false, CameraAngle.FOCUS_RIGHT,
                                "sincere", "warm_smile", true, true, EffectType.GENTLE_NOD, false);
                    } else {
                        return new StageCue(null, false, CameraAngle.TWO_SEAT,
                                "flustered", "warm_smile", true, true, EffectType.WARM_LEAN, false);
                    }
                } else if ("BLOCKED".equals(variant)) {
                    // Option B: Conductor calls -> Stranger stands up first and walks straight past MC
                    if (beatIndex == 0) {
                        return new StageCue(null, false, CameraAngle.TWO_SEAT,
                                "awkward", "annoyed", true, true, EffectType.NONE, false, false, false, true);
                    } else if (beatIndex == 1) {
                        return new StageCue(null, false, CameraAngle.TWO_SEAT,
                                "surprised", "annoyed", true, true, EffectType.AWKWARD_SLUMP, false);
                    } else if (beatIndex == 2) {
                        return new StageCue(null, false, CameraAngle.FOCUS_RIGHT,
                                "awkward", "annoyed", true, true, EffectType.NONE, false);
                    } else {
                        return new StageCue(CG_QUIET_WINDOW, false, CameraAngle.WINDOW_PAN,
                                "deadpan", "annoyed", true, false, EffectType.QUIET_LOOK_AWAY, false);
                    }
                } else {
                    // STANDARD: Best Friends / Rivals / Just Strangers — Conductor calls -> MC stands & looks back
                    String maleMoodS08 = (m >= 0) ? "sincere" : "neutral";
                    String femMoodS08 = (m >= 0) ? "shy" : "neutral";
                    if (beatIndex == 0) {
                        return new StageCue(null, false, CameraAngle.TWO_SEAT,
                                maleMoodS08, femMoodS08, true, true, EffectType.NONE, false, false, false, true);
                    } else if (beatIndex == 1) {
                        // Two-seat parting sprites before final choice; EndingView immediately plays the Ending CG
                        return new StageCue(null, false, CameraAngle.TWO_SEAT,
                                maleMoodS08, femMoodS08, true, true, EffectType.STAND_UP, false);
                    } else {
                        return new StageCue(null, false, CameraAngle.TWO_SEAT,
                                maleMoodS08, femMoodS08, true, true, EffectType.NONE, false);
                    }
                }

            default:
                return new StageCue(null, false, CameraAngle.TWO_SEAT,
                        "neutral", "neutral", true, true, EffectType.NONE, false);
        }
    }

    // Returns stage direction for Beat 0 of a player choice (steady two-shot when speaking, or rainy window on Quiet).
    public static StageCue forChoiceSpeakBeat(DialogueChoice choice) {
        String tone = (choice != null && choice.getTone() != null) ? choice.getTone() : "";
        switch (tone) {
            case "Flirty":
            case "Sarcastic":
                return new StageCue(null, false, CameraAngle.TWO_SEAT,
                        "smirk", null, true, true, EffectType.NONE, false);
            case "Sincere":
                return new StageCue(null, false, CameraAngle.TWO_SEAT,
                        "sincere", null, true, true, EffectType.NONE, false);
            case "Blunt":
                return new StageCue(null, false, CameraAngle.TWO_SEAT,
                        "deadpan", null, true, true, EffectType.NONE, false);
            default:
                // Quiet / ContinueChoice switches briefly to the rainy window angle
                return new StageCue(CG_QUIET_WINDOW, false, CameraAngle.WINDOW_PAN,
                        "deadpan", "neutral", true, true, EffectType.QUIET_LOOK_AWAY, false);
        }
    }

    // Returns stage direction for Beat 1 of a choice (zooms in only on genuine emotional/warm reactions).
    public static StageCue forChoiceReactionBeat(DialogueChoice choice, String reaction, Momentum momentum) {
        boolean isWarm = momentum != null && momentum.getValue() >= 0;
        boolean isHighMomentum = momentum != null && momentum.getValue() >= 2;
        String lower = (reaction != null) ? reaction.toLowerCase() : "";
        String tone = (choice != null && choice.getTone() != null) ? choice.getTone() : "";

        switch (tone) {
            case "Flirty":
                if (isHighMomentum || lower.contains("nagulat") || lower.contains("smile")
                        || lower.contains("sure") || lower.contains("ohh") || lower.contains("sabay tayo")) {
                    boolean numberShared = lower.contains("sure") || lower.contains("okay lang")
                            || lower.contains("sabay tayo");
                    return new StageCue(null, false, CameraAngle.FOCUS_RIGHT,
                            "flustered", "flustered", true, true, EffectType.KILIG_FLUSTER,
                            false, false, numberShared);
                }
                return new StageCue(null, false, CameraAngle.TWO_SEAT,
                        "surprised", "surprised", true, true, EffectType.SHAKE_BLUR, false);

            case "Sincere":
                if (isWarm) {
                    boolean igShared = lower.contains("follow mo") || lower.contains("waved warmly");
                    boolean extraWarm = igShared || lower.contains("smile") || lower.contains("thank you")
                            || lower.contains("salamat") || lower.contains("nice meeting")
                            || lower.contains("napangiti") || lower.contains("masaya") || lower.contains("oo nga");
                    return new StageCue(null, false, extraWarm ? CameraAngle.FOCUS_RIGHT : CameraAngle.TWO_SEAT,
                            extraWarm ? "relieved_smile" : "sincere", extraWarm ? "warm_smile" : "shy",
                            true, true, EffectType.WARM_LEAN, false, false, igShared);
                }
                return new StageCue(null, false, CameraAngle.TWO_SEAT,
                        "sincere", "shy", true, true, EffectType.GENTLE_NOD, false);

            case "Sarcastic":
                if (isHighMomentum || lower.contains("natawa") || lower.contains("napangiti")
                        || lower.contains("haha") || lower.contains("laugh") || lower.contains("smirk")) {
                    return new StageCue(null, false, CameraAngle.TWO_SEAT,
                            "laughing", "laughing", true, true, EffectType.PLAYFUL_HOP, false);
                }
                return new StageCue(null, false, CameraAngle.TWO_SEAT,
                        "awkward", "annoyed", true, true, EffectType.SHAKE_BLUR, false);

            case "Blunt":
                return new StageCue(null, false, CameraAngle.TWO_SEAT,
                        "deadpan", "annoyed", true, true, EffectType.AWKWARD_SLUMP, false);

            default:
                return new StageCue(CG_QUIET_WINDOW, false, CameraAngle.WINDOW_PAN,
                        "deadpan", "neutral", true, true, EffectType.QUIET_LOOK_AWAY, false);
        }
    }
}
