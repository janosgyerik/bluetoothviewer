/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.bluetoothviewer;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread that listens for
 * incoming connections, a thread for connecting with a device, and a
 * thread for performing data transmissions when connected.
 */
public class BluetoothDeviceConnector implements DeviceConnector {

    private static final String TAG = BluetoothDeviceConnector.class.getSimpleName();
    private static final boolean D = true;

    public static final int CHANNEL = 1;

    private static final int STATE_NONE = 0;       // we're doing nothing
    private static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    private static final int STATE_CONNECTED = 3;  // now connected to a remote device

    private final BluetoothAdapter mAdapter;
    private final MessageHandler mHandler;
    private final String mAddress;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int mState;


    /**
     * Prepare a new Bluetooth session.
     *
     * @param handler A Handler to send messages back to the UI Activity
     */
    public BluetoothDeviceConnector(MessageHandler handler, String address) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        mHandler = handler;
        mAddress = address;
    }

    /**
     * Set the current state of the connection
     *
     * @param state An integer defining the current connection state
     */
    private synchronized void setState(int state) {
        if (D) Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;
    }

    private BluetoothAdapter getBluetoothAdapter() {
        return BluetoothAdapter.getDefaultAdapter();
    }

    @Override
    public synchronized void connect() {
        BluetoothDevice device = getBluetoothAdapter().getRemoteDevice(mAddress);
        connect(device);
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     *
     * @param device The BluetoothDevice to connect
     */
    public synchronized void connect(BluetoothDevice device) {
        if (D) Log.d(TAG, "connect to: " + device);

        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING && mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to connect with the given device
        try {
            mConnectThread = new ConnectThread(device);
            mConnectThread.start();
            setState(STATE_CONNECTING);
            mHandler.sendConnectingTo(device.getName());
        } catch (SecurityException e) {
            Log.e(TAG, e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, e.getMessage(), e);
        } catch (NoSuchMethodException e) {
            Log.e(TAG, e.getMessage(), e);
        } catch (IllegalAccessException e) {
            Log.e(TAG, e.getMessage(), e);
        } catch (InvocationTargetException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     *
     * @param socket The BluetoothSocket on which the connection was made
     * @param device The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        if (D) Log.d(TAG, "connected");

        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        setState(STATE_CONNECTED);
        mHandler.sendConnectedTo(device.getName());
    }

    /**
     * Stop all threads
     */
    @Override
    public synchronized void disconnect() {
        if (D) Log.d(TAG, "shutdown");

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.shutdown();
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        setState(STATE_NONE);
        mHandler.sendNotConnected();
    }

    @Override
    public void sendAsciiMessage(CharSequence chars) {
        write((chars.toString() + "\n").getBytes());
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     *
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    private void write(byte[] out) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        r.write(out);
    }

    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
            mmDevice = device;
            BluetoothSocket tmp = null;

            Log.i(TAG, "calling device.createRfcommSocket with channel " + CHANNEL + " ...");
            try {
                // call hidden method, see BluetoothDevice source code for more details:
                // https://android.googlesource.com/platform/frameworks/base/+/master/core/java/android/bluetooth/BluetoothDevice.java
                Method m = device.getClass().getMethod("createRfcommSocket", int.class);
                tmp = (BluetoothSocket) m.invoke(device, CHANNEL);
                Log.i(TAG, "setting socket to result of createRfcommSocket");
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
            mmSocket = tmp;
        }

        @Override
        public void run() {
            Log.i(TAG, "BEGIN mConnectThread");
            setName("ConnectThread");

            // Always cancel discovery because it will slow down a connection
            mAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage(), e);
                setState(STATE_NONE);
                mHandler.sendConnectionFailed();

                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() socket during connection failure", e2);
                }
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (BluetoothDeviceConnector.this) {
                mConnectThread = null;
            }

            // Start the connected thread
            connected(mmSocket, mmDevice);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "create ConnectedThread");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        private boolean stop = false;
        private boolean hasReadAnything = false;

        public void shutdown() {
            stop = true;
            if (!hasReadAnything) return;
            if (mmInStream != null) {
                try {
                    mmInStream.close();
                } catch (IOException e) {
                    Log.e(TAG, "close() of InputStream failed.");
                }
            }
        }

        @Override
        public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");

            BufferedReader reader = new BufferedReader(new InputStreamReader(mmInStream));

            while (!stop) {
                try {
                    String line = reader.readLine();
                    if (line != null) {
                        mHandler.sendLineRead(line);
                    }
                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    setState(STATE_NONE);
                    mHandler.sendConnectionLost();
                    break;
                }
            }
        }

        /**
         * Write to the connected OutStream.
         *
         * @param bytes The bytes to write
         */
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
                mHandler.sendBytesWritten(bytes);
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }
}
