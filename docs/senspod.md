Random notes about Sensaris senspods

http://sensing2010.blogspot.fr/

### Find the device

    hcitool scan --refresh

This will print the mac address of the device. For example:
`00:07:80:93:54:5B SENSPOD_3002`

### Connect to the device

You need to use the mac address in the next command like this:

    rfcomm connect 0 00:07:80:93:54:5B 1 

A screen should pop up asking you the PIN (passkey), for example, 1111.

When you run the command, the left small led should light up in blue. The output should look like this:

    Connected /dev/rfcomm0 to 00:07:80:93:54:5B on channel 1
    Press CTRL-C for hangup

This command has to keep running, to maintain the link with the senspod. To stop it in the end when you no longer want to work with the senspod by pressing Ctrl-C.

If any of these commands (`rfcomm` or `hcitool`) don't work, you might need to install some packages, as written in the documentation. To search for ubuntu packages use `apt-cache search somekeyword` and install with `apt-get install somepackagename`.

### Communicating with the device

In another terminal, start `cutecom`. If the command cannot be found, install it with `apt-get install cutecom`.

Set the device name to `/dev/rfcomm0`.

The baud rate should be set to 115200. Other parameters:

    data bits: 8;
    stop bits: 1;
    parity: none.

And then click **Open Device**.

EVERY NEXT TIME

1) Switch on the device.

2) Open terminal and type: `rfcomm connect 0 00:07:80:93:54:5B 1`

When you run the command, the blue led should light up. The output should look like this:

    Connected /dev/rfcomm0 to 00:07:80:93:54:5B on channel 1
    Press CTRL-C for hangup

3) In another terminal, start `cutecom`

Click Open Device. Wait a few moments until the sentences appear on the screen. If nothing change turn off the device, close the cutecom and repeat the steps 1-3.

If the data are not coming still then reinstall cutecome:

    sudo apt-get remove --purge cutecom
    sudo apt-get install cutecom

### Sensor commands

Note that all commands are case sensitive.

Sensor Control Commands:

- `suspend` --> stops all measuring tasks
- `reboot` --> restart senspod (run bootloader)
- `reset` --> restart firmware (without bootloader)
- `shutdown` --> shutdown and poweroff

Sensor configuration commands:

- `setecho` --> enable or disable command echo. "0" - disable, "1" - enable.
- `settime` --> manually configure time on RTC. Takes argument: ddmmyyhhmmss.

Note: The manual says that GPS auto configure time when it has a fix reading UTC offset to use in sens.cfg file, however it did not set the right time for Japan (offset timezone is 9).

RTC is synchronized with (GPS_UTC_Time + OffsetTimeZone).

OffsetTimeZone 9 #Offset from UTC 0-24

The solution here is to add missing hours later. That's what we did.

File management commands:

- `LIST` --> displays the list of files with size, date and time
- `GET filename` --> displays a chosen file
- `PUT filesize mime filename` --> send a file to the card (to be used with Obex OPP)
- `DEL filename` --> deletes a chosen file from SD card

### Changing the configuration file

First you need to download the configuration from the device, modify it on your computer, and upload it to the device.

#### Getting the file from the device

Open `cutecom` and click on **Open Device**.
If sentences are running, type "suspend" in a command line.
Type `GET sens.cfg`. The content of the configuration file will appear on the screen. It starts with the following lines:

    ##########################################
    ################Configure Senspod##############
    ##########################################
    ##Echo Commande on BT rfcomm
    EchoEnable 1 #default to 1 Set to 0 to Disable
    ...

You can edit the content in any text editor (copy and paste).
Save the edited file with the name `sens.cfg` in your working directory.

### Sending the file to the device

- Quit `cutecom`
- Disconnect `rfcomm0` connection
- Right click on the Bluetooth icon in the top panel -> Send files to device.

---

(unformatted text below)

How to get data from senspod
Connect your device through rfcomm to serial terminal.
Open cutecom and send suspend command.
Type LIST to see what files do you have on your senspod's SD card.
To get the data you will need to look at log files (that have LOG extension). Type GET filename.LOG and ENTER. The content will appear in the window of cutecom.
There are at least two options to download the content:
copy and paste into a new text file, or
check the box called "Log to". Choose/type the name and choose the location where to safe the log file. The new data will be temporary saved in this file every time you open the device and check the box "Log to".
***It's advisable to safe the file as... something else every time after you done downloading the content (you can create a script that will do it for you), because next time you open your device and try to get other files it will overwrite the content, which was created previously.

If there are no log files on your device, you might want to check if your device is configured correcly.:
type GET sens.cfg
check if the senspod in MEASURE and RECORD mode (mode 3).
If not you will have to download the sens.cfg, set the right mode and upload the changed file to the device. Be coutios about the danger of manipulating the file. You can break your senspod. For more information check the original manuals from Sensaris or earlier posts in this blog.
