package com.sabaytayo;

import java.util.HashMap;
import java.util.Map;
import javafx.scene.image.Image;

// Holds every expression sprite for the player (MC), the stranger (FEM-C), and the conductor cameo.
public class SpriteSet {

    private final Map<String, Image> maleSprites = new HashMap<>();
    private final Map<String, Image> femSprites = new HashMap<>();
    private final Image conductor;

    public SpriteSet() {
        conductor = ImageLoader.load("/assets/CONDUCTOR/cameo_conductor.png");

        addMale("neutral", "male_neutral");
        addMale("sincere", "male_sincere");
        addMale("concerned", "male_concerned");
        addMale("smirk", "male_smirk");
        addMale("awkward", "male_awkward");
        addMale("deadpan", "male_deadpan");
        addMale("flustered", "male_flustered");
        addMale("laughing", "male_laughing");
        addMale("relieved_smile", "male_relieved_smile");
        addMale("surprised", "male_surprised");

        addFem("neutral", "fem_neutral");
        addFem("shy", "fem_shy_apologetic");
        addFem("neutral_talking", "fem_neutral_talking");
        addFem("warm_smile_1", "fem_warm_smile1");
        addFem("warm_smile_2", "fem_warm_smile2");
        addFem("flustered", "fem_flustered");
        addFem("flustered_laugh", "fem_flustered_laugh");
        addFem("laughing", "fem_laughing");
        addFem("annoyed", "fem_annoyed");
        addFem("surprised", "fem_surprised");
        addFem("wince", "fem_wince");
        addFem("wincing_talking", "fem_wincing_talking");
        addFem("phone_looking", "fem_phone_looking");
    }

    private void addMale(String mood, String fileName) {
        maleSprites.put(mood, ImageLoader.load("/assets/MC/" + fileName + ".png"));
    }

    private void addFem(String mood, String fileName) {
        femSprites.put(mood, ImageLoader.load("/assets/FEM-C/" + fileName + ".png"));
    }

    public Image getMale(String mood) {
        return maleSprites.get(mood);
    }

    public Image getFem(String mood) {
        return femSprites.get(mood);
    }

    public Image getConductor() {
        return conductor;
    }
}
