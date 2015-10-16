<h1>Summary</h1>
This program is used for creating a graphic representation of where a TAG is located based on the distances between TAGs and ANCHORS.

When the program is executed it takes 2 parameters as input: the path to the configuration file and the device ID from which you are running the program.

The configuration file has the list of services that need to be enabled for the device you are currently running the program from.
These are some of the services:

**anchor**: opens a serial port connection to the Arduino that has the DW1000 antenna connected and every time it receives a range report from the Arduino, it sends the distance to the **master** service using an HTTP request.

**master**: this service enables an HTTP server. Other **anchor** services will send HTTP requests to the server sending as a parameter the anchor ID, the tag ID and the distance between this 2.

**webApp**: this is the frontend of the application. The webpage will draw a graphic representation of where the Anchors are located and the calculated position of the Tag. 

For calculating the position of the Tag, we use a method called trilateration. This method takes as input the distance between 3 Anchors and the Tag along with the coordinates of each Anchor. Since we already know the location of the 3 Anchors, it's possible to calculate the location of the Tag.

<h1>Example</h1>

YouTube video:

[![Alt text for your video](https://img.youtube.com/vi/5WXWx_jxGw8/0.jpg)](https://www.youtube.com/watch?v=5WXWx_jxGw8)


<h1>Architecture</h1>
This is an example of the architecture I have running.
The **anchor** services are running in 3 different devices: 2 raspberry pi and 1 mac.
The mac is also running the **master** service and the **webApp** service.

![alt tag](https://github.com/jogando/dw1000server/blob/master/DW1000Server/doc/HLD.jpg)



<h1>Running the program</h1>
The program has been tested in Mac OS and in Raspbian

\<path to JAR\>: this is the path to the generated JAR.
\<path to config file\>: this is the path to the JSON config file.
\<device id\>:device ID of the computer you are running the program from.

<h2>Raspbian</h2>
java -Djava.library.path=/usr/lib/jni -cp \<path to JAR\> common.Program configPath:\<path to config file\> deviceId:\<device id\>

Example:
java -Djava.library.path=/usr/lib/jni -cp /home/pi/dw1000server.jar common.Program configPath:/home/pi/dw1000server.JSON deviceId:raspberry1

<h2>Mac OS</h2>
java -cp \<path to JAR\> common.Program configPath:\<path to config file\> deviceId:\<device id\>



<h1>Config file</h1>
This is the structure of the config file.
*  listNetworkDevices: contains all the devices running services
  *  id: deviceId
  *  ip: ip address of the device
  *  listServices: list of services that will run on the device
*  scene:
  *  endX: max coordinates of the place where the anchors are located.
  *  endY: max coordinates of the place where the anchors are located.

<h1>Anchor service</h1>
Parameters:
*  anchorId: string for identifying the anchor
*  portName: serial port name in which the Arduino is connected to
*  coordinateX: X coordinate in which the Anchor is located at
*  coordinateY: Y coordinate in which the Anchor is located at

<h1>Master service</h1>
Parameters:
*  httpPort: port number for the HTTP server

<h1>WebApp service</h1>
Parameters:
*  webPath: full path to the "index.html" file
*  httpPort: port number for the HTTP server


