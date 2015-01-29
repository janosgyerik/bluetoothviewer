package net.bluetoothviewer;

import java.util.List;

public class MockSenspodConnector implements DeviceConnector {

    private static final int SLEEP_MILLIS = 1000;

    private final MessageHandler messageHandler;
    private final List<String> lines;

    private volatile boolean running = false;

    public MockSenspodConnector(MessageHandler messageHandler, List<String> lines) {
        this.messageHandler = messageHandler;
        this.lines = lines;
    }

    @Override
    public synchronized void connect() {
        if (running) {
            return;
        }
        running = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    for (String line : lines) {
                        if (!running) {
                            return;
                        }
                        messageHandler.sendLineRead(line);
                        try {
                            Thread.sleep(SLEEP_MILLIS);
                        } catch (InterruptedException e) {
                            // ok to be interrupted
                        }
                    }
                }
            }
        }).start();
    }

    @Override
    public synchronized void disconnect() {
        running = false;
    }

    @Override
    public void sendAsciiMessage(CharSequence chars) {
        // do nothing
    }
}
