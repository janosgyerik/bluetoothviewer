package net.bluetoothviewer.readers;

import java.io.IOException;
import java.io.InputStream;

public interface DeviceReader {

    void init(InputStream inputStream);

    byte[] readValue() throws IOException;

    String valueAsString(byte[] bytes);
}
