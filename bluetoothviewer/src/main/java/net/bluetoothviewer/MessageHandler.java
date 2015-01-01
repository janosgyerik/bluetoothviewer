package net.bluetoothviewer;

public interface MessageHandler {

    int MSG_NOT_CONNECTED = 10;
    int MSG_CONNECTING = 11;
    int MSG_CONNECTED = 12;
    int MSG_CONNECTION_FAILED = 13;
    int MSG_CONNECTION_LOST = 14;
    int MSG_LINE_READ = 21;
    int MSG_BYTES_WRITTEN = 22;

    void sendLineRead(String line);

    void sendBytesWritten(byte[] bytes);

    void sendConnectingTo(String deviceName);

    void sendConnectedTo(String deviceName);

    void sendNotConnected();

    void sendConnectionFailed();

    void sendConnectionLost();

}
