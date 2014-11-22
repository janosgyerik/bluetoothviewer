package net.bluetoothviewer;

import android.app.Activity;
import android.content.Intent;

public class ThemeUtils {

     //TODO: Use Shared Preferences to store the current theme of the app
    private static Theme currentTheme = Theme.BLACK;

    private static enum Theme {
        BLACK, WHITE;

        public Theme nextTheme() {
            return values()[(ordinal() + 1) % values().length];
        }
    }

    public static void updateTheme(Activity activity) {
        switch (currentTheme) {
            case BLACK:
                activity.setTheme(R.style.LightTheme);
                break;
            case WHITE:
                activity.setTheme(R.style.DarkTheme);
                break;
        }
    }

    /**
     * Set the theme of the Activity, and restart it by creating a new Activity of the same type.
     */
    public static void cycleThemes(Activity activity) {
        currentTheme = currentTheme.nextTheme();
        restartActivity(activity);
    }

    private static void restartActivity(Activity activity) {
        activity.finish();
        activity.startActivity(new Intent(activity, activity.getClass()));
    }
}
