#!/bin/bash
sudo java -jar /home/admintx/syncWeb/target/nwbase.jar &
sudo /home/admintx/syncSetExe/x.sh &
sleep 2
/usr/bin/google-chrome-stable --kiosk http://192.168.0.219 --simulate-outdated-no-au='Tue, 31 Dec 2099 23:59:59 GMT' --disable-session-crashed-bubble



#/usr/bin/google-chrome-stable --disable-session-crashed-bubble --simulate-outdated-no-au='Tue, 31 Dec 2099 23:59:59 GMT' --kiosk http://192.168.0.219 

#/home/admintx/syncSetExe/x.sh &
#/usr/bin/google-chrome-stable --disable-session-crashed-bubble --simulate-outdated-no-au='Tue, 31 Dec 2099 23:59:59 GMT' --kiosk http://127.0.0.1 
