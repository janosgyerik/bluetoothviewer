package net.bluetoothviewer.util;

import android.app.Application;

import net.bluetoothviewer.application.BluetoothViewerApplication;

public class ApplicationUtils {
    private ApplicationUtils() {
        // utility class, forbidden constructor
    }

    public static boolean isLiteVersion(Application application) {
        return ((BluetoothViewerApplication) application).isLiteVersion();
    }
}
