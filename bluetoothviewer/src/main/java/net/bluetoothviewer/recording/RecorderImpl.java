package net.bluetoothviewer.recording;

import java.util.ArrayList;
import java.util.List;

public class RecorderImpl implements Recorder {

    private final List<byte[]> bytesList = new ArrayList<byte[]>();

    @Override
    public void append(byte[] bytes) {
        bytesList.add(bytes);
    }

    @Override
    public void clear() {
        bytesList.clear();
    }

    @Override
    public byte[] getBytes() {
        byte[] bytes = new byte[count()];
        int pos = 0;
        for (byte[] chunk : bytesList) {
            System.arraycopy(chunk, 0, bytes, pos, chunk.length);
            pos += chunk.length;
        }
        return bytes;
    }

    private int count() {
        int total = 0;
        for (byte[] bytes : bytesList) {
            total += bytes.length;
        }
        return total;
    }
}
