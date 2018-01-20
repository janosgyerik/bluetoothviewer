package net.bluetoothviewer;

import android.content.res.AssetManager;

import net.bluetoothviewer.util.AssetUtils;

import java.io.File;
import java.util.List;

public class MockLineByLineConnector implements DeviceConnector {

    public static final String SAMPLES_SUBDIR = "samples/line-by-line";

    private static final int SLEEP_MILLIS = 1000;

    private final MessageHandler messageHandler;
    private final AssetManager assets;
    private final String filename;

    private boolean running = false;

    public MockLineByLineConnector(MessageHandler messageHandler, AssetManager assets, String filename) {
        this.messageHandler = messageHandler;
        this.assets = assets;
        this.filename = filename;
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

                String mockFilePath = new File(SAMPLES_SUBDIR, filename).toString();
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
                            Thread.currentThread().interrupt();
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
