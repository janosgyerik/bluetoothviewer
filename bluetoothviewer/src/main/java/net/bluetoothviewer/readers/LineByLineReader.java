package net.bluetoothviewer.readers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class LineByLineReader implements DeviceReader {

    private BufferedReader reader;

    @Override
    public void init(InputStream inputStream) {
        reader = new BufferedReader(new InputStreamReader(inputStream));
    }

    @Override
    public byte[] readValue() throws IOException {
        String line = reader.readLine();
        if (line == null) {
            return null;
        }
        return line.getBytes();
    }

    @Override
    public String valueAsString(byte[] bytes) {
        return new String(bytes);
    }
}
