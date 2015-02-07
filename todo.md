TODO
====

Very crude stuff. This is mostly for myself, not intended for other humans.

split the full and lite versions
--------------------------------
- make the menu different in full and lite
    - buy the full app only in lite
- Drop the please buy the full up text in the full app

clean up deprecated code
------------------------
- find a way to detect all deprecated code
- Context.MODE_WORLD_READABLE
- addPreferencesFromResource

migrate to modern style
-----------------------
- change intro text and make links clickable
- use modern action toolbar
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
