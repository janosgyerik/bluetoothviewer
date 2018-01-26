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
import android.content.res.AssetManager;
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
import net.bluetoothviewer.readers.BinaryReader;
import net.bluetoothviewer.readers.DeviceReader;
import net.bluetoothviewer.readers.LineByLineReader;
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
        BLUETOOTH,
        MOCK
    }

    public enum InputType {
        TEXT,
        BINARY
    }

    public enum Message {
        DEVICE_CONNECTOR_TYPE,
        INPUT_TYPE,
        BLUETOOTH_ADDRESS,
        MOCK_FILENAME,
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

        boolean foundMockDevices = false;
        if (getIntent().getBooleanExtra(EXTRA_MOCK_DEVICES_ENABLED, false)) {
            ArrayAdapter<MockDeviceEntry> mockDevicesAdapter = new ArrayAdapter<MockDeviceEntry>(this, R.layout.device_name);
            ListView mockListView = (ListView) findViewById(R.id.mock_devices);
            mockListView.setAdapter(mockDevicesAdapter);
            mockListView.setOnItemClickListener(new MockDeviceClickListener(mockDevicesAdapter));
            mockListView.setOnItemLongClickListener(new MockDeviceClickListener(mockDevicesAdapter));

            for (String filename : AssetUtils.listFiles(getResources().getAssets(), MockDeviceConnector.SAMPLES_SUBDIR)) {
                mockDevicesAdapter.add(new MockDeviceEntry(filename));
                foundMockDevices = true;
            }

            if (foundMockDevices) {
                findViewById(R.id.title_mock_devices).setVisibility(View.VISIBLE);
            }
        }

        boolean foundBluetoothDevices = false;
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
            foundBluetoothDevices = true;
        }

        if (!foundBluetoothDevices && !foundMockDevices) {
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

    private abstract class AbstractItemClickListener<T> implements OnItemClickListener, AdapterView.OnItemLongClickListener {
        final ArrayAdapter<T> adapter;

        private AbstractItemClickListener(ArrayAdapter<T> adapter) {
            this.adapter = adapter;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            T item = adapter.getItem(position);
            if (item != null) {
                Intent intent = new Intent();
                intent.putExtra(Message.INPUT_TYPE.toString(), InputType.TEXT);
                putExtras(intent, item);
                setResult(Activity.RESULT_OK, intent);
            }
            finish();
        }


        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            T item = adapter.getItem(position);
            if (item != null) {
                Intent intent = new Intent();
                intent.putExtra(Message.INPUT_TYPE.toString(), InputType.BINARY);
                putExtras(intent, item);
                setResult(Activity.RESULT_OK, intent);
            }
            finish();
            return false;
        }

        abstract void putExtras(Intent intent, T item);
    }

    private class BluetoothDeviceClickListener extends AbstractItemClickListener<BluetoothDeviceEntry> {
        private BluetoothDeviceClickListener(ArrayAdapter<BluetoothDeviceEntry> adapter) {
            super(adapter);
        }

        @Override
        void putExtras(Intent intent, BluetoothDeviceEntry item) {
            intent.putExtra(Message.DEVICE_CONNECTOR_TYPE.toString(), ConnectorType.BLUETOOTH);
            intent.putExtra(Message.BLUETOOTH_ADDRESS.toString(), item.address);
        }
    }

    private class MockDeviceClickListener extends AbstractItemClickListener<MockDeviceEntry> {
        private MockDeviceClickListener(ArrayAdapter<MockDeviceEntry> adapter) {
            super(adapter);
        }

        @Override
        void putExtras(Intent intent, MockDeviceEntry item) {
            intent.putExtra(Message.DEVICE_CONNECTOR_TYPE.toString(), ConnectorType.MOCK);
            intent.putExtra(Message.MOCK_FILENAME.toString(), item.filename);
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

    public static DeviceConnector createDeviceConnector(Intent data, MessageHandler messageHandler, AssetManager assetManager) {
        DeviceReader reader;
        InputType inputType = (InputType) data.getSerializableExtra(Message.INPUT_TYPE.toString());
        switch (inputType) {
            case TEXT:
                reader = new LineByLineReader();
                break;
            case BINARY:
                reader = new BinaryReader();
                break;
            default:
                return null;
        }

        ConnectorType connectorType =
                (ConnectorType) data.getSerializableExtra(Message.DEVICE_CONNECTOR_TYPE.toString());

        switch (connectorType) {
            case MOCK:
                String filename = data.getStringExtra(Message.MOCK_FILENAME.toString());
                return new MockDeviceConnector(messageHandler, assetManager, filename, reader);
            case BLUETOOTH:
                String address = data.getStringExtra(Message.BLUETOOTH_ADDRESS.toString());
                return new BluetoothDeviceConnector(messageHandler, address, reader);
            default:
                return null;
        }
    }
}
