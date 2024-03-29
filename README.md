# Arduino Self Balancing Robot

This repository is used by me, to keep track of all the files i need for my maturity project: an Arduino self balancing robot. If you found this repository randomly, feel free to download and use the softwares included in the project.

## Content of the folders
- "./Arduino software/". Contains the main firmware (stable and unstable) and several other arduino sketches used to test different parts of the robot (motor drivers, gyroscope, servo, ultrasounds, etc.). The board used in my case is an Arduino ("ripoff") Pro Micro.

- "./Processing/". Contains mainly the android application (in Processing), an android base app in Java and the PC app in Java. Working on a javax.swing version.

- "./Python software/". Contains the pc software, written in python. In order to run it requires the pyserial module to be installed. If you don't connect an xbox controller it will simply act as a serial monitor, displaying some stats about the robot, otherwise you can use a controller to control the robot.

- "./Schematics and PCB": contains all the schematics and pcb. It's more of a quick test version, so i'm planning on releasing a new updated version of the board soon based on the problems i had with this version, using an online schematic and PCB designer (probably EasyEDA).

- "./YABR/": probably the most important folder, it contains the original project i got inspired from. [Here's a link to the original website](http://www.brokking.net/yabr_main.html)

## TODO
- Add Raspberry Pi software (once I understand which ones of the thousands files are worth publishing). Originally this was supposed to include a Raspberry Pi, but I had some problems with the power so i stopped developing for it. But now that Raspy 4 is out I'll probably include again to add a feature of object detection.

- Revise board schematics and PCB.

- Add datasheets

Author: Francesco Berton
