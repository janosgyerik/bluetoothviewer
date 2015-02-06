package net.bluetoothviewer;

import android.content.res.AssetManager;

import net.bluetoothviewer.util.AssetUtils;

import java.util.List;

public class MockSenspodConnector implements DeviceConnector {

    public static final String SUBDIR = "samples/senspod";

    private static final int SLEEP_MILLIS = 1000;

    private final String filename;
    private final AssetManager assets;
    private final MessageHandler messageHandler;

    private volatile boolean running = false;

    public MockSenspodConnector(String filename, AssetManager assets, MessageHandler messageHandler) {
        this.filename = filename;
        this.assets = assets;
        this.messageHandler = messageHandler;
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
                messageHandler.sendConnectingTo(filename);

                String mockFilePath = SUBDIR + "/" + filename;
                List<String> lines = AssetUtils.readLinesFromStream(assets, mockFilePath);

                if (!lines.isEmpty()) {
                    loopLinesUntilStopped(lines);
                }

                messageHandler.sendConnectionLost();
            }

            private void loopLinesUntilStopped(List<String> lines) {
                messageHandler.sendConnectedTo(filename);

                while (running) {
                    for (String line : lines) {
                        if (!running) {
                            break;
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
