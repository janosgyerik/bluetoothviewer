package net.bluetoothviewer.readers;

import net.bluetoothviewer.util.TextUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class BinaryReader implements DeviceReader {

    private static final int CHUNK_SIZE = 32;

    private InputStream inputStream;

    @Override
    public void init(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    public byte[] readValue() throws IOException {
        byte[] buffer = new byte[CHUNK_SIZE];
        int bytesRead = inputStream.read(buffer, 0, buffer.length);
        if (bytesRead == -1) {
            return null;
        }
        return Arrays.copyOf(buffer, bytesRead);
    }

    @Override
    public String valueAsString(byte[] bytes) {
        return TextUtils.bytesToHexDump(bytes);
    }
}
