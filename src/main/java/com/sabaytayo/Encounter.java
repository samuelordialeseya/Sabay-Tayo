package com.sabaytayo;

import java.util.HashMap;

// Assembles and manages the 12-scene dialogue graph for the bus encounter.
// Scenes are stored in a HashMap and accessed by their scene ID string.
public class Encounter {

    // The scene graph: maps scene ID -> Scene object
    private HashMap<String, Scene> scenes;

    // ID of the scene currently being shown to the player
    private String currentSceneId;

    // Tracks whether the game has ended (after the final choice in S08)
    private boolean gameOver;

    // Builds the full scene graph and starts at Scene 1.
    public Encounter() {
        scenes = new HashMap<>();
        gameOver = false;
        buildSceneGraph();
        currentSceneId = "S01";
    }

    // Constructs all 12 scenes and adds them to the HashMap.
    private void buildSceneGraph() {

        // S01: Boarding (Narration + Continue)
        Scene s01 = new Scene("S01");
        s01.addDialogueLine("(Scene 1 - Boarding) The bus is packed. You spot an empty seat next to a stranger listening to music.");
        s01.addChoice(new ContinueChoice("Continue...", "S02"));
        s01.setDefaultNextSceneId("S02");
        scenes.put("S01", s01);

        // S02: Traffic Jam - Turn 1 (Sincere / Sarcastic / Blunt)
        Scene s02 = new Scene("S02");
        s02.addDialogueLine("(Scene 2 - Traffic) The bus crawls through heavy traffic. The stranger glances out the window.");
        s02.addDialogueLine("Stranger: \"Grabe, ang traffic naman ngayon.\"");
        s02.addChoice(new SincereChoice(
            "[Sincere] \"Oo nga, sana mawala na agad para makaalis na tayo.\" (Scene 2)",
            "S03",
            "They give you a warm smile. \"Oo nga eh. Sana mabilis na.\"",
            "They nod quietly and look back out the window."
        ));
        s02.addChoice(new SarcasticChoice(
            "[Sarcastic] \"Dito na daw tayo magpapasko! (Scene 2)\"",
            "S03",
            "They laugh a little. \"Ang kulit mo naman.\"",
            "They raise an eyebrow and shift slightly away."
        ));
        s02.addChoice(new BluntChoice(
            "[Blunt] \"Normal naman ang traffic kapag ganito. (Scene 2)\"",
            "S03",
            "They nod slowly. \"Totoo naman.\"",
            "They frown a bit and go back to their phone."
        ));
        scenes.put("S02", s02);

        // S03: Sudden Brake - Turn 2 (Sincere / Sarcastic / Blunt)
        Scene s03 = new Scene("S03");
        s03.addDialogueLine("(Scene 3 - Sudden Brake) The bus brakes hard. The stranger bumps their head on the seat in front.");
        s03.addDialogueLine("Stranger: \"Aray! Grabe naman 'yon!\"");
        s03.addChoice(new SincereChoice(
            "[Sincere] \"Okay ka lang? Baka nasaktan ka. (Scene 3)\"",
            "S04",
            "They look surprised but grateful. \"Salamat sa pag-aalala. Okay lang.\"",
            "They wave it off. \"Okay lang naman.\""
        ));
        s03.addChoice(new SarcasticChoice(
            "[Sarcastic] \"Si kuya driver, may pa-headbang pa! (Scene 3)\"",
            "S04",
            "They laugh despite themselves. \"Ay, natawa naman ako doon.\"",
            "They give you a flat look. \"May commentary pa talaga.\""
        ));
        s03.addChoice(new BluntChoice(
            "[Blunt] \"Mag-ingat ka, baka mauntog ka ulit. (Scene 3)\"",
            "S04",
            "They blink. \"Oo nga, salamat.\"",
            "They look unimpressed. \"...Okay.\""
        ));
        scenes.put("S03", s03);

        // S04: Beat A (Narration + Continue)
        Scene s04 = new Scene("S04");
        s04.addDialogueLine("(Scene 4 - Beat A) The bus steadies. Passengers shuffle closer together. You and the stranger accidentally make eye contact, then both look away.");
        s04.addChoice(new ContinueChoice("Continue...", "S05"));
        s04.setDefaultNextSceneId("S05");
        scenes.put("S04", s04);

        // S05: Saan Ka Bababa? - Turn 3 (Sincere / Sarcastic / Blunt + Flirty gated M >= 2)
        Scene s05 = new Scene("S05");
        s05.addDialogueLine("(Scene 5 - Destination) The stranger pulls out one earphone and glances at you.");
        s05.addDialogueLine("Stranger: \"Ikaw, saan ka bababa?\"");
        s05.addChoice(new SincereChoice(
            "[Sincere] \"Sa may SM pa. Ikaw, malayo pa ba? (Scene 5)\"",
            "S05_DONE",
            "They relax a bit. \"Tatlong stop pa. Okay lang makipag-kwentuhan.\"",
            "They give a short nod. \"Okay.\""
        ));
        s05.addChoice(new SarcasticChoice(
            "[Sarcastic] \"Hindi ko alam, depende sa traffic. (Scene 5)\"",
            "S05_DONE",
            "They smirk. \"Ay, sinusuwerte ka. Hindi ako sasagot hangga't hindi mo sinasabi.\"",
            "They roll their eyes. \"Sige na nga.\""
        ));
        s05.addChoice(new BluntChoice(
            "[Blunt] \"Bakit mo tinatanong? (Scene 5)\"",
            "S05_DONE",
            "They blink. \"Curious lang. Bawal bang magtanong?\"",
            "They look away. \"Okay, sorry na.\""
        ));
        s05.addChoice(new FlirtyChoice(
            "[Flirty] \"Bakit, balak mo bang sumabay? (Scene 5)\"",
            "S05_DONE",
            "They laugh softly. \"Depende kung maganda ang kwento mo hanggang doon.\"",
            "They look caught off guard. \"Ah... sige.\""
        ));
        // S05 has no defaultNextSceneId — branching is handled by Encounter after choices
        scenes.put("S05", s05);

        // S05_DONE: Invisible transition node (momentum branch happens here)
        // Handled in advanceAfterS05() - no actual scene needed

        // S06A: Warm Path - Turn 4A (Sincere / Sarcastic / Blunt)
        Scene s06a = new Scene("S06A");
        s06a.addDialogueLine("(Scene 6A - Warm) The conversation has loosened up. The stranger puts their phone away and faces you.");
        s06a.addDialogueLine("Stranger: \"Nga pala, ano'ng pangalan mo? Hindi ko pa naitanong.\"");
        s06a.addChoice(new SincereChoice(
            "[Sincere] Say your name warmly. (Scene 6A)",
            "S07",
            "They smile back. \"Masaya akong makilala ka.\"",
            "They nod. \"Okay, nice to meet you.\""
        ));
        s06a.addChoice(new SarcasticChoice(
            "[Sarcastic] \"Ngayon ka lang nagtanong, ha? (Scene 6A)\"",
            "S07",
            "They chuckle. \"Ang yabang mo, pero sige, tatandaan ko.\"",
            "They give a half-smile. \"Sige na nga.\""
        ));
        s06a.addChoice(new BluntChoice(
            "[Blunt] Just say your name. (Scene 6A)",
            "S07",
            "They nod. \"Madali lang tandaan. Okay.\"",
            "They look away again. \"Alright.\""
        ));
        scenes.put("S06A", s06a);

        // S06B: Cold Path - Turn 4B (Sincere / Sarcastic / Blunt)
        Scene s06b = new Scene("S06B");
        s06b.addDialogueLine("(Scene 6B - Cold) The conversation has gone quiet. The stranger is back on their phone, staring out the window.");
        s06b.addDialogueLine("Stranger: \"(Staring out) Ang tagal pa ng byahe.\"");
        s06b.addChoice(new SincereChoice(
            "[Sincere] \"Pasensya na kung naging awkward kanina. (Scene 6B)\"",
            "S07",
            "They glance at you. \"Okay lang naman hehe. Nice meeting you.\"",
            "They stay quiet. \"...It's fine.\""
        ));
        s06b.addChoice(new SarcasticChoice(
            "[Sarcastic] \"Nakaka-excite nga ang view, 'di ba? (Scene 6B)\"",
            "S07",
            "They snort. \"Haha, sige na nga, panalo ka doon.\"",
            "They stare at you. \"...Okay.\""
        ));
        s06b.addChoice(new BluntChoice(
            "[Blunt] \"Oo. Wala rin akong masabi. (Scene 6B)\"",
            "S07",
            "They shrug. \"Buti na lang aminado ka.\"",
            "They look away. \"Same.\""
        ));
        scenes.put("S06B", s06b);

        // S07: Beat B (Narration + Continue)
        Scene s07 = new Scene("S07");
        s07.addDialogueLine("(Scene 7 - Beat B) The stranger glances outside and starts fixing their bag. Their stop is coming up.");
        s07.addChoice(new ContinueChoice("Continue...", "S08"));
        s07.setDefaultNextSceneId("S08");
        scenes.put("S07", s07);

        // S08: Farewell - Turn 5 (Sincere / Sarcastic / Blunt) -> game over after choice
        Scene s08 = new Scene("S08");
        s08.addDialogueLine("(Scene 8 - Farewell) The bus slows down. The stranger stands up and shoulders their bag.");
        s08.addDialogueLine("Stranger: \"Dito na ako bababa. Hanggang dito na lang pala tayo.\"");
        s08.addChoice(new SincereChoice(
            "[Sincere] \"Ingat ka. Sana magkita ulit tayo. (Scene 8)\"",
            "ENDING",
            "They pause and smile. \"Salamat sa masayang kwentuhan. Ingat ikaw ;)\"",
            "They nod. \"Salamat. Ingat din.\""
        ));
        s08.addChoice(new SarcasticChoice(
            "[Sarcastic] \"Sayang, bitin pa naman. (Scene 8)\"",
            "ENDING",
            "They laugh as they step away. \"Haha, ikaw talaga. Sige, bahala ka na.\"",
            "They give a dry smile. \"Yeah. Bye.\""
        ));
        s08.addChoice(new BluntChoice(
            "[Blunt] \"Sige, ingat. (Scene 8)\"",
            "ENDING",
            "They give a small wave. \"Sige. Bye.\"",
            "They walk off without looking back."
        ));
        scenes.put("S08", s08);
    }

    // Returns the scene currently being displayed.
    public Scene getCurrentScene() {
        return scenes.get(currentSceneId);
    }

    // Returns the current scene ID.
    public String getCurrentSceneId() {
        return currentSceneId;
    }

    // Advances to a specific scene by ID.
    public void advanceTo(String sceneId) {
        currentSceneId = sceneId;
    }

    // Determines the Turn 4 scene based on current Momentum.
    // Called after any choice in S05 is selected.
    public String getMomentumBranch(Momentum momentum) {
        if (momentum.getValue() >= 0) {
            return "S06A";
        } else {
            return "S06B";
        }
    }

    // Returns true when the encounter is finished (after the S08 choice).
    public boolean isGameOver() {
        return gameOver;
    }

    // Marks the encounter as finished.
    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }

    // Returns the full scene map (for testing or future access).
    public HashMap<String, Scene> getScenes() {
        return scenes;
    }
}
