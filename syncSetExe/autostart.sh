#!/bin/bash
sudo java -jar /home/admintx/syncWeb/target/nwbase.jar &
sudo /home/admintx/syncSetExe/x.sh &
sleep 2
/usr/bin/google-chrome-stable --kiosk http://192.168.0.219 --simulate-outdated-no-au='Tue, 31 Dec 2099 23:59:59 GMT' --disable-session-crashed-bubble



