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

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import net.bluetoothviewer.library.R;
import net.bluetoothviewer.util.AssetUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * This Activity appears as a dialog. It lists already paired devices,
 * and it can scan for devices nearby. When the user selects a device,
 * its MAC address is returned to the caller as the result of this activity.
 */
public class DeviceListActivity extends Activity {

    private static final String TAG = "DeviceListActivity";
    private static final boolean D = true;

    protected static final String EXTRA_MOCK_DEVICES_ENABLED = "MOCK_DEVICES_ENABLED";

    public enum ConnectorType {
        Bluetooth,
        Mock
    }

    public enum Message {
        DeviceConnectorType,
        BluetoothAddress,
        MockFilename,
    }

    private abstract static class DeviceListEntry {
        @Override
        public String toString() {
            return String.format("%s%n%s", getFirstLine(), getSecondLine());
        }

        abstract String getFirstLine();

        abstract String getSecondLine();
    }

    private static class BluetoothDeviceEntry extends DeviceListEntry {
        private final String name;
        private final String address;

        BluetoothDeviceEntry(String name, String address) {
            this.name = name;
            this.address = address;
        }

        @Override
        protected String getFirstLine() {
            return name;
        }

        @Override
        protected String getSecondLine() {
            return address;
        }
    }

    private static class MockDeviceEntry extends DeviceListEntry {
        private final String filename;

        MockDeviceEntry(String filename) {
            this.filename = filename;
        }

        @Override
        String getFirstLine() {
            return filename;
        }

        @Override
        String getSecondLine() {
            return "";
        }
    }

    private final BluetoothAdapterWrapper mBtAdapter = BluetoothAdapterFactory.getBluetoothAdapterWrapper();
    private ArrayAdapter<BluetoothDeviceEntry> mNewDevicesArrayAdapter;
    private final Set<String> mNewDevicesSet = new HashSet<String>();

    private Button scanButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.device_list);

        // Set default result to CANCELED, in case the user backs out
        setResult(Activity.RESULT_CANCELED);

        boolean noAvailableDevices = true;

        if (getIntent().getBooleanExtra(EXTRA_MOCK_DEVICES_ENABLED, false)) {
            String[] filenames = AssetUtils.listFiles(getResources().getAssets(), MockLineByLineConnector.SAMPLES_SUBDIR);
            if (filenames.length > 0) {
                ArrayAdapter<MockDeviceEntry> mockDevicesAdapter = new ArrayAdapter<MockDeviceEntry>(this, R.layout.device_name);
                ListView mockListView = (ListView) findViewById(R.id.mock_devices);
                mockListView.setAdapter(mockDevicesAdapter);
                mockListView.setOnItemClickListener(new MockDeviceClickListener(mockDevicesAdapter));

                for (String filename : filenames) {
                    mockDevicesAdapter.add(new MockDeviceEntry(filename));
                }

                findViewById(R.id.title_mock_devices).setVisibility(View.VISIBLE);
                noAvailableDevices = false;
            }
        }

        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
        if (pairedDevices != null && !pairedDevices.isEmpty()) {
            ArrayAdapter<BluetoothDeviceEntry> pairedDevicesAdapter = new ArrayAdapter<BluetoothDeviceEntry>(this, R.layout.device_name);
            ListView pairedListView = (ListView) findViewById(R.id.paired_devices);
            pairedListView.setAdapter(pairedDevicesAdapter);
            pairedListView.setOnItemClickListener(new BluetoothDeviceClickListener(pairedDevicesAdapter));

            for (BluetoothDevice device : pairedDevices) {
                pairedDevicesAdapter.add(new BluetoothDeviceEntry(device.getName(), device.getAddress()));
            }

            findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
            noAvailableDevices = false;
        }

        if (noAvailableDevices) {
            findViewById(R.id.label_none_found).setVisibility(View.VISIBLE);
        }

        mNewDevicesArrayAdapter = new ArrayAdapter<BluetoothDeviceEntry>(this, R.layout.device_name);
        ListView newDevicesListView = (ListView) findViewById(R.id.new_devices);
        newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
        newDevicesListView.setOnItemClickListener(new BluetoothDeviceClickListener(mNewDevicesArrayAdapter));

        IntentFilter bluetoothDeviceFoundFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, bluetoothDeviceFoundFilter);

        IntentFilter discoveryFinishedFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, discoveryFinishedFilter);

        scanButton = (Button) findViewById(R.id.button_scan);
        scanButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                doDiscovery();
                v.setVisibility(View.GONE);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mBtAdapter.cancelDiscovery();

        this.unregisterReceiver(mReceiver);
    }

    /**
     * Start device discovery with the BluetoothAdapter
     */
    private void doDiscovery() {
        if (D) Log.d(TAG, "doDiscovery()");

        mNewDevicesArrayAdapter.clear();
        mNewDevicesSet.clear();

        setProgressBarIndeterminateVisibility(true);

        findViewById(R.id.label_none_found).setVisibility(View.GONE);
        findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);
        findViewById(R.id.label_scanning).setVisibility(View.VISIBLE);

        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }

        mBtAdapter.startDiscovery();
    }

    private class BluetoothDeviceClickListener implements OnItemClickListener {
        private final ArrayAdapter<BluetoothDeviceEntry> adapter;

        private BluetoothDeviceClickListener(ArrayAdapter<BluetoothDeviceEntry> adapter) {
            this.adapter = adapter;
        }

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            Intent intent = new Intent();
            intent.putExtra(Message.DeviceConnectorType.toString(), ConnectorType.Bluetooth);
            intent.putExtra(Message.BluetoothAddress.toString(), adapter.getItem(i).address);

            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    }

    private class MockDeviceClickListener implements OnItemClickListener {
        private final ArrayAdapter<MockDeviceEntry> adapter;

        public MockDeviceClickListener(ArrayAdapter<MockDeviceEntry> adapter) {
            this.adapter = adapter;
        }

        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            Intent intent = new Intent();
            intent.putExtra(Message.DeviceConnectorType.toString(), ConnectorType.Mock);
            intent.putExtra(Message.MockFilename.toString(), adapter.getItem(arg2).filename);

            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                String parcelableExtraName = BluetoothDevice.EXTRA_DEVICE;
                BluetoothDevice device = intent.getParcelableExtra(parcelableExtraName);
                if (device != null) {
                    String address = device.getAddress();
                    if (!mNewDevicesSet.contains(address)) {
                        mNewDevicesSet.add(address);
                        mNewDevicesArrayAdapter.add(new BluetoothDeviceEntry(device.getName(), address));
                    }
                } else {
                    Log.e(TAG, "Could not get parcelable extra: " + parcelableExtraName);
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                setProgressBarIndeterminateVisibility(false);
                findViewById(R.id.label_scanning).setVisibility(View.GONE);
                if (mNewDevicesSet.isEmpty()) {
                    findViewById(R.id.label_none_found).setVisibility(View.VISIBLE);
                } else {
                    findViewById(R.id.label_none_found).setVisibility(View.GONE);
                }
                scanButton.setVisibility(View.VISIBLE);
            }
        }
    };
}
