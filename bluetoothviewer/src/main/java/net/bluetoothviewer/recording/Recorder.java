package net.bluetoothviewer.recording;

public interface Recorder {
    void append(byte[] bytes);

    void clear();

    byte[] getBytes();
}
