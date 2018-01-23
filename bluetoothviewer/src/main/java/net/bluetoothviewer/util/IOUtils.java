package net.bluetoothviewer.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IOUtils {

    private IOUtils() {
        // utility class, forbidden constructor
    }

    public static List<String> readLinesFromStream(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        List<String> lines = new ArrayList<String>();
        while ((line = reader.readLine()) != null) {
            lines.add(line);
        }
        return lines;
    }

    public static List<byte[]> readChunksFromStream(InputStream inputStream, int chunkSize) throws IOException {
        int bytesRead;
        byte[] data = new byte[chunkSize];

        List<byte[]> chunks = new ArrayList<byte[]>();
        while ((bytesRead = inputStream.read(data, 0, data.length)) != -1) {
            chunks.add(Arrays.copyOf(data, bytesRead));
        }
        return chunks;
    }
}
