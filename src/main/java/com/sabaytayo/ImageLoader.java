package com.sabaytayo;

import java.io.InputStream;
import javafx.scene.image.Image;

// Shared helper for loading images from the resources folder.
// Returns null if the file is missing so the views can use their fallbacks.
public class ImageLoader {

    public static Image load(String resourcePath) {
        if (resourcePath == null) {
            return null;
        }
        try (InputStream stream = ImageLoader.class.getResourceAsStream(resourcePath)) {
            if (stream != null) {
                return new Image(stream);
            }
        } catch (Exception e) {
            System.err.println("Notice: Could not load image " + resourcePath);
        }
        return null;
    }
}
