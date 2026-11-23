package com.sabaytayo;

import java.util.HashMap;

// Assembles and manages the 12-scene dialogue graph for the bus encounter.
// Uses Option A (micro-beats inside scenes) and a Two-Lane Warm/Cold split & merge.
public class Encounter {

    // The scene graph: maps scene ID -> Scene object
    private final HashMap<String, Scene> scenes;

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

        // S01: The Empty Seat (Opening Scene - Non-verbal commuter realism)
        Scene s01 = new Scene("S01");
        s01.addDialogueLine("It was a rainy afternoon. The bus was almost full, and most passengers were looking at their phones or quietly resting. You noticed an empty seat beside a stranger by the window wearing earphones.");
        s01.addDialogueLine("You: \"Excuse me, may nakaupo ba rito?\"");
        s01.addDialogueLine("{S_they} glanced briefly at the empty space beside {s_them}, gently shifted {s_their} bag onto {s_their} lap, and gave a small nod. You settled in as the bus rolled into the afternoon traffic.");
        s01.addChoice(new ContinueChoice("Continue...", "S02"));
        s01.setDefaultNextSceneId("S02");
        scenes.put("S01", s01);

        // S02: The Awkward Silence - Turn 1 (Player decides whether to initiate or stay quiet)
        Scene s02 = new Scene("S02");
        s02.addDialogueLine("A few minutes passed as traffic built up. {S_they} adjusted one earphone and looked outside, not particularly interested in starting a conversation.");
        s02.addDialogueLine("You wondered whether you should say something or simply enjoy the quiet ride.");
        s02.addChoice(new SincereChoice(
            "[Sincere] \"Ang traffic ngayon, no? Mukhang matatagalan pa tayo rito.\"",
            "S03",
            "\"Oo nga eh.\" {S_they} glanced briefly at you before looking outside again. \"Sana nga bumilis na. Medyo mahaba pa yata ang biyahe.\"",
            "\"Oo nga eh.\" {S_they} gave a brief, polite glance before returning {s_their} attention to the window."
        ));
        s02.addChoice(new SarcasticChoice(
            "[Sarcastic] \"Parang mas mabilis pa yata kung nilakad na lang natin 'to.\"",
            "S03",
            "(Napatingin sa iyo and smiled slightly.) \"Haha, baka nga. Ang bagal talaga ng usad.\"",
            "(Napatingin sa iyo and smiled awkwardly.) \"Okaayy??\" {S_they} gave a brief look before turning back to the window."
        ));
        s02.addChoice(new BluntChoice(
            "[Blunt] \"Mukhang hindi tayo makakarating agad.\"",
            "S03",
            "{S_they} nodded slightly, seemingly agreeing with your observation, before looking back down at {s_their} phone.",
            "\"...\" ({S_they} glanced at you briefly, then looked right back down at {s_their} phone.)"
        ));
        s02.addChoice(new ContinueChoice(
            "[Quiet] Stay silent and look outside.",
            "S03",
            "You decided not to say anything. Instead, you looked outside as the sound of the bus engine and the rain against the windows filled the silence."
        ));
        scenes.put("S02", s02);

        // S03: The Sudden Brake - Turn 2 (Branches to post-brake beats S04A, S04B, S04C)
        Scene s03 = new Scene("S03");
        s03.addDialogueLine("A vehicle suddenly stopped in front of the bus. The driver slammed on the brakes! {S_they} lurched forward and lightly hit {s_their} head on the seat in front.");
        s03.addDialogueLine("Stranger: \"Aray! Grabe naman 'yon!\"");
        s03.addDialogueLine("{S_they} immediately held {s_their} head and frowned. You noticed what happened and wondered how you should react.");
        s03.addChoice(new SincereChoice(
            "[Sincere] \"Okay ka lang ba? Nasaktan ka ba? Baka nahihilo ka?\"",
            "S04A",
            "\"Oo, okay lang naman. Hindi naman masyadong masakit. Salamat.\" {S_they} gave you a small nod before adjusting {s_their} position.",
            "\"Oo, okay lang naman. Salamat.\" {S_they} gave a small nod and adjusted {s_their} seat."
        ));
        s03.addChoice(new SarcasticChoice(
            "[Sarcastic] \"Araymo! Si kuya driver talaga, may pa-headbang experience pa.\"",
            "S04B",
            "(Napatingin sa iyo at bahagyang natawa.) \"Grabe ka naman! Pero ang lakas nga ng preno.\"",
            "(Napatingin sa iyo.) \"Hala, grabe ka naman. Masakit kaya.\" {S_they} did not seem particularly amused."
        ));
        s03.addChoice(new BluntChoice(
            "[Blunt] \"Mag-ingat ka. Baka mauntog ka ulit kapag pumreno.\"",
            "S04C",
            "\"Oo nga. Hindi ko naman kasi inasahan.\" {S_they} adjusted {s_their} seat and held onto the side of it.",
            "\"Oo nga. Hindi ko naman kasi inasahan.\" {S_they} adjusted {s_their} seat and kept a quiet distance."
        ));
        s03.addChoice(new ContinueChoice(
            "[Quiet] Say nothing and let {s_them} recover.",
            "S04C",
            "You noticed that {s_they} had hit {s_their} head, but you decided not to say anything. {S_they} slowly adjusted {s_them}self and looked outside again."
        ));
        scenes.put("S03", s03);

        // S04A: After the Incident (Warm Sincere Beat -> triggers Momentum split S04_DONE)
        Scene s04a = new Scene("S04A");
        s04a.addDialogueLine("The bus continued moving. {S_they} now held onto the seat in front whenever the bus slowed down. A few moments later, {s_they} glanced briefly in your direction.");
        s04a.addDialogueLine("Stranger: \"Buti na lang hindi naman malakas. Thank you ulit.\"");
        s04a.addChoice(new ContinueChoice("Continue...", "S04_DONE"));
        s04a.setDefaultNextSceneId("S04_DONE");
        scenes.put("S04A", s04a);

        // S04B: After the Incident (Sarcastic Beat -> triggers Momentum split S04_DONE)
        Scene s04b = new Scene("S04B");
        s04b.addDialogueLine("The bus continued moving. After the sudden incident, the atmosphere settled again as {s_they} held onto the seat in front.");
        s04b.addDialogueLine("Stranger: \"Sana next time hindi na gano'n kalakas 'yung preno.\"");
        s04b.addChoice(new ContinueChoice("Continue...", "S04_DONE"));
        s04b.setDefaultNextSceneId("S04_DONE");
        scenes.put("S04B", s04b);

        // S04C: After the Incident (Blunt / Quiet Beat -> triggers Momentum split S04_DONE)
        Scene s04c = new Scene("S04C");
        s04c.addDialogueLine("The bus continued moving in quiet tension. {S_they} held onto the seat in front whenever the bus slowed down and returned {s_their} attention to the rainy window.");
        s04c.addChoice(new ContinueChoice("Continue...", "S04_DONE"));
        s04c.setDefaultNextSceneId("S04_DONE");
        scenes.put("S04C", s04c);

        // S05A: WARM LANE PART 1 (Lianne Sc 6 + Sc 7: Proper Introduction & Common Ground - Turn 3A)
        Scene s05a = new Scene("S05A");
        s05a.addDialogueLine("A few minutes later, traffic started to ease. {S_they} put {s_their} phone down, no longer as occupied as before. You took the chance to introduce yourself.");
        s05a.addDialogueLine("You: \"By the way, I don't think we've introduced ourselves yet. I'm {player}.\"");
        s05a.addDialogueLine("Stranger: \"Oh. I'm {seatmate}. Nice to meet you.\"");
        s05a.addDialogueLine("{S_they} gave a small, polite smile before checking the road signs outside. You wondered what to ask next.");
        s05a.addChoice(new SincereChoice(
            "[Sincere] \"Madalas ka bang sumakay sa bus na 'to?\"",
            "S06A",
            "\"Oo, minsan. Usually kapag may kailangan akong puntahan. Medyo familiar na rin ako sa route.\"",
            "\"Oo, minsan kapag may kailangan akong puntahan.\""
        ));
        s05a.addChoice(new SarcasticChoice(
            "[Sarcastic] \"Mukhang kabisado mo na yata lahat ng lubak dito.\"",
            "S06A",
            "(Bahagyang napangiti.) \"Hindi naman lahat. Pero may mga part talaga na ramdam mo—lalo na kapag biglang preno.\"",
            "\"Hindi naman. Sanay lang sa daan.\""
        ));
        s05a.addChoice(new BluntChoice(
            "[Blunt] \"Malayo pa ba destination mo?\"",
            "S06A",
            "\"Medyo malayo pa. Ikaw, ilang stops ka pa?\"",
            "\"Medyo malayo pa.\""
        ));
        s05a.addChoice(new ContinueChoice(
            "[Quiet] Let the conversation rest and watch the streets pass.",
            "S06A",
            "You decided to let the conversation rest. You looked outside as the bus passed through familiar streets while {s_they} returned to {s_their} phone."
        ));
        s05a.addChoice(new FlirtyChoice(
            "[Flirty] \"At least hindi naman boring ang biyahe kapag may katabi kang gaya mo.\"",
            "S06A",
            "(Bahagyang nagulat.) \"Ah... wow.\" {S_they} looked away, a small smile appearing before turning back to the window.",
            "(Bahagyang nagulat.) \"Ah... okay.\" {S_they} looked away quietly."
        ));
        scenes.put("S05A", s05a);

        // S05B: COLD LANE PART 1 (Tone-Consistent Cold Sc 6 + Sc 7: Unspoken Distance - Turn 3B)
        Scene s05b = new Scene("S05B");
        s05b.addDialogueLine("A few minutes later, traffic started to ease, but the quiet wall between you remained. Neither of you brought up names.");
        s05b.addDialogueLine("{S_they} kept {s_their} earphones in, occasionally glancing at the road signs outside. You wondered whether to break the ice or let the silence stay.");
        s05b.addChoice(new SincereChoice(
            "[Sincere] \"Madalas ka bang sumakay sa bus na 'to? Pasensya na pala kanina kung medyo awkward.\"",
            "S06B",
            "\"Okay lang. Oo, minsan kapag may kailangan akong puntahan.\"",
            "\"Ah, okay lang. Minsan lang kapag kailangan.\""
        ));
        s05b.addChoice(new SarcasticChoice(
            "[Sarcastic] \"Mukhang kabisado mo na yata lahat ng lubak dito.\"",
            "S06B",
            "(Bahagyang napangiti.) \"Hindi naman lahat. Pero ramdam mo talaga kapag biglang preno.\"",
            "\"Hindi naman.\" {S_they} glanced away without much reaction."
        ));
        s05b.addChoice(new BluntChoice(
            "[Blunt] \"Malayo pa ba destination mo?\"",
            "S06B",
            "\"Medyo malayo pa. Ikaw?\"",
            "\"Medyo malayo pa.\" {S_they} turned right back toward the window."
        ));
        s05b.addChoice(new ContinueChoice(
            "[Quiet] Say nothing and let the quiet ride continue.",
            "S06B",
            "You decided not to force small talk. You looked outside at the familiar streets while {s_they} stayed focused on {s_their} phone."
        ));
        scenes.put("S05B", s05b);

        // S06A: WARM LANE PART 2 (Lianne Sc 8 + Sc 9A: A Warmer Conversation - Turn 4A -> Merges to S07)
        Scene s06a = new Scene("S06A");
        s06a.addDialogueLine("The bus moved along as a few passengers got off. {S_they} seemed more familiar with your presence now and removed one earphone before glancing at you.");
        s06a.addDialogueLine("Stranger: \"Ikaw ba, madalas ka rin sumakay dito?\"");
        s06a.addChoice(new SincereChoice(
            "[Sincere] \"Oo, minsan. Mas convenient din kasi kaysa maghanap pa ng ibang masasakyan.\"",
            "S07",
            "\"Ah, oo nga. Mas madali rin kapag familiar ka na sa route.\"",
            "\"Ah, totoo naman.\""
        ));
        s06a.addChoice(new SarcasticChoice(
            "[Sarcastic] \"Oo, parang regular customer na nga ako rito. Kulang na lang may loyalty card.\"",
            "S07",
            "(Bahagyang natawa.) \"Baka next time may free ride ka na.\"",
            "\"Haha, siguro nga.\""
        ));
        s06a.addChoice(new BluntChoice(
            "[Blunt] \"Oo. Mas convenient kasi.\"",
            "S07",
            "\"Ah, okay. Fair enough.\"",
            "\"Ah, okay.\""
        ));
        s06a.addChoice(new ContinueChoice(
            "[Quiet] Simply nod and look outside.",
            "S07",
            "You simply nodded and looked outside. {S_they} did not force the conversation to continue and returned {s_their} attention to the road."
        ));
        s06a.addChoice(new FlirtyChoice(
            "[Flirty] \"Oo, pero mas naging interesting naman 'yung biyahe ngayon.\"",
            "S07",
            "(Napatingin sa iyo.) \"Ah... ganun ba?\" {S_they} gave a small smile before looking away.",
            "(Napatingin sa iyo.) \"Ah... okay.\" {S_they} glanced away."
        ));
        scenes.put("S06A", s06a);

        // S06B: COLD LANE PART 2 (Lianne Sc 8 + Sc 9B: A Still-Awkward Conversation - Turn 4B -> Merges to S07)
        Scene s06b = new Scene("S06B");
        s06b.addDialogueLine("The bus continued moving, but the conversation had become less frequent. {S_they} looked outside, occasionally checking {s_their} phone.");
        s06b.addDialogueLine("You: \"Okay ka lang? Hindi ba ako nakakaistorbo?\"");
        s06b.addDialogueLine("Stranger: \"Ah, okay lang naman. Sanay lang talaga akong tahimik kapag nasa biyahe.\"");
        s06b.addChoice(new SincereChoice(
            "[Sincere] \"Gets ko naman. Okay lang din sa akin kahit tahimik lang.\"",
            "S07",
            "\"Thank you. Mas gusto ko rin minsan 'yung tahimik lang.\"",
            "\"Thank you. Mas gusto ko rin minsan 'yung tahimik lang.\""
        ));
        s06b.addChoice(new SarcasticChoice(
            "[Sarcastic] \"Akala ko naman bawal na akong magsalita.\"",
            "S07",
            "\"Hindi naman. Hindi lang talaga ako palasalita.\"",
            "\"Hindi naman. Hindi lang talaga ako palasalita.\" {S_they} answered without much expression and looked outside again."
        ));
        s06b.addChoice(new BluntChoice(
            "[Blunt] \"Ah, okay. Hindi na lang ako magsasalita.\"",
            "S07",
            "\"Ah... okay.\" {S_they} gave a small nod.",
            "\"Ah... okay.\" {S_they} gave a small nod. The conversation stopped there."
        ));
        s06b.addChoice(new ContinueChoice(
            "[Quiet] Respect the silence and watch the passing scenery.",
            "S07",
            "You decided to respect the silence and focused on the passing scenery. {S_they} seemed comfortable continuing the ride without further conversation."
        ));
        scenes.put("S06B", s06b);

        // S07: MERGE - Almost There (Lianne Sc 10 - Turn 5)
        Scene s07 = new Scene("S07");
        s07.addDialogueLine("Time passed, and the bus was getting closer to your destination. Several passengers began standing up to prepare for their stops. {S_they} checked {s_their} phone and looked outside.");
        s07.addDialogueLine("You realized you might not have another opportunity to speak before going your separate ways.");
        s07.addChoice(new SincereChoice(
            "[Sincere] \"Mukhang malapit ka na rin sa bababaan mo. Ingat ka pauwi.\"",
            "S08",
            "\"Ikaw rin. Ingat.\"",
            "\"Ikaw rin. Ingat.\""
        ));
        s07.addChoice(new SarcasticChoice(
            "[Sarcastic] \"Mukhang dito na matatapos ang bus adventure natin.\"",
            "S08",
            "(Bahagyang napangiti.) \"Oo nga. Buti naman nakarating din.\"",
            "\"Oo nga. Buti nakarating din.\""
        ));
        s07.addChoice(new BluntChoice(
            "[Blunt] \"Dito na yata ako bababa. Mauna na ako.\"",
            "S08",
            "\"Ah, sige. Ingat.\"",
            "\"Ah, sige. Ingat.\""
        ));
        s07.addChoice(new ContinueChoice(
            "[Quiet] Say nothing and prepare your things.",
            "S08",
            "You decided not to say anything else. You simply prepared your things and waited for your stop."
        ));
        s07.addChoice(new FlirtyChoice(
            "[Flirty] \"Sayang naman. Parang gusto ko pa sanang makilala ka.\"",
            "S08",
            "(Napatingin sa iyo.) \"Ohh...\" {S_they} looked away for a moment, seemingly thinking about what to say next.",
            "(Napatingin sa iyo.) \"Ah... sige.\" {S_they} looked away quietly."
        ));
        scenes.put("S07", s07);

        // S08: Before the Ride Ends (Lianne Sc 11 + Sc 12 transition - Final Interaction -> ENDING)
        Scene s08 = new Scene("S08");
        s08.addDialogueLine("The bus finally approached your stop. You stood up and prepared to leave. Before stepping away, you looked at {s_them} one last time.");
        s08.addDialogueLine("You wondered what to say before stepping off into the cool air after the rain.");
        s08.addChoice(new SincereChoice(
            "[Sincere] \"It was nice meeting you. Ingat ka lagi.\"",
            "ENDING",
            "\"Nice meeting you, too. Ingat!\" You gave {s_them} a small nod before stepping off the bus.",
            "\"Ingat din.\" You gave {s_them} a small nod before stepping off the bus."
        ));
        s08.addChoice(new SarcasticChoice(
            "[Sarcastic] \"Baka next time magkita ulit tayo rito. Sana hindi na traffic.\"",
            "ENDING",
            "(Bahagyang natawa.) \"Sana nga. Ingat!\" You smiled and headed toward the bus exit.",
            "\"Sana nga. Ingat.\" You made your way toward the bus exit."
        ));
        s08.addChoice(new BluntChoice(
            "[Blunt] \"Sige, mauna na ako. Ingat.\"",
            "ENDING",
            "\"Sige. Ingat din.\" You stepped away from the seat and headed toward the exit.",
            "\"Sige.\" You stepped away from the seat and made your way toward the exit."
        ));
        s08.addChoice(new ContinueChoice(
            "[Quiet] Give a silent nod and head for the exit.",
            "ENDING",
            "You stepped off the bus without another word and felt the cool air after the rain as the bus slowly moved away."
        ));
        s08.addChoice(new FlirtyChoice(
            "[Flirty] \"I was hoping we could talk again sometime. Would it be okay if I asked for your number?\"",
            "ENDING",
            "(Briefly surprised, then smiled softly.) \"Ah... sure. Okay lang naman. Maybe we can see.\"",
            "(Briefly surprised.) \"Ah... maybe next time na lang. Ingat.\""
        ));
        scenes.put("S08", s08);
    }

    // Returns the scene currently being displayed.
    public Scene getCurrentScene() {
        return scenes.get(currentSceneId);
    }

    // Looks up a scene by ID, throwing SceneNotFoundException if missing.
    public Scene getScene(String sceneId) throws SceneNotFoundException {
        if (sceneId == null || !scenes.containsKey(sceneId)) {
            throw new SceneNotFoundException(sceneId);
        }
        return scenes.get(sceneId);
    }

    // Returns the current scene ID.
    public String getCurrentSceneId() {
        return currentSceneId;
    }

    // Advances to a specific scene by ID, validating that the scene exists.
    public void advanceTo(String sceneId) throws SceneNotFoundException {
        if (sceneId == null || !scenes.containsKey(sceneId)) {
            throw new SceneNotFoundException(sceneId);
        }
        currentSceneId = sceneId;
    }

    // Determines the Two-Lane mid-ride branch based on current Momentum.
    // Called after the post-brake beat (S04A / S04B / S04C) finishes.
    public String getMomentumBranch(Momentum momentum) {
        if (momentum.getValue() >= 0) {
            return "S05A";
        } else {
            return "S05B";
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
