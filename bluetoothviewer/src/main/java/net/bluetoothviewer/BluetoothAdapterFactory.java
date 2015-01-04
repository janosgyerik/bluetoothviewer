package net.bluetoothviewer;

import android.bluetooth.BluetoothAdapter;

public class BluetoothAdapterFactory {
    private BluetoothAdapterFactory() {
        // utility class
    }

    public static BluetoothAdapterWrapper getBluetoothAdapterWrapper() {
        BluetoothAdapter defaultAdapter = BluetoothAdapter.getDefaultAdapter();
        return defaultAdapter != null ? new BluetoothAdapterDelegate(defaultAdapter) : new NullBluetoothWrapper();
    }
}
