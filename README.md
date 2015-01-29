# real_sense_ros
ROSBridge interface to the Intel RealSense SDK

1.)  Launch rosbridge with websockets in your ros instance:
roslaunch rosbridge_server rosbridge_websocket.launch

2.)  Run PublishHand.java ws://<ROSBRIDGE SERVE>:9090

3.) Start RViz and view /rssdk/hand_marker.  

4.)  Put your hand in front of the sensor.  A marker should appear
with the q location of the hand, that moves when your hand moves.  The
color of the marker changes when you open and close your hand.

A video of the marker in RViz is here: 
http://youtu.be/V3-eEGE4-Tc



