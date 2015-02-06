TODO
====

Very crude stuff. This is mostly for myself, not intended for other humans.

implement simulator mode for easier testing
-------------------------------------------
- mock devices
    + MockAscii
    - MockZephyr
- debug mode in prefs
    - enable mock devices
- when bt is off, add button to enable easily

migrate to modern style
-----------------------
- use modern action toolbar
- use regular buttons

better handling of recording
----------------------------
- create common interface for receiving messages from devices
    - BluetoothViewer: specialized class for viewing data
    - DataRecorder: specialized class for recording data to file
        - toggled by preferences
- the implementations are independent and unaware of each other

split the full and lite versions
--------------------------------
- move the common code to common library
- create and confirm lite version
- create and confirm full version

generalize
----------
The viewer and recorder are not really about bluetooth anymore:
they could receive data from anywhere.
Rename the classes appropriately.
The package name can stay the same, for "historical" reasons.

next
----
- make it possible to connect to specific UUID
- make it possible to connect to specific channel
- easier testing
    - dummy bt device for testing
    - setup robotium and create some ui tests
- transform lines
    - delete first n characters
    - delete until first occurrence of
    - perform substitution
    - prepend timestamp
    - prepend gps data
- clear button
- hexa view
- generalize and finish build.sh
- save data to files on android
- exclude lines
    - matching string
    - matching regex
- toolbar: record on/off
- files app to view recorded data

later
-----
- make device list scrollable when too long
- remember texts recently sent to bluetooth devices
- adjustable font size
