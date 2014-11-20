package net.bluetoothviewer;

import android.app.Activity;
import android.content.Intent;

public class ThemeUtils {
    public final static int THEME_WHITE = 0;
    public final static int THEME_BLACK = 1;

    private static Themes mTheme = Themes.BLACK;

    private enum Themes {
        BLACK, WHITE;
    }

    /** Set the theme of the activity, according to the configuration. */
    public static void onActivityCreateSetTheme(Activity activity) {
        switch (mTheme) {
            case BLACK:
                activity.setTheme(R.style.LightTheme);
                break;
            case WHITE:
                activity.setTheme(R.style.DarkTheme);
                break;
        }
    }

    public static void cycleThemes(Activity activity) {
        mTheme = Themes.values()[(mTheme.ordinal()+1)%Themes.values().length];
        changeToTheme(activity, mTheme);
    }

    /**
     * Set the theme of the Activity, and restart it by creating a new Activity of the same type.
     */
    private static void changeToTheme(Activity activity, Themes theme) {
        mTheme = theme;
        activity.finish(); // Finish activity in order to recreate activity with new theme
        activity.startActivity(new Intent(activity, activity.getClass()));
    }
}