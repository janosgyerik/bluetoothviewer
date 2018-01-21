package net.bluetoothviewer.util;

import android.content.res.AssetManager;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

public class AssetUtils {

    private static final String TAG = AssetUtils.class.getSimpleName();

    private AssetUtils() {
        // utility class, forbidden constructor
    }

    public static List<String> readLinesFromStream(AssetManager assets, String filename) {
        InputStream inputStream;
        try {
            inputStream = assets.open(filename);
        } catch (IOException e) {
            Log.e(TAG, "Could not open asset file: " + filename);
            return Collections.emptyList();
        }

        List<String> lines;
        try {
            lines = IOUtils.readLinesFromStream(inputStream);
        } catch (IOException e) {
            Log.e(TAG, "Could not read lines from asset file: " + filename);
            return Collections.emptyList();
        }

        try {
            inputStream.close();
        } catch (IOException e) {
            Log.w(TAG, "Could not close asset file: " + filename);
        }
        return lines;
    }

    public static String[] listFiles(AssetManager assets, String subdir) {
        try {
            return assets.list(subdir);
        } catch (IOException e) {
            return new String[0];
        }
    }
}
