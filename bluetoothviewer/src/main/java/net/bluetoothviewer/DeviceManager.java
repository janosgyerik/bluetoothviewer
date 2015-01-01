package net.bluetoothviewer;

public interface DeviceManager {

    void shutdown();

    void sendAsciiMessage(CharSequence chars);
}
