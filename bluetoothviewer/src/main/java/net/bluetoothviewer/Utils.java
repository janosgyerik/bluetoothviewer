package net.bluetoothviewer;

import android.app.Activity;
import android.content.Intent;


public class Utils
{
    public final static int THEME_WHITE = 0;
    public final static int THEME_BLACK = 1;

    private static int sTheme = Utils.THEME_WHITE;

    private static int numThemes = 2; // Hard coded value determined from above momentarily

    /**
     * Set the theme of the Activity, and restart it by creating a new Activity of the same type.
     */
    public static void changeToTheme(Activity activity, int theme)
    {
        sTheme = theme;
        activity.finish();

        activity.startActivity(new Intent(activity, activity.getClass()));

    }

    /** Set the theme of the activity, according to the configuration. */
    public static void onActivityCreateSetTheme(Activity activity)
    {
        switch (sTheme)
        {
            case THEME_WHITE:
                activity.setTheme(R.style.LightTheme);
                break;
            case THEME_BLACK:
                activity.setTheme(R.style.DarkTheme);
                break;
        }
    }

    public static void toggleThemes(Activity activity){
        sTheme = (sTheme+1) % numThemes;
        changeToTheme(activity, sTheme);

    }
}
