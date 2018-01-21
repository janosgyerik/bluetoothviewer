TODO
====

Very crude stuff. This is mostly for myself, not intended for other humans.

improve connectivity
--------------------

- need to be able to see binary data
    - add support for devices that send binary data
    - add binary mock device
    - toggle binary mode:
        - read data in chucks instead of by lines
        - display in ascii mode
- experiment:
    - try to connect by uuid, see https://github.com/janosgyerik/bluetoothviewer/issues/3
        - try to connect to mac, PC, etc
        - print as much debug info as possible about remote device
        - try with different channels
- TBD

migrate to modern style
-----------------------

- replace menu with navigation drawer http://developer.android.com/training/implementing-navigation/nav-drawer.html
- use regular buttons

better handling of recording
----------------------------

- create common interface for receiving messages from devices
    - BluetoothViewer: specialized class for viewing data
    - DataRecorder: specialized class for recording data to file
        - toggled by preferences
- the implementations are independent and unaware of each other

next
----

- make it possible to connect to specific UUID
- make it possible to connect to specific channel
- transform lines
    - delete first n characters
    - delete until first occurrence of
    - perform substitution
    - prepend timestamp
    - prepend gps data
- clear button
- hexa view
- save data to files on android
- exclude lines
    - matching string
    - matching regex
- toolbar: record on/off
- files app to view recorded data

minor
-----

- reduce code duplication in xml using styles

later
-----

- make device list scrollable when too long
- remember texts recently sent to bluetooth devices
    - preset buttons
- adjustable font size
