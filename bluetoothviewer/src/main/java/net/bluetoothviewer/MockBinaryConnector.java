package net.bluetoothviewer;

import android.content.res.AssetManager;

import net.bluetoothviewer.util.AssetUtils;
import net.bluetoothviewer.util.TextUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MockBinaryConnector implements DeviceConnector {

    private static final int CHUNK_SIZE = 16;

    public static final String SAMPLES_SUBDIR = "samples/binary";

    private static final int SLEEP_MILLIS = 1000;

    private final MessageHandler messageHandler;
    private final AssetManager assets;
    private final String filename;

    private boolean running = false;

    public MockBinaryConnector(MessageHandler messageHandler, AssetManager assets, String filename) {
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
        new Thread(new MockDeviceRunnable()).start();
    }

    @Override
    public synchronized void disconnect() {
        running = false;
    }

    @Override
    public void sendAsciiMessage(CharSequence chars) {
        // do nothing
    }

    class MockDeviceRunnable implements Runnable {
        @Override
        public void run() {
            messageHandler.sendConnectingTo(filename);

            String mockFilePath = new File(SAMPLES_SUBDIR, filename).toString();
            List<String> chunks = new ArrayList<String>();
            for (byte[] chunk : AssetUtils.readChunksFromStream(assets, mockFilePath, CHUNK_SIZE)) {
                chunks.add(TextUtils.bytesToHexDump(chunk));
            }

            if (!chunks.isEmpty()) {
                loopChunksUntilStopped(chunks);
            }

            messageHandler.sendConnectionLost();
        }

        private void loopChunksUntilStopped(List<String> chunks) {
            messageHandler.sendConnectedTo(filename);

            while (running) {
                for (String chunk : chunks) {
                    if (!running) {
                        break;
                    }
                    messageHandler.sendLineRead(chunk);
                    try {
                        Thread.sleep(SLEEP_MILLIS);
                    } catch (InterruptedException e) {
                        // ok to be interrupted
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
    }
}
