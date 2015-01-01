TODO
====

Very crude stuff. This is mostly for myself, not intended for other humans.

implement simulator mode for easier testing
-------------------------------------------
- mock devices
    - MockSenspod
    - GpsTracker - using senspod format
    - MockZephyr
- debug mode in prefs
    - register mock devices
- when bt is off, add button to enable easily
- make it work again with bluetooth

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
- use modern action toolbar
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
