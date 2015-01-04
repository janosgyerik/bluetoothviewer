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
import android.widget.TextView;

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

    public static enum ConnectorType {
        Bluetooth,
        Mock
    }

    public static enum Message {
        DeviceConnectorType,
        BluetoothAddress,
        MockFilename,
    }

    private final BluetoothAdapterWrapper mBtAdapter = BluetoothAdapterFactory.getBluetoothAdapterWrapper();
    private ArrayAdapter<String> mNewDevicesArrayAdapter;
    private final Set<String> mNewDevicesSet = new HashSet<String>();
    private ArrayAdapter<String> mMockDevicesAdapter;

    private Button scanButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.device_list);

        // Set default result to CANCELED, in case the user backs out
        setResult(Activity.RESULT_CANCELED);

        // Initialize the button to perform device discovery
        scanButton = (Button) findViewById(R.id.button_scan);
        scanButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                doDiscovery();
                v.setVisibility(View.GONE);
            }
        });

        mMockDevicesAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
        ListView mockListView = (ListView) findViewById(R.id.mock_devices);
        mockListView.setAdapter(mMockDevicesAdapter);
        mockListView.setOnItemClickListener(mMockDeviceClickListener);
        findViewById(R.id.mock_devices).setVisibility(View.VISIBLE);
        mMockDevicesAdapter.add("MockSenspod");
        mMockDevicesAdapter.add("MockZephyr");
        mMockDevicesAdapter.add("AndroidGps");

        ArrayAdapter<String> pairedDevicesAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
        ListView pairedListView = (ListView) findViewById(R.id.paired_devices);
        pairedListView.setAdapter(pairedDevicesAdapter);
        pairedListView.setOnItemClickListener(mDeviceClickListener);

        mNewDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
        ListView newDevicesListView = (ListView) findViewById(R.id.new_devices);
        newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
        newDevicesListView.setOnItemClickListener(mDeviceClickListener);

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);

        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filter);

        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

        if (pairedDevices != null && !pairedDevices.isEmpty()) {
            findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
            for (BluetoothDevice device : pairedDevices) {
                pairedDevicesAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            String noDevices = getResources().getText(R.string.none_paired).toString();
            pairedDevicesAdapter.add(noDevices);
        }
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
        setTitle(R.string.scanning);

        findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);

        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }

        mBtAdapter.startDiscovery();
    }

    private OnItemClickListener mMockDeviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            // Cancel discovery because it's costly and we're about to connect
            mBtAdapter.cancelDiscovery();

            Intent intent = new Intent();
            intent.putExtra(Message.DeviceConnectorType.toString(), ConnectorType.Mock);
            Log.d(TAG, "arg2 = " + arg2);
            Log.d(TAG, "arg3 = " + arg3);
            intent.putExtra(Message.MockFilename.toString(), "test");

            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    };

    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            // Cancel discovery because it's costly and we're about to connect
            mBtAdapter.cancelDiscovery();

            // Get the device MAC address, which is the last 17 chars in the View
            CharSequence info = ((TextView) v).getText();
            if (info != null) {
                // TODO this is not so cool...
//                CharSequence address = info.toString().substring(info.length() - 17);

                Intent intent = new Intent();
//                intent.putExtra(EXTRA_DEVICE_ADDRESS, address);

                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        }
    };

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null) {
                    String address = device.getAddress();
                    if (!mNewDevicesSet.contains(address)) {
                        mNewDevicesSet.add(address);
                        mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                    }
                } else {
                    Log.e(TAG, "Could not get parcelable extra from device: " + BluetoothDevice.EXTRA_DEVICE);
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                setProgressBarIndeterminateVisibility(false);
                setTitle(R.string.select_device);
                if (mNewDevicesSet.isEmpty()) {
                    String noDevices = getResources().getText(R.string.none_found).toString();
                    mNewDevicesArrayAdapter.add(noDevices);
                }
                scanButton.setVisibility(View.VISIBLE);
            }
        }
    };
}
