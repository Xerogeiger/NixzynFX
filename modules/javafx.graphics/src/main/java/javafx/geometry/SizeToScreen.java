package javafx.geometry;

import javafx.stage.Screen;

public class SizeToScreen {
    private static final Rectangle2D SCREEN_BOUNDS = Screen.getPrimary().getBounds();
    private static final Rectangle2D SCREEN_BOUNDS_PERCENT = SCREEN_BOUNDS.scale(0.01);

    public static double percentOfWidth(double percent) {
        return SCREEN_BOUNDS_PERCENT.getWidth() * percent;
    }

    public static double percentOfHeight(double percent) {
        return SCREEN_BOUNDS_PERCENT.getHeight() * percent;
    }

    public static double getWidth() {
        return SCREEN_BOUNDS.getWidth();
    }

    public static double getHeight() {
        return SCREEN_BOUNDS.getHeight();
    }
}
