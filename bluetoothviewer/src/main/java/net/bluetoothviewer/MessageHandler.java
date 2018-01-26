package net.bluetoothviewer;

public interface MessageHandler {

    class Constants {
        private Constants() {
            // forbidden constructor
        }

        static final int MSG_NOT_CONNECTED = 10;
        static final int MSG_CONNECTING = 11;
        static final int MSG_CONNECTED = 12;
        static final int MSG_CONNECTION_FAILED = 13;
        static final int MSG_CONNECTION_LOST = 14;
        static final int MSG_CHUNK_READ = 21;
        static final int MSG_BYTES_WRITTEN = 22;
    }

    void sendChunkRead(byte[] chunk);

    void sendBytesWritten(byte[] bytes);

    void sendConnectingTo(String deviceName);

    void sendConnectedTo(String deviceName);

    void sendNotConnected();

    void sendConnectionFailed();

    void sendConnectionLost();

}
