BluetoothViewer
===============

This app is a simple Bluetooth connection debugging tool:

1. Connect to a Bluetooth device
2. Display incoming raw data
3. Send raw data to the Bluetooth device

You can confirm successful pairing, monitor incoming raw data and see
exactly what is transmitted from the Bluetooth device.

* Android app on Google Play, with screenshots:
  https://play.google.com/store/apps/details?id=net.bluetoothviewer


Limitations
-----------

The app works only with certain types of Bluetooth devices:

- Devices that work in *server mode*. That is, devices that
  listen to and accept incoming Bluetooth connections.

- Devices that accept connections on *channel=1*

- Devices that don't require a specific UUID during connection.

- Devices that can be paired.

All these conditions are required, at least for now.
I plan to add in the next release the option to set the channel,
and to set a specific UUID.

Another limitation is that the current version shows incoming
data in ASCII format. If your device sends binary data, that
won't be very readable. I plan two features to help with that:

- Add a hexadecimal view that can be easily switched on/off

- Make it possible to send the received data as an attachment

Finally, keep in mind that some devices need some sort of
"activation signal" first before they would start sending data.
This depends on the device, and you would have to look at the
technical documentation of your device to figure this out.


Feature ideas
-------------

I plan to add the following features in the future:

* Option to specify channel to use when connecting

* Option to specify UUID to use when connecting

* Option to add timestamp to incoming messages

* Option to add GPS info to incoming messages

* Design a plugin framework for customized views tailored to specific
  Bluetooth sensors

For more details, see the more detailed (but quite crude) `todo.md` file.


Contributing code
-----------------

You can contribute improvements in whatever way is convenient for you, for example:

* Create a Pull Request on GitHub:
  https://github.com/janosgyerik/bluetoothviewer

* Email your patches to info@janosgyerik.com


Sponsors
--------

* Alan Haddy (www.ipegcorp.com): option to record incoming Bluetooth
  data and send as email attachment


Disclaimer
----------

The source code is a modified version of the BluetoothChat sample
that is included in the Android SDK.
