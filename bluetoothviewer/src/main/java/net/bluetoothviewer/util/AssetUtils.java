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

    public static List<byte[]> readChunksFromStream(AssetManager assets, String path, int chunkSize) {
        InputStream inputStream;
        try {
            inputStream = assets.open(path);
        } catch (IOException e) {
            Log.e(TAG, "Could not open asset file: " + path);
            return Collections.emptyList();
        }

        List<byte[]> chunks;
        try {
            chunks = IOUtils.readChunksFromStream(inputStream, chunkSize);
        } catch (IOException e) {
            Log.e(TAG, "Could not read chunks from asset file: " + path);
            return Collections.emptyList();
        }

        try {
            inputStream.close();
        } catch (IOException e) {
            Log.w(TAG, "Could not close asset file: " + path);
        }
        return chunks;
    }

    public static String[] listFiles(AssetManager assets, String subdir) {
        try {
            return assets.list(subdir);
        } catch (IOException e) {
            return new String[0];
        }
    }
}
