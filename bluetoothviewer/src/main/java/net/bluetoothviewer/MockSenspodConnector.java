package net.bluetoothviewer;

import java.util.List;

public class MockSenspodConnector implements DeviceConnector {
    public MockSenspodConnector(List<String> filename) {

    }

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
