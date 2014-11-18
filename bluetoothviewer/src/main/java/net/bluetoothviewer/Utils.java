package net.bluetoothviewer;

import android.app.Activity;
import android.content.Intent;


public class Utils
{
    public final static int THEME_WHITE = 1;
    public final static int THEME_BLACK = 2;

    public static int sTheme = Utils.THEME_WHITE;

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
}
