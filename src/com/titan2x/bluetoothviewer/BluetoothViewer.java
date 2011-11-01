/*
 * Copyright (C) 2010 Janos Gyerik
 *
 * This file is part of BluetoothViewer.
 *
 * BluetoothViewer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BluetoothViewer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with BluetoothViewer.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.titan2x.bluetoothviewer;

import com.titan2x.bluetoothviewer.R;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This is the main Activity that displays the current session.
 */
public class BluetoothViewer extends Activity {
    // Debugging
    private static final String TAG = "BluetoothViewer";
    private static final boolean D = true;

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    // Layout Views
    private TextView mTitle;
    private ListView mConversationView;
    private EditText mOutEditText;
    private Button mSendButton;
    private View mSendTextContainer;
    
    // Toolbar
    private ImageButton mToolbarConnectButton;
    private ImageButton mToolbarDisconnectButton;
    private ImageButton mToolbarPauseButton;
    private ImageButton mToolbarPlayButton;

    // Name of the connected device
    private String mConnectedDeviceName = null;
    // Array adapter for the conversation thread
    private ArrayAdapter<String> mConversationArrayAdapter;
    // String buffer for outgoing messages
    private StringBuffer mOutStringBuffer;
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the chat services
    private BluetoothChatService mChatService = null;

    // State variables
    private boolean paused = false;
    private boolean connected = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(D) Log.e(TAG, "+++ ON CREATE +++");

        // Set up the window layout
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.main);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);

        // Set up the custom title
        mTitle = (TextView) findViewById(R.id.title_left_text);
        mTitle.setText(R.string.app_name);
        mTitle = (TextView) findViewById(R.id.title_right_text);
        
        mSendTextContainer = (View) findViewById(R.id.send_text_container);
        
        mToolbarConnectButton = (ImageButton) findViewById(R.id.toolbar_btn_connect);
        mToolbarConnectButton.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		startDeviceListActivity();
        	}
        });

        mToolbarDisconnectButton = (ImageButton) findViewById(R.id.toolbar_btn_disconnect);
        mToolbarDisconnectButton.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				disconnectDevices();
			}
        });
        
        mToolbarPauseButton = (ImageButton) findViewById(R.id.toolbar_btn_pause);
        mToolbarPauseButton.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				paused = true;
				onPausedStateChanged();
			}
        });
        
        mToolbarPlayButton = (ImageButton) findViewById(R.id.toolbar_btn_play);
        mToolbarPlayButton.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				paused = false;
				onPausedStateChanged();
			}
        });
        
        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
    }
    
    private void startDeviceListActivity() {
        Intent serverIntent = new Intent(this, DeviceListActivity.class);
        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
    }

    @Override
    public void onStart() {
        super.onStart();
        if(D) Log.e(TAG, "++ ON START ++");

        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        // Otherwise, setup the chat session
        } else {
            if (mChatService == null) setupChat();
        }
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        if(D) Log.e(TAG, "+ ON RESUME +");

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
              // Start the Bluetooth chat services
              //mChatService.start();
            }
        }
    }

    private void setupChat() {
        Log.d(TAG, "setupChat()");

        // Initialize the array adapter for the conversation thread
        mConversationArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);
        mConversationView = (ListView) findViewById(R.id.in);
        mConversationView.setAdapter(mConversationArrayAdapter);
        
        /*
        mConversationArrayAdapter.add("some example text");
        mConversationArrayAdapter.add("a longer example text that will not fit on a single line");
        mConversationArrayAdapter.add("some example text");
        mConversationArrayAdapter.add("a longer example text that will not fit on a single line");
        mConversationArrayAdapter.add("some example text");
        mConversationArrayAdapter.add("a longer example text that will not fit on a single line");
        mConversationArrayAdapter.add("some example text");
        mConversationArrayAdapter.add("a longer example text that will not fit on a single line");
        mConversationArrayAdapter.add("some example text");
        mConversationArrayAdapter.add("a longer example text that will not fit on a single line");
        mConversationArrayAdapter.add("some example text");
        mConversationArrayAdapter.add("a longer example text that will not fit on a single line");
        mConversationArrayAdapter.add("some example text");
        mConversationArrayAdapter.add("a longer example text that will not fit on a single line");
        mConversationArrayAdapter.add("some example text");
        mConversationArrayAdapter.add("a longer example text that will not fit on a single line");
        mConversationArrayAdapter.add("some example text");
        mConversationArrayAdapter.add("a longer example text that will not fit on a single line");
        mConversationArrayAdapter.add("some example text");
        mConversationArrayAdapter.add("a longer example text that will not fit on a single line");
        mConversationArrayAdapter.add("some example text");
        mConversationArrayAdapter.add("a longer example text that will not fit on a single line");
        mConversationArrayAdapter.add("some example text");
        mConversationArrayAdapter.add("a longer example text that will not fit on a single line");
        mConversationArrayAdapter.add("some example text");
        mConversationArrayAdapter.add("a longer example text that will not fit on a single line");
        mConversationArrayAdapter.add("some example text");
        mConversationArrayAdapter.add("a longer example text that will not fit on a single line");
        mConversationArrayAdapter.add("some example text");
        mConversationArrayAdapter.add("a longer example text that will not fit on a single line");
        mConversationArrayAdapter.add("some example text");
        mConversationArrayAdapter.add("a longer example text that will not fit on a single line");
        mConversationArrayAdapter.add("some example text");
        mConversationArrayAdapter.add("a longer example text that will not fit on a single line");
        mConversationArrayAdapter.add("some example text");
        mConversationArrayAdapter.add("a longer example text that will not fit on a single line");
        */
        
        mConversationArrayAdapter.add("Welcome to Bluetooth Viewer!");
        mConversationArrayAdapter.add("This is a simple application that " +
        		"can connect to any Bluetooth device and show incoming raw data. ");
        mConversationArrayAdapter.add("Use the toolbar below to connect / disconnect " +
        		"and perform other operations on the remote device.");
        mConversationArrayAdapter.add("In order to work, Bluetooth must be enabled on " +
        		"your device, and you must pair with the remote device first.");
        mConversationArrayAdapter.add("For more info and to report bugs, see the project website: " +
        		"http://launchpad.net/bluetoothviewer");

        // Initialize the compose field with a listener for the return key
        mOutEditText = (EditText) findViewById(R.id.edit_text_out);
        mOutEditText.setOnEditorActionListener(mWriteListener);

        // Initialize the send button with a listener that for click events
        mSendButton = (Button) findViewById(R.id.button_send);
        mSendButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                TextView view = (TextView) findViewById(R.id.edit_text_out);
                String message = view.getText().toString();
                sendMessage(message);
            }
        });

        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothChatService(mHandler);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
        
        onBluetoothStateChanged();
    }

    @Override
    public synchronized void onPause() {
        super.onPause();
        if(D) Log.e(TAG, "- ON PAUSE -");
    }

    @Override
    public void onStop() {
        super.onStop();
        if(D) Log.e(TAG, "-- ON STOP --");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop the Bluetooth chat services
        if (mChatService != null) mChatService.stop();
        if(D) Log.e(TAG, "--- ON DESTROY ---");
    }

    /**
     * Sends a message.
     * @param message  A string of text to send.
     */
    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
        	message += "\n";
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mChatService.write(send);

            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
            mOutEditText.setText(mOutStringBuffer);
        }
    }

    // The action listener for the EditText widget, to listen for the return key
    private TextView.OnEditorActionListener mWriteListener =
        new TextView.OnEditorActionListener() {
        public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
            // If the action is a key-up event on the return key, send the message
            if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
                String message = view.getText().toString();
                sendMessage(message);
            }
            if(D) Log.i(TAG, "END onEditorAction");
            return true;
        }
    };

    // The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_STATE_CHANGE:
                if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                switch (msg.arg1) {
                case BluetoothChatService.STATE_CONNECTED:
                	connected = true;
                    mTitle.setText(mConnectedDeviceName);
                    break;
                case BluetoothChatService.STATE_CONNECTING:
                    mTitle.setText(R.string.title_connecting);
                    break;
                case BluetoothChatService.STATE_NONE:
                	connected = false;
                    mTitle.setText(R.string.title_not_connected);
                    break;
                }
                onBluetoothStateChanged();
                break;
            case MESSAGE_WRITE:
                byte[] writeBuf = (byte[]) msg.obj;
                // construct a string from the buffer
                String writeMessage = new String(writeBuf);
                mConversationArrayAdapter.add(">>> " + writeMessage);
                if (D) Log.d(TAG, "written = '" + writeMessage + "'");
                break;
            case MESSAGE_READ:
            	if (paused) break;
                byte[] readBuf = (byte[]) msg.obj;
                // construct a string from the valid bytes in the buffer
                String readMessage = new String(readBuf, 0, msg.arg1);
                if (D) Log.d(TAG, readMessage);
                mConversationArrayAdapter.add(readMessage);
                break;
            case MESSAGE_DEVICE_NAME:
                // save the connected device's name
                mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                Toast.makeText(getApplicationContext(), "Connected to "
                               + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                break;
            case MESSAGE_TOAST:
                Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                               Toast.LENGTH_SHORT).show();
                break;
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(D) Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
        case REQUEST_CONNECT_DEVICE:
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                // Get the device MAC address
                String address = data.getExtras()
                                     .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                // Get the BLuetoothDevice object
                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                // Attempt to connect to the device
                mChatService.connect(device);
            }
            break;
        case REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
                // Bluetooth is now enabled, so set up a chat session
                setupChat();
            } else {
                // User did not enable Bluetooth or an error occured
                Log.d(TAG, "BT not enabled");
                Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_quit:
        	this.finish();
        }
        return false;
    }

    private void disconnectDevices() {
    	if (mChatService != null) mChatService.stop();
    	
    	onBluetoothStateChanged();
    }

    private void onBluetoothStateChanged() {
    	if (connected) {
			mToolbarConnectButton.setVisibility(View.GONE);
			mToolbarDisconnectButton.setVisibility(View.VISIBLE);
			mSendTextContainer.setVisibility(View.VISIBLE);
    	}
    	else {
			mToolbarConnectButton.setVisibility(View.VISIBLE);
			mToolbarDisconnectButton.setVisibility(View.GONE);
			mSendTextContainer.setVisibility(View.GONE);
    	}
		paused = false;
    	onPausedStateChanged();
    }

    private void onPausedStateChanged() {
    	if (connected) {
	    	if (paused) {
	    		mToolbarPlayButton.setVisibility(View.VISIBLE);
	    		mToolbarPauseButton.setVisibility(View.GONE);
	    	}
	    	else {
	    		mToolbarPlayButton.setVisibility(View.GONE);
	    		mToolbarPauseButton.setVisibility(View.VISIBLE);
	    	}
    	}
    	else {
    		mToolbarPlayButton.setVisibility(View.GONE);
    		mToolbarPauseButton.setVisibility(View.GONE);
    	}
    }
    
}