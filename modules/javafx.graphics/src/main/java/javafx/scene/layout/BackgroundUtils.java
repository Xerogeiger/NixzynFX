package javafx.scene.layout;

import javafx.geometry.Insets;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

/**
 * A quick way to avoid css for creating simple Backgrounds
 */
public class BackgroundUtils {
    public static final Background WHITE_BACKGROUND = simpleSolidBackground(Color.WHITE);
    public static final Background TRANSPARENT_BACKGROUND = simpleSolidBackground(Color.TRANSPARENT);

    /**
     * This method returns a simple full-color {@code Background}.
     *
     * @return the created {@code Background}
     */
    public static Background simpleSolidBackground(Paint p) {
        return new Background(new BackgroundFill(p, CornerRadii.EMPTY, Insets.EMPTY));
    }
}
