package net.bluetoothviewer;

import android.content.res.AssetManager;
import android.util.Log;

import net.bluetoothviewer.readers.DeviceReader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class MockDeviceConnector implements DeviceConnector {

    private static final String TAG = MockDeviceConnector.class.getSimpleName();

    public static final String SAMPLES_SUBDIR = "samples";

    private static final int SLEEP_MILLIS = 1000;

    private final MessageHandler messageHandler;
    private final AssetManager assets;
    private final String filename;
    private final DeviceReader reader;

    private boolean running = false;

    public MockDeviceConnector(MessageHandler messageHandler, AssetManager assets, String filename, DeviceReader reader) {
        this.messageHandler = messageHandler;
        this.assets = assets;
        this.filename = filename;
        this.reader = reader;
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

    @Override
    public String valueAsString(byte[] bytes) {
        return reader.valueAsString(bytes);
    }

    class MockDeviceRunnable implements Runnable {
        @Override
        public void run() {
            messageHandler.sendConnectingTo(filename);

            String mockFilePath = new File(SAMPLES_SUBDIR, filename).toString();
            InputStream inputStream;
            try {
                inputStream = assets.open(mockFilePath);
            } catch (IOException e) {
                Log.e(TAG, "Error while opening mock data file: " + mockFilePath, e);
                messageHandler.sendConnectionLost();
                return;
            }

            reader.init(inputStream);

            messageHandler.sendConnectedTo(filename);

            try {
                while (running) {
                    byte[] chunk = reader.readValue();
                    if (chunk == null) {
                        break;
                    }

                    messageHandler.sendChunkRead(chunk);
                    sleep();
                }
            } catch (IOException e) {
                Log.e(TAG, "Error while reading data from mock device", e);
            }

            messageHandler.sendConnectionLost();
        }

        private void sleep() {
            try {
                Thread.sleep(SLEEP_MILLIS);
            } catch (InterruptedException e) {
                // ok to be interrupted
                Thread.currentThread().interrupt();
            }
        }
    }
}
