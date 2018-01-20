package net.bluetoothviewer;

import android.os.Handler;

import static net.bluetoothviewer.MessageHandler.Constants.MSG_BYTES_WRITTEN;
import static net.bluetoothviewer.MessageHandler.Constants.MSG_CONNECTED;
import static net.bluetoothviewer.MessageHandler.Constants.MSG_CONNECTING;
import static net.bluetoothviewer.MessageHandler.Constants.MSG_CONNECTION_FAILED;
import static net.bluetoothviewer.MessageHandler.Constants.MSG_CONNECTION_LOST;
import static net.bluetoothviewer.MessageHandler.Constants.MSG_LINE_READ;
import static net.bluetoothviewer.MessageHandler.Constants.MSG_NOT_CONNECTED;

public class MessageHandlerImpl implements MessageHandler {
    private final Handler handler;

    public MessageHandlerImpl(Handler handler) {
        this.handler = handler;
    }

    @Override
    public void sendLineRead(String line) {
        handler.obtainMessage(MSG_LINE_READ, -1, -1, line).sendToTarget();
    }

    @Override
    public void sendBytesWritten(byte[] bytes) {
        handler.obtainMessage(MSG_BYTES_WRITTEN, -1, -1, bytes).sendToTarget();
    }

    @Override
    public void sendConnectingTo(String deviceName) {
        sendMessage(MSG_CONNECTING, deviceName);
    }

    @Override
    public void sendConnectedTo(String deviceName) {
        sendMessage(MSG_CONNECTED, deviceName);
    }

    @Override
    public void sendNotConnected() {
        sendMessage(MSG_NOT_CONNECTED);
    }

    @Override
    public void sendConnectionFailed() {
        sendMessage(MSG_CONNECTION_FAILED);
    }

    @Override
    public void sendConnectionLost() {
        sendMessage(MSG_CONNECTION_LOST);
    }

    private void sendMessage(int messageId, String deviceName) {
        handler.obtainMessage(messageId, -1, -1, deviceName).sendToTarget();
    }

    private void sendMessage(int messageId) {
        handler.obtainMessage(messageId).sendToTarget();
    }
}
