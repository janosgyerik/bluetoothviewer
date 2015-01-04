package net.bluetoothviewer;


import android.bluetooth.BluetoothDevice;

import java.util.Collections;
import java.util.Set;

public class NullBluetoothWrapper implements BluetoothAdapterWrapper {
    @Override
    public Set<BluetoothDevice> getBondedDevices() {
        return Collections.emptySet();
    }

    @Override
    public void cancelDiscovery() {
        // nothing to cancel
    }

    @Override
    public boolean isDiscovering() {
        return false;
    }

    @Override
    public void startDiscovery() {
        // nothing to discover
    }
}
