package net.bluetoothviewer.util;

import android.content.res.AssetManager;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AssetUtils {

    private static final String TAG = AssetUtils.class.getSimpleName();

    private AssetUtils() {
        // utility class, forbidden constructor
    }

    public static List<String> readLinesFromStream(AssetManager assets, String filename) {
        try {
            InputStream inputStream = assets.open(filename);
            return readLinesFromStream(inputStream);
        } catch (IOException e) {
            Log.e(TAG, "Could not open asset file: " + filename);
            return Collections.emptyList();
        }
    }

    private static List<String> readLinesFromStream(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        List<String> lines = new ArrayList<String>();
        while ((line = reader.readLine()) != null) {
            lines.add(line);
        }
        return lines;
    }
}
