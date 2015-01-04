package net.bluetoothviewer;

public class MockSenspodConnector implements DeviceConnector {
    @Override
    public void connect() {
        // do nothing
    }

    @Override
    public void disconnect() {
        // do nothing
    }

    @Override
    public void sendAsciiMessage(CharSequence chars) {
        // do nothing
    }
}
